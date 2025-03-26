package org.example.projects.repository;

import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, String> {
    @EntityGraph(attributePaths = "productionLineCode")
    Page<ProductionLine> findAll(Pageable pageable);

    Optional<ProductionLine> findByProductionLineName(String productionLineName);

    Optional<ProductionLine> findFirstByProductionLineName(String productionLineName);

    Optional<ProductionLine> findByProductionLineCode(String productionLineCode);

    Page<ProductionLine> findByProductionLineNameAndProductionLineStatus(String productionLineName, Status status, Pageable pageable);

    Page<ProductionLine> findByProductionLineStatus(Status status, Pageable pageable);

    Page<ProductionLine> findByProductionLineName(String productionLineName, Pageable pageable);
}
