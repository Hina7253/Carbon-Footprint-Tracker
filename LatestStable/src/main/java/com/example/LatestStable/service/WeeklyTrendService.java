package com.example.LatestStable.service;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeeklyTrendService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WeeklyTrendService.class);

    private final WebsiteAnalysisRepository analysisRepository;

    public WeeklyTrendService(
            WebsiteAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    // ── WEEKLY TREND DATA ─────────────────────────────────────────
    // Last 7 days ka data — graph ke liye
    public Map<String, Object> getWeeklyTrend() {
        log.info("Fetching weekly trend data");

        LocalDateTime sevenDaysAgo =
                LocalDateTime.now().minusDays(7);

        List<WebsiteAnalysis> recentAnalyses =
                analysisRepository.findRecentCompletedAnalyses(
                        sevenDaysAgo);

        // Group by day
        Map<String, List<WebsiteAnalysis>> byDay = recentAnalyses
                .stream()
                .collect(Collectors.groupingBy(a ->
                        a.getCompletedAt() != null
                                ? a.getCompletedAt().toLocalDate().toString()
                                : "unknown"
                ));

        // Build daily stats
        List<Map<String, Object>> dailyData = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime day =
                    LocalDateTime.now().minusDays(i);
            String dayStr = day.toLocalDate().toString();
            String dayLabel = day.format(
                    DateTimeFormatter.ofPattern("MMM dd"));

            List<WebsiteAnalysis> dayAnalyses =
                    byDay.getOrDefault(dayStr, new ArrayList<>());

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date",  dayStr);
            dayData.put("label", dayLabel);
            dayData.put("totalAnalyses", dayAnalyses.size());
            dayData.put("avgCo2Grams",
                    calculateAvgCo2(dayAnalyses));
            dayData.put("avgPageSizeBytes",
                    calculateAvgBytes(dayAnalyses));
            dayData.put("gradeDistribution",
                    calculateGradeDistribution(dayAnalyses));

            dailyData.add(dayData);
        }

        // Overall 7-day stats
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAnalyses",   recentAnalyses.size());
        summary.put("avgCo2Grams",
                calculateAvgCo2(recentAnalyses));
        summary.put("mostAnalyzedUrl",
                findMostAnalyzed(recentAnalyses));
        summary.put("cleanestSite",
                findCleanest(recentAnalyses));
        summary.put("dirtiestSite",
                findDirtiest(recentAnalyses));
        summary.put("trend",
                calculateTrend(dailyData));

        Map<String, Object> result = new HashMap<>();
        result.put("dailyData", dailyData);
        result.put("summary",   summary);
        result.put("period",    "Last 7 days");

        return result;
    }

    // ── URL-SPECIFIC TREND ────────────────────────────────────────
    // Ek specific website ka historical trend
    public Map<String, Object> getUrlTrend(String websiteUrl) {
        log.info("Fetching trend for URL: {}", websiteUrl);

        List<WebsiteAnalysis> urlAnalyses =
                analysisRepository
                        .findByWebsiteUrlOrderByCreatedAtDesc(websiteUrl);

        List<Map<String, Object>> trendData = urlAnalyses.stream()
                .filter(a -> a.getCo2PerVisitGrams() != null)
                .limit(10)
                .map(a -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date",
                            a.getCompletedAt() != null
                                    ? a.getCompletedAt().toString()
                                    : a.getCreatedAt().toString());
                    point.put("co2PerVisitGrams",
                            a.getCo2PerVisitGrams());
                    point.put("grade",  a.getGrade());
                    point.put("bytes",
                            a.getTotalTransferBytes());
                    return point;
                })
                .collect(Collectors.toList());

        // Reverse to show oldest first
        Collections.reverse(trendData);

        Map<String, Object> result = new HashMap<>();
        result.put("websiteUrl",  websiteUrl);
        result.put("trendData",   trendData);
        result.put("totalScans",  urlAnalyses.size());
        result.put("isImproving",
                isImproving(trendData));
        result.put("message",
                buildTrendMessage(trendData, websiteUrl));

        return result;
    }

    // ── CALCULATE TREND ───────────────────────────────────────────
    // Improving, worsening, or stable?
    private String calculateTrend(
            List<Map<String, Object>> dailyData) {

        List<Double> co2Values = dailyData.stream()
                .map(d -> (Double) d.get("avgCo2Grams"))
                .filter(v -> v > 0)
                .collect(Collectors.toList());

        if (co2Values.size() < 2) return "insufficient_data";

        double first = co2Values.get(0);
        double last  = co2Values.get(co2Values.size() - 1);

        if (last < first * 0.95) return "improving";
        if (last > first * 1.05) return "worsening";
        return "stable";
    }

    private boolean isImproving(
            List<Map<String, Object>> trendData) {

        if (trendData.size() < 2) return false;

        Object first = trendData.get(0).get("co2PerVisitGrams");
        Object last  = trendData.get(
                trendData.size() - 1).get("co2PerVisitGrams");

        if (first instanceof Double && last instanceof Double) {
            return (Double) last < (Double) first;
        }
        return false;
    }

    private String buildTrendMessage(
            List<Map<String, Object>> trendData,
            String url) {

        if (trendData.size() < 2)
            return "Not enough data to show trend for " + url;

        boolean improving = isImproving(trendData);
        return improving
                ? "✅ " + url + " is getting greener over time!"
                : "⚠️ " + url + " carbon footprint has increased";
    }

    // ── STAT HELPERS ──────────────────────────────────────────────
    private double calculateAvgCo2(
            List<WebsiteAnalysis> analyses) {

        return analyses.stream()
                .filter(a -> a.getCo2PerVisitGrams() != null)
                .mapToDouble(WebsiteAnalysis::getCo2PerVisitGrams)
                .average()
                .orElse(0.0);
    }

    private double calculateAvgBytes(
            List<WebsiteAnalysis> analyses) {

        return analyses.stream()
                .filter(a -> a.getTotalTransferBytes() != null)
                .mapToDouble(WebsiteAnalysis::getTotalTransferBytes)
                .average()
                .orElse(0.0);
    }

    private Map<String, Long> calculateGradeDistribution(
            List<WebsiteAnalysis> analyses) {

        return analyses.stream()
                .filter(a -> a.getGrade() != null)
                .collect(Collectors.groupingBy(
                        WebsiteAnalysis::getGrade,
                        Collectors.counting()));
    }

    private String findMostAnalyzed(
            List<WebsiteAnalysis> analyses) {

        return analyses.stream()
                .collect(Collectors.groupingBy(
                        WebsiteAnalysis::getWebsiteUrl,
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private String findCleanest(
            List<WebsiteAnalysis> analyses) {

        return analyses.stream()
                .filter(a -> a.getCo2PerVisitGrams() != null)
                .min(Comparator.comparingDouble(
                        WebsiteAnalysis::getCo2PerVisitGrams))
                .map(WebsiteAnalysis::getWebsiteUrl)
                .orElse("N/A");
    }

    private String findDirtiest(
            List<WebsiteAnalysis> analyses) {

        return analyses.stream()
                .filter(a -> a.getCo2PerVisitGrams() != null)
                .max(Comparator.comparingDouble(
                        WebsiteAnalysis::getCo2PerVisitGrams))
                .map(WebsiteAnalysis::getWebsiteUrl)
                .orElse("N/A");
    }
}