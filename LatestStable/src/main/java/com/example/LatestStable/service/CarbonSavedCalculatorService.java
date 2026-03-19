package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CarbonSavedCalculatorService {


    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(
                    CarbonSavedCalculatorService.class);

    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;
    private final CarbonCalculatorService carbonCalculatorService;


    public CarbonSavedCalculatorService(
            WebsiteAnalysisRepository analysisRepository,
            PageResourcesRepository resourceRepository,
            CarbonCalculatorService carbonCalculatorService) {
        this.analysisRepository   = analysisRepository;
        this.resourceRepository   = resourceRepository;
        this.carbonCalculatorService = carbonCalculatorService;
    }

    // Main method
    public Map<String, Object> calculatePotentialSavings(Long analysisId) {

        log.info("Calculating savings for analysis: {}", analysisId);

        WebsiteAnalysis analysis = analysisRepository
                .findById(analysisId)
                .orElseThrow(() ->
                        new RuntimeException("Analysis not found: " + analysisId));

        List<PageResources> resources =
                resourceRepository
                        .findByWebsiteAnalysis_IdOrderBySizeBytesDesc(analysisId);

        // Current state
        long currentBytes = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .mapToLong(PageResources::getSizeBytes)
                .sum();

        double currentCo2 = analysis.getCo2PerVisitGrams() != null
                ? analysis.getCo2PerVisitGrams() : 0.0;

        // Calculate optimized state
        long optimizedBytes = calculateOptimizedBytes(resources);
        double savedBytes   = currentBytes - optimizedBytes;
        double savedPercent = currentBytes > 0
                ? (savedBytes / currentBytes) * 100 : 0;

        // Optimized CO2
        double optimizedCo2 =
                carbonCalculatorService.calculateCo2PerVisit(optimizedBytes);
        double savedCo2PerVisit = currentCo2 - optimizedCo2;

        // Annual savings
        long monthlyVisits = analysis.getMonthlyVisits() != null
                ? analysis.getMonthlyVisits() : 10000L;

        double savedCo2YearlyKg =
                carbonCalculatorService.calculateAnnualCo2Kg(
                        savedCo2PerVisit, monthlyVisits);

        // Build per-resource savings breakdown
        List<Map<String, Object>> resourceSavings =
                buildResourceSavings(resources);

        // Real world equivalents of savings
        Map<String, Object> savingsEquivalents =
                buildSavingsEquivalents(savedCo2YearlyKg);

        // Final optimized grade
        String optimizedGrade =
                carbonCalculatorService.calculateGrade(optimizedCo2);

        // Build response
        Map<String, Object> result = new HashMap<>();

        // Current state
        Map<String, Object> current = new HashMap<>();
        current.put("bytes",           currentBytes);
        current.put("bytesFormatted",  formatBytes(currentBytes));
        current.put("co2PerVisitGrams", round(currentCo2));
        current.put("grade",           analysis.getGrade());
        result.put("current", current);

        // Optimized state
        Map<String, Object> optimized = new HashMap<>();
        optimized.put("bytes",          optimizedBytes);
        optimized.put("bytesFormatted", formatBytes(optimizedBytes));
        optimized.put("co2PerVisitGrams", round(optimizedCo2));
        optimized.put("grade",          optimizedGrade);
        result.put("optimized", optimized);

        // Savings
        Map<String, Object> savings = new HashMap<>();
        savings.put("bytesSaved",         (long) savedBytes);
        savings.put("bytesSavedFormatted", formatBytes((long) savedBytes));
        savings.put("percentageSaved",     round(savedPercent));
        savings.put("co2SavedPerVisitGrams", round(savedCo2PerVisit));
        savings.put("co2SavedYearlyKg",    round(savedCo2YearlyKg));
        savings.put("gradeImprovement",
                analysis.getGrade() + " → " + optimizedGrade);
        result.put("savings", savings);

        result.put("realWorldEquivalents", savingsEquivalents);
        result.put("resourceBreakdown",    resourceSavings);
        result.put("totalOptimizations",   resourceSavings.size());

        return result;
    }

    // ── CALCULATE OPTIMIZED BYTES ─────────────────────────────────
    // Har resource type ke liye reduction apply karo
    private long calculateOptimizedBytes(List<PageResources> resources) {
        return resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .mapToLong(r -> {
                    long size = r.getSizeBytes();
                    double reduction = getReductionFactor(
                            r.getResourceType(), size);
                    return (long)(size * (1.0 - reduction));
                })
                .sum();
    }

    // ── REDUCTION FACTOR PER TYPE ──
    private double getReductionFactor(ResourceType type, long size) {
        return switch (type) {
            case IMAGE  -> {
                // WebP conversion saves 30-50%
                if (size > 500_000) yield 0.60;
                if (size > 200_000) yield 0.45;
                if (size > 50_000)  yield 0.30;
                yield 0.15;
            }
            case VIDEO  -> 0.35; // H.265 compression
            case SCRIPT -> {
                // Minification + tree shaking
                if (size > 200_000) yield 0.55;
                if (size > 50_000)  yield 0.40;
                yield 0.20;
            }
            case STYLE  -> 0.30; // CSS purging + minification
            case FONT   -> 0.40; // Subsetting + woff2
            case API_CALL -> 0.20; // Caching
            default     -> 0.10;
        };
    }

    // ── BUILD PER-RESOURCE SAVINGS ────────────────────────────────
    private List<Map<String, Object>> buildResourceSavings(
            List<PageResources> resources) {

        return resources.stream()
                .filter(r -> r.getSizeBytes() != null
                        && r.getSizeBytes() > 10000) // Only > 10KB
                .map(r -> {
                    long size = r.getSizeBytes();
                    double factor = getReductionFactor(
                            r.getResourceType(), size);
                    long saved = (long)(size * factor);

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("url",          shortenUrl(r.getResourceUrl()));
                    entry.put("type",         r.getResourceType());
                    entry.put("currentSize",  formatBytes(size));
                    entry.put("savedSize",    formatBytes(saved));
                    entry.put("savingPct",    (int)(factor * 100) + "%");
                    entry.put("action",       getAction(r.getResourceType()));
                    return entry;
                })
                .sorted((a, b) -> {
                    // Sort by saved size (largest saving first)
                    String sa = (String) a.get("savedSize");
                    String sb2 = (String) b.get("savedSize");
                    return sb2.compareTo(sa);
                })
                .limit(10) // Top 10 optimizations
                .collect(Collectors.toList());
    }

}
