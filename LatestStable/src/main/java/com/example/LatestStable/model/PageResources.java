package com.example.LatestStable.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String url;
    private String resourceType;   // IMAGE, SCRIPT, CSS, FONT, VIDEO, XHR
    private double sizeKB;
    private double co2Contribution;  // is resource ka CO2 share
}
