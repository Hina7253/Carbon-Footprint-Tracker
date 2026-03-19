package com.example.LatestStable.controller;


import com.example.LatestStable.service.BadgeService;
import com.example.LatestStable.service.CompareService;
import com.example.LatestStable.service.IndustryCompareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    }
