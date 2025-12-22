package com.mediaserver.controller;

import com.mediaserver.dto.ScanResultDto;
import com.mediaserver.service.MegaScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScanController {

    private final MegaScanService megaScanService;

    @PostMapping
    public ResponseEntity<ScanResultDto> scanFolder(@RequestParam(required = false) String path) {
        ScanResultDto result = megaScanService.scanFolder(path);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/async")
    public ResponseEntity<String> scanFolderAsync(@RequestParam(required = false) String path) {
        megaScanService.scanFolderAsync(path);
        return ResponseEntity.accepted().body("Scan started");
    }
}
