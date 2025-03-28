package org.example.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlanDTO {
    private Long planId;
    private String productName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Integer targetQty;
    private String manager;
    private Priority priority;
    private PlanStatus planStatus;
    private String fileUrl;
    private Set<String> productionLineNames;
    private Set<String> productNames;

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
                .productNames(productionPlan.getProducts().stream()
                        .map(Product::getProductName)
                        .collect(Collectors.toSet()))
                .productionLineNames(productionPlan.getProductionLines().stream()
                        .map(ProductionLine::getProductionLineName)
                        .collect(Collectors.toSet()))
                .build();
    }
}


