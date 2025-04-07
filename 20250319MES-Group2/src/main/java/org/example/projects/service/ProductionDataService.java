package org.example.projects.service;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.MaterialConsumption;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.dto.ProductionDataDTO;
import org.example.projects.repository.MaterialConsumptionRepository;
import org.example.projects.repository.ProductionDataRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Log4j2
public class ProductionDataService {

    @Autowired
    private ProductionDataRepository productionDataRepository;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @Autowired
    private MaterialConsumptionRepository materialConsumptionRepository;

    public List<ProductionData> getAllProductionResults() {
        return productionDataRepository.findAll();
    }

    public Page<ProductionDataDTO> filterProductionData(String productionLineName, String productName,
                                                        LocalDate startDate, LocalDate endDate, String status,
                                                        Pageable pageable) {
        PlanStatus statusEnum = (status != null && !status.isEmpty()) ? PlanStatus.valueOf(status.toUpperCase()) : null;

        return productionDataRepository.filterProductionData(
                        productionLineName, productName, startDate, endDate, statusEnum, pageable)
                .map(ProductionDataDTO::fromEntity);
    }

    @Transactional
    public void deleteProductionData(String id) {
        log.info("Attempting to delete ProductionData with ID: {}", id);

        ProductionData productionData = productionDataRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductionData not found"));

        log.info("Found ProductionData: {}", productionData);

        // Break relationships without deleting Product
        productionData.setProduct(null);
        productionData.setProductionLine(null);
        productionData.setProductionPlan(null);

        // Delete MaterialConsumptions if necessary (if cascade is not used)
        for (MaterialConsumption consumption : new ArrayList<>(productionData.getMaterialConsumptions())) {
            materialConsumptionRepository.delete(consumption);
        }

        // Clear materialConsumptions list
        productionData.getMaterialConsumptions().clear();
        productionDataRepository.save(productionData);

        // Delete ProductionData
        productionDataRepository.delete(productionData);
    }
}

