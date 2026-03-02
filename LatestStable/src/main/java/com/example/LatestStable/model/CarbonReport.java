package com.example.LatestStable.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carbon_reports")
@Data
@NoArgsConstructor

public class CarbonReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String url;
    private double totalPageSizeKB; // KB m page size
    private double co2PerVisitGrams; // grams Co2 per visit
    private double co2PerYearKg;  // yearly Co2
    private String carbonGrade;   // A+, A, B, C, D, E
    private boolean greenHosted;  // Renewable energy pr host h
    private LocalDateTime scannedAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<PageResources> resources;

    @OneToMany(cascade = CascadeType.ALL)
    private List<OptimizationSuggestion> suggestions;



}
