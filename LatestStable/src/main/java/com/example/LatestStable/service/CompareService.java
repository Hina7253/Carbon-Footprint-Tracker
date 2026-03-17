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


}
