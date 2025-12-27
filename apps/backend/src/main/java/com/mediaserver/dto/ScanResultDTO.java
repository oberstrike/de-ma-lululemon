package com.mediaserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultDTO {
    private String scannedPath;
    private Date startTime;
    private Date endTime;
    private int categoriesCreated;
    private int categoriesUpdated;
    private int moviesDiscovered;
    private int moviesSkipped;
    private List<String> errors;
    private boolean success;
}
