package org.example.projects.service;

import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.ProductionPlanDTO;
import org.example.projects.repository.ProductRepository;
import org.example.projects.repository.ProductionLineRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.example.projects.util.FileUploadUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProductionPlanService {
    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ModelMapper modelMapper;

    //모든 생산계획 불러오기(페이지)
    public Page<ProductionPlan> getAllPlans(Pageable pageable) {
        return productionPlanRepository.findAll(pageable);
    }

    // 모든 생산계획 Entity로서 불러오기
    public List<ProductionPlan> getAllPlansAsEntity() {
        return productionPlanRepository.findAll();
    }

    @Transactional
    public void createProductionPlan(ProductionPlanDTO dto, String productionLineName, MultipartFile file) throws IOException {
        // Find or create ProductionLine
        ProductionLine productionLine = productionLineRepository.findByProductionLineName(productionLineName)
                .orElseGet(() -> {
                    ProductionLine newProductionLine = new ProductionLine();
                    newProductionLine.setProductionLineName(productionLineName);
                    newProductionLine.setProductionLineStatus(Status.NORMAL);
                    return productionLineRepository.save(newProductionLine);
                });

        // Create ProductionPlan
        ProductionPlan plan = new ProductionPlan();
        plan.setProductName(dto.getProductName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setTargetQty(dto.getTargetQty());
        plan.setManager(dto.getManager());
        plan.setPriority(dto.getPriority());
        plan.setPlanStatus(dto.getPlanStatus());

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uploadDir = "uploads/";
            FileUploadUtil.saveFile(uploadDir, fileName, file);
            plan.setFileUrl(uploadDir + fileName);
        }

        // Create and save Product
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductionLine(productionLine);
        product.setProductionPlan(plan);
        product.setRegBy(dto.getManager());

        // Set up relationships
        plan.getProducts().add(product);
        plan.getProductionLines().add(productionLine);
        productionLine.getProductionPlans().add(plan);

        // Save the plan and productionLine
        productionPlanRepository.save(plan);
        productionLineRepository.save(productionLine);
    }

}

