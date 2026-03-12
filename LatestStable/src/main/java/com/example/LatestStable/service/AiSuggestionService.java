package com.example.LatestStable.service;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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


}
