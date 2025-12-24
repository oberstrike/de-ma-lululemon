package com.mediaserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "media")
@Data
public class MediaProperties {
    private Storage storage = new Storage();
    private Mega mega = new Mega();
    private Streaming streaming = new Streaming();
    private Cors cors = new Cors();
    private Download download = new Download();

    @Data
    public static class Storage {
        private String path = "/var/media/videos";
        private String tempPath = "/var/media/temp";
        private int maxCacheSizeGb = 100;
    }

    @Data
    public static class Mega {
        private String email;
        private String password;
        private String rootFolder = "/";
        private String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm"};
        private String scanCron = "0 0 * * * *"; // Default: every hour
        private boolean scanEnabled = true;
    }

    @Data
    public static class Streaming {
        private int chunkSize = 1048576; // 1MB
    }

    @Data
    public static class Cors {
        private String[] allowedOrigins = {"http://localhost:4200"};
    }

    @Data
    public static class Download {
        private int processTimeoutMinutes = 60; // Default: 1 hour timeout for mega-get
    }
}
