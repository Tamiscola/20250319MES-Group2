package org.example.projects.dto;

import lombok.*;
import org.example.projects.domain.ProductionData;
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

    private String productionLineName;

    private String productName;

    private Integer plannedQuantity;

    private Integer actualQuantity;

    private Double yieldRate;

    private PlanStatus status;

    private LocalDate startTime;

    private LocalDate endTime;

    private LocalDate resultDate;

    public static ProductionDataDTO fromEntity(ProductionData entity) {
        return ProductionDataDTO.builder()
                .productionResultId(entity.getProductionResultId())
                .productionLineName(entity.getProductionLine().getProductionLineName())
                .productName(entity.getProduct().getProductName()) // Map product name from Product entity
                .plannedQuantity(entity.getPlannedQuantity())
                .actualQuantity(entity.getActualQuantity())
                .yieldRate(entity.getYieldRate())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .resultDate(entity.getResultDate())
                .build();
    }
}

