package org.example.projects.domain;

import javax.persistence.*;
import lombok.*;
import org.example.projects.domain.enums.PlanStatus;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;

@Entity
@Table(name = "production_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductionData {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "production_result_id", columnDefinition = "CHAR(36)")
    private String productionResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_code", nullable = false)
    private ProductionLine productionLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", nullable = false)
    private ProductionPlan productionPlan;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer plannedQuantity;

    @Column(nullable = false)
    private Integer actualQuantity;

    private Double yieldRate;

    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    private LocalDate startTime;
    private LocalDate endTime;

    @Builder.Default
    private LocalDate resultDate = LocalDate.now();
}

