package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Product;
import org.example.projects.domain.Process;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.Task;
import org.example.projects.domain.enums.*;
import org.example.projects.dto.ProductionPlanDTO;
import org.example.projects.repository.*;
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
    private ProcessRepository processRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ModelMapper modelMapper;

    //모든 생산계획 불러오기(페이지)
    public Page<ProductionPlanDTO> getAllPlans(Pageable pageable) {
        Page<ProductionPlan> planPage = productionPlanRepository.findAll(pageable);
        return planPage.map(plan -> {
            ProductionPlanDTO dto = ProductionPlanDTO.fromEntity(plan);
            if (plan.getProductionLines() != null) {
                dto.setProductionLineNames(plan.getProductionLines().stream()
                        .map(ProductionLine::getProductionLineName)
                        .collect(Collectors.toSet()));
            } else {
                dto.setProductionLineNames(null); // Or an empty set, depending on your needs
            }
            if (plan.getProducts() != null) {
                dto.setProductNames(plan.getProducts().stream()
                        .map(Product::getProductName)
                        .collect(Collectors.toSet()));
            } else {
                dto.setProductNames(null);  // Or an empty set
            }
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

    // Product, ProductionLine 과 함께 생산계획 ID로 불러오기
    @Transactional(readOnly = true)
    public ProductionPlan getProductionPlanWithAssociations(Long id) {
        return productionPlanRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new RuntimeException("Production plan not found with id: " + id));
    }

    // 생산계획 추가 기능
    @Transactional
    public void createProductionPlan(ProductionPlanDTO dto, String productionLineName, MultipartFile file) throws IOException {
        // Find or create ProductionLine
        ProductionLine productionLine = productionLineRepository.findFirstByProductionLineName(productionLineName)
                .orElseGet(() -> {
                    ProductionLine newProductionLine = new ProductionLine();
                    newProductionLine.setProductionLineName(productionLineName);
                    newProductionLine.setProductionLineStatus(Status.NORMAL);
                    return productionLineRepository.save(newProductionLine);
                });

        // Create ProductionPlan
        ProductionPlan plan = ProductionPlan.builder()
                .productName(dto.getProductName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .targetQty(dto.getTargetQty())
                .manager(dto.getManager())
                .priority(dto.getPriority())
                .planStatus(dto.getPlanStatus())
                .build();

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uploadDir = "uploads/";
            FileUploadUtil.saveFile(uploadDir, fileName, file);
            plan.setFileUrl(uploadDir + fileName);
        }

        // Save ProductionPlan
        productionPlanRepository.save(plan);

        // Create and save Product
        Product product = Product.builder()
                .productName(dto.getProductName())
                .productionLine(productionLine)
                .productionPlan(plan)
                .regBy(dto.getManager())
                .quantity(0)
                .build();

        productRepository.save(product);

        // Create Processes and Tasks
        for (ProcessType processType : ProcessType.values()) {
            Process process = Process.builder()
                    .processType(processType)
                    .productionLine(productionLine)
                    .completed(false)
                    .progress(0)
                    .build();

            processRepository.save(process);

            List<TaskType> tasksForProcess = TaskType.getTasksForProcess(process);
            List<Task> processTasks = tasksForProcess.stream()
                    .map(taskType -> Task.builder()
                            .taskType(taskType)
                            .process(process)
                            .completed(false)
                            .progress(0)
                            .build())
                    .collect(Collectors.toList());

            taskRepository.saveAll(processTasks);
            process.setTasks(processTasks);
            processRepository.save(process);

        }
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
        List<ProductionLine> productionLines = productionLineRepository.findByProductionLineName(productionLineName);
        ProductionLine newProductionLine;
        if (productionLines.isEmpty()) {
            newProductionLine = new ProductionLine();
            newProductionLine.setProductionLineName(productionLineName);
            newProductionLine.setProductionLineStatus(Status.NORMAL);
            newProductionLine = productionLineRepository.save(newProductionLine);
        } else {
            newProductionLine = productionLines.get(0); // Use the first matching production line
        }

        // Correctly manage the production lines
        plan.getProductionLines().clear();  // Remove all existing lines
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
                URI uri = new URI(plan.getFileUrl());
                if ("file".equals(uri.getScheme())) {
                    // Handle local files
                    Path filePath = Paths.get(uri);
                    Files.deleteIfExists(filePath);
                } else {
                    // Handle remote files (HTTP/HTTPS)
                    deleteRemoteFile(uri); // Implement your HTTP deletion logic
                }
            } catch (URISyntaxException | IOException e) {
                log.warn("Failed to delete file: " + plan.getFileUrl(), e);
            }
        }
        productionPlanRepository.delete(plan);
    }

    // HTTP 첨부파일 삭제
    private void deleteRemoteFile(URI uri) throws IOException {
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            log.warn("Failed to delete remote file. Response code: " + responseCode);
        }
    }


    // 생산계획 검색 기능 (검색어, 우선순위, 상태)
    public Page<ProductionPlanDTO> searchPlans(String keyword, String priority, String status, Pageable pageable) {
        Priority priorityEnum = priority.isEmpty() ? null : Priority.valueOf(priority);
        PlanStatus statusEnum = status.isEmpty() ? null : PlanStatus.valueOf(status);

        Page<ProductionPlan> result;

        if (priorityEnum != null && statusEnum != null) {
            result = productionPlanRepository.findByProductNameContainingAndPriorityAndPlanStatus(keyword, priorityEnum, statusEnum, pageable);
        } else if (priorityEnum != null) {
            result = productionPlanRepository.findByProductNameContainingAndPriority(keyword, priorityEnum, pageable);
        } else if (statusEnum != null) {
            result = productionPlanRepository.findByProductNameContainingAndPlanStatus(keyword, statusEnum, pageable);
        } else {
            result = productionPlanRepository.findByProductNameContaining(keyword, pageable);
        }

        return result.map(ProductionPlanDTO::fromEntity);
    }

}