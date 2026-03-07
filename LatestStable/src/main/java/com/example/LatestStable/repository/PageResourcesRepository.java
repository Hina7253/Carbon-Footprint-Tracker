package com.example.LatestStable.repository;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PageResourcesRepository extends JpaRepsitory<PageResources, Long> {

    // ─── FIND ALL RESOURCES FOR AN ANALYSIS
    List<PageResources> findByWebsiteAnalysis_IdOrderBySizeBytesDesc(Long analysisId);

    // FIND BY TYPE
    List<PageResources> findByWebsiteAnalysis_IdAndResourceType(
            Long analysisId,
            ResourceType resourceType
    );

    // FIND THIRD-PARTY RESOURCES
    List<PageResources> findByWebsiteAnalysis_IdAndIsThirdPartyTrue(Long analysisId);

    // TOP HEAVY RESOURCES
    @Query("SELECT r FROM PageResource r " +
            "WHERE r.websiteAnalysis.id = :analysisId " +
            "AND r.sizeBytes IS NOT NULL " +
            "ORDER BY r.sizeBytes DESC")
    List<PageResources> findHeaviestResources(@Param("analysisId") Long analysisId);

    // TOTAL SIZE CALCULATION
    @Query("SELECT COALESCE(SUM(r.sizeBytes), 0) FROM PageResource r " +
            "WHERE r.websiteAnalysis.id = :analysisId")
    Long calculateTotalBytes(@Param("analysisId") Long analysisId);

}
