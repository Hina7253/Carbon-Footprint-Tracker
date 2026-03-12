package com.example.LatestStable.controller;

import com.example.LatestStable.service.AnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
