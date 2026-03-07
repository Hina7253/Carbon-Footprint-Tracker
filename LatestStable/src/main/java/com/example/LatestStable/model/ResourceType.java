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


}
