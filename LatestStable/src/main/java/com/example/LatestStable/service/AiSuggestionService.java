package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import okhttp3.OkHttpClient;
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


}
