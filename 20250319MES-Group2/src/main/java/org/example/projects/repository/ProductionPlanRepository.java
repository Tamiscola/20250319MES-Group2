package org.example.projects.repository;

import org.example.projects.domain.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
}
