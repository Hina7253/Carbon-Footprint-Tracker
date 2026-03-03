package com.example.LatestStable.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class OptimizationSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Category;  // IMAGE, JAVASCRIPT, CACHING etc
    private String issue;     // "Uncompressed images found"
    private String suggestion;  //"Convert to WebP format"
    private String impact;       // HIGH, MEDIUM, LOW
    private double potentialSavingKb;

}
