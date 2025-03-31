package org.example.projects.service;

import org.example.projects.domain.ProductionData;
import org.example.projects.repository.ProductionDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionDataService {

    @Autowired
    private ProductionDataRepository productionDataRepository;

    public List<ProductionData> getAllProductionResults() {
        return productionDataRepository.findAll();
    }
}

