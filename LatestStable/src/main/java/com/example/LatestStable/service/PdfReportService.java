package com.example.LatestStable.service;

import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;

public class PdfReportService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PdfReportService.class);

    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;

    public PdfReportService(
            WebsiteAnalysisRepository analysisRepository,
            PageResourcesRepository resourceRepository) {
        this.analysisRepository = analysisRepository;
        this.resourceRepository = resourceRepository;
    }

}
