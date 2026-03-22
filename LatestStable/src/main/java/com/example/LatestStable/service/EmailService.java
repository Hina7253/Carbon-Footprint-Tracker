package com.example.LatestStable.service;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.format.DateTimeFormatter;

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

    // ── BUILD ANALYSIS EMAIL HTML ─────────────────────────────────
    private String buildAnalysisEmailHtml(
            WebsiteAnalysis analysis) {

        String gradeColor = getGradeColor(analysis.getGrade());
        String gradeEmoji = getGradeEmoji(analysis.getGrade());

        double co2 = analysis.getCo2PerVisitGrams() != null
                ? analysis.getCo2PerVisitGrams() : 0.0;
        double yearlyKg = analysis.getCo2YearlyKg() != null
                ? analysis.getCo2YearlyKg() : 0.0;
        long monthlyVisits = analysis.getMonthlyVisits() != null
                ? analysis.getMonthlyVisits() : 10000;

        String analyzedAt = analysis.getCompletedAt() != null
                ? analysis.getCompletedAt().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                : "Recently";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport"
                    content="width=device-width,
                    initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;
                         background-color:#f0fdf4;
                         font-family:Arial,sans-serif;">

              <!-- Container -->
              <div style="max-width:600px;margin:40px auto;
                          background:white;border-radius:16px;
                          overflow:hidden;
                          box-shadow:0 4px 20px rgba(0,0,0,0.1);">

                <!-- Header -->
                <div style="background:linear-gradient(
                              135deg,#052e16,#14532d);
                            padding:40px 30px;
                            text-align:center;">
                  <h1 style="color:white;margin:0;font-size:28px;">
                    🌿 Carbon Scope
                  </h1>
                  <p style="color:#86efac;margin:8px 0 0;">
                    Digital Carbon Footprint Report
                  </p>
                </div>

                <!-- Website Info -->
                <div style="padding:30px;
                            border-bottom:1px solid #f0fdf4;">
                  <h2 style="color:#14532d;margin:0 0 8px;">
                    Analysis Complete!
                  </h2>
                  <p style="color:#6b7280;margin:0;">
                    <strong>Website:</strong> %s
                  </p>
                  <p style="color:#6b7280;margin:4px 0 0;">
                    <strong>Analyzed:</strong> %s
                  </p>
                </div>

                <!-- Grade Card -->
                <div style="padding:30px;text-align:center;
                            background:%s10;">
                  <div style="display:inline-block;
                              background:%s;
                              color:white;
                              width:80px;height:80px;
                              border-radius:50%%;
                              line-height:80px;
                              font-size:36px;
                              font-weight:bold;
                              margin-bottom:16px;">
                    %s
                  </div>
                  <h2 style="color:%s;margin:0;">
                    Carbon Grade: %s %s
                  </h2>
                  <p style="color:#6b7280;">
                    %s
                  </p>
                </div>

                <!-- Stats Grid -->
                <div style="padding:30px;
                            display:grid;
                            gap:16px;">

                  <!-- CO2 per visit -->
                  <div style="background:#f0fdf4;
                              border-radius:12px;
                              padding:20px;
                              display:flex;
                              justify-content:space-between;
                              align-items:center;">
                    <div>
                      <p style="margin:0;color:#6b7280;
                                font-size:12px;">
                        CO₂ PER VISIT
                      </p>
                      <p style="margin:4px 0 0;
                                color:#14532d;
                                font-size:24px;
                                font-weight:bold;">
                        %.4f g
                      </p>
                    </div>
                    <span style="font-size:32px;">🌱</span>
                  </div>

                  <!-- Annual CO2 -->
                  <div style="background:#fef9c3;
                              border-radius:12px;
                              padding:20px;
                              display:flex;
                              justify-content:space-between;
                              align-items:center;">
                    <div>
                      <p style="margin:0;color:#6b7280;
                                font-size:12px;">
                        ANNUAL CO₂ (%d monthly visits)
                      </p>
                      <p style="margin:4px 0 0;
                                color:#854d0e;
                                font-size:24px;
                                font-weight:bold;">
                        %.2f kg/year
                      </p>
                    </div>
                    <span style="font-size:32px;">📅</span>
                  </div>

                </div>

                <!-- AI Suggestions -->
                %s

                <!-- CTA Button -->
                <div style="padding:30px;text-align:center;">
                  <a href="%s"
                     style="background:#16a34a;
                            color:white;
                            padding:16px 32px;
                            border-radius:8px;
                            text-decoration:none;
                            font-weight:bold;
                            font-size:16px;">
                    View Full Report →
                  </a>
                </div>

                <!-- Footer -->
                <div style="background:#f9fafb;
                            padding:20px;
                            text-align:center;
                            border-top:1px solid #e5e7eb;">
                  <p style="color:#9ca3af;
                            font-size:12px;margin:0;">
                    Powered by 🌿 Carbon Scope
                    — Building a greener web
                  </p>
                </div>

              </div>
            </body>
            </html>
            """.formatted(
                analysis.getWebsiteUrl(),
                analyzedAt,
                gradeColor,
                gradeColor,
                analysis.getGrade() != null
                        ? analysis.getGrade() : "?",
                gradeColor,
                analysis.getGrade() != null
                        ? analysis.getGrade() : "?",
                gradeEmoji,
                getGradeMessage(analysis.getGrade()),
                co2,
                monthlyVisits,
                yearlyKg,
                buildSuggestionsSection(
                        analysis.getAiSuggestions()),
                frontendUrl
        );
    }

    // ── BUILD OPTIMIZATION EMAIL ──────────────────────────────────
    private String buildOptimizationEmailHtml(
            WebsiteAnalysis analysis) {

        String suggestions = analysis.getAiSuggestions() != null
                ? analysis.getAiSuggestions()
                : "No suggestions available";

        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family:Arial,sans-serif;
                         background:#f0fdf4;
                         padding:40px 20px;">
              <div style="max-width:600px;margin:0 auto;
                          background:white;
                          border-radius:16px;
                          overflow:hidden;">

                <div style="background:#14532d;
                            padding:30px;
                            text-align:center;">
                  <h1 style="color:white;margin:0;">
                    💡 Optimization Tips
                  </h1>
                  <p style="color:#86efac;margin:8px 0 0;">
                    For %s
                  </p>
                </div>

                <div style="padding:30px;">
                  <p style="color:#374151;">
                    Here are your personalized optimization tips
                    to reduce the carbon footprint of
                    <strong>%s</strong>:
                  </p>
                  <div style="background:#f0fdf4;
                              border-left:4px solid #16a34a;
                              padding:20px;
                              border-radius:0 8px 8px 0;
                              white-space:pre-line;
                              color:#374151;">
                    %s
                  </div>
                </div>

                <div style="padding:20px;text-align:center;">
                  <a href="%s"
                     style="background:#16a34a;
                            color:white;
                            padding:12px 24px;
                            border-radius:8px;
                            text-decoration:none;">
                    View Full Analysis →
                  </a>
                </div>

              </div>
            </body>
            </html>
            """.formatted(
                analysis.getWebsiteUrl(),
                analysis.getWebsiteUrl(),
                suggestions,
                frontendUrl
        );
    }

    // ── SUGGESTIONS SECTION ───────────────────────────────────────
    private String buildSuggestionsSection(String suggestions) {
        if (suggestions == null || suggestions.isBlank())
            return "";

        return """
            <div style="padding:0 30px 20px;">
              <h3 style="color:#14532d;">
                🤖 AI Optimization Suggestions
              </h3>
              <div style="background:#f0fdf4;
                          border-left:4px solid #16a34a;
                          padding:16px;
                          border-radius:0 8px 8px 0;
                          white-space:pre-line;
                          color:#374151;
                          font-size:14px;">
                %s
              </div>
            </div>
            """.formatted(suggestions);
    }

    // ── GRADE HELPERS ─────────────────────────────────────────────
    private String getGradeColor(String grade) {
        if (grade == null) return "#6b7280";
        return switch (grade) {
            case "A" -> "#16a34a";
            case "B" -> "#65a30d";
            case "C" -> "#d97706";
            case "D" -> "#ea580c";
            default  -> "#dc2626";
        };
    }

    private String getGradeEmoji(String grade) {
        if (grade == null) return "❓";
        return switch (grade) {
            case "A" -> "🌿";
            case "B" -> "✅";
            case "C" -> "⚡";
            case "D" -> "⚠️";
            default  -> "🔥";
        };
    }

    private String getGradeMessage(String grade) {
        if (grade == null)
            return "Analysis complete";
        return switch (grade) {
            case "A" ->
                    "Excellent! Your site is very eco-friendly 🎉";
            case "B" ->
                    "Good job! Better than most websites";
            case "C" ->
                    "Average performance. Room for improvement";
            case "D" ->
                    "Below average. Take action to reduce emissions";
            default  ->
                    "High carbon site. Urgent optimization needed!";
        };
    }

}
