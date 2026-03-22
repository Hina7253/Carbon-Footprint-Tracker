package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfReportService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PdfReportService.class);

    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;

    public PdfReportService(
            WebsiteAnalysisRepository analysisRepository,
            PageResourcesRepository resourceRepository) {
        this.analysisRepository = analysisRepository;
        this.resourceRepository = resourceRepository;
    }

    // ── GENERATE PDF ──────────────────────────────────────────────
    // Returns PDF as byte array — controller will send as download
    public byte[] generateReport(Long analysisId) {

        log.info("Generating PDF report for: {}", analysisId);

        WebsiteAnalysis analysis = analysisRepository
                .findById(analysisId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Analysis not found: " + analysisId));

        List<PageResources> resources =
                resourceRepository
                        .findByWebsiteAnalysis_IdOrderBySizeBytesDesc(
                                analysisId);

        // Build HTML content for PDF
        String html = buildReportHtml(analysis, resources);

        // Convert HTML to PDF using iText
        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            ConverterProperties properties =
                    new ConverterProperties();
            HtmlConverter.convertToPdf(
                    html, outputStream, properties);

            log.info("PDF generated successfully for analysis: {}",
                    analysisId);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage());
            throw new RuntimeException(
                    "PDF generation failed: " + e.getMessage());
        }
    }

    // ── BUILD HTML REPORT ─────────────────────────────────────────
    private String buildReportHtml(
            WebsiteAnalysis analysis,
            List<PageResources> resources) {

        String gradeColor = getGradeColor(analysis.getGrade());
        String analyzedAt = analysis.getCompletedAt() != null
                ? analysis.getCompletedAt().format(
                DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm"))
                : "N/A";

        double co2 = analysis.getCo2PerVisitGrams() != null
                ? analysis.getCo2PerVisitGrams() : 0.0;
        double yearlyKg = analysis.getCo2YearlyKg() != null
                ? analysis.getCo2YearlyKg() : 0.0;
        double energy = analysis.getEnergyUsageKwh() != null
                ? analysis.getEnergyUsageKwh() : 0.0;
        long totalBytes =
                analysis.getTotalTransferBytes() != null
                        ? analysis.getTotalTransferBytes() : 0L;

        // Resources table rows
        String resourceRows = buildResourceRows(resources);

        // Type breakdown
        String typeBreakdown = buildTypeBreakdown(resources);

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                body {
                  font-family: Arial, sans-serif;
                  color: #1f2937;
                  background: white;
                }
                .header {
                  background: #052e16;
                  color: white;
                  padding: 40px;
                  text-align: center;
                }
                .header h1 {
                  font-size: 28px;
                  margin-bottom: 8px;
                }
                .header p { color: #86efac; }

                .section {
                  padding: 30px 40px;
                  border-bottom: 1px solid #e5e7eb;
                }
                .section h2 {
                  color: #14532d;
                  font-size: 18px;
                  margin-bottom: 20px;
                  padding-bottom: 8px;
                  border-bottom: 2px solid #86efac;
                }

                .grade-card {
                  text-align: center;
                  padding: 30px;
                  background: %s20;
                  border-radius: 12px;
                  margin: 20px 0;
                }
                .grade-circle {
                  display: inline-block;
                  background: %s;
                  color: white;
                  width: 80px;
                  height: 80px;
                  border-radius: 50%%;
                  line-height: 80px;
                  font-size: 36px;
                  font-weight: bold;
                  margin-bottom: 12px;
                }

                .stats-grid {
                  display: grid;
                  grid-template-columns: 1fr 1fr 1fr;
                  gap: 16px;
                  margin: 20px 0;
                }
                .stat-card {
                  background: #f0fdf4;
                  border-radius: 8px;
                  padding: 16px;
                  text-align: center;
                }
                .stat-label {
                  font-size: 10px;
                  color: #6b7280;
                  text-transform: uppercase;
                  margin-bottom: 8px;
                }
                .stat-value {
                  font-size: 20px;
                  font-weight: bold;
                  color: #14532d;
                }

                table {
                  width: 100%%;
                  border-collapse: collapse;
                  font-size: 12px;
                }
                th {
                  background: #14532d;
                  color: white;
                  padding: 10px 8px;
                  text-align: left;
                }
                td {
                  padding: 8px;
                  border-bottom: 1px solid #e5e7eb;
                }
                tr:nth-child(even) td {
                  background: #f9fafb;
                }

                .ai-box {
                  background: #f0fdf4;
                  border-left: 4px solid #16a34a;
                  padding: 20px;
                  border-radius: 0 8px 8px 0;
                  white-space: pre-line;
                  font-size: 13px;
                  line-height: 1.6;
                }

                .footer {
                  background: #f9fafb;
                  padding: 20px 40px;
                  text-align: center;
                  font-size: 12px;
                  color: #9ca3af;
                }

                .badge {
                  display: inline-block;
                  padding: 3px 8px;
                  border-radius: 4px;
                  font-size: 11px;
                  font-weight: bold;
                }
                .badge-image  { background:#dbeafe;color:#1e40af; }
                .badge-script { background:#fef9c3;color:#854d0e; }
                .badge-style  { background:#fae8ff;color:#6b21a8; }
                .badge-font   { background:#fee2e2;color:#991b1b; }
                .badge-other  { background:#f3f4f6;color:#374151; }
              </style>
            </head>
            <body>

              <!-- Header -->
              <div class="header">
                <h1>🌿 Carbon Scope Report</h1>
                <p>Digital Carbon Footprint Analysis</p>
                <p style="margin-top:8px;font-size:14px;">
                  Generated: %s
                </p>
              </div>

              <!-- Website Info -->
              <div class="section">
                <h2>Website Information</h2>
                <table>
                  <tr>
                    <td><strong>URL</strong></td>
                    <td>%s</td>
                  </tr>
                  <tr>
                    <td><strong>Monthly Visits</strong></td>
                    <td>%,d</td>
                  </tr>
                  <tr>
                    <td><strong>Analysis Date</strong></td>
                    <td>%s</td>
                  </tr>
                  <tr>
                    <td><strong>Total Resources</strong></td>
                    <td>%d resources found</td>
                  </tr>
                </table>
              </div>

              <!-- Grade -->
              <div class="section">
                <h2>Carbon Grade</h2>
                <div class="grade-card">
                  <div class="grade-circle">%s</div>
                  <h3 style="color:%s;font-size:22px;">
                    Grade %s — %s
                  </h3>
                </div>
              </div>

              <!-- Carbon Metrics -->
              <div class="section">
                <h2>Carbon Metrics</h2>
                <div class="stats-grid">
                  <div class="stat-card">
                    <div class="stat-label">CO₂ Per Visit</div>
                    <div class="stat-value">%.4f g</div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-label">Annual CO₂</div>
                    <div class="stat-value">%.2f kg</div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-label">Energy/Visit</div>
                    <div class="stat-value">%.6f kWh</div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-label">Page Weight</div>
                    <div class="stat-value">%s</div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-label">Trees/Year</div>
                    <div class="stat-value">%.2f 🌳</div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-label">km Equivalent</div>
                    <div class="stat-value">%.0f km 🚗</div>
                  </div>
                </div>
              </div>

              <!-- Resource Breakdown -->
              <div class="section">
                <h2>Resource Type Breakdown</h2>
                %s
              </div>

              <!-- Top Resources Table -->
              <div class="section">
                <h2>Top 10 Heaviest Resources</h2>
                <table>
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Resource URL</th>
                      <th>Size</th>
                      <th>CO₂ (g)</th>
                      <th>3rd Party</th>
                    </tr>
                  </thead>
                  <tbody>
                    %s
                  </tbody>
                </table>
              </div>

              <!-- AI Suggestions -->
              <div class="section">
                <h2>🤖 AI Optimization Suggestions</h2>
                <div class="ai-box">%s</div>
              </div>

              <!-- Footer -->
              <div class="footer">
                <p>Generated by 🌿 Carbon Scope —
                   Digital Carbon Footprint Intelligence</p>
                <p style="margin-top:4px;">
                  Building a greener, more sustainable web
                </p>
              </div>

            </body>
            </html>
            """.formatted(
                gradeColor, gradeColor,
                analyzedAt,
                analysis.getWebsiteUrl(),
                analysis.getMonthlyVisits() != null
                        ? analysis.getMonthlyVisits() : 10000,
                analyzedAt,
                resources.size(),
                analysis.getGrade() != null
                        ? analysis.getGrade() : "?",
                gradeColor,
                analysis.getGrade() != null
                        ? analysis.getGrade() : "?",
                getGradeMessage(analysis.getGrade()),
                co2,
                yearlyKg,
                energy,
                formatBytes(totalBytes),
                yearlyKg / 21.0,
                yearlyKg / 0.21,
                typeBreakdown,
                resourceRows,
                analysis.getAiSuggestions() != null
                        ? analysis.getAiSuggestions()
                        : "No suggestions available"
        );
    }

    // ── BUILD RESOURCE TABLE ROWS ─────────────────────────────────
    private String buildResourceRows(List<PageResources> resources) {
        return resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .limit(10)
                .map(r -> {
                    String shortUrl = r.getResourceUrl().length() > 50
                            ? "..." + r.getResourceUrl()
                            .substring(r.getResourceUrl().length() - 47)
                            : r.getResourceUrl();

                    String typeBadge = getTypeBadge(
                            r.getResourceType().name());

                    return """
                    <tr>
                      <td>%s</td>
                      <td style="font-size:10px;">%s</td>
                      <td>%s</td>
                      <td>%.6f</td>
                      <td>%s</td>
                    </tr>
                    """.formatted(
                            typeBadge,
                            shortUrl,
                            formatBytes(r.getSizeBytes()),
                            r.getCo2ContributionGrams() != null
                                    ? r.getCo2ContributionGrams() : 0.0,
                            r.isThirdParty() ? "✓ Yes" : "No"
                    );
                })
                .collect(Collectors.joining());
    }

    // ── BUILD TYPE BREAKDOWN TABLE ────────────────────────────────
    private String buildTypeBreakdown(
            List<PageResources> resources) {

        // Group by type
        var grouped = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getResourceType().name()));

        long totalBytes = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .mapToLong(PageResources::getSizeBytes)
                .sum();

        StringBuilder table = new StringBuilder();
        table.append("""
            <table>
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Count</th>
                  <th>Total Size</th>
                  <th>% of Total</th>
                </tr>
              </thead>
              <tbody>
            """);

        grouped.forEach((type, typeResources) -> {
            long typeBytes = typeResources.stream()
                    .mapToLong(PageResources::getSizeBytes)
                    .sum();
            double pct = totalBytes > 0
                    ? (typeBytes * 100.0 / totalBytes) : 0;

            table.append("""
                <tr>
                  <td>%s</td>
                  <td>%d</td>
                  <td>%s</td>
                  <td>%.1f%%</td>
                </tr>
                """.formatted(
                    getTypeBadge(type),
                    typeResources.size(),
                    formatBytes(typeBytes),
                    pct));
        });

        table.append("</tbody></table>");
        return table.toString();
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private String getTypeBadge(String type) {
        String cssClass = switch (type) {
            case "IMAGE"    -> "badge-image";
            case "SCRIPT"   -> "badge-script";
            case "STYLE"    -> "badge-style";
            case "FONT"     -> "badge-font";
            default         -> "badge-other";
        };
        return "<span class=\"badge " + cssClass + "\">"
                + type + "</span>";
    }

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

    private String getGradeMessage(String grade) {
        if (grade == null) return "Analysis Complete";
        return switch (grade) {
            case "A" -> "Excellent — Very Eco-Friendly";
            case "B" -> "Good — Above Average";
            case "C" -> "Average — Room for Improvement";
            case "D" -> "Below Average — Take Action";
            default  -> "High Carbon — Urgent Action Needed";
        };
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB",
                bytes / (1024.0 * 1024));
    }
}