package com.example.LatestStable.controller;

import com.example.LatestStable.service.EmailService;
import com.example.LatestStable.service.PdfReportService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")
public class EmailController {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final PdfReportService pdfReportService;

    public EmailController(
            EmailService emailService,
            PdfReportService pdfReportService) {
        this.emailService    = emailService;
        this.pdfReportService = pdfReportService;
    }

}
