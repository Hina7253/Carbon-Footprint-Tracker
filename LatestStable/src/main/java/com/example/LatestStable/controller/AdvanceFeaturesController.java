package com.example.LatestStable.controller;

import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import com.example.LatestStable.service.AiChatService;
import com.example.LatestStable.service.AiCodeSuggestionService;
import com.example.LatestStable.service.CarbonSavedCalculatorService;
import com.example.LatestStable.service.WeeklyTrendService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")

public class AdvanceFeaturesController {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(
                    AdvancedFeaturesController.class);

    private final CarbonSavedCalculatorService savingsService;
    private final WeeklyTrendService trendService;
    private final AiCodeSuggestionService codeService;
    private final AiChatService chatService;
    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;

    public AdvancedFeaturesController(
            CarbonSavedCalculatorService savingsService,
            WeeklyTrendService trendService,
            AiCodeSuggestionService codeService,
            AiChatService chatService,
            WebsiteAnalysisRepository analysisRepository,
            PageResourcesRepository resourceRepository) {
        this.savingsService      = savingsService;
        this.trendService        = trendService;
        this.codeService         = codeService;
        this.chatService         = chatService;
        this.analysisRepository  = analysisRepository;
        this.resourceRepository  = resourceRepository;
    }

}
