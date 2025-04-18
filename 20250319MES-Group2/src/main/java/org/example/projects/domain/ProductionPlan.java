package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private Long planId;    // 생산계획 ID

    @ManyToMany
    @JoinTable(
            name = "production_plan_line",
            joinColumns = @JoinColumn(name = "production_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "production_line_code")
    )
    @Builder.Default
    private Set<ProductionLine> productionLines = new HashSet<>();   // 생산라인

    @ManyToMany
    @JoinTable(
            name = "production_plan_product",
            joinColumns = @JoinColumn(name = "production_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> products = new HashSet<>();

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
