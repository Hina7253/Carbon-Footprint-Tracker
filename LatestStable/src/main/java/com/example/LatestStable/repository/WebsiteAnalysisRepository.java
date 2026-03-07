package com.example.LatestStable.repository;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.model.WebsiteAnalysis.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WebsiteAnalysisRepository extends JpaRepository<WebsiteAnalysis, Long> {
    List<WebsiteAnalysis> findByWebsiteUrlOrderByCreatedAtDesc(String websiteUrl);
    Optional<WebsiteAnalysis> findTopByWebsiteUrlOrderByCreatedAtDesc(String websiteUrl);
    List<WebsiteAnalysis> findByStatus(AnalysisStatus status);
    List<WebsiteAnalysis> findByStatusOrderByCreatedAtDesc(AnalysisStatus status);

    @Query("SELECT w FROM WebsiteAnalysis w " +
            "WHERE w.status = 'COMPLETED' " +
            "AND w.createdAt >= :since " +
            "ORDER BY w.co2PerVisitGrams DESC")
    List<WebsiteAnalysis> findRecentCompletedAnalyses(@Param("since") LocalDateTime since);

    @Query("SELECT w FROM WebsiteAnalysis w " +
            "WHERE w.status = 'COMPLETED' " +
            "AND w.co2PerVisitGrams IS NOT NULL " +
            "ORDER BY w.co2PerVisitGrams DESC")
    List<WebsiteAnalysis> findTopPolluters();

    long countByWebsiteUrl(String websiteUrl);

    List<WebsiteAnalysis> findByCreatedAtBetween(
            LocalDateTime from,
            LocalDateTime to
    );
}
