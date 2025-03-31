package org.example.projects.repository;

import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionDataRepository extends JpaRepository<ProductionData, String> {
    Optional<ProductionData> findByProductionPlan(ProductionPlan plan);
}
