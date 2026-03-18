package com.example.LatestStable.service;

import java.util.HashMap;
import java.util.Map;

public class IndustryCompareService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(
                    IndustryCompareService.class);

    // ── INDUSTRY BENCHMARKS ───────────────────────────────────────
    // gCO2 per visit — HTTP Archive + WebsiteCarbon data
    private static final Map<String, IndustryBenchmark>
            BENCHMARKS = new HashMap<>();

    static {
        BENCHMARKS.put("ecommerce",
                new IndustryBenchmark(
                        "E-Commerce", 0.89, 3800000L,
                        "Amazon, Flipkart, eBay"));

        BENCHMARKS.put("news",
                new IndustryBenchmark(
                        "News & Media", 0.76, 3200000L,
                        "BBC, CNN, Times of India"));

        BENCHMARKS.put("blog",
                new IndustryBenchmark(
                        "Blog & CMS", 0.45, 1900000L,
                        "WordPress, Medium, Blogger"));

        BENCHMARKS.put("banking",
                new IndustryBenchmark(
                        "Banking & Finance", 0.68, 2900000L,
                        "HDFC, SBI, ICICI"));

        BENCHMARKS.put("education",
                new IndustryBenchmark(
                        "Education", 0.52, 2200000L,
                        "Coursera, NPTEL, Universities"));

        BENCHMARKS.put("government",
                new IndustryBenchmark(
                        "Government", 0.61, 2600000L,
                        "Gov.in, India.gov, NIC sites"));

        BENCHMARKS.put("technology",
                new IndustryBenchmark(
                        "Technology", 0.48, 2000000L,
                        "GitHub, Stack Overflow, Dev.to"));

        BENCHMARKS.put("healthcare",
                new IndustryBenchmark(
                        "Healthcare", 0.58, 2450000L,
                        "Apollo, Practo, 1mg"));

        BENCHMARKS.put("entertainment",
                new IndustryBenchmark(
                        "Entertainment", 1.12, 4700000L,
                        "Netflix, Hotstar, YouTube"));

        BENCHMARKS.put("social",
                new IndustryBenchmark(
                        "Social Media", 0.95, 4000000L,
                        "Facebook, Instagram, Twitter"));
    }

    // ── COMPARE WITH INDUSTRY ─────────────────────────────────────
    public Map<String, Object> compareWithIndustry(
            String websiteUrl,
            double co2PerVisitGrams,
            long totalBytes,
            String grade,
            String industry) {

        String industryKey = industry.toLowerCase().trim();
        IndustryBenchmark benchmark =
                BENCHMARKS.getOrDefault(industryKey,
                        BENCHMARKS.get("blog")); // default = blog

        log.info("Comparing {} with industry: {}",
                websiteUrl, benchmark.name());

        Map<String, Object> result = new HashMap<>();

        // Site data
        result.put("websiteUrl",       websiteUrl);
        result.put("websiteGrade",     grade);
        result.put("websiteCo2Grams",  co2PerVisitGrams);
        result.put("websiteBytes",     totalBytes);

        // Industry benchmark
        result.put("industryName",     benchmark.name());
        result.put("industryExamples", benchmark.examples());
        result.put("industryCo2Grams", benchmark.avgCo2Grams());
        result.put("industryAvgBytes", benchmark.avgBytes());

        // Comparison
        double pctDiff = benchmark.avgCo2Grams() > 0
                ? ((benchmark.avgCo2Grams() - co2PerVisitGrams)
                / benchmark.avgCo2Grams()) * 100
                : 0;

        result.put("percentageBetterThanIndustry",
                Math.round(pctDiff * 100.0) / 100.0);

        result.put("isBetterThanIndustry",
                co2PerVisitGrams < benchmark.avgCo2Grams());

        // Verdict
        result.put("verdict",
                buildIndustryVerdict(
                        websiteUrl, co2PerVisitGrams,
                        benchmark, pctDiff));

        // All industries for comparison chart
        result.put("allIndustries",
                getAllIndustriesForChart(co2PerVisitGrams));

        return result;
    }

    // ── ALL INDUSTRIES LIST ───────────────────────────────────────
    public Map<String, Object> getAllIndustries() {
        Map<String, Object> result = new HashMap<>();
        BENCHMARKS.forEach((key, benchmark) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name",       benchmark.name());
            entry.put("avgCo2",     benchmark.avgCo2Grams());
            entry.put("avgBytes",   benchmark.avgBytes());
            entry.put("examples",   benchmark.examples());
            result.put(key, entry);
        });
        return result;
    }

    // ── AVAILABLE INDUSTRIES ──────────────────────────────────────
    public java.util.List<String> getAvailableIndustries() {
        return new java.util.ArrayList<>(BENCHMARKS.keySet());
    }

    // ── BUILD VERDICT ─────────────────────────────────────────────
    private String buildIndustryVerdict(
            String websiteUrl,
            double co2PerVisitGrams,
            IndustryBenchmark benchmark,
            double pctDiff) {

        if (pctDiff > 0) {
            return String.format(
                    "✅ %s is %.1f%% more eco-friendly than " +
                            "the average %s website (%.4fg vs %.4fg CO₂/visit)",
                    websiteUrl, pctDiff,
                    benchmark.name(),
                    co2PerVisitGrams,
                    benchmark.avgCo2Grams()
            );
        } else {
            return String.format(
                    "⚠️ %s produces %.1f%% more CO₂ than " +
                            "the average %s website (%.4fg vs %.4fg CO₂/visit)",
                    websiteUrl, Math.abs(pctDiff),
                    benchmark.name(),
                    co2PerVisitGrams,
                    benchmark.avgCo2Grams()
            );
        }
    }

    // ── ALL INDUSTRIES FOR CHART DATA ─────────────────────────────
    private java.util.List<Map<String, Object>>
    getAllIndustriesForChart(double websiteCo2) {

        java.util.List<Map<String, Object>> chart =
                new java.util.ArrayList<>();

        // Add current website
        Map<String, Object> siteEntry = new HashMap<>();
        siteEntry.put("name",   "Your Site");
        siteEntry.put("co2",    websiteCo2);
        siteEntry.put("isYours", true);
        chart.add(siteEntry);

        // Add all industries
        BENCHMARKS.forEach((key, benchmark) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name",    benchmark.name());
            entry.put("co2",     benchmark.avgCo2Grams());
            entry.put("isYours", false);
            chart.add(entry);
        });

        // Sort by CO2
        chart.sort((a, b) ->
                Double.compare(
                        (Double) a.get("co2"),
                        (Double) b.get("co2")));

        return chart;
        // ── RECORD: Industry Benchmark ────────────────────────────────
        private record IndustryBenchmark(
                String name,
                double avgCo2Grams,
                long avgBytes,
                String examples
        ) {}
    }



    }
