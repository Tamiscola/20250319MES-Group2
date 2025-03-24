package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
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
    public Page<ProductionPlanDTO> getAllPlans(Pageable pageable) {
        Page<ProductionPlan> planPage = productionPlanRepository.findAll(pageable);
        Set<ProductionPlan> plansWithProductionLines = new HashSet<>(productionPlanRepository.findAllWithProductionLines());
        Set<ProductionPlan> plansWithProducts = new HashSet<>(productionPlanRepository.findAllWithProducts());

        Map<Long, Set<String>> productionLineMap = plansWithProductionLines.stream()
                .collect(Collectors.toMap(
                        ProductionPlan::getPlanId,
                        p -> p.getProductionLines().stream().map(ProductionLine::getProductionLineName).collect(Collectors.toSet())
                ));

        Map<Long, Set<String>> productMap = plansWithProducts.stream()
                .collect(Collectors.toMap(
                        ProductionPlan::getPlanId,
                        p -> p.getProducts().stream().map(Product::getProductName).collect(Collectors.toSet())
                ));

        return planPage.map(plan -> {
            ProductionPlanDTO dto = ProductionPlanDTO.fromEntity(plan);
            dto.setProductionLineNames(new HashSet<>(productionLineMap.getOrDefault(plan.getPlanId(), new HashSet<>())));
            dto.setProductNames(new HashSet<>(productMap.getOrDefault(plan.getPlanId(), new HashSet<>())));
            return dto;
        });
    }

    // 모든 생산계획 Entity로서 불러오기
    public List<ProductionPlan> getAllPlansAsEntity() {
        return productionPlanRepository.findAll();
    }

    // 생산계획 ID로 불러오기
    public ProductionPlan getProductionPlanById(Long id) {
        return productionPlanRepository.findById(id).orElse(null);
    }

    // 생산계획 추가 기능
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

    // 생산계획 수정 기능
    @Transactional
    public void modifyProductionPlan(ProductionPlanDTO dto, String productionLineName, MultipartFile file) throws IOException {
        ProductionPlan plan = productionPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RuntimeException("Production plan not found"));

        // Update basic fields
        plan.setProductName(dto.getProductName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setTargetQty(dto.getTargetQty());
        plan.setManager(dto.getManager());
        plan.setPriority(dto.getPriority());
        plan.setPlanStatus(dto.getPlanStatus());

        // Handle file upload if a new file is provided
        if (file != null && !file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uploadDir = "uploads/";
            FileUploadUtil.saveFile(uploadDir, fileName, file);
            plan.setFileUrl(uploadDir + fileName);
        }

        // Handle production line change if necessary
        ProductionLine newProductionLine = productionLineRepository.findByProductionLineName(productionLineName)
                .orElseGet(() -> {
                    ProductionLine line = new ProductionLine();
                    line.setProductionLineName(productionLineName);
                    line.setProductionLineStatus(Status.NORMAL);
                    return productionLineRepository.save(line);
                });

        // Correctly manage the production lines
        // Clear existing production lines before adding the new one
        plan.getProductionLines().clear();  //  Remove all existing lines
        plan.getProductionLines().add(newProductionLine);

        productionPlanRepository.save(plan);
    }

    // 생산계획 삭제 기능
    @Transactional
    public void deleteProductionPlan(Long id) {
        ProductionPlan plan = productionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production plan not found with id: " + id));

        // Detach from production lines
        Set<ProductionLine> lines = new HashSet<>(plan.getProductionLines());
        for (ProductionLine line : lines) {
            line.getProductionPlans().remove(plan);
            productionLineRepository.save(line);
        }
        plan.getProductionLines().clear();

        // Delete file if exists
        if (plan.getFileUrl() != null && !plan.getFileUrl().isEmpty()) {
            try {
                Path filePath = Paths.get(plan.getFileUrl());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Failed to delete file: " + plan.getFileUrl(), e);
            }
        }
        productionPlanRepository.delete(plan);
    }
}

