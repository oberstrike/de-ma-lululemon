package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.dto.ScanResultDto;
import com.mediaserver.entity.Category;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.repository.CategoryRepository;
import com.mediaserver.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class MegaScanService {

    private final MediaProperties properties;
    private final CategoryRepository categoryRepository;
    private final MovieRepository movieRepository;

    @Scheduled(cron = "${media.mega.scan-cron:0 0 * * * *}")
    public void scheduledScan() {
        if (!properties.getMega().isScanEnabled()) {
            log.debug("Scheduled scan is disabled");
            return;
        }
        log.info("Starting scheduled Mega folder scan");
        scanFolder(null);
    }

    @Async
    public CompletableFuture<ScanResultDto> scanFolderAsync(String folderPath) {
        return CompletableFuture.completedFuture(scanFolder(folderPath));
    }

    @Transactional
    public ScanResultDto scanFolder(String folderPath) {
        log.info("Starting Mega folder scan: {}", folderPath);

        String rootPath = folderPath != null ? folderPath : properties.getMega().getRootFolder();
        ScanResultDto.ScanResultDtoBuilder result = ScanResultDto.builder()
                .scannedPath(rootPath)
                .startTime(new Date());

        int categoriesCreated = 0;
        int categoriesUpdated = 0;
        int moviesDiscovered = 0;
        int moviesSkipped = 0;
        List<String> errors = new ArrayList<>();

        try {
            // List top-level folders (categories)
            List<MegaEntry> entries = listFolder(rootPath);

            for (MegaEntry entry : entries) {
                if (entry.isDirectory()) {
                    // This is a category folder
                    String categoryPath = rootPath.endsWith("/")
                            ? rootPath + entry.name()
                            : rootPath + "/" + entry.name();

                    Category category = getOrCreateCategory(entry.name(), categoryPath);
                    if (category.getId() == null || categoryRepository.findById(category.getId()).isEmpty()) {
                        categoriesCreated++;
                    } else {
                        categoriesUpdated++;
                    }
                    category = categoryRepository.save(category);

                    // Scan movies in category folder
                    try {
                        List<MegaEntry> movieEntries = listFolder(categoryPath);
                        for (MegaEntry movieEntry : movieEntries) {
                            if (!movieEntry.isDirectory() && isVideoFile(movieEntry.name())) {
                                String moviePath = categoryPath + "/" + movieEntry.name();

                                if (movieRepository.existsByMegaPath(moviePath)) {
                                    moviesSkipped++;
                                    continue;
                                }

                                Movie movie = createMovieFromEntry(movieEntry, moviePath, category);
                                movieRepository.save(movie);
                                moviesDiscovered++;
                                log.debug("Discovered movie: {} in category: {}", movie.getTitle(), category.getName());
                            }
                        }
                    } catch (Exception e) {
                        String error = "Error scanning category " + entry.name() + ": " + e.getMessage();
                        errors.add(error);
                        log.error(error, e);
                    }
                } else if (isVideoFile(entry.name())) {
                    // Video in root folder (uncategorized)
                    String moviePath = rootPath.endsWith("/")
                            ? rootPath + entry.name()
                            : rootPath + "/" + entry.name();

                    if (movieRepository.existsByMegaPath(moviePath)) {
                        moviesSkipped++;
                        continue;
                    }

                    Movie movie = createMovieFromEntry(entry, moviePath, null);
                    movieRepository.save(movie);
                    moviesDiscovered++;
                    log.debug("Discovered uncategorized movie: {}", movie.getTitle());
                }
            }

        } catch (Exception e) {
            String error = "Scan failed: " + e.getMessage();
            errors.add(error);
            log.error("Mega folder scan failed", e);
        }

        ScanResultDto scanResult = result
                .endTime(new Date())
                .categoriesCreated(categoriesCreated)
                .categoriesUpdated(categoriesUpdated)
                .moviesDiscovered(moviesDiscovered)
                .moviesSkipped(moviesSkipped)
                .errors(errors)
                .success(errors.isEmpty())
                .build();

        log.info("Scan completed: {} categories, {} movies discovered, {} skipped",
                categoriesCreated + categoriesUpdated, moviesDiscovered, moviesSkipped);

        return scanResult;
    }

    private List<MegaEntry> listFolder(String path) throws Exception {
        log.debug("Listing Mega folder: {}", path);

        ProcessBuilder pb = new ProcessBuilder("mega-ls", "-l", path);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        List<MegaEntry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("FLAGS")) continue;

                MegaEntry entry = parseMegaLsLine(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mega-ls failed with exit code: " + exitCode);
        }

        return entries;
    }

    private MegaEntry parseMegaLsLine(String line) {
        // mega-ls -l output format: FLAGS SIZE DATE NAME
        // Example: "d--- 0 01Jan2024 12:00:00 FolderName"
        // Example: "---- 1.5G 01Jan2024 12:00:00 Movie.mp4"

        String[] parts = line.trim().split("\\s+", 4);
        if (parts.length < 4) return null;

        String flags = parts[0];
        String sizeStr = parts[1];
        // parts[2] is date, we skip it
        String name = parts.length > 3 ? parts[3] : parts[2];

        // Handle date+time taking multiple parts
        if (name.matches("\\d{2}:\\d{2}:\\d{2}")) {
            // This is time, real name is after
            String[] reParts = line.trim().split("\\s+", 5);
            if (reParts.length >= 5) {
                name = reParts[4];
            } else {
                return null;
            }
        }

        boolean isDirectory = flags.startsWith("d");
        long size = parseSize(sizeStr);

        return new MegaEntry(name, isDirectory, size);
    }

    private long parseSize(String sizeStr) {
        try {
            sizeStr = sizeStr.toUpperCase();
            double value = Double.parseDouble(sizeStr.replaceAll("[^0-9.]", ""));

            if (sizeStr.endsWith("GB") || sizeStr.endsWith("G")) {
                return (long) (value * 1024 * 1024 * 1024);
            } else if (sizeStr.endsWith("MB") || sizeStr.endsWith("M")) {
                return (long) (value * 1024 * 1024);
            } else if (sizeStr.endsWith("KB") || sizeStr.endsWith("K")) {
                return (long) (value * 1024);
            }
            return (long) value;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isVideoFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        for (String ext : properties.getMega().getVideoExtensions()) {
            if (lowerName.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Category getOrCreateCategory(String name, String megaPath) {
        return categoryRepository.findByMegaPath(megaPath)
                .orElseGet(() -> categoryRepository.findByName(name)
                        .map(cat -> {
                            cat.setMegaPath(megaPath);
                            return cat;
                        })
                        .orElse(Category.builder()
                                .name(name)
                                .megaPath(megaPath)
                                .description("Auto-discovered from Mega folder")
                                .build()));
    }

    private Movie createMovieFromEntry(MegaEntry entry, String megaPath, Category category) {
        String title = extractTitleFromFileName(entry.name());
        Integer year = extractYearFromFileName(entry.name());

        return Movie.builder()
                .title(title)
                .megaPath(megaPath)
                .megaUrl(megaPath) // Will be resolved to actual URL when downloading
                .fileSize(entry.size())
                .category(category)
                .status(MovieStatus.PENDING)
                .year(year)
                .contentType(detectContentType(entry.name()))
                .build();
    }

    private String extractTitleFromFileName(String fileName) {
        // Remove extension
        String name = fileName;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }

        // Remove common patterns like (2023), [1080p], etc.
        name = name.replaceAll("\\[.*?\\]", "")
                   .replaceAll("\\(\\d{4}\\)", "")
                   .replaceAll("\\d{3,4}p", "")
                   .replaceAll("BluRay|WEB-DL|HDRip|DVDRip|BRRip|HDTV", "")
                   .replaceAll("x264|x265|HEVC|AAC|DTS", "")
                   .replaceAll("[._-]+", " ")
                   .trim();

        // Capitalize words
        String[] words = name.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }

        return result.toString().trim();
    }

    private Integer extractYearFromFileName(String fileName) {
        // Look for year pattern (1900-2099)
        Pattern yearPattern = Pattern.compile("(19|20)\\d{2}");
        Matcher matcher = yearPattern.matcher(fileName);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String detectContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".mp4")) return "video/mp4";
        if (lowerName.endsWith(".mkv")) return "video/x-matroska";
        if (lowerName.endsWith(".avi")) return "video/x-msvideo";
        if (lowerName.endsWith(".mov")) return "video/quicktime";
        if (lowerName.endsWith(".wmv")) return "video/x-ms-wmv";
        if (lowerName.endsWith(".flv")) return "video/x-flv";
        if (lowerName.endsWith(".webm")) return "video/webm";
        return "video/mp4";
    }

    private record MegaEntry(String name, boolean isDirectory, long size) {}
}
