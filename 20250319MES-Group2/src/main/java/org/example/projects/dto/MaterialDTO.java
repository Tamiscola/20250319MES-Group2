package org.example.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.Material;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDTO {
    private Long mId;            // 자재 ID
    private String mName;           // 자재명
    private String mCategory;       // 카테고리
    private ProcessType mProcess;        // 소요부문
    private Integer mQuantity;          // 수량
    private Double mPrice;          // 가격
    private Status mStatus;    // 재고 상태 (NORMAL, DEFECTED)

    public static MaterialDTO fromEntity(Material material){
        return MaterialDTO.builder()
                .mId(material.getMId())
                .mName(material.getMName())
                .mCategory(material.getMCategory())
                .mProcess(material.getMProcess())
                .mQuantity(material.getMQuantity())
                .mPrice(material.getMPrice())
                .mStatus(material.getMStatus())
                .build();
    }
}