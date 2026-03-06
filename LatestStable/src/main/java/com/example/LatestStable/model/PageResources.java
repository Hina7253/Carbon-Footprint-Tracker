package com.example.LatestStable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "page_resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResources {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = False)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private WebsiteAnalysis websiteAnalysis;

    @Column(length = 2048)
    private String foundOnPage;

    @Column(nullable = false, length = 2048)
    private String resourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Column
    private Long sizeBytes;

    @Column
    private Double co2ContributionGrams;

    @Column
    @Builder.Default
    private Boolean isCached = false;

    @Column
    @Builder.Default
    private Boolean isThirdParty = false;

    @Column
    private Integer httpStatus;

    @Column(length = 255)
    private String contentType;

    @Column
    private Double optimizationPotential;

    public void detectThirdParty(String baseDomain) {
        if (this.resourceUrl == null || baseDomain == null) {
            this.isThirdParty = false;
            return;
        }
        this.isThirdParty = !this.resourceUrl.contains(baseDomain);
    }
}
