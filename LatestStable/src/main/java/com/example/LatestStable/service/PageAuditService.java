package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;

public class PageAuditService {
    public List<PageResources> auditPage(String url) {
        List<PageResources> resources = new ArrayList<>();

        // Chrome Options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
    }
}
