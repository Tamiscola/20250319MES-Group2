package org.example.projects.repository;

import org.example.projects.domain.Process;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, String> {

    @EntityGraph(attributePaths = "productionLineCode")
    Page<ProductionLine> findAll(Pageable pageable);

    List<ProductionLine> findByProductionLineName(String productionLineName);

    Optional<ProductionLine> findFirstByProductionLineName(String productionLineName);

    Optional<ProductionLine> findByProductionLineCode(String productionLineCode);

    Page<ProductionLine> findByProductionLineNameAndProductionLineStatus(String productionLineName, Status status, Pageable pageable);

    Page<ProductionLine> findByProductionLineStatus(Status status, Pageable pageable);

    Page<ProductionLine> findByProductionLineName(String productionLineName, Pageable pageable);

    Page<ProductionLine> findByProductionLineNameAndProductionLineStatusAndRegDate(String productionLineName, Status status, LocalDate regDate, Pageable pageable);

    Page<ProductionLine> findByProductionLineNameAndRegDate(String productionLineName, LocalDate regDate, Pageable pageable);

    Page<ProductionLine> findByProductionLineStatusAndRegDate(Status status, LocalDate regDate, Pageable pageable);

    Page<ProductionLine> findByRegDate(LocalDate regDate, Pageable pageable);
}
