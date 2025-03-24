package org.example.projects.repository;

import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long>, JpaSpecificationExecutor<ProductionPlan> {
    @EntityGraph(attributePaths = {"products", "productionLines"})
    Page<ProductionPlan> findAll(Pageable pageable);

//    @Query("SELECT DISTINCT p FROM ProductionPlan p LEFT JOIN FETCH p.productionLines")
//    List<ProductionPlan> findAllWithProductionLines();
//
//    @Query("SELECT DISTINCT p FROM ProductionPlan p LEFT JOIN FETCH p.products")
//    List<ProductionPlan> findAllWithProducts();

    Page<ProductionPlan> findByProductNameContainingAndPriorityAndPlanStatus(
            String keyword, Priority priority, PlanStatus status, Pageable pageable);

    Page<ProductionPlan> findByProductNameContainingAndPriority(
            String keyword, Priority priority, Pageable pageable);

    Page<ProductionPlan> findByProductNameContainingAndPlanStatus(
            String keyword, PlanStatus status, Pageable pageable);

    Page<ProductionPlan> findByProductNameContaining(String keyword, Pageable pageable);
}