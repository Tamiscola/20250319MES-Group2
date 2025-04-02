package org.example.projects.repository;

import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductionDataRepository extends JpaRepository<ProductionData, String> {
    Optional<ProductionData> findByProductionPlan(ProductionPlan plan);

    void deleteByStatus(PlanStatus status);

    @Query("SELECT p FROM ProductionData p " +
            "WHERE (:productionLineName IS NULL OR p.productionLine.productionLineName = :productionLineName) " +
            "AND (:productName IS NULL OR p.product.productName = :productName) " +
            "AND (:startDate IS NULL OR p.resultDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.resultDate <= :endDate) " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<ProductionData> filterProductionData(@Param("productionLineName") String productionLineName,
                                              @Param("productName") String productName,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("status") PlanStatus status,
                                              Pageable pageable);

}
