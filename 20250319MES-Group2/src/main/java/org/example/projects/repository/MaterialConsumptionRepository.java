package org.example.projects.repository;

import org.example.projects.domain.MaterialConsumption;
import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.ProcessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialConsumptionRepository extends JpaRepository<MaterialConsumption, Long> {

    List<MaterialConsumption> findByProductionPlan(ProductionPlan productionPlan);

    List<MaterialConsumption> findByProductionData(ProductionData productionData);

    List<MaterialConsumption> findByProcessType(ProcessType processType);

    @Query("SELECT SUM(mc.consumptionCost) FROM MaterialConsumption mc WHERE mc.productionData = ?1")
    Double calculateTotalCostForProductionData(ProductionData productionData);
}
