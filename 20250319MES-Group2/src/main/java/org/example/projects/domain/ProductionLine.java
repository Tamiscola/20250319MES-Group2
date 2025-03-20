package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.Status;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "production_lines")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "productionPlan")
public class ProductionLine {
    @Id     // Primary Key
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "CHAR(36)", name = "production_line_code")
    @Type(type = "uuid-char")
    private String productionLineCode;    // 생산라인 코드(고유, 기본키)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status productionLineStatus;    // 생산라인상태 (정상(Normal), 불량(Defected))

    @Column(nullable = false, name = "production_line_name")
    private String productionLineName;  // 생산라인명
    private String location;    // 생산라인 위치
    private String manager;     // 라인 담당자
    private Integer capacity;   // 생산라인 용량(개/시간)
    private String equipment;       // 생산설비

    @OneToMany(mappedBy = "production_line", cascade = CascadeType.ALL)
    private List<Process> productionProcesses;      // 생산공정 단계

    @OneToMany(mappedBy = "production_line", cascade = CascadeType.ALL)
    private List<Product> products;       // 생산품목

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", nullable = false)
    private ProductionPlan productionPlan;        // 생산계획 (ex: 월 목표수량)

    @Column(columnDefinition = "LOCALDATE DEFAULT CURRENT_TIMESTAMP")
    private LocalDate regDate;      // 등록 일자
}
