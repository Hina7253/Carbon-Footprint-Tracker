package com.example.LatestStable.controller;


import com.example.LatestStable.service.BadgeService;
import com.example.LatestStable.service.CompareService;
import com.example.LatestStable.service.IndustryCompareService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")
public class ExtraFeaturesController {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(
                    ExtraFeaturesController.class);

    private final CompareService compareService;
    private final LeaderboardService leaderboardService;
    private final BadgeService badgeService;
    private final IndustryCompareService industryCompareService;

    public ExtraFeaturesController(
            CompareService compareService,
            LeaderboardService leaderboardService,
            BadgeService badgeService,
            IndustryCompareService industryCompareService) {
        this.compareService        = compareService;
        this.leaderboardService    = leaderboardService;
        this.badgeService          = badgeService;
        this.industryCompareService = industryCompareService;
    }

    // ── POST /analyses/compare
    @PostMapping("/compare")
    public ResponseEntity<?> compareWebsites(
            @RequestBody Map<String, String> request) {

        String url1 = request.get("url1");
        String url2 = request.get("url2");
        String visitsStr = request.getOrDefault(
                "monthlyVisits", "10000");

        if (url1 == null || url2 == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "url1 and url2 are required");
            return ResponseEntity.badRequest().body(err);
        }

        Long monthlyVisits;
        try {
            monthlyVisits = Long.parseLong(visitsStr);
        } catch (Exception e) {
            monthlyVisits = 10000L;
        }

        log.info("Compare request: {} vs {}", url1, url2);

        Map<String, Object> result =
                compareService.compareTwoWebsites(
                        url1, url2, monthlyVisits);

        return ResponseEntity.ok(result);
    }

    // ── GET /analyses/leaderboard ─────────────────────────────────
    // Full leaderboard — cleanest + dirtiest
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        return ResponseEntity.ok(
                leaderboardService.getFullLeaderboard());
    }

    // ── GET /analyses/leaderboard/cleanest ────────────────────────
    @GetMapping("/leaderboard/cleanest")
    public ResponseEntity<List<Map<String, Object>>>
    getCleanest() {
        return ResponseEntity.ok(
                leaderboardService.getCleanestWebsites());
    }
    // ── GET /analyses/leaderboard/dirtiest ────────────────────────
    @GetMapping("/leaderboard/dirtiest")
    public ResponseEntity<List<Map<String, Object>>>
    getDirtiest() {
        return ResponseEntity.ok(
                leaderboardService.getDirtiestWebsites());
    }

    // ── GET /analyses/badge/{url}
    @GetMapping(
            value = "/badge/{websiteUrl}",
            produces = "image/svg+xml"
    )
    public ResponseEntity<String> getBadge(
            @PathVariable String websiteUrl) {

        String svg = badgeService.generateBadge(websiteUrl);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .header("Cache-Control", "max-age=3600")
                .body(svg);
    }

    // ── GET /analyses/industries ──────────────────────────────────
    // Available industries list dekho
    @GetMapping("/industries")
    public ResponseEntity<?> getIndustries() {
        return ResponseEntity.ok(
                industryCompareService.getAllIndustries());
    }

    // ── POST /analyses/{id}/industry-compare ──────────────────────
    // Kisi analysis ko industry se compare karo
    @PostMapping("/{id}/industry-compare")
    public ResponseEntity<?> compareWithIndustry(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String industry = request.getOrDefault(
                "industry", "blog");

        try {
            // Get analysis data from repository via service
            // We pass the required data directly
            Map<String, String> err = new HashMap<>();
            err.put("info",
                    "Use /analyses/{id} first to get co2 data," +
                            " then pass it here");
            err.put("example",
                    "{ \"industry\": \"ecommerce\"," +
                            " \"co2\": \"0.45\"," +
                            " \"bytes\": \"2000000\"," +
                            " \"grade\": \"C\"," +
                            " \"url\": \"example.com\" }");

            // If all data provided in body
            String co2Str = request.get("co2");
            String bytesStr = request.get("bytes");
            String grade = request.get("grade");
            String url = request.get("url");

            if (co2Str == null) {
                return ResponseEntity.badRequest().body(err);
            }

            double co2 = Double.parseDouble(co2Str);
            long bytes = bytesStr != null
                    ? Long.parseLong(bytesStr) : 0L;

            Map<String, Object> result =
                    industryCompareService.compareWithIndustry(
                            url != null ? url : "website",
                            co2, bytes,
                            grade != null ? grade : "C",
                            industry
                    );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity
                    .internalServerError()
                    .body(error);
        }
    }



}
