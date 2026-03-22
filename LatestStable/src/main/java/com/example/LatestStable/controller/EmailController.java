package com.example.LatestStable.controller;

import com.example.LatestStable.service.EmailService;
import com.example.LatestStable.service.PdfReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")
public class EmailController {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final PdfReportService pdfReportService;

    public EmailController(
            EmailService emailService,
            PdfReportService pdfReportService) {
        this.emailService    = emailService;
        this.pdfReportService = pdfReportService;
    }

    // ── POST /analyses/{id}/send-report ──────────────────────────
    // Analysis report email karo
    // Body: { "email": "user@example.com" }
    @PostMapping("/{id}/send-report")
    public ResponseEntity<?> sendReport(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "email is required"));
        }

        try {
            emailService.sendAnalysisReport(id, email);
            return ResponseEntity.ok(Map.of(
                    "message", "Report sent to " + email,
                    "success", true
            ));
        } catch (Exception e) {
            log.error("Send report failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Failed to send email",
                            "message", e.getMessage()
                    ));
        }
    }

    // ── POST /analyses/{id}/send-tips ────────────────────────────
    // Optimization tips email karo
    @PostMapping("/{id}/send-tips")
    public ResponseEntity<?> sendTips(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "email is required"));
        }

        try {
            emailService.sendOptimizationTips(id, email);
            return ResponseEntity.ok(Map.of(
                    "message", "Tips sent to " + email,
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
