package com.carbonscope.service;

import com.carbonscope.config.AppConfig.CarbonConstants;
import com.carbonscope.entity.PageResource;
import com.carbonscope.entity.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class CarbonCalculatorService {

    // Spring injects this bean (defined in AppConfig)
    private final CarbonConstants carbonConstants;

    // Industry average for comparison
    private static final double AVERAGE_PAGE_CO2_GRAMS = 0.5;

    // ── MAIN CALCULATION: Bytes → CO2 ─────────────────────────────
    /**
     * Calculate CO2 emissions for a single page visit.
     *
     * @param totalBytes Total bytes transferred when loading the page
     * @return CO2 in grams for one visit
     */
    public double calculateCo2PerVisit(long totalBytes) {
        // Convert bytes to gigabytes
        // Example: 2,000,000 bytes = 0.002 GB
        double gigabytes = totalBytes / 1_000_000_000.0;

        // Calculate energy used (kWh)
        // Example: 0.002 GB × 0.06 kWh/GB = 0.00012 kWh
        double energyKwh = gigabytes * carbonConstants.kwhPerGb();

        // Calculate CO2 (grams)
        // Example: 0.00012 kWh × 490 gCO2/kWh = 0.0588 grams CO2
        double co2Grams = energyKwh * carbonConstants.gramsCo2PerKwh();

        log.debug("Carbon calc: {}B → {}GB → {}kWh → {}gCO2",
                totalBytes, gigabytes, energyKwh, co2Grams);

        return co2Grams;
    }

    /**
     * Calculate energy usage in kWh for one page visit.
     */
    public double calculateEnergyKwh(long totalBytes) {
        double gigabytes = totalBytes / 1_000_000_000.0;
        return gigabytes * carbonConstants.kwhPerGb();
    }

    // ── ANNUAL CALCULATION ────────────────────────────────────────
    /**
     * Scale per-visit CO2 to annual emissions based on traffic.
     *
     * @param co2PerVisitGrams CO2 per single visit in grams
     * @param monthlyVisits    How many visitors per month
     * @return Annual CO2 in KILOGRAMS
     */
    public double calculateAnnualCo2Kg(double co2PerVisitGrams, long monthlyVisits) {
        // Annual visits = monthly × 12
        long annualVisits = monthlyVisits * 12;

        // Annual CO2 in grams
        double annualCo2Grams = co2PerVisitGrams * annualVisits;

        // Convert grams to kilograms
        double annualCo2Kg = annualCo2Grams / 1000.0;

        log.debug("Annual CO2: {}g/visit × {} visits = {}kg/year",
                co2PerVisitGrams, annualVisits, annualCo2Kg);

        return annualCo2Kg;
    }

    // ── INDIVIDUAL RESOURCE CARBON ────────────────────────────────
    /**
     * Calculate CO2 contribution of a SINGLE resource.
     * Applies the resource type multiplier.
     *
     * @param sizeBytes    File size in bytes
     * @param resourceType The type (IMAGE, VIDEO, etc.)
     * @return CO2 in grams for this resource per visit
     */
    public double calculateResourceCo2(long sizeBytes, ResourceType resourceType) {
        double baseCo2 = calculateCo2PerVisit(sizeBytes);
        double multiplier = resourceType.getCarbonMultiplier();
        return baseCo2 * multiplier;
    }

    // ── GRADE CALCULATOR ─────────────────────────────────────────
    /**
     * Assign a letter grade based on CO2 per visit.
     *
     * Grading scale (based on websitecarbon.com methodology):
     *   A: < 0.095g  → Top 10% cleanest websites
     *   B: < 0.185g  → Above average
     *   C: < 0.34g   → Average
     *   D: < 0.49g   → Below average
     *   E: < 0.65g   → Poor
     *   F: >= 0.65g  → Very poor
     */
    public String calculateGrade(double co2PerVisitGrams) {
        if (co2PerVisitGrams < 0.095) return "A";
        if (co2PerVisitGrams < 0.185) return "B";
        if (co2PerVisitGrams < 0.340) return "C";
        if (co2PerVisitGrams < 0.490) return "D";
        if (co2PerVisitGrams < 0.650) return "E";
        return "F";
    }

    // ── REAL-WORLD EQUIVALENTS ────────────────────────────────────
    /**
     * Convert yearly CO2 kg to relatable real-world equivalents.
     * These make abstract numbers meaningful to users.
     *
     * FORMULAS:
     *   Driving: 1 km driving ≈ 0.21 kg CO2 (average EU car)
     *   Trees:   1 tree absorbs ≈ 21 kg CO2/year
     *   Phone:   Charging smartphone ≈ 0.005 kg CO2
     *   Search:  1 Google search ≈ 0.0002 kg CO2 (Google's own estimate)
     */
    public CarbonEquivalents calculateEquivalents(double yearlyKgCo2) {
        return new CarbonEquivalents(
                yearlyKgCo2 / 0.21,      // km driven
                yearlyKgCo2 / 21.0,      // trees needed to absorb it
                yearlyKgCo2 / 0.005,     // smartphone charges
                yearlyKgCo2 / 0.0002     // Google searches
        );
    }

    // ── OPTIMIZATION POTENTIAL ────────────────────────────────────
    /**
     * Estimate how much a resource could be reduced in size.
     * Returns 0.0 (none) to 1.0 (100% reducible).
     *
     * LOGIC:
     *   - Large images (>200KB) could often be compressed 60%+
     *   - Unminified JS (>100KB) could be minified/tree-shaken 40-70%
     *   - Cached resources already minimize repeat download
     *   - Modern formats (WebP, Woff2) are already optimized
     */
    public double calculateOptimizationPotential(
            long sizeBytes, ResourceType type, boolean isCached, String contentType) {

        if (isCached) return 0.1; // Already cached, low optimization needed

        return switch (type) {
            case IMAGE -> {
                // Large images → high optimization potential
                if (sizeBytes > 500_000) yield 0.7;       // > 500KB → 70% reducible
                if (sizeBytes > 200_000) yield 0.5;       // > 200KB → 50% reducible
                if (sizeBytes > 50_000)  yield 0.3;       // > 50KB  → 30% reducible
                yield 0.1;
            }
            case VIDEO -> sizeBytes > 1_000_000 ? 0.4 : 0.2; // Videos are hard to optimize
            case SCRIPT -> {
                // Unminified JS is very compressible
                if (contentType != null && contentType.contains("javascript")) {
                    if (sizeBytes > 100_000) yield 0.6;
                    if (sizeBytes > 50_000)  yield 0.4;
                }
                yield 0.2;
            }
            case FONT -> 0.3;   // Fonts can be subset
            case STYLE -> 0.25; // CSS can be purged and minified
            default -> 0.1;
        };
    }

    // ── PERFORMANCE CATEGORY ──────────────────────────────────────
    /**
     * Human-readable performance category.
     */
    public String getPerformanceCategory(double co2PerVisitGrams) {
        if (co2PerVisitGrams < 0.185) return "Clean";
        if (co2PerVisitGrams < 0.490) return "Average";
        return "Dirty";
    }

    /**
     * Calculate what percentile the site falls in.
     * "Better than X% of websites"
     * This is a simplified model based on log-normal distribution
     * of website sizes from HTTP Archive data.
     */
    public double calculatePercentileBetter(double co2PerVisitGrams) {
        // Simplified: based on websitecarbon.com grade distribution
        // A: top 10%, B: top 20%, C: top 50%, D: top 65%...
        if (co2PerVisitGrams < 0.095) return 90.0;
        if (co2PerVisitGrams < 0.185) return 75.0;
        if (co2PerVisitGrams < 0.340) return 50.0;
        if (co2PerVisitGrams < 0.490) return 35.0;
        if (co2PerVisitGrams < 0.650) return 20.0;
        return 10.0;
    }

    // ── RECORD: Carbon Equivalents result ─────────────────────────
    /**
     * Immutable container for the equivalents calculation result.
     * Using Java Record (Java 16+) for clean, concise code.
     */
    public record CarbonEquivalents(
            double kmDriven,
            double treesNeeded,
            double smartphoneCharges,
            double googleSearches
    ) {}
}