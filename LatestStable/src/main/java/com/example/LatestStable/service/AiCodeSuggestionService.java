package com.example.LatestStable.service;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class AiCodeSuggestionService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(
                    AiCodeSuggestionService.class);

    private final OkHttpClient httpClient;

    @Value("${openai.api.key:no-key}")
    private String openAiApiKey;

    public AiCodeSuggestionService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // ── GENERATE CODE FIXES ───────────────────────────────────────
    public Map<String, Object> generateCodeFixes(
            String websiteUrl,
            List<PageResources> resources,
            String grade) {

        log.info("Generating code fixes for: {}", websiteUrl);

        // Group resources by type
        Map<ResourceType, List<PageResources>> byType =
                resources.stream()
                        .collect(Collectors.groupingBy(
                                PageResources::getResourceType));

        List<Map<String, Object>> codeFixes = new ArrayList<>();

        // ── IMAGE FIXES ───────────────────────────────────────────
        List<PageResources> images =
                byType.getOrDefault(ResourceType.IMAGE,
                        new ArrayList<>());

        if (!images.isEmpty()) {
            codeFixes.add(buildImageFix(images));
        }

        // ── SCRIPT FIXES ──────────────────────────────────────────
        List<PageResources> scripts =
                byType.getOrDefault(ResourceType.SCRIPT,
                        new ArrayList<>());

        if (!scripts.isEmpty()) {
            codeFixes.add(buildScriptFix(scripts));
        }

        // ── FONT FIXES ────────────────────────────────────────────
        List<PageResources> fonts =
                byType.getOrDefault(ResourceType.FONT,
                        new ArrayList<>());

        if (!fonts.isEmpty()) {
            codeFixes.add(buildFontFix(fonts));
        }

        // ── CSS FIXES ─────────────────────────────────────────────
        List<PageResources> styles =
                byType.getOrDefault(ResourceType.STYLE,
                        new ArrayList<>());

        if (!styles.isEmpty()) {
            codeFixes.add(buildStyleFix(styles));
        }

        // ── CACHING FIX ───────────────────────────────────────────
        codeFixes.add(buildCachingFix());

        // If AI key available, enhance with AI
        String aiEnhancement = "";
        if (!"no-key".equals(openAiApiKey)) {
            aiEnhancement = getAiCodeHelp(
                    websiteUrl, resources, grade);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("websiteUrl",     websiteUrl);
        result.put("grade",          grade);
        result.put("codeFixes",      codeFixes);
        result.put("totalFixes",     codeFixes.size());
        result.put("aiCodeHelp",     aiEnhancement);
        result.put("estimatedImpact",
                "Implementing all fixes can improve " +
                        "grade by 1-2 levels");

        return result;
    }

    // ── IMAGE CODE FIX ────────────────────────────────────────────
    private Map<String, Object> buildImageFix(
            List<PageResources> images) {

        // Get largest image URL
        String largestImgUrl = images.stream()
                .filter(r -> r.getSizeBytes() != null)
                .max(Comparator.comparingLong(
                        PageResources::getSizeBytes))
                .map(PageResources::getResourceUrl)
                .orElse("image.jpg");

        String shortUrl = shortenUrl(largestImgUrl);

        Map<String, Object> fix = new HashMap<>();
        fix.put("category",    "Image Optimization");
        fix.put("impact",      "HIGH — saves 30-60% bandwidth");
        fix.put("difficulty",  "Easy");
        fix.put("affectedCount", images.size() + " images");

        fix.put("before", """
            <!-- ❌ BEFORE: Large unoptimized image -->
            <img src="%s"
                 width="800"
                 height="600">
            """.formatted(shortUrl));

        fix.put("after", """
            <!-- ✅ AFTER: Optimized with WebP + lazy loading -->
            <picture>
              <source srcset="%s.avif" type="image/avif">
              <source srcset="%s.webp" type="image/webp">
              <img src="%s"
                   width="800"
                   height="600"
                   loading="lazy"
                   decoding="async"
                   alt="Description of image">
            </picture>
            """.formatted(
                shortUrl.replaceAll("\\.[^.]+$", ""),
                shortUrl.replaceAll("\\.[^.]+$", ""),
                shortUrl));

        fix.put("howTo",
                "1. Convert images using: 'cwebp input.jpg -o output.webp'\n" +
                        "2. Or use online tool: squoosh.app\n" +
                        "3. Add loading='lazy' to all images below the fold");

        return fix;
    }

    // ── SCRIPT CODE FIX ───────────────────────────────────────────
    private Map<String, Object> buildScriptFix(
            List<PageResources> scripts) {

        Map<String, Object> fix = new HashMap<>();
        fix.put("category",    "JavaScript Optimization");
        fix.put("impact",      "HIGH — saves 40-60% JS size");
        fix.put("difficulty",  "Medium");
        fix.put("affectedCount", scripts.size() + " scripts");

        fix.put("before", """
            <!-- ❌ BEFORE: Blocking render scripts -->
            <head>
              <script src="/bundle.js"></script>
              <script src="/analytics.js"></script>
            </head>
            """);

        fix.put("after", """
            <!-- ✅ AFTER: Non-blocking, deferred scripts -->
            <head>
              <!-- Critical only -->
              <script src="/critical.min.js"></script>
            </head>
            <body>
              <!-- Deferred non-critical scripts -->
              <script src="/bundle.min.js" defer></script>
              <script src="/analytics.js"
                      defer
                      data-domain="yourdomain.com">
              </script>
            </body>
            """);

        fix.put("webpackConfig", """
            // webpack.config.js — Tree shaking config
            module.exports = {
              mode: 'production',
              optimization: {
                usedExports: true,      // Tree shaking
                minimize: true,         // Minification
                splitChunks: {
                  chunks: 'all',        // Code splitting
                  maxSize: 244000       // Max 244KB chunks
                }
              }
            }
            """);

        fix.put("howTo",
                "1. Run: npm run build (production mode)\n" +
                        "2. Add 'defer' to all non-critical scripts\n" +
                        "3. Use webpack-bundle-analyzer to find bloat");

        return fix;
    }

    // ── FONT CODE FIX ─────────────────────────────────────────────
    private Map<String, Object> buildFontFix(
            List<PageResources> fonts) {

        Map<String, Object> fix = new HashMap<>();
        fix.put("category",    "Font Optimization");
        fix.put("impact",      "MEDIUM — saves 30-50% font size");
        fix.put("difficulty",  "Easy");
        fix.put("affectedCount", fonts.size() + " font files");

        fix.put("before", """
            <!-- ❌ BEFORE: Multiple heavy font imports -->
            <link href="https://fonts.googleapis.com/css2?
                family=Roboto:wght@100;200;300;400;500;600;700;800;900
                &display=swap" rel="stylesheet">
            """);

        fix.put("after", """
            <!-- ✅ AFTER: Preloaded, subsetted, limited weights -->
            <link rel="preconnect"
                  href="https://fonts.googleapis.com">
            <link rel="preconnect"
                  href="https://fonts.gstatic.com"
                  crossorigin>

            <!-- Only 2 weights instead of 9 -->
            <link href="https://fonts.googleapis.com/css2?
                family=Roboto:wght@400;700
                &display=swap&subset=latin"
                  rel="stylesheet"
                  media="print"
                  onload="this.media='all'">
            """);

        fix.put("selfHosting", """
            /* ✅ BETTER: Self-host fonts with woff2 */
            @font-face {
              font-family: 'Roboto';
              src: url('/fonts/roboto-400.woff2') format('woff2');
              font-weight: 400;
              font-display: swap; /* Prevents invisible text */
            }
            """);

        fix.put("howTo",
                "1. Use only 1-2 font families\n" +
                        "2. Use only needed weights (400, 700)\n" +
                        "3. Add font-display: swap to avoid FOIT\n" +
                        "4. Self-host using: google-webfonts-helper.herokuapp.com");

        return fix;
    }

    // ── CSS CODE FIX ──────────────────────────────────────────────
    private Map<String, Object> buildStyleFix(
            List<PageResources> styles) {

        Map<String, Object> fix = new HashMap<>();
        fix.put("category",    "CSS Optimization");
        fix.put("impact",      "MEDIUM — saves 40-70% CSS size");
        fix.put("difficulty",  "Easy");
        fix.put("affectedCount", styles.size() + " stylesheets");

        fix.put("before", """
            <!-- ❌ BEFORE: Full CSS loaded always -->
            <link rel="stylesheet" href="/styles.css">
            <!-- styles.css might be 200KB but 80% unused -->
            """);

        fix.put("after", """
            <!-- ✅ AFTER: Only critical CSS inline -->
            <style>
              /* Critical above-fold CSS here (< 14KB) */
              body { margin: 0; font-family: sans-serif; }
              header { background: #333; color: white; }
            </style>

            <!-- Non-critical CSS loaded async -->
            <link rel="preload"
                  href="/non-critical.min.css"
                  as="style"
                  onload="this.rel='stylesheet'">
            """);

        fix.put("purgeConfig", """
            // tailwind.config.js — PurgeCSS setup
            module.exports = {
              content: [
                './src/**/*.{html,js,jsx,ts,tsx}',
                './public/index.html'
              ],
              // Tailwind auto-purges unused classes
            }

            // Or use PurgeCSS standalone:
            // npx purgecss --css styles.css
            //   --content index.html
            //   --output purged.css
            """);

        return fix;
    }

    // ── CACHING CODE FIX ──────────────────────────────────────────
    private Map<String, Object> buildCachingFix() {
        Map<String, Object> fix = new HashMap<>();
        fix.put("category",   "Server Caching");
        fix.put("impact",     "HIGH — repeat visitors use 0 bandwidth");
        fix.put("difficulty", "Easy");

        fix.put("nginxConfig", """
            # nginx.conf — Aggressive caching for static assets
            location ~* \\.(js|css|png|jpg|jpeg|gif|webp|svg|woff2)$ {
                expires 1y;
                add_header Cache-Control "public, immutable";
                add_header Vary "Accept-Encoding";
                gzip_static on;
            }
            """);

        fix.put("apacheConfig", """
            # .htaccess — Apache caching
            <FilesMatch "\\.(js|css|png|jpg|webp|woff2)$">
                Header set Cache-Control "max-age=31536000, immutable"
            </FilesMatch>

            # Enable Gzip compression
            AddOutputFilterByType DEFLATE text/css
            AddOutputFilterByType DEFLATE application/javascript
            """);

        fix.put("howTo",
                "1. Set Cache-Control headers for static assets\n" +
                        "2. Use content hashing: bundle.a1b2c3.js\n" +
                        "3. Enable Gzip/Brotli on your server\n" +
                        "4. Use a CDN (Cloudflare free tier works great)");

        return fix;
    }

    // ── AI CODE HELP ──────────────────────────────────────────────
    private String getAiCodeHelp(
            String url,
            List<PageResources> resources,
            String grade) {

        try {
            long totalBytes = resources.stream()
                    .filter(r -> r.getSizeBytes() != null)
                    .mapToLong(PageResources::getSizeBytes)
                    .sum();

            String prompt =
                    "Website: " + url + "\n" +
                            "Grade: " + grade + "\n" +
                            "Total size: " + totalBytes + " bytes\n" +
                            "Give ONE specific code snippet to reduce " +
                            "carbon footprint. Be concise and practical.";

            String body = "{" +
                    "\"model\":\"gpt-4o-mini\"," +
                    "\"messages\":[{\"role\":\"user\"," +
                    "\"content\":\"" +
                    prompt.replace("\"", "\\\"")
                            .replace("\n", "\\n") +
                    "\"}]," +
                    "\"max_tokens\":300}";

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(RequestBody.create(body,
                            MediaType.parse("application/json")))
                    .addHeader("Authorization",
                            "Bearer " + openAiApiKey)
                    .build();

            try (Response response =
                         httpClient.newCall(request).execute()) {

                if (response.isSuccessful()
                        && response.body() != null) {
                    return parseOpenAiResponse(
                            response.body().string());
                }
            }
        } catch (Exception e) {
            log.error("AI code help failed: {}", e.getMessage());
        }
        return "";
    }

    private String parseOpenAiResponse(String json) {
        try {
            int idx = json.indexOf("\"content\":");
            if (idx == -1) return "";
            int start = json.indexOf("\"", idx + 10);
            int end   = start + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"'
                        && json.charAt(end - 1) != '\\')
                    break;
                end++;
            }
            return json.substring(start + 1, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        } catch (Exception e) {
            return "";
        }
    }

    private String shortenUrl(String url) {
        if (url == null) return "resource";
        String[] parts = url.split("/");
        return parts[parts.length - 1].isEmpty()
                ? "resource" : parts[parts.length - 1];
    }
}