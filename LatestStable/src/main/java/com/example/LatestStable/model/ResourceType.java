package com.example.LatestStable.model;

public enum ResourceType {
    IMAGE       ("Image",           1.2),   // Images compress well but are numerous
    VIDEO       ("Video",           3.5),   // Videos are by far the worst
    SCRIPT      ("JavaScript",      1.0),   // Baseline multiplier
    STYLE       ("CSS Stylesheet",  0.8),   // Usually small
    FONT        ("Web Font",        1.1),   // Often loaded multiple times
    API_CALL    ("API Call",        1.3),   // Server-side computation cost
    DOCUMENT    ("Document",        1.5),   // PDFs etc.
    HTML        ("HTML Page",       1.0),   // The page itself
    OTHER       ("Other Resource",  1.0);   // Unknown types

    private final String displayName;
    private final double carbonMultiplier;

    ResourceType(String displayName, double carbonMultiplier) {
        this.displayName = displayName;
        this.carbonMultiplier = carbonMultiplier;
    }
    // getters
    public String getDisplayName() {
        return displayName;
    }
    public double getCarbonMultiplier() {
        return carbonMultiplier;
    }

    public static ResourceType detectFromUrl(String url) {
        if (url == null || url.isBlank()) return OTHER;

        String lowerUrl = url.toLowerCase();

        // Check for API calls first (subdomain patterns)
        if (lowerUrl.contains("api.") || lowerUrl.contains("/api/")
                || lowerUrl.contains("/v1/") || lowerUrl.contains("/v2/")) {
            return API_CALL;
        }

        // Check file extensions
        if (lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|svg|ico|avif).*")) return IMAGE;
        if (lowerUrl.matches(".*\\.(mp4|webm|ogg|avi|mov|mkv).*"))           return VIDEO;
        if (lowerUrl.matches(".*\\.(js|mjs|cjs).*"))                          return SCRIPT;
        if (lowerUrl.matches(".*\\.(css).*"))                                  return STYLE;
        if (lowerUrl.matches(".*\\.(woff|woff2|ttf|otf|eot).*"))              return FONT;
        if (lowerUrl.matches(".*\\.(pdf|doc|docx|xls|xlsx).*"))               return DOCUMENT;
        if (lowerUrl.matches(".*\\.(html|htm|php|asp|aspx).*"))               return HTML;

        return OTHER;
    }


}
