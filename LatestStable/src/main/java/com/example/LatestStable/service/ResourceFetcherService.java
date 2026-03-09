// java
package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import com.example.LatestStable.model.WebsiteAnalysis;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ResourceFetcherService {

    private static final Logger log = LoggerFactory.getLogger(ResourceFetcherService.class);

    private final OkHttpClient okHttpClient;
    private final CarbonCalculatorService carbonCalculatorService;

    public ResourceFetcherService(OkHttpClient okHttpClient, CarbonCalculatorService carbonCalculatorService) {
        this.okHttpClient = okHttpClient;
        this.carbonCalculatorService = carbonCalculatorService;
    }

    @Async("taskExecutor")
    public CompletableFuture<PageResources> fetchResourceDetails(
            String resourceUrl,
            WebsiteAnalysis analysis,
            String foundOnPage,
            String baseDomain) {

        PageResources resource = new PageResources();
        resource.setWebsiteAnalysis(analysis);
        resource.setResourceUrl(resourceUrl);
        resource.setFoundOnPage(foundOnPage);
        resource.setResourceType(ResourceType.detectFromUrl(resourceUrl));

        resource.detectThirdParty(baseDomain);

        try {
            Request request = new Request.Builder()
                    .url(resourceUrl)
                    .head()
                    .addHeader("User-Agent", "Mozilla/5.0 (compatible; CarbonScopeBot/1.0)")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {

                resource.setHttpStatus(response.code());

                String contentLength = response.header("Content-Length");
                if (contentLength != null) {
                    long sizeBytes = Long.parseLong(contentLength);
                    resource.setSizeBytes(sizeBytes);

                    double co2 = carbonCalculatorService.calculateResourceCo2(
                            sizeBytes,
                            resource.getResourceType()
                    );
                    resource.setCo2ContributionGrams(co2);

                    double potential = carbonCalculatorService
                            .calculateOptimizationPotential(
                                    sizeBytes,
                                    resource.getResourceType(),
                                    resource.getIsCached(),
                                    resource.getContentType()
                            );
                    resource.setOptimizationPotential(potential);
                }

                String contentType = response.header("Content-Type");
                if (contentType != null) {
                    resource.setContentType(contentType.split(";")[0].trim());
                }

                String cacheControl = response.header("Cache-Control");
                if (cacheControl != null &&
                        (cacheControl.contains("max-age") ||
                                cacheControl.contains("immutable"))) {
                    resource.setIsCached(true);
                }
            }

        } catch (Exception e) {
            log.debug("Could not fetch resource: {} - {}", resourceUrl, e.getMessage());
            resource.setHttpStatus(0);
        }

        return CompletableFuture.completedFuture(resource);
    }
}