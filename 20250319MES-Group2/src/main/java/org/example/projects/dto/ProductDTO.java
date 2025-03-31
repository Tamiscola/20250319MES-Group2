package org.example.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.Product;
import org.example.projects.domain.enums.Status;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String productId;
    private String productName;
    private Status productStatus;
    private LocalDate manufacturedDate;
    private String regBy;
    private LocalDate regDate;
    private String productionLineCode;
    private Long productionPlanId;

    public static ProductDTO fromEntity(Product product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productStatus(product.getProductStatus())
                .manufacturedDate(product.getManufacturedDate())
                .regBy(product.getRegBy())
                .regDate(product.getRegDate())
                .productionLineCode(product.getProductionLine().getProductionLineCode())
                .productionPlanId(product.getProductionPlan().getPlanId())
                .build();
    }
}

