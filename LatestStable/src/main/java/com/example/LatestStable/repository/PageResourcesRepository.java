package com.example.LatestStable.repository;

import com.example.LatestStable.model.PageResources;
import com.example.LatestStable.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageResourcesRepository
        extends JpaRepository<PageResources, Long> {

    List<PageResources> findByWebsiteAnalysis_IdOrderBySizeBytesDesc(
            Long analysisId);

    List<PageResources> findByWebsiteAnalysis_IdAndResourceType(
            Long analysisId,
            ResourceType resourceType);

    List<PageResources> findByWebsiteAnalysis_IdAndIsThirdPartyTrue(
            Long analysisId);

    @Query("SELECT r FROM PageResources r " +
            "WHERE r.websiteAnalysis.id = :analysisId " +
            "AND r.sizeBytes IS NOT NULL " +
            "ORDER BY r.sizeBytes DESC")
    List<PageResources> findHeaviestResources(
            @Param("analysisId") Long analysisId);

    @Query("SELECT COALESCE(SUM(r.sizeBytes), 0) FROM PageResources r " +
            "WHERE r.websiteAnalysis.id = :analysisId")
    Long calculateTotalBytes(@Param("analysisId") Long analysisId);

    @Query("SELECT r.resourceType, COUNT(r) FROM PageResources r " +
            "WHERE r.websiteAnalysis.id = :analysisId " +
            "GROUP BY r.resourceType " +
            "ORDER BY COUNT(r) DESC")
    List<Object[]> countByResourceType(
            @Param("analysisId") Long analysisId);

    void deleteByWebsiteAnalysis_Id(Long analysisId);
}