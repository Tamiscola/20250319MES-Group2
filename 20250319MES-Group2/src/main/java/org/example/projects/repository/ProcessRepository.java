package org.example.projects.repository;

import org.example.projects.domain.Process;
import org.example.projects.domain.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    List<Process> findByProductionLine(ProductionLine line);
}
