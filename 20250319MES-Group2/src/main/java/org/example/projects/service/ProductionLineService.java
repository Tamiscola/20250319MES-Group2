package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.dto.ProductionLineDTO;
import org.example.projects.repository.ProductionLineRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductionLineService {
    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<ProductionLineDTO> getAllLines(Pageable pageable){
        Page<ProductionLine> LinePage = productionLineRepository.findAll(pageable);
        return LinePage.map(line->{
            ProductionLineDTO dto = ProductionLineDTO.fromEntity(line);
            return dto;
        });
    }

    public List<ProductionLine> getAllLinesAsEntity(){
        return productionLineRepository.findAll();
    }

    public ProductionLine getProductionLineByCode(String code) {
        return productionLineRepository.findByProductionLineCode(code).orElse(null);
    }

    @Transactional
    public void createProductionLine(ProductionLineDTO dto) throws IOException{

        ProductionLine line = new ProductionLine();
        line.setProductionLineName(dto.getProductionLineName());
        line.setLocation(dto.getLocation());
        line.setManager(dto.getManager());
        line.setCapacity(dto.getCapacity());
        line.setEquipment(dto.getEquipment());
        line.setTodayQty(dto.getTodayQty());
        line.setAchievedQty(dto.getAchievedQty());
        line.setProductionLineStatus(dto.getProductionLineStatus());

        productionLineRepository.save(line);
    }

    @Transactional
    public void modifyProductionLine(ProductionLineDTO dto) throws IOException{
        ProductionLine line = productionLineRepository.findByProductionLineCode(dto.getProductionLineCode())
                .orElseThrow(() -> new RuntimeException("Production line not found"));

        line.setProductionLineName(dto.getProductionLineName());
        line.setLocation(dto.getLocation());
        line.setManager(dto.getManager());
        line.setCapacity(dto.getCapacity());
        line.setEquipment(dto.getEquipment());
        line.setTodayQty(dto.getTodayQty());
        line.setAchievedQty(dto.getAchievedQty());
        line.setProductionLineStatus(dto.getProductionLineStatus());

        productionLineRepository.save(line);
    }

    /*// 생산계획 삭제 기능
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
    }*/
}
