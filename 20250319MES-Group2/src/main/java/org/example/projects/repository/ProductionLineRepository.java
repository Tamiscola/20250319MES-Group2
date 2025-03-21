package org.example.projects.repository;

import org.example.projects.domain.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, String> {
    Optional<ProductionLine> findByProductionLineName(String productionLineName);
}
