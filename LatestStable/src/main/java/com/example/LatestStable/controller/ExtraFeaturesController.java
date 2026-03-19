package com.example.LatestStable.controller;


import com.example.LatestStable.service.BadgeService;
import com.example.LatestStable.service.CompareService;
import com.example.LatestStable.service.IndustryCompareService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
