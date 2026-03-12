package com.example.LatestStable.controller;

import com.example.LatestStable.dto.AnalysisRequestDTO;
import com.example.LatestStable.dto.AnalysisResponseDTO;
import com.example.LatestStable.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    // ── POST /analyses ────
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
    // ── GET /analyses/{id} ──
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnalysis(@PathVariable Long id) {

        try {
            AnalysisResponseDTO response =
                    analysisService.getAnalysis(id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }
    // ── GET /analyses/history ─

}
