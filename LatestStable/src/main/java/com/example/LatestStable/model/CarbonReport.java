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
    private double totalPageSize;
    private double co2PerVisitGrams;
    private double co2PerYearKg;
    private String carbonGrade;
    private boolean greenHosted;
    private LocalDateTime scannedAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<PageResources> resources;

    @OneToMany(cascade = CascadeType.ALL)
    private List<OptimizationSuggestion> suggestions;



}
