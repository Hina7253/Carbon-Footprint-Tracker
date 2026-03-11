package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.WebsiteAnalysis;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class WebCrawlerService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WebCrawlerService.class);

    private final OkHttpClient okHttpClient;
    private final ResourceFetcherService resourceFetcherService;

    public WebCrawlerService(
            OkHttpClient okHttpClient,
            ResourceFetcherService resourceFetcherService) {
        this.okHttpClient          = okHttpClient;
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
            if (html == null) return resources;

            Document doc = Jsoup.parse(html, pageUrl);
            Set<String> resourceUrls =
                    extractAllResourceUrls(doc, pageUrl);
            log.info("Found {} resources on {}",
                    resourceUrls.size(), pageUrl);

            List<CompletableFuture<PageResources>> futures =
                    new ArrayList<>();

            for (String resourceUrl : resourceUrls) {
                futures.add(
                        resourceFetcherService.fetchResourceDetails(
                                resourceUrl, analysis, pageUrl, baseDomain)
                );
            }

            CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            ).join();

            for (CompletableFuture<PageResources> f : futures) {
                resources.add(f.get());
            }

        } catch (Exception e) {
            log.error("Error crawling {}: {}", pageUrl, e.getMessage());
        }

        return resources;
    }

    public List<PageResources> crawlMultiplePages(
            String startUrl,
            WebsiteAnalysis analysis,
            String baseDomain,
            int maxPages) {

        List<PageResources> allResources  = new ArrayList<>();
        Set<String> visitedPages          = new HashSet<>();
        List<String> pagesToVisit         = new ArrayList<>();
        pagesToVisit.add(startUrl);
        int pagesVisited = 0;

        while (!pagesToVisit.isEmpty() && pagesVisited < maxPages) {
            String currentPage = pagesToVisit.remove(0);
            if (visitedPages.contains(currentPage)) continue;
            visitedPages.add(currentPage);
            pagesVisited++;

            allResources.addAll(
                    crawlPage(currentPage, analysis, baseDomain));

            if (pagesVisited < maxPages) {
                for (String link :
                        findInternalLinks(currentPage, baseDomain)) {
                    if (!visitedPages.contains(link))
                        pagesToVisit.add(link);
                }
            }
        }

        log.info("Crawled {} pages, {} total resources",
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
                                    "AppleWebKit/537.36 Chrome/120.0.0.0")
                    .build();

            try (Response response =
                         okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null)
                    return response.body().string();
            }
        } catch (Exception e) {
            log.error("Error fetching {}: {}", url, e.getMessage());
        }
        return null;
    }

    private Set<String> extractAllResourceUrls(
            Document doc, String baseUrl) {

        Set<String> urls = new HashSet<>();

        for (Element e : doc.select("img[src]"))
            addUrl(urls, e.absUrl("src"));
        for (Element e : doc.select("img[srcset]"))
            parseSrcset(e.attr("srcset"), urls);
        for (Element e : doc.select("script[src]"))
            addUrl(urls, e.absUrl("src"));
        for (Element e : doc.select("link[rel=stylesheet]"))
            addUrl(urls, e.absUrl("href"));
        for (Element e : doc.select("link[as=font]"))
            addUrl(urls, e.absUrl("href"));
        for (Element e : doc.select("video[src],source[src]"))
            addUrl(urls, e.absUrl("src"));
        for (Element e : doc.select("link[href]")) {
            String rel = e.attr("rel").toLowerCase();
            if (rel.contains("icon") || rel.contains("image"))
                addUrl(urls, e.absUrl("href"));
        }
        for (Element e : doc.select("[style*=url(]"))
            extractUrlsFromCss(e.attr("style"), baseUrl, urls);

        urls.remove("");
        return urls;
    }

    private List<String> findInternalLinks(
            String pageUrl, String baseDomain) {

        List<String> links = new ArrayList<>();
        try {
            String html = fetchHtml(pageUrl);
            if (html == null) return links;
            Document doc = Jsoup.parse(html, pageUrl);
            for (Element a : doc.select("a[href]")) {
                String href = a.absUrl("href");
                if (href.contains(baseDomain)
                        && !href.contains("#")
                        && href.startsWith("http"))
                    links.add(href.replaceAll("/$", ""));
            }
        } catch (Exception e) {
            log.debug("Error finding links: {}", e.getMessage());
        }
        return links;
    }

    private void addUrl(Set<String> urls, String url) {
        if (url != null && !url.isBlank() && url.startsWith("http"))
            urls.add(url.split("\\?")[0]);
    }

    private void parseSrcset(String srcset, Set<String> urls) {
        if (srcset == null || srcset.isBlank()) return;
        for (String part : srcset.split(","))
            addUrl(urls, part.trim().split("\\s+")[0]);
    }

    private void extractUrlsFromCss(
            String css, String baseUrl, Set<String> urls) {
        if (css == null) return;
        var pattern = java.util.regex.Pattern.compile(
                "url\\(['\"]?([^'\"\\)]+)['\"]?\\)");
        var matcher = pattern.matcher(css);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.startsWith("data:")) {
                if (!url.startsWith("http")) url = baseUrl + "/" + url;
                addUrl(urls, url);
            }
        }
    }
}