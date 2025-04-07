package org.example.projects.dto;

import lombok.*;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Status;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionDataDTO {
    private String productionResultId;

    private ProductionLine productionLine;

    private String productionLineName;

    private Product product;

    private String productName;

    private Integer plannedQuantity;

    private Integer actualQuantity;

    private Double yieldRate;

    private PlanStatus status;

    private LocalDate startTime;

    private LocalDate endTime;

    private LocalDate resultDate;

    private Double totalMaterialCost;

    private Double unitCost;

    public static ProductionDataDTO fromEntity(ProductionData entity) {
        return ProductionDataDTO.builder()
                .productionResultId(entity.getProductionResultId())
                .productionLineName(entity.getProductionLine().getProductionLineName()) // Map production line name
                .productionLine(entity.getProductionLine()) // Map full ProductionLine object
                .productName(entity.getProduct().getProductName()) // Map product name
                .product(entity.getProduct()) // Map full Product object
                .plannedQuantity(entity.getPlannedQuantity())
                .actualQuantity(entity.getActualQuantity())
                .yieldRate(entity.getYieldRate())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .resultDate(entity.getResultDate())
                .totalMaterialCost(entity.getTotalMaterialCost())
                .unitCost(entity.getUnitCost())
                .build();
    }
}

