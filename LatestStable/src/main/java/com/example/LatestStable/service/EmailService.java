package com.example.LatestStable.service;

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
}
