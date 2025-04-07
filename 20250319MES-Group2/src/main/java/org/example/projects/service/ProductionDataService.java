package org.example.projects.service;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.dto.ProductionDataDTO;
import org.example.projects.repository.ProductionDataRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@Log4j2
public class ProductionDataService {

    @Autowired
    private ProductionDataRepository productionDataRepository;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

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

        // Get the product before nullifying the relationship
        Product product = productionData.getProduct();

        if (product != null) {
            // Remove the product from all production plans first
            for (ProductionPlan plan : new HashSet<>(product.getProductionPlans())) {
                plan.getProducts().remove(product);
                // Save the updated plan if you're not using cascade
                productionPlanRepository.save(plan);
            }
            product.getProductionPlans().clear();

            // Clear bidirectional relationship
            product.setProductionData(null);
            productionDataRepository.save(productionData);
        }

        // Break relationships
        productionData.setProduct(null);
        productionData.setProductionLine(null);
        productionData.setProductionPlan(null);

        // Delete entity
        productionDataRepository.delete(productionData);
    }
}

