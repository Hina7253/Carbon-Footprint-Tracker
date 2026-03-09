package com.example.LatestStable.service;

import com.example.LatestStable.dto.AnalysisRequestDTO;
import com.example.LatestStable.dto.AnalysisResponseDTO;
import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisService {
    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;
    private final WebCrawlerService crawlerService;
    private final CarbonCalculatorService carbonCalculatorService;

    // ── START ANALYSIS ────────────────────────────────────────────
    // This is called by the Controller when user hits "Analyze"
    @Transactional
    public AnalysisResponseDTO startAnalysis(AnalysisRequestDTO request) {

        log.info("Starting analysis for: {}", request.getUrl());

        // Step 1: Save analysis record with PROCESSING status
        WebsiteAnalysis analysis = WebsiteAnalysis.builder()
                .websiteUrl(request.getNormalizedUrl())
                .monthlyVisits(request.getMonthlyVisits())
                .crawlPages(request.getCrawlPages())
                .status(AnalysisStatus.PROCESSING)
                .build();

        analysis = analysisRepository.save(analysis);
        log.info("Analysis created with ID: {}", analysis.getId());

        try {
            // Step 2: Crawl the website
            String baseDomain = request.getBaseDomain();
            List<PageResource> resources;

            if (Boolean.TRUE.equals(request.getEnableCrawlMode())
                    && request.getCrawlPages() > 1) {
                // Multi-page crawl
                resources = crawlerService.crawlMultiplePages(
                        request.getNormalizedUrl(),
                        analysis,
                        baseDomain,
                        request.getCrawlPages()
                );
            } else {
                // Single page crawl
                resources = crawlerService.crawlPage(
                        request.getNormalizedUrl(),
                        analysis,
                        baseDomain
                );
            }

            // Step 3: Save all resources to database
            resourceRepository.saveAll(resources);

            // Step 4: Calculate total metrics
            long totalBytes = resources.stream()
                    .filter(r -> r.getSizeBytes() != null)
                    .mapToLong(PageResource::getSizeBytes)
                    .sum();

            double co2PerVisit = carbonCalculatorService
                    .calculateCo2PerVisit(totalBytes);

            double co2Yearly = carbonCalculatorService
                    .calculateAnnualCo2Kg(
                            co2PerVisit,
                            analysis.getMonthlyVisits()
                    );

            double energyKwh = carbonCalculatorService
                    .calculateEnergyKwh(totalBytes);

            String grade = carbonCalculatorService
                    .calculateGrade(co2PerVisit);

            // Step 5: Update analysis with results
            analysis.setTotalTransferBytes(totalBytes);
            analysis.setCo2PerVisitGrams(co2PerVisit);
            analysis.setCo2YearlyKg(co2Yearly);
            analysis.setEnergyUsageKwh(energyKwh);
            analysis.setGrade(grade);
            analysis.setStatus(AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(LocalDateTime.now());
            analysis.setResources(resources);

        }
