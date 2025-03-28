package org.example.projects.repository;

import org.example.projects.domain.Process;
import org.example.projects.domain.ProductionLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, String> {
    @EntityGraph(attributePaths = "productionLineCode")
    Page<ProductionLine> findAll(Pageable pageable);

    List<ProductionLine> findByProductionLineName(String productionLineName);

    Optional<ProductionLine> findFirstByProductionLineName(String productionLineName);

    Optional<ProductionLine> findByProductionLineCode(String productionLineCode);
}
