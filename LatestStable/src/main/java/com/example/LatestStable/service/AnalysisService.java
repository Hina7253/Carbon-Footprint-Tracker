package com.example.LatestStable.service;

import com.example.LatestStable.dto.AnalysisRequestDTO;
import com.example.LatestStable.dto.AnalysisResponseDTO;
import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.model.WebsiteAnalysis.AnalysisStatus;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service


public class AnalysisService {
    private final AiSuggestionService aiSuggestionService;
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AnalysisService.class);

    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;
    private final WebCrawlerService crawlerService;
    private final CarbonCalculatorService carbonCalculatorService;

    public AnalysisService(
            WebsiteAnalysisRepository analysisRepository,
            PageResourcesRepository resourceRepository,
            WebCrawlerService crawlerService,
            CarbonCalculatorService carbonCalculatorService,
            AiSuggestionService aiSuggestionService) {    // ← YE ADD KARO
        this.analysisRepository    = analysisRepository;
        this.resourceRepository    = resourceRepository;
        this.crawlerService        = crawlerService;
        this.carbonCalculatorService = carbonCalculatorService;
        this.aiSuggestionService   = aiSuggestionService; // ← YE ADD KARO
    }


    @Transactional
    public AnalysisResponseDTO startAnalysis(AnalysisRequestDTO request) {

        log.info("Starting analysis for: {}", request.getUrl());

        WebsiteAnalysis analysis = WebsiteAnalysis.builder()
                .websiteUrl(request.getNormalizedUrl())
                .monthlyVisits((Long) request.getMonthlyVisits())
                .crawlPages(request.getCrawlPages())
                .status(AnalysisStatus.PROCESSING)
                .build();

        analysis = analysisRepository.save(analysis);

        try {
            String baseDomain = request.getBaseDomain();
            List<PageResources> resources;

            if (Boolean.TRUE.equals(request.getEnableCrawlMode())
                    && request.getCrawlPages() > 1) {
                resources = crawlerService.crawlMultiplePages(
                        request.getNormalizedUrl(),
                        analysis,
                        baseDomain,
                        request.getCrawlPages()
                );
            } else {
                resources = crawlerService.crawlPage(
                        request.getNormalizedUrl(),
                        analysis,
                        baseDomain
                );
            }

            resourceRepository.saveAll(resources);

            long totalBytes = resources.stream()
                    .filter(r -> r.getSizeBytes() != null)
                    .mapToLong(PageResources::getSizeBytes)
                    .sum();

            double co2PerVisit =
                    carbonCalculatorService.calculateCo2PerVisit(totalBytes);

            double co2Yearly =
                    carbonCalculatorService.calculateAnnualCo2Kg(
                            co2PerVisit,
                            analysis.getMonthlyVisits()
                    );

            double energyKwh =
                    carbonCalculatorService.calculateEnergyKwh(totalBytes);

            String grade =
                    carbonCalculatorService.calculateGrade(co2PerVisit);

            analysis.setTotalTransferBytes(totalBytes);
            analysis.setCo2PerVisitGrams(co2PerVisit);
            analysis.setCo2YearlyKg(co2Yearly);
            analysis.setEnergyUsageKwh(energyKwh);
            analysis.setGrade(grade);
            analysis.setStatus(AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(LocalDateTime.now());
            analysis.setResources(resources);

            analysis = analysisRepository.save(analysis);

            return buildResponse(analysis, resources);

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage());
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage(e.getMessage());
            analysisRepository.save(analysis);
            throw new RuntimeException("Analysis failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AnalysisResponseDTO getAnalysis(Long id) {
        WebsiteAnalysis analysis = analysisRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Analysis not found: " + id));

        List<PageResources> resources =
                resourceRepository
                        .findByWebsiteAnalysis_IdOrderBySizeBytesDesc(id);

        return buildResponse(analysis, resources);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponseDTO> getHistory() {
        List<WebsiteAnalysis> analyses =
                analysisRepository.findByStatusOrderByCreatedAtDesc(
                        AnalysisStatus.COMPLETED
                );

        return analyses.stream()
                .map(a -> buildResponse(a, new ArrayList<>()))
                .collect(Collectors.toList());
    }

    private AnalysisResponseDTO buildResponse(
            WebsiteAnalysis analysis,
            List<PageResources> resources) {

        // Carbon Equivalents
        AnalysisResponseDTO.CarbonEquivalents equivalents = null;

        if (analysis.getCo2YearlyKg() != null) {
            CarbonCalculatorService.CarbonEquivalents eq =
                    carbonCalculatorService.calculateEquivalents(
                            analysis.getCo2YearlyKg()
                    );
            equivalents = AnalysisResponseDTO.CarbonEquivalents.builder()
                    .kmDriven(round(eq.kmDriven()))
                    .treesNeeded(round(eq.treesNeeded()))
                    .smartphoneCharges(round(eq.smartphoneCharges()))
                    .googleSearches(round(eq.googleSearches()))
                    .build();
        }

        // Carbon Metrics
        AnalysisResponseDTO.CarbonMetrics carbonMetrics =
                AnalysisResponseDTO.CarbonMetrics.builder()
                        .co2PerVisitGrams(round(analysis.getCo2PerVisitGrams()))
                        .co2YearlyKg(round(analysis.getCo2YearlyKg()))
                        .energyUsageKwh(round(analysis.getEnergyUsageKwh()))
                        .equivalents(equivalents)
                        .build();

        // Resource Breakdown
        Map<ResourceType,
                AnalysisResponseDTO.ResourceTypeBreakdown> breakdown =
                buildBreakdown(resources, analysis.getTotalTransferBytes());

        // Third party count
        long thirdPartyCount = resources.stream()
                .filter(r -> Boolean.TRUE.equals(r.isThirdParty()))
                .count();

        // Resource Summary
        AnalysisResponseDTO.ResourceSummary resourceSummary =
                AnalysisResponseDTO.ResourceSummary.builder()
                        .totalTransferBytes(analysis.getTotalTransferBytes())
                        .totalTransferFormatted(
                                formatBytes(analysis.getTotalTransferBytes()))
                        .totalResourceCount(resources.size())
                        .thirdPartyCount((int) thirdPartyCount)
                        .breakdown(breakdown)
                        .build();

        // Top 5 Hotspots
        List<AnalysisResponseDTO.ResourceDetail> hotspots = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .sorted(Comparator.comparingLong(
                        PageResources::getSizeBytes).reversed())
                .limit(5)
                .map(this::buildResourceDetail)
                .collect(Collectors.toList());

        // Comparison Data
        AnalysisResponseDTO.ComparisonData comparison = null;

        if (analysis.getCo2PerVisitGrams() != null) {
            comparison = AnalysisResponseDTO.ComparisonData.builder()
                    .averageWebsiteCo2Grams(0.5)
                    .percentileBetter(
                            carbonCalculatorService.calculatePercentileBetter(
                                    analysis.getCo2PerVisitGrams()))
                    .performanceCategory(
                            carbonCalculatorService.getPerformanceCategory(
                                    analysis.getCo2PerVisitGrams()))
                    .build();
        }

        return AnalysisResponseDTO.builder()
                .id(analysis.getId())
                .websiteUrl(analysis.getWebsiteUrl())
                .status(analysis.getStatus())
                .grade(analysis.getGrade())
                .analyzedAt(analysis.getCompletedAt())
                .errorMessage(analysis.getErrorMessage())
                .carbonMetrics(carbonMetrics)
                .resourceSummary(resourceSummary)
                .hotspots(hotspots)
                .aiSuggestions(analysis.getAiSuggestions())
                .comparison(comparison)
                .build();
    }

    private Map<ResourceType, AnalysisResponseDTO.ResourceTypeBreakdown>
    buildBreakdown(List<PageResources> resources, Long totalBytes) {

        Map<ResourceType, AnalysisResponseDTO.ResourceTypeBreakdown>
                breakdown = new HashMap<>();

        Map<ResourceType, List<PageResources>> grouped = resources.stream()
                .collect(Collectors.groupingBy(
                        PageResources::getResourceType));

        for (Map.Entry<ResourceType,
                List<PageResources>> entry : grouped.entrySet()) {

            ResourceType type = entry.getKey();
            List<PageResources> typeResources = entry.getValue();

            long typeBytes = typeResources.stream()
                    .filter(r -> r.getSizeBytes() != null)
                    .mapToLong(PageResources::getSizeBytes)
                    .sum();

            double percentage = (totalBytes != null && totalBytes > 0)
                    ? (typeBytes * 100.0 / totalBytes)
                    : 0.0;

            breakdown.put(type,
                    AnalysisResponseDTO.ResourceTypeBreakdown.builder()
                            .type(type)
                            .count(typeResources.size())
                            .totalBytes(typeBytes)
                            .totalBytesFormatted(formatBytes(typeBytes))
                            .percentageOfTotal(round(percentage))
                            .build());
        }

        return breakdown;
    }

    private AnalysisResponseDTO.ResourceDetail buildResourceDetail(
            PageResources resource) {

        return AnalysisResponseDTO.ResourceDetail.builder()
                .url(resource.getResourceUrl())
                .type(resource.getResourceType())
                .sizeBytes(resource.getSizeBytes())
                .sizeFormatted(formatBytes(resource.getSizeBytes()))
                .co2Grams(round(resource.getCo2ContributionGrams()))
                .isThirdParty(resource.isThirdParty())
                .isCached(resource.isCached())
                .optimizationPotential(resource.getOptimizationPotential())
                .optimizationTip(
                        getOptimizationTip(resource.getResourceType()))
                .build();
    }

    private String formatBytes(Long bytes) {
        if (bytes == null) return "Unknown";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB",
                bytes / (1024.0 * 1024 * 1024));
    }

    private String getOptimizationTip(ResourceType type) {
        return switch (type) {
            case IMAGE ->
                    "Convert to WebP/AVIF, use lazy loading";
            case VIDEO ->
                    "Use adaptive streaming, compress with H.265";
            case SCRIPT ->
                    "Minify, tree-shake, use code splitting";
            case STYLE ->
                    "Purge unused CSS, minify stylesheet";
            case FONT ->
                    "Use woff2, subset fonts, limit font weights";
            case API_CALL ->
                    "Cache API responses, reduce call frequency";
            default ->
                    "Compress and cache this resource";
        };
    }

    private Double round(Double value) {
        if (value == null) return null;
        return Math.round(value * 10000.0) / 10000.0;
    }
}