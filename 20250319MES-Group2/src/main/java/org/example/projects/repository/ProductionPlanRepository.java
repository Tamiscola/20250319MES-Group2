package org.example.projects.repository;

import org.example.projects.domain.ProductionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
    @EntityGraph(attributePaths = {"products", "productionLines"})
    Page<ProductionPlan> findAll(Pageable pageable);

    @Query("SELECT DISTINCT p FROM ProductionPlan p LEFT JOIN FETCH p.productionLines")
    List<ProductionPlan> findAllWithProductionLines();

    @Query("SELECT DISTINCT p FROM ProductionPlan p LEFT JOIN FETCH p.products")
    List<ProductionPlan> findAllWithProducts();
}
