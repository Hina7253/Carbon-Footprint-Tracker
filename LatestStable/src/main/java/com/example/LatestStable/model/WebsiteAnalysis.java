package com.example.LatestStable.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "website_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String websiteUrl;

    @Column(nullable = false)
    @Builder.Default
    private Long monthlyVisits = 10000L;

    @Column(nullable = false)
    @Builder.Default
    private Integer crawlPages = 1;

    @Column
    private Long totalTransferBytes;

    @Column
    private Double co2PerVisitGrams;

    @Column
    private Double co2YearlyKg;

    @Column
    private Double energyUsageKwh;

    @Column(length = 1)
    private String grade;



}
