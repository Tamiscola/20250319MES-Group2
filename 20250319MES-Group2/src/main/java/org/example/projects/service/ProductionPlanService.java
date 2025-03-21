package org.example.projects.service;

import org.example.projects.domain.ProductionPlan;
import org.example.projects.repository.ProductionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionPlanService {
    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    //모든 생산계획 불러오기(페이지)
    public Page<ProductionPlan> getAllPlans(Pageable pageable) {
        return productionPlanRepository.findAll(pageable);
    }

    // 모든 생산계획 Entity로서 불러오기
    public List<ProductionPlan> getAllPlansAsEntity() {
        return productionPlanRepository.findAll();
    }
}
