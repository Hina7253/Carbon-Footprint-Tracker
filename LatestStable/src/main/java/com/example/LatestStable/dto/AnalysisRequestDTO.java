package com.example.LatestStable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.processing.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequestDTO {
    @NotBlank(message = "Website URL is required")
    @Pattern(
            regexp = "^(https?://)([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(.*)?$",
            message = "Please provide a valid URL starting with http:// or https://"
    )
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;

    // MONTHLY VISIT
    @Min(value = 1, message = "Monthly visits must be at least 1")
    @Max(value = 1_000_000_000L, message = "Monthly visits seems unrealistically high")
    @Builder.Default
    private Long monthlyVisits = 10000L;

    // HOW MANY PAGES TO CRAWL
    @Min(value = 1, message = "Must crawl at least 1 page")
    @Max(value = 10, message = "Maximum 10 pages to prevent abuse")
    @Builder.Default
    private Integer crawlPages = 1;

    // ENABLE MULTI-PAGE CRAWL MODE
    @Builder.Default
    private Boolean enableCrawlMode = false;

    // ── HELPER: Normalize the URL ─────────────────────────────────
    /**
     * Ensures the URL is clean and consistent before processing.
     *
     * Examples:
     *   "example.com"        → "https://example.com"
     *   "HTTP://EXAMPLE.COM" → "http://EXAMPLE.COM"
     *   "https://example.com/" → "https://example.com/" (unchanged)
     */
    public String getNormalizedUrl() {
        if (url == null) return null;
        String trimmed = url.trim();
        // Add https:// if no protocol specified
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    /**
     * Extract just the domain from the full URL.
     * Example: "https://www.example.com/page" → "example.com"
     */
    public String getBaseDomain() {
        try {
            String normalized = getNormalizedUrl();
            // Remove protocol
            String withoutProtocol = normalized.replaceFirst("https?://", "");
            // Remove www.
            String withoutWww = withoutProtocol.replaceFirst("^www\\.", "");
            // Get just the domain (remove path)
            return withoutWww.split("/")[0].split("\\?")[0];
        } catch (Exception e) {
            return url;
        }
    }
}
