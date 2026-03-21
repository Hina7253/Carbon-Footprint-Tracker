package com.example.LatestStable.service;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import org.springframework.beans.factory.annotation.Value;

public class EmailService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final WebsiteAnalysisRepository analysisRepository;

    @Value("${spring.mail.username:noreply@carbonscope.app}")
    private String fromEmail;

    @Value("${app.name:Carbon Scope}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(
            JavaMailSender mailSender,
            WebsiteAnalysisRepository analysisRepository) {
        this.mailSender         = mailSender;
        this.analysisRepository = analysisRepository;
    }

    // ── SEND ANALYSIS REPORT ──────────────────────────────────────
    // Analysis complete hone ke baad report email karo
    public void sendAnalysisReport(
            Long analysisId, String toEmail) {

        WebsiteAnalysis analysis = analysisRepository
                .findById(analysisId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Analysis not found: " + analysisId));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(
                    "🌿 Carbon Report: " +
                            analysis.getWebsiteUrl() +
                            " — Grade " + analysis.getGrade());

            String htmlContent =
                    buildAnalysisEmailHtml(analysis);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Analysis report sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException(
                    "Email failed: " + e.getMessage());
        }
    }

    // ── SEND OPTIMIZATION TIPS ────────────────────────────────────
    // Weekly optimization tips email
    public void sendOptimizationTips(
            Long analysisId, String toEmail) {

        WebsiteAnalysis analysis = analysisRepository
                .findById(analysisId)
                .orElseThrow(() ->
                        new RuntimeException("Analysis not found"));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(
                    "💡 Optimization Tips for " +
                            analysis.getWebsiteUrl());

            helper.setText(
                    buildOptimizationEmailHtml(analysis), true);

            mailSender.send(message);
            log.info("Optimization tips sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send tips email: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Email failed: " + e.getMessage());
        }
    }
}
