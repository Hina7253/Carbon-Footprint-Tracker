package com.example.LatestStable.service;

import com.example.LatestStable.repository.WebsiteAnalysisRepository;

public class EmailService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final WebsiteAnalysisRepository analysisRepository;
}
