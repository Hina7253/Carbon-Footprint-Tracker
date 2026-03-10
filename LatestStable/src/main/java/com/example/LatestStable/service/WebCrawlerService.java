package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.WebsiteAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service

@RequiredArgsConstructor
public class WebCrawlerService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WebCrawlerService.class);

    private final OkHttpClient okHttpClient;
    private final ResourceFetcherService resourceFetcherService;

    public WebCrawlerService(OkHttpClient okHttpClient, ResourceFetcherService resourceFetcherService) {
        this.okHttpClient = okHttpClient;
        this.resourceFetcherService = resourceFetcherService;
    }

    public List<PageResources> crawlPage(
            String pageUrl,
            WebsiteAnalysis analysis,
            String baseDomain) {

        log.info("Crawling page: {}", pageUrl);
        List<PageResources> resources = new ArrayList<>();

        try {
            String html = fetchHtml(pageUrl);
            if (html == null) {
                log.warn("Could not fetch HTML for: {}", pageUrl);
                return resources;
            }

            Document doc = Jsoup.parse(html, pageUrl);
            Set<String> resourceUrls = extractAllResourceUrls(doc, pageUrl);
            log.info("Found {} resources on {}", resourceUrls.size(), pageUrl);

            List<CompletableFuture<PageResources>> futures = new ArrayList<>();

            for (String resourceUrl : resourceUrls) {
                CompletableFuture<PageResources> future =
                        resourceFetcherService.fetchResourceDetails(
                                resourceUrl,
                                analysis,
                                pageUrl,
                                baseDomain
                        );
                futures.add(future);
            }

            CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            ).join();

            for (CompletableFuture<PageResources> future : futures) {
                resources.add(future.get());
            }

        } catch (Exception e) {
            log.error("Error crawling page {}: {}", pageUrl, e.getMessage());
        }

        return resources;
    }

    public List<PageResources> crawlMultiplePages(
            String startUrl,
            WebsiteAnalysis analysis,
            String baseDomain,
            int maxPages) {

        List<PageResources> allResources = new ArrayList<>();
        Set<String> visitedPages = new HashSet<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(startUrl);

        int pagesVisited = 0;

        while (!pagesToVisit.isEmpty() && pagesVisited < maxPages) {
            String currentPage = pagesToVisit.remove(0);

            if (visitedPages.contains(currentPage)) continue;
            visitedPages.add(currentPage);
            pagesVisited++;

            List<PageResources> pageResources =
                    crawlPage(currentPage, analysis, baseDomain);
            allResources.addAll(pageResources);

            if (pagesVisited < maxPages) {
                List<String> internalLinks =
                        findInternalLinks(currentPage, baseDomain);

                for (String link : internalLinks) {
                    if (!visitedPages.contains(link)) {
                        pagesToVisit.add(link);
                    }
                }
            }
        }

        log.info("Crawled {} pages, found {} total resources",
                pagesVisited, allResources.size());
        return allResources;
    }

    private String fetchHtml(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                    .addHeader("Accept",
                            "text/html,application/xhtml+xml,*/*;q=0.8")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (Exception e) {
            log.error("Error fetching HTML from {}: {}", url, e.getMessage());
        }
        return null;
    }

    private Set<String> extractAllResourceUrls(Document doc, String baseUrl) {
        Set<String> urls = new HashSet<>();

        // Images
        for (Element img : doc.select("img[src]")) {
            addUrl(urls, img.absUrl("src"));
        }
        for (Element img : doc.select("img[srcset]")) {
            parseSrcset(img.attr("srcset"), urls);
        }

        // Scripts
        for (Element script : doc.select("script[src]")) {
            addUrl(urls, script.absUrl("src"));
        }

        // CSS
        for (Element css : doc.select("link[rel=stylesheet]")) {
            addUrl(urls, css.absUrl("href"));
        }

        // Fonts
        for (Element font : doc.select("link[as=font]")) {
            addUrl(urls, font.absUrl("href"));
        }

        // Videos
        for (Element video : doc.select("video[src], source[src]")) {
            addUrl(urls, video.absUrl("src"));
        }

        // Favicons
        for (Element link : doc.select("link[href]")) {
            String rel = link.attr("rel").toLowerCase();
            if (rel.contains("icon") || rel.contains("image")) {
                addUrl(urls, link.absUrl("href"));
            }
        }

        // Background images in inline styles
        for (Element el : doc.select("[style*=url(]")) {
            extractUrlsFromCss(el.attr("style"), baseUrl, urls);
        }

        urls.remove("");
        urls.remove(null);

        return urls;
    }

    private List<String> findInternalLinks(String pageUrl, String baseDomain) {
        List<String> internalLinks = new ArrayList<>();

        try {
            String html = fetchHtml(pageUrl);
            if (html == null) return internalLinks;

            Document doc = Jsoup.parse(html, pageUrl);

            for (Element link : doc.select("a[href]")) {
                String href = link.absUrl("href");

                if (href.contains(baseDomain)
                        && !href.contains("#")
                        && (href.startsWith("http://")
                        || href.startsWith("https://"))) {
                    internalLinks.add(href.replaceAll("/$", ""));
                }
            }
        } catch (Exception e) {
            log.debug("Error finding internal links: {}", e.getMessage());
        }

        return internalLinks;
    }

    private void addUrl(Set<String> urls, String url) {
        if (url != null && !url.isBlank()
                && (url.startsWith("http://")
                || url.startsWith("https://"))) {
            urls.add(url.split("\\?")[0]);
        }
    }

    private void parseSrcset(String srcset, Set<String> urls) {
        if (srcset == null || srcset.isBlank()) return;
        for (String part : srcset.split(",")) {
            String trimmed = part.trim().split("\\s+")[0];
            addUrl(urls, trimmed);
        }
    }

    private void extractUrlsFromCss(
            String css, String baseUrl, Set<String> urls) {
        if (css == null) return;
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        "url\\(['\"]?([^'\"\\)]+)['\"]?\\)"
                );
        java.util.regex.Matcher matcher = pattern.matcher(css);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.startsWith("data:")) {
                if (!url.startsWith("http")) {
                    url = baseUrl + "/" + url;
                }
                addUrl(urls, url);
            }
        }
    }
}