package org.example.projects.dto;

import lombok.Builder;
import lombok.Data;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ProductionPlanDTO {
    private Long productionId;       // 생산계획 ID
    private Integer targetQty;       // 목표 생산 수량
    private String manager;          // 생산 담당자
    private Priority priority;       // 생산 우선순위
    private PlanStatus planStatus;   // 생산계획 현황
    private String fileUrl;          // 파일 경로 (if relevant to the use case)
    private List<String> productionLineNames; // List of production line names
    private List<String> productNames; // List of product names

    public static ProductionPlanDTO toDTO(ProductionPlan productionPlan) {
        return ProductionPlanDTO.builder()
                .productionId(productionPlan.getProductionId())
                .targetQty(productionPlan.getTargetQty())
                .manager(productionPlan.getManager())
                .priority(productionPlan.getPriority())
                .planStatus(productionPlan.getPlanStatus())
                .fileUrl(productionPlan.getFileUrl())
                .productionLineNames(productionPlan.getProductionLines().stream()
                        .map(ProductionLine::getProductionLineName)
                        .collect(Collectors.toList()))
                .productNames(productionPlan.getProducts().stream()
                        .map(Product::getProductName) // Assuming Product has a getName() method
                        .collect(Collectors.toList()))
                .build();
    }
}
