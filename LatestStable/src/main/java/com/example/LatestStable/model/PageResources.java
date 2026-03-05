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
}
