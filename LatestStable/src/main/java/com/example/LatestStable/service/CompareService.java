package com.example.LatestStable.service;

import com.example.LatestStable.dto.AnalysisRequestDTO;
import com.example.LatestStable.dto.AnalysisResponseDTO;

import java.util.HashMap;
import java.util.Map;

public class CompareService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CompareService.class);

    private final AnalysisService analysisService;

    public CompareService(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // ── COMPARE 2 WEBSITES ────────────────────────────────────────
    public Map<String, Object> compareTwoWebsites(
            String url1, String url2, Long monthlyVisits) {

        log.info("Comparing: {} vs {}", url1, url2);

        // Analyze both websites
        AnalysisRequestDTO req1 = new AnalysisRequestDTO();
        req1.setUrl(url1);
        req1.setMonthlyVisits(monthlyVisits);
        req1.setCrawlPages(1);
        req1.setEnableCrawlMode(false);

        AnalysisRequestDTO req2 = new AnalysisRequestDTO();
        req2.setUrl(url2);
        req2.setMonthlyVisits(monthlyVisits);
        req2.setCrawlPages(1);
        req2.setEnableCrawlMode(false);

        AnalysisResponseDTO result1 = analysisService.startAnalysis(req1);
        AnalysisResponseDTO result2 = analysisService.startAnalysis(req2);

        // Build comparison result
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("site1", result1);
        comparison.put("site2", result2);

        // Determine winner
        String winner = determineWinner(result1, result2);
        comparison.put("winner", winner);

        // Calculate difference
        Map<String, Object> diff = calculateDifference(result1, result2);
        comparison.put("difference", diff);

        // Verdict message
        comparison.put("verdict", buildVerdict(result1, result2));

        return comparison;
    }

    // ── DETERMINE WINNER ──────────────────────────────────────────
    private String determineWinner(
            AnalysisResponseDTO r1, AnalysisResponseDTO r2) {

        Double co2Site1 = getCo2(r1);
        Double co2Site2 = getCo2(r2);

        if (co2Site1 == null && co2Site2 == null) return "tie";
        if (co2Site1 == null) return r2.getWebsiteUrl();
        if (co2Site2 == null) return r1.getWebsiteUrl();

        if (co2Site1 < co2Site2) return r1.getWebsiteUrl();
        if (co2Site2 < co2Site1) return r2.getWebsiteUrl();
        return "tie";
    }

    // ── CALCULATE DIFFERENCE ──────────────────────────────────────
    private Map<String, Object> calculateDifference(
            AnalysisResponseDTO r1, AnalysisResponseDTO r2) {

        Map<String, Object> diff = new HashMap<>();

        Double co2Site1 = getCo2(r1);
        Double co2Site2 = getCo2(r2);

        if (co2Site1 != null && co2Site2 != null) {
            double diffValue = Math.abs(co2Site1 - co2Site2);
            double percentage = co2Site1 > 0
                    ? (diffValue / Math.max(co2Site1, co2Site2)) * 100
                    : 0;

            diff.put("co2DifferenceGrams",
                    Math.round(diffValue * 10000.0) / 10000.0);
            diff.put("percentageDifference",
                    Math.round(percentage * 100.0) / 100.0);

            // Which site is cleaner and by how much
            String cleanerSite = co2Site1 < co2Site2
                    ? r1.getWebsiteUrl() : r2.getWebsiteUrl();
            diff.put("cleanerSite", cleanerSite);
            diff.put("percentageCleaner",
                    Math.round(percentage * 100.0) / 100.0);
        }

        // Grade comparison
        diff.put("grade1", r1.getGrade());
        diff.put("grade2", r2.getGrade());

        // Size comparison
        Long bytes1 = getBytes(r1);
        Long bytes2 = getBytes(r2);

        if (bytes1 != null && bytes2 != null) {
            diff.put("sizeDifferenceBytes",
                    Math.abs(bytes1 - bytes2));
            diff.put("lighterSite", bytes1 < bytes2
                    ? r1.getWebsiteUrl() : r2.getWebsiteUrl());
        }

        return diff;
    }

    // ── BUILD VERDICT ─────────────────────────────────────────────
    private String buildVerdict(
            AnalysisResponseDTO r1, AnalysisResponseDTO r2) {

        Double co2Site1 = getCo2(r1);
        Double co2Site2 = getCo2(r2);

        if (co2Site1 == null || co2Site2 == null) {
            return "Could not complete comparison";
        }

        if (Math.abs(co2Site1 - co2Site2) < 0.001) {
            return "Both websites have similar carbon footprint!";
        }

        String cleanerUrl = co2Site1 < co2Site2
                ? r1.getWebsiteUrl() : r2.getWebsiteUrl();
        double diff = Math.abs(co2Site1 - co2Site2);
        double pct = (diff / Math.max(co2Site1, co2Site2)) * 100;

        return String.format(
                "%s is %.1f%% more eco-friendly (%.4fg vs %.4fg CO2/visit)",
                cleanerUrl, pct, co2Site1, co2Site2
        );
    }

    private Double getCo2(AnalysisResponseDTO r) {
        if (r == null || r.getCarbonMetrics() == null) return null;
        return r.getCarbonMetrics().getCo2PerVisitGrams();
    }

    private Long getBytes(AnalysisResponseDTO r) {
        if (r == null || r.getResourceSummary() == null) return null;
        return r.getResourceSummary().getTotalTransferBytes();
    }


}
