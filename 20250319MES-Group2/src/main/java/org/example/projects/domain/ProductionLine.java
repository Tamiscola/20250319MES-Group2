package org.example.projects.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "production_lines")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLine {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "production_line_code", columnDefinition = "CHAR(36)")
    private String productionLineCode;    // 생산라인 코드(고유, 기본키)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "production_line_status")
    private Status productionLineStatus;    // 생산라인상태 (정상(Normal), 불량(Defected))

    @Transient // Marks this as a derived field (not persisted in DB)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public PlanStatus getPlanStatus() {
        if (productionPlans == null || productionPlans.isEmpty()) {
            return PlanStatus.STANDBY;
        }
        // Get the first plan's status (assuming one active plan per line)
        return productionPlans.stream()
                .findFirst()
                .map(ProductionPlan::getPlanStatus)
                .orElse(PlanStatus.STANDBY);
    }

    @Column(nullable = false, name = "production_line_name")
    private String productionLineName;  // 생산라인명
    private String location;    // 생산라인 위치
    private String manager;     // 라인 담당자
    private Integer capacity;   // 생산라인 용량(개/시간)
    private String equipment;       // 생산설비

    @Transient  // Marks this as non-persistent
    public Integer getTargetQty() {
        return productionPlans.stream()
                .map(ProductionPlan::getTargetQty)
                .reduce(0, Integer::sum);  // Sum up all target quantities from associated plans
    }

    private Integer achievedQty;   // 달성 수량
    private Integer todayQty;   // 금일 생산 수량

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Process> productionProcesses;      // 생산공정 단계

    @Enumerated(EnumType.STRING)
    private ProcessType processType;

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL)
    private List<Product> products;       // 생산품목

    @ManyToMany(mappedBy = "productionLines")
    @Builder.Default
    private Set<ProductionPlan> productionPlans = new HashSet<>();  // 생산계획 (ex: 월 목표수량)

    @Builder.Default
    private LocalDate regDate = LocalDate.now();    // 등록 일자

    private double progress;  // 진행률
}
