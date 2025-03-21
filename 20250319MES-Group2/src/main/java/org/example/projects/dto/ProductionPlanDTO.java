package org.example.projects.dto;

import lombok.Builder;
import lombok.Data;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ProductionPlanDTO {
    private Long planId;
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer targetQty;
    private String manager;
    private Priority priority;
    private PlanStatus planStatus;
    private String fileUrl;
    private List<String> productionLineNames;
    private List<String> productNames;

    public static ProductionPlanDTO fromEntity(ProductionPlan productionPlan) {
        return ProductionPlanDTO.builder()
                .planId(productionPlan.getPlanId())
                .productName(productionPlan.getProductName())
                .startDate(productionPlan.getStartDate())
                .endDate(productionPlan.getEndDate())
                .targetQty(productionPlan.getTargetQty())
                .manager(productionPlan.getManager())
                .priority(productionPlan.getPriority())
                .planStatus(productionPlan.getPlanStatus())
                .fileUrl(productionPlan.getFileUrl())
                .productionLineNames(productionPlan.getProductionLines().stream()
                        .map(ProductionLine::getProductionLineName)
                        .collect(Collectors.toList()))
                .productNames(productionPlan.getProducts().stream()
                        .map(Product::getProductName)
                        .collect(Collectors.toList()))
                .build();
    }
}


