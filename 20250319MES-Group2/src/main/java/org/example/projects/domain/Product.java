package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.Status;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id     // Primary Key
    @GeneratedValue(generator = "uuid3")
    @GenericGenerator(name = "uuid3", strategy = "uuid2")
    @Column(columnDefinition = "CHAR(36)", name = "product_id")
    @Type(type = "uuid-char")
    private String productId;

    @Column(nullable = false)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)      // 한 Process 안 많은 Task들
    @JoinColumn(name = "production_line_code", nullable = false)
    private ProductionLine productionLine;

    @Enumerated(EnumType.STRING)
    private Status productStatus;

    private LocalDate manufacturedDate;

    @Column(nullable = false)
    private String regBy;

    @Column(columnDefinition = "LOCALDATE DEFAULT CURRENT_TIMESTAMP")
    private LocalDate regDate;
}
