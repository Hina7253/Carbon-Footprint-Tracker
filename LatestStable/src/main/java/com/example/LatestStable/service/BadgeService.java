package com.example.LatestStable.service;

import com.example.LatestStable.repository.WebsiteAnalysisRepository;

public class BadgeService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BadgeService.class);

    private final WebsiteAnalysisRepository analysisRepository;

    public BadgeService(
            WebsiteAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }
}
