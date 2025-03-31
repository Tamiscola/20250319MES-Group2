package org.example.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.Status;

import java.util.List;
import java.util.stream.Collectors;
import org.example.projects.domain.enums.Status;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductionLineDTO {
    private String productionLineCode;
    private String productionLineName;
    private Status productionLineStatus;
    private String location;
    private String manager;
    private Integer capacity;
    private String equipment;
    private Integer achievedQty;
    private Integer todayQty;
    private Integer targetQty;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate regDate;

    public static ProductionLineDTO fromEntity(ProductionLine productionLine) {
        return ProductionLineDTO.builder()
                .productionLineCode(productionLine.getProductionLineCode())
                .productionLineName(productionLine.getProductionLineName())
                .productionLineStatus(productionLine.getProductionLineStatus())
                .location(productionLine.getLocation())
                .manager(productionLine.getManager())
                .capacity(productionLine.getCapacity())
                .equipment(productionLine.getEquipment())
                .achievedQty(productionLine.getAchievedQty())
                .todayQty(productionLine.getTodayQty())
                .targetQty(productionLine.getTargetQty())
                .regDate(productionLine.getRegDate())
                .build();
    }
}


