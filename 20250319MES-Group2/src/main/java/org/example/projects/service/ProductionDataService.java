package org.example.projects.service;

import org.example.projects.domain.ProductionData;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.dto.ProductionDataDTO;
import org.example.projects.repository.ProductionDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductionDataService {

    @Autowired
    private ProductionDataRepository productionDataRepository;

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
}

