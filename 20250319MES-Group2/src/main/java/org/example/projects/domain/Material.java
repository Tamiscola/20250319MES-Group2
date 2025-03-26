package org.example.projects.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mId;            // 자재 ID

    private String mName;           // 자재명

    private String mCategory;       // 카테고리

    @Enumerated(EnumType.STRING)
    private ProcessType mProcess;        // 소요부문

    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer mQuantity;          // 수량

    private Double mPrice;          // 가격

    private Status mStatus;    // 재고 상태 (NORMAL, DEFECTED)
}