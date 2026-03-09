package com.example.LatestStable.service;

import com.example.LatestStable.dto.AnalysisRequestDTO;
import com.example.LatestStable.dto.AnalysisResponseDTO;
import com.example.LatestStable.dto.AnalysisResponseDTO.*;
import com.example.LatestStable.entity.PageResource;
import com.example.LatestStable.entity.ResourceType;
import com.example.LatestStable.entity.WebsiteAnalysis;
import com.example.LatestStable.entity.WebsiteAnalysis.AnalysisStatus;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

            analysis = analysisRepository.save(analysis);
            log.info("Analysis completed! Grade: {}, CO2: {}g/visit",
                    grade, co2PerVisit);

            // Step 6: Build and return response
            return buildResponse(analysis, resources);

        } catch (Exception e) {
            // If anything fails, mark as FAILED
            log.error("Analysis failed for {}: {}", request.getUrl(), e.getMessage());
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage(e.getMessage());
            analysisRepository.save(analysis);
            throw new RuntimeException("Analysis failed: " + e.getMessage());
        }
    }

    // ── GET ANALYSIS BY ID ────────────────────────────────────────
    @Transactional(readOnly = true)
    public AnalysisResponseDTO getAnalysis(Long id) {
        WebsiteAnalysis analysis = analysisRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Analysis not found with id: " + id)
                );

        List<PageResource> resources =
                resourceRepository.findByWebsiteAnalysis_IdOrderBySizeBytesDesc(id);

        return buildResponse(analysis, resources);
    }

    // ── GET HISTORY ───────────────────────────────────────────────
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

    // ── BUILD RESPONSE DTO ────────────────────────────────────────
    // Converts database entity → API response DTO
    private AnalysisResponseDTO buildResponse(
            WebsiteAnalysis analysis,
            List<PageResource> resources) {

        // Carbon Equivalents
        CarbonEquivalents equivalents = null;
        if (analysis.getCo2YearlyKg() != null) {
            CarbonCalculatorService.CarbonEquivalents eq =
                    carbonCalculatorService.calculateEquivalents(
                            analysis.getCo2YearlyKg()
                    );
            equivalents = CarbonEquivalents.builder()
                    .kmDriven(round(eq.kmDriven()))
                    .treesNeeded(round(eq.treesNeeded()))
                    .smartphoneCharges(round(eq.smartphoneCharges()))
                    .googleSearches(round(eq.googleSearches()))
                    .build();
        }

        // Carbon Metrics
        CarbonMetrics carbonMetrics = CarbonMetrics.builder()
                .co2PerVisitGrams(
                        round(analysis.getCo2PerVisitGrams()))
                .co2YearlyKg(
                        round(analysis.getCo2YearlyKg()))
                .energyUsageKwh(
                        round(analysis.getEnergyUsageKwh()))
                .equivalents(equivalents)
                .build();

        // Resource Breakdown by type
        Map<ResourceType, ResourceTypeBreakdown> breakdown =
                buildBreakdown(resources, analysis.getTotalTransferBytes());

        // Third party count
        long thirdPartyCount = resources.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsThirdParty()))
                .count();

        // Resource Summary
        ResourceSummary resourceSummary = ResourceSummary.builder()
                .totalTransferBytes(analysis.getTotalTransferBytes())
                .totalTransferFormatted(
                        formatBytes(analysis.getTotalTransferBytes()))
                .totalResourceCount(resources.size())
                .thirdPartyCount((int) thirdPartyCount)
                .breakdown(breakdown)
                .build();

        // Top 5 Hotspots (heaviest resources)
        List<ResourceDetail> hotspots = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .sorted(Comparator.comparingLong(
                        PageResource::getSizeBytes).reversed())
                .limit(5)
                .map(this::buildResourceDetail)
                .collect(Collectors.toList());

        // Comparison Data
        ComparisonData comparison = null;
        if (analysis.getCo2PerVisitGrams() != null) {
            comparison = ComparisonData.builder()
                    .averageWebsiteCo2Grams(0.5)
                    .percentileBetter(carbonCalculatorService
                            .calculatePercentileBetter(
                                    analysis.getCo2PerVisitGrams()))
                    .performanceCategory(carbonCalculatorService
                            .getPerformanceCategory(
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

    // ── HELPER: Build resource type breakdown ─────────────────────
    private Map<ResourceType, ResourceTypeBreakdown> buildBreakdown(
            List<PageResource> resources, Long totalBytes) {

        Map<ResourceType, ResourceTypeBreakdown> breakdown = new HashMap<>();

        // Group resources by type
        Map<ResourceType, List<PageResource>> grouped = resources.stream()
                .collect(Collectors.groupingBy(PageResource::getResourceType));

        for (Map.Entry<ResourceType, List<PageResource>> entry
                : grouped.entrySet()) {

            ResourceType type = entry.getKey();
            List<PageResource> typeResources = entry.getValue();

            long typeBytes = typeResources.stream()
                    .filter(r -> r.getSizeBytes() != null)
                    .mapToLong(PageResource::getSizeBytes)
                    .sum();

            double percentage = (totalBytes != null && totalBytes > 0)
                    ? (typeBytes * 100.0 / totalBytes) : 0.0;

            breakdown.put(type, ResourceTypeBreakdown.builder()
                    .type(type)
                    .count(typeResources.size())
                    .totalBytes(typeBytes)
                    .totalBytesFormatted(formatBytes(typeBytes))
                    .percentageOfTotal(round(percentage))
                    .build());
        }

        return breakdown;
    }

    // ── HELPER: Build individual resource detail ──────────────────
    private ResourceDetail buildResourceDetail(PageResource resource) {
        return ResourceDetail.builder()
                .url(resource.getResourceUrl())
                .type(resource.getResourceType())
                .sizeBytes(resource.getSizeBytes())
                .sizeFormatted(formatBytes(resource.getSizeBytes()))
                .co2Grams(round(resource.getCo2ContributionGrams()))
                .isThirdParty(resource.getIsThirdParty())
                .isCached(resource.getIsCached())
                .optimizationPotential(resource.getOptimizationPotential())
                .optimizationTip(
                        getOptimizationTip(resource.getResourceType()))
                .build();
    }

    // ── HELPER: Format bytes to human-readable ────────────────────
    // 1048576 → "1.0 MB"
    // 2560    → "2.5 KB"
    private String formatBytes(Long bytes) {
        if (bytes == null) return "Unknown";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // ── HELPER: Get tip for resource type ─────────────────────────
    private String getOptimizationTip(ResourceType type) {
        return switch (type) {
            case IMAGE  -> "Convert to WebP/AVIF, use lazy loading";
            case VIDEO  -> "Use adaptive streaming, compress with H.265";
            case SCRIPT -> "Minify, tree-shake, use code splitting";
            case STYLE  -> "Purge unused CSS, minify stylesheet";
            case FONT   -> "Use woff2, subset fonts, limit font weights";
            case API_CALL -> "Cache API responses, reduce call frequency";
            default     -> "Compress and cache this resource";
        };
    }

    // ── HELPER: Round to 4 decimal places ─────────────────────────
    private Double round(Double value) {
        if (value == null) return null;
        return Math.round(value * 10000.0) / 10000.0;
    }
}
