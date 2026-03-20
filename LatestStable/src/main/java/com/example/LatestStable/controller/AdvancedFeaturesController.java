package com.example.LatestStable.controller;

import com.example.LatestStable.service.*;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.WebsiteAnalysis;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")
public class AdvancedFeaturesController {

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

    @GetMapping("/{id}/savings")
    public ResponseEntity<?> getSavings(
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    savingsService.calculatePotentialSavings(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trend/weekly")
    public ResponseEntity<?> getWeeklyTrend() {
        return ResponseEntity.ok(
                trendService.getWeeklyTrend());
    }

    @GetMapping("/trend/url")
    public ResponseEntity<?> getUrlTrend(
            @RequestParam String url) {
        return ResponseEntity.ok(
                trendService.getUrlTrend(url));
    }

    @GetMapping("/{id}/code-fixes")
    public ResponseEntity<?> getCodeFixes(
            @PathVariable Long id) {
        try {
            WebsiteAnalysis analysis = analysisRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Not found: " + id));

            List<PageResources> resources =
                    resourceRepository
                            .findByWebsiteAnalysis_IdOrderBySizeBytesDesc(id);

            return ResponseEntity.ok(
                    codeService.generateCodeFixes(
                            analysis.getWebsiteUrl(),
                            resources,
                            analysis.getGrade()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<?> chat(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String question = body.get("question");

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "question field is required"));
        }

        try {
            return ResponseEntity.ok(
                    chatService.chat(id, question));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}