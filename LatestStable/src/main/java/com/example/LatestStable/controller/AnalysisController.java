package com.example.LatestStable.controller;

import com.example.LatestStable.service.AnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")
public class AnalysisController {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AnalysisController.class);
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }
    @PostMapping
    public ResponseEntity<?> startAnalysis(
            @Valid @RequestBody AnalysisRequestDTO request) {

        log.info("Received analysis request for: {}", request.getUrl());

        try {
            AnalysisResponseDTO response =
                    analysisService.startAnalysis(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", "Analysis failed");
            error.put("message", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

}
