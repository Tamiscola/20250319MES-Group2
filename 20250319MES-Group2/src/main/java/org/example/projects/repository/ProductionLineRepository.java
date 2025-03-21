package org.example.projects.repository;

import org.example.projects.domain.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, String> {
}
