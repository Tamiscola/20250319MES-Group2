package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.Status;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "productionLine")
public class Product {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "product_id", columnDefinition = "CHAR(36)")
    private String productId;

    @Column(nullable = false)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_code")
    private ProductionLine productionLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id")
    private ProductionPlan productionPlan;

    @Enumerated(EnumType.STRING)
    private Status productStatus;

    private LocalDate manufacturedDate;

    @Column(nullable = false)
    private String regBy;

    @Builder.Default
    private LocalDate regDate = LocalDate.now();

    @Column(nullable = false)
    private Integer quantity;
}

