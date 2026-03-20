package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.PageResourcesRepository;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// User analysis ke baare mein kuch bhi poochh sakta hai
// "Which image causes most carbon?"
// "How can I improve my grade?"
// "What is third party impact?"
@Service
public class AiChatService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AiChatService.class);

    private final WebsiteAnalysisRepository analysisRepository;
    private final PageResourcesRepository resourceRepository;
    private final OkHttpClient httpClient;

    @Value("${openai.api.key:no-key}")
    private String openAiApiKey;

    public AiChatService(
            WebsiteAnalysisRepository analysisRepository,
            PageResourcesRepository resourceRepository,
            OkHttpClient httpClient) {
        this.analysisRepository = analysisRepository;
        this.resourceRepository = resourceRepository;
        this.httpClient         = httpClient;
    }

    // ── CHAT WITH ANALYSIS ────────────────────────────────────────
    public Map<String, Object> chat(
            Long analysisId, String userQuestion) {

        log.info("Chat for analysis {}: {}", analysisId, userQuestion);

        WebsiteAnalysis analysis = analysisRepository
                .findById(analysisId)
                .orElseThrow(() ->
                        new RuntimeException("Analysis not found"));

        List<PageResources> resources =
                resourceRepository
                        .findByWebsiteAnalysis_IdOrderBySizeBytesDesc(
                                analysisId);

        // Build context from analysis data
        String context = buildContext(analysis, resources);

        // Get answer
        String answer;
        if ("no-key".equals(openAiApiKey)) {
            answer = getRuleBasedAnswer(
                    userQuestion, analysis, resources);
        } else {
            answer = getAiAnswer(
                    userQuestion, context, analysis);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("question",   userQuestion);
        result.put("answer",     answer);
        result.put("websiteUrl", analysis.getWebsiteUrl());
        result.put("grade",      analysis.getGrade());
        return result;
    }

    // ── BUILD CONTEXT ─────────────────────────────────────────────
    private String buildContext(
            WebsiteAnalysis analysis,
            List<PageResources> resources) {

        long totalBytes = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .mapToLong(PageResources::getSizeBytes)
                .sum();

        String topResources = resources.stream()
                .filter(r -> r.getSizeBytes() != null)
                .limit(5)
                .map(r -> r.getResourceType() + ": "
                        + r.getSizeBytes() / 1024 + "KB — "
                        + shortenUrl(r.getResourceUrl()))
                .collect(Collectors.joining("\n"));

        long thirdParty = resources.stream()
                .filter(PageResources::isThirdParty)
                .count();

        return String.format("""
            Website Analysis Data:
            URL: %s
            Grade: %s
            CO2 per visit: %.4f grams
            CO2 per year: %.2f kg
            Total page size: %d KB
            Total resources: %d
            Third-party resources: %d
            Monthly visits: %d

            Top 5 heaviest resources:
            %s
            """,
                analysis.getWebsiteUrl(),
                analysis.getGrade(),
                analysis.getCo2PerVisitGrams() != null
                        ? analysis.getCo2PerVisitGrams() : 0.0,
                analysis.getCo2YearlyKg() != null
                        ? analysis.getCo2YearlyKg() : 0.0,
                totalBytes / 1024,
                resources.size(),
                thirdParty,
                analysis.getMonthlyVisits() != null
                        ? analysis.getMonthlyVisits() : 10000,
                topResources
        );
    }

    // ── AI ANSWER ─────────────────────────────────────────────────
    private String getAiAnswer(
            String question,
            String context,
            WebsiteAnalysis analysis) {

        try {
            String systemPrompt =
                    "You are a web sustainability expert. " +
                            "Answer questions about a website's carbon " +
                            "footprint using the analysis data provided. " +
                            "Be concise (2-3 sentences), specific, " +
                            "and actionable.";

            String userMessage = context +
                    "\n\nUser Question: " + question;

            String body = "{"
                    + "\"model\":\"gpt-4o-mini\","
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":\""
                    + systemPrompt.replace("\"", "\\\"") + "\"},"
                    + "{\"role\":\"user\",\"content\":\""
                    + userMessage.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    + "\"}"
                    + "],"
                    + "\"max_tokens\":200}";

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(RequestBody.create(body,
                            MediaType.parse("application/json")))
                    .addHeader("Authorization",
                            "Bearer " + openAiApiKey)
                    .build();

            try (Response response =
                         httpClient.newCall(request).execute()) {

                if (response.isSuccessful()
                        && response.body() != null) {
                    return parseResponse(
                            response.body().string());
                }
            }
        } catch (Exception e) {
            log.error("AI chat failed: {}", e.getMessage());
        }

        return getRuleBasedAnswer(question, analysis, null);
    }

    // ── RULE-BASED ANSWERS (No API key needed) ────────────────────
    private String getRuleBasedAnswer(
            String question,
            WebsiteAnalysis analysis,
            List<PageResources> resources) {

        String q = question.toLowerCase();

        // Grade related
        if (q.contains("grade") || q.contains("score")) {
            return String.format(
                    "Your website received a grade of '%s'. " +
                            "%s " +
                            "This is based on %.4f grams of CO₂ per visit.",
                    analysis.getGrade(),
                    getGradeExplanation(analysis.getGrade()),
                    analysis.getCo2PerVisitGrams() != null
                            ? analysis.getCo2PerVisitGrams() : 0.0);
        }

        // CO2 related
        if (q.contains("co2") || q.contains("carbon")
                || q.contains("emission")) {
            return String.format(
                    "Your site emits %.4f grams of CO₂ per visit. " +
                            "With %d monthly visitors, " +
                            "that's %.2f kg of CO₂ per year — " +
                            "equivalent to driving %.0f km by car.",
                    analysis.getCo2PerVisitGrams() != null
                            ? analysis.getCo2PerVisitGrams() : 0.0,
                    analysis.getMonthlyVisits() != null
                            ? analysis.getMonthlyVisits() : 10000,
                    analysis.getCo2YearlyKg() != null
                            ? analysis.getCo2YearlyKg() : 0.0,
                    analysis.getCo2YearlyKg() != null
                            ? analysis.getCo2YearlyKg() / 0.21 : 0.0);
        }

        // Image related
        if (q.contains("image") || q.contains("img")
                || q.contains("photo")) {
            return "Images are typically the biggest carbon " +
                    "culprit on websites. Convert images to WebP " +
                    "format (30-50% smaller), add loading='lazy' " +
                    "attribute, and use responsive srcset.";
        }

        // How to improve
        if (q.contains("improve") || q.contains("better")
                || q.contains("fix") || q.contains("reduce")) {
            return String.format(
                    "To improve from grade %s: " +
                            "1) Compress and convert images to WebP, " +
                            "2) Minify JavaScript and CSS, " +
                            "3) Reduce third-party scripts, " +
                            "4) Enable server-side caching.",
                    analysis.getGrade());
        }

        // Third party
        if (q.contains("third") || q.contains("external")) {
            return "Third-party resources (scripts, fonts, " +
                    "APIs from other domains) can significantly " +
                    "increase your carbon footprint. " +
                    "Audit each one and remove non-essential " +
                    "tracking scripts.";
        }

        // Default
        return String.format(
                "Your website '%s' has a carbon grade of '%s' " +
                        "with %.4f grams CO₂ per visit. " +
                        "Ask me about images, scripts, grade, CO₂, " +
                        "or how to improve!",
                analysis.getWebsiteUrl(),
                analysis.getGrade(),
                analysis.getCo2PerVisitGrams() != null
                        ? analysis.getCo2PerVisitGrams() : 0.0);
    }

    private String getGradeExplanation(String grade) {
        return switch (grade != null ? grade : "?") {
            case "A" -> "Excellent! Your site is very eco-friendly.";
            case "B" -> "Good! Better than most websites.";
            case "C" -> "Average. Room for improvement.";
            case "D" -> "Below average. Take action soon.";
            case "E", "F" -> "High carbon! Urgent optimization needed.";
            default -> "Grade not available.";
        };
    }

    private String parseResponse(String json) {
        try {
            int idx   = json.indexOf("\"content\":");
            int start = json.indexOf("\"", idx + 10);
            int end   = start + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"'
                        && json.charAt(end - 1) != '\\')
                    break;
                end++;
            }
            return json.substring(start + 1, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        } catch (Exception e) {
            return "Could not parse response";
        }
    }

    private String shortenUrl(String url) {
        if (url == null) return "";
        return url.length() > 50
                ? "..." + url.substring(url.length() - 47) : url;
    }
}