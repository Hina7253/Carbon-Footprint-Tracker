package com.example.LatestStable.service;

import java.util.HashMap;
import java.util.Map;

public class CompareService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CompareService.class);

    private final AnalysisService analysisService;

    public CompareService(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }
}
