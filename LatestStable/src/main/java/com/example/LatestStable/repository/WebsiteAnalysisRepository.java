package com.example.LatestStable.repository;

import com.example.LatestStable.model.WebsiteAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebsiteAnalysisRepository extends JpaRepository<WebsiteAnalysis, Long> {
    List<WebsiteAnalysis> findByWebsiteUrlOrderByCreatedAtDesc(String websiteUrl);
    Optional<WebsiteAnalysis> findTopByWebsiteUrlOrderByCreatedAtDesc(String websiteUrl);
    List<WebsiteAnalysis> findByStatus(AnalysisStatus status);
}
