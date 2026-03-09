package com.example.LatestStable.service;

import com.example.LatestStable.entity.PageResource;
import com.example.LatestStable.entity.ResourceType;
import com.example.LatestStable.entity.WebsiteAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceFetcherService {

    private final OkHttpClient okHttpClient;
    private final CarbonCalculatorService carbonCalculatorService;

    // ── WHAT IS @Async? ───────────────────────────────────────────
    // @Async means this method runs in a SEPARATE THREAD
    // So when we call this for 100 resources, all 100 run
    // at the SAME TIME instead of one by one
    // CompletableFuture = a "promise" that result will come later
    @Async("taskExecutor")
    public CompletableFuture<PageResource> fetchResourceDetails(
            String resourceUrl,
            WebsiteAnalysis analysis,
            String foundOnPage,
            String baseDomain) {

        PageResource resource = PageResource.builder()
                .websiteAnalysis(analysis)
                .resourceUrl(resourceUrl)
                .foundOnPage(foundOnPage)
                .resourceType(ResourceType.detectFromUrl(resourceUrl))
                .build();

        resource.detectThirdParty(baseDomain);

        try {
            // ── HEAD REQUEST ──────────────────────────────────────
            // HEAD = like GET but server sends ONLY headers, no body
            // Much faster! We just need Content-Length for file size
            Request request = new Request.Builder()
                    .url(resourceUrl)
                    .head()
                    .addHeader("User-Agent",
                            "Mozilla/5.0 (compatible; CarbonScopeBot/1.0)")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {

                resource.setHttpStatus(response.code());

                // ── GET FILE SIZE ─────────────────────────────────
                // Content-Length header tells us file size in bytes
                String contentLength = response.header("Content-Length");
                if (contentLength != null) {
                    long sizeBytes = Long.parseLong(contentLength);
                    resource.setSizeBytes(sizeBytes);

                    // Calculate CO2 for this resource
                    double co2 = carbonCalculatorService.calculateResourceCo2(
                            sizeBytes,
                            resource.getResourceType()
                    );
                    resource.setCo2ContributionGrams(co2);

                    // Calculate optimization potential
                    double potential = carbonCalculatorService
                            .calculateOptimizationPotential(
                                    sizeBytes,
                                    resource.getResourceType(),
                                    resource.getIsCached(),
                                    resource.getContentType()
                            );
                    resource.setOptimizationPotential(potential);
                }

                // ── CONTENT TYPE ──────────────────────────────────
                // "image/webp", "text/javascript", "font/woff2" etc.
                String contentType = response.header("Content-Type");
                if (contentType != null) {
                    resource.setContentType(
                            contentType.split(";")[0].trim()
                    );
                }

                // ── CHECK IF CACHED ───────────────────────────────
                // Cache-Control header tells us if browser can cache
                String cacheControl = response.header("Cache-Control");
                if (cacheControl != null &&
                        (cacheControl.contains("max-age") ||
                                cacheControl.contains("immutable"))) {
                    resource.setIsCached(true);
                }
            }

        } catch (Exception e) {
            // Don't crash if one resource fails
            // Just log and continue with other resources
            log.debug("Could not fetch resource: {} - {}",
                    resourceUrl, e.getMessage());
            resource.setHttpStatus(0);
        }

        return CompletableFuture.completedFuture(resource);
    }
}