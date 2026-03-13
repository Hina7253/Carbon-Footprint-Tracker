package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiSuggestionService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AiSuggestionService.class);
    private final OkHttpClient httpClient;

    // application.properties se API key read karega
    // Agar key nahi hai to "no-key" use karega
    @Value("${openai.api.key:no-key}")
    private String openAiApiKey;

    public AiSuggestionService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
    // MAIN METHOD FOR GENERATE AI SUGGESTION
    public String generateSuggestions(
            String websiteUrl,
            double co2PerVisitGrams,
            long totalBytes,
            String grade,
            List<PageResources> resources) {

        // Agar API key nahi hai to default suggestions do
        if ("no-key".equals(openAiApiKey) ||
                openAiApiKey == null ||
                openAiApiKey.isBlank()) {
            log.info("No OpenAI key — using rule-based suggestions");
            return generateRuleBasedSuggestions(
                    websiteUrl, co2PerVisitGrams,
                    totalBytes, grade, resources);
        }

        try {
            return callOpenAi(websiteUrl, co2PerVisitGrams,
                    totalBytes, grade, resources);
        } catch (Exception e) {
            log.error("OpenAI call failed: {}", e.getMessage());
            // Fallback to rule-based if AI fails
            return generateRuleBasedSuggestions(
                    websiteUrl, co2PerVisitGrams,
                    totalBytes, grade, resources);
        }
    }

    // ── OPENAI API CALL ───────────────────────────────────────────
    private String callOpenAi(
            String websiteUrl,
            double co2PerVisitGrams,
            long totalBytes,
            String grade,
            List<PageResources> resources) throws Exception {
        // Build prompt for AI
        String prompt = buildPrompt(
                websiteUrl, co2PerVisitGrams,
                totalBytes, grade, resources);

        // Build JSON request body manually
        String requestBody = "{"
                + "\"model\": \"gpt-4o-mini\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": "
                + "\"You are a web performance expert. "
                + "Give concise, actionable suggestions to reduce "
                + "a website carbon footprint. "
                + "Format: numbered list, max 5 points.\"},"
                + "  {\"role\": \"user\", \"content\": \""
                + prompt.replace("\"", "\\\"")
                .replace("\n", "\\n")
                + "\"}"
                + "],"
                + "\"max_tokens\": 500,"
                + "\"temperature\": 0.7"
                + "}";

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException(
                        "OpenAI API error: " + response.code());
            }

            String responseBody = response.body().string();
            return extractTextFromOpenAiResponse(responseBody);
        }
    }
    // ── EXTRACT TEXT FROM OPENAI JSON RESPONSE ────────────────────
    // OpenAI returns: {"choices":[{"message":{"content":"..."}}]}
    private String extractTextFromOpenAiResponse(String json) {
        try {
            // Find "content": "..." in the JSON
            int contentIndex = json.indexOf("\"content\":");
            if (contentIndex == -1) return "Could not parse AI response";

            int startQuote = json.indexOf("\"", contentIndex + 10);
            if (startQuote == -1) return "Could not parse AI response";

            // Find the closing quote (handle escaped quotes)
            int endQuote = startQuote + 1;
            while (endQuote < json.length()) {
                if (json.charAt(endQuote) == '"'
                        && json.charAt(endQuote - 1) != '\\') {
                    break;
                }
                endQuote++;
            }

            return json.substring(startQuote + 1, endQuote)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

        } catch (Exception e) {
            log.error("Error parsing OpenAI response: {}", e.getMessage());
            return "Error parsing AI response";
        }
    }
    // ── RULE-BASED SUGGESTIONS ────────────────────────────────────
    // OpenAI key nahi hai to ye use hoga
    // Yahan hum data dekh ke smart suggestions dete hain
    private String generateRuleBasedSuggestions(
            String websiteUrl,
            double co2PerVisitGrams,
            long totalBytes,
            String grade,
            List<PageResources> resources) {

        StringBuilder sb = new StringBuilder();
        sb.append("🌿 Carbon Optimization Suggestions for ")
                .append(websiteUrl).append("\n\n");

        int tipNumber = 1;

        // ── Check images ──────────────────────────────────────────
        long imageBytes = resources.stream()
                .filter(r -> r.getResourceType() == ResourceType.IMAGE
                        && r.getSizeBytes() != null)
                .mapToLong(PageResources::getSizeBytes)
                .sum();

        long imageCount = resources.stream()
                .filter(r -> r.getResourceType() == ResourceType.IMAGE)
                .count();

        if (imageBytes > 500_000) {
            sb.append(tipNumber++).append(". 🖼️ IMAGE OPTIMIZATION\n");
            sb.append("   Found ").append(imageCount)
                    .append(" images totaling ")
                    .append(formatBytes(imageBytes)).append(".\n");
            sb.append("   → Convert images to WebP/AVIF format ")
                    .append("(saves 30-50%)\n");
            sb.append("   → Add lazy loading: ")
                    .append("loading=\"lazy\" attribute\n");
            sb.append("   → Use responsive images with srcset\n\n");
        }
        // ── Check scripts ─────────────────────────────────────────
        long scriptBytes = resources.stream()
                .filter(r -> r.getResourceType() == ResourceType.SCRIPT
                        && r.getSizeBytes() != null)
                .mapToLong(PageResources::getSizeBytes)
                .sum();

        if (scriptBytes > 200_000) {
            sb.append(tipNumber++).append(". ⚡ JAVASCRIPT OPTIMIZATION\n");
            sb.append("   JavaScript totals ")
                    .append(formatBytes(scriptBytes)).append(".\n");
            sb.append("   → Enable code splitting and tree-shaking\n");
            sb.append("   → Minify and compress JS bundles\n");
            sb.append("   → Remove unused dependencies\n\n");
        }// ── Check fonts ───────────────────────────────────────────
        long fontCount = resources.stream()
                .filter(r -> r.getResourceType() == ResourceType.FONT)
                .count();

        if (fontCount > 2) {
            sb.append(tipNumber++).append(". 🔤 FONT OPTIMIZATION\n");
            sb.append("   Found ").append(fontCount)
                    .append(" font files.\n");
            sb.append("   → Limit to 2 font families maximum\n");
            sb.append("   → Use font-display: swap\n");
            sb.append("   → Subset fonts to only needed characters\n\n");
        }

        // ── Check third party ─────────────────────────────────────
        long thirdPartyCount = resources.stream()
                .filter(PageResources::isThirdParty)
                .count();

        if (thirdPartyCount > 5) {
            sb.append(tipNumber++).append(". 🌐 THIRD-PARTY RESOURCES\n");
            sb.append("   Found ").append(thirdPartyCount)
                    .append(" third-party resources.\n");
            sb.append("   → Audit and remove unnecessary trackers\n");
            sb.append("   → Self-host critical third-party resources\n");
            sb.append("   → Lazy load non-critical third-party scripts\n\n");
        }

        // ── Grade-based suggestion ────────────────────────────────
        sb.append(tipNumber++).append(". ♻️ CACHING STRATEGY\n");
        sb.append("   → Set Cache-Control headers for static assets\n");
        sb.append("   → Use a CDN for global content delivery\n");
        sb.append("   → Enable Brotli/Gzip compression on server\n\n");

        // ── Grade summary ─────────────────────────────────────────
        sb.append("📊 Current Grade: ").append(grade)
                .append(" | CO2: ")
                .append(String.format("%.4f", co2PerVisitGrams))
                .append("g per visit\n");

        if ("A".equals(grade) || "B".equals(grade)) {
            sb.append("✅ Great job! Your site is already eco-friendly.");
        } else if ("C".equals(grade) || "D".equals(grade)) {
            sb.append("⚠️ Average site. Implement above suggestions");
            sb.append(" to reach grade A.");
        } else {
            sb.append("❌ High carbon site. Priority: reduce image");
            sb.append(" and JS sizes immediately.");
        }

        return sb.toString();
    }

    // ── HELPER: Format bytes ──────────────────────────────────────
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }






}
