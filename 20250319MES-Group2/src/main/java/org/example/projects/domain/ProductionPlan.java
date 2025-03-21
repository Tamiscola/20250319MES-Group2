package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "production_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "production_plan_id")
    private Long planId;

    @OneToMany(mappedBy = "productionPlan", cascade = CascadeType.ALL)
    private List<ProductionLine> productionLines;

    @OneToMany(mappedBy = "productionPlan", cascade = CascadeType.ALL)
    private List<Product> products;

    private String productName;  // 제품명
    private LocalDate startDate; // 생산 시작일
    private LocalDate endDate;   // 생산 종료일
    private Integer targetQty;   // 목표 생산 수량
    private String manager;      // 생산 담당자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;   // 생산 우선순위 (LOW, NORMAL, HIGH)

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", nullable = false)
    private PlanStatus planStatus;  // 생산계획 현황 (STANDBY, IN_PROGRESS, COMPLETED)

    @Column(name = "file_url")
    private String fileUrl;      // 파일 경로 (파일 업로드 후 경로 저장)
}


