package com.example.LatestStable.service;

import java.util.HashMap;

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
}
