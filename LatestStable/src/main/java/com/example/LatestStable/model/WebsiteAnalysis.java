package com.example.LatestStable.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "website_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteAnalysis {

    // ── PRIMARY KEY ──────────────────────────────────────────────
    // @Id          → This field is the PRIMARY KEY
    // @GeneratedValue → Database auto-generates this value
    // IDENTITY strategy → uses database auto-increment (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── WEBSITE URL ──────────────────────────────────────────────
    // @Column(nullable = false) → NOT NULL constraint in database
    // The URL entered by the user: "https://example.com"
    @Column(nullable = false, length = 2048)
    private String websiteUrl;

    // ── MONTHLY VISITS ───────────────────────────────────────────
    // How many monthly visitors? Needed to calculate yearly CO2.
    // Default = 10000 (a reasonable assumption)
    @Column(nullable = false)
    @Builder.Default
    private Long monthlyVisits = 10000L;

    // ── HOW MANY PAGES TO CRAWL ──────────────────────────────────
    // User can choose to analyze 1, 3, or 5 pages of the website.
    // More pages = more accurate but slower.
    @Column(nullable = false)
    @Builder.Default
    private Integer crawlPages = 1;

    // ── DATA TRANSFER METRICS ────────────────────────────────────
    // totalTransferBytes: Total size of ALL resources on the page
    // Unit: bytes (1 KB = 1024 bytes, 1 MB = 1,048,576 bytes)
    @Column
    private Long totalTransferBytes;

    // ── CARBON EMISSIONS CALCULATED ──────────────────────────────
    // co2PerVisitGrams: CO2 in grams for ONE person visiting
    // co2YearlyKg: CO2 in kilograms per year (based on monthlyVisits)
    @Column
    private Double co2PerVisitGrams;

    @Column
    private Double co2YearlyKg;

    // ── ENERGY USAGE ─────────────────────────────────────────────
    // kwh = kilowatt-hours of electricity per visit
    // Energy used by: user device + network transmission + servers
    @Column
    private Double energyUsageKwh;

    // ── PERFORMANCE GRADE ────────────────────────────────────────
    // A letter grade we calculate: A, B, C, D, or F
    // Based on: page weight, co2, optimization potential
    @Column(length = 1)
    private String grade;

    // ── ANALYSIS STATUS ──────────────────────────────────────────
    // Tracks where in the process this analysis is.
    // PENDING → PROCESSING → COMPLETED or FAILED
    @Enumerated(EnumType.STRING)  // Store as "PENDING" string, not 0/1/2
    @Column(nullable = false)
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.PENDING;

    // ── ERROR MESSAGE ─────────────────────────────────────────────
    // If analysis fails (URL not reachable, timeout etc.)
    // we store the reason here so user knows what went wrong.
    @Column(length = 1000)
    private String errorMessage;

    // ── AI SUGGESTIONS ───────────────────────────────────────────
    // The AI-generated optimization recommendations.
    // @Lob = Large Object (TEXT type in database, not VARCHAR)
    // Stores potentially large text like AI responses.
    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiSuggestions;

    // ── TIMESTAMPS ───────────────────────────────────────────────
    // When was this analysis created and last updated?
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    // ── RELATIONSHIP TO PAGE RESOURCES ───────────────────────────
    // @OneToMany = One analysis has MANY resources
    // mappedBy = "websiteAnalysis" means the PageResource entity
    //   has a field called "websiteAnalysis" that owns the FK
    // cascade = SAVE/DELETE analysis → also save/delete its resources
    // orphanRemoval = if resource removed from list → delete from DB
    // @Builder.Default → needed because Lombok @Builder needs this
    //   for collections with default initialization
    @OneToMany(mappedBy = "websiteAnalysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY) // LAZY = don't load resources unless asked
    @Builder.Default
    private List<PageResources> resources = new ArrayList<>();

    public static Object builder() {


    }

    // ── LIFECYCLE CALLBACKS ───────────────────────────────────────
    // @PrePersist = Called automatically BEFORE saving to database
    // We use this to auto-set createdAt timestamp.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Throwable getId() {
        return null;
    }

    public long getMonthlyVisits() {
        return 0;
    }

    public void setTotalTransferBytes(long totalBytes) {
    }

    public void setCo2PerVisitGrams(double co2PerVisit) {
    }

    public void setCo2YearlyKg(double co2Yearly) {
    }

    public void setEnergyUsageKwh(double energyKwh) {
    }

    public void setGrade(String grade) {
    }

    public void setStatus(AnalysisStatus analysisStatus) {
    }

    public void setCompletedAt(LocalDateTime now) {
    }

    public void setResources(List<PageResources> resources) {
    }

    public void setErrorMessage(String message) {
    }

    public Double getCo2YearlyKg() {
    }

    public double getCo2PerVisitGrams() {
    }

    public Double getEnergyUsageKwh() {
    }

    public Object getWebsiteUrl() {
    }

    public Object getStatus() {
    }

    // ─────────────────────────────────────────────────────────────
    // INNER ENUM: Analysis Status
    // Defined here since it's only used by this class
    // ─────────────────────────────────────────────────────────────
    public enum AnalysisStatus {
        PENDING,        // Just created, waiting to start
        PROCESSING,     // Currently crawling the website
        COMPLETED,      // Analysis finished successfully
        FAILED          // Something went wrong
    }
}