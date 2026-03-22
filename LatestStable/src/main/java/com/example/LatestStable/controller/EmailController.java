package com.example.LatestStable.controller;

import com.example.LatestStable.service.EmailService;
import com.example.LatestStable.service.PdfReportService;
import org.springframework.http.MediaType;
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
    // Analysis report email karna
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
    // Optimization tips email karna
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

    // ── GET /analyses/{id}/pdf ────────────────────────────────────
    // PDF report download karna
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id) {

        try {
            byte[] pdfBytes =
                    pdfReportService.generateReport(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    "carbon-report-" + id + ".pdf"
            );
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("PDF failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

}
