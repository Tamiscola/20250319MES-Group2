package org.example.projects.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.repository.ProductionLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductionLineService {
    @Autowired
    private ProductionLineRepository productionLineRepository;

    public List<ProductionLine> getAllProductionLines() {
        return productionLineRepository.findAll();
    }
}
