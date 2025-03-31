package org.example.projects.repository;

import org.example.projects.domain.ProductionData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionDataRepository extends JpaRepository<ProductionData, String> {
}
