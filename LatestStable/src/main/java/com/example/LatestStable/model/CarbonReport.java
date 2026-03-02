package com.example.LatestStable.model;

import java.time.LocalDateTime;

public class CarbonReport {
    private Long id;
    private String url;
    private double totalPageSize;
    private double co2PerVisitGrams;
    private double co2PerYearKg;
    private String carbonGrade;
    private boolean greenHosted;
    private LocalDateTime scannedAt;
}
