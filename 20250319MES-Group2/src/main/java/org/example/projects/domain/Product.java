package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.Status;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private ProductionData productionData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_code")
    private ProductionLine productionLine;

    @ManyToMany(mappedBy = "products", fetch = FetchType.EAGER)
    @Builder.Default
    private Set<ProductionPlan> productionPlans = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Status productStatus;

    private LocalDate manufacturedDate;

    @Column(nullable = false)
    private String regBy;

    @Builder.Default
    private LocalDate regDate = LocalDate.now();

    @Column(nullable = true)
    private Integer quantity = 0;

    public void change(String productName,
                        String regBy,
                        LocalDate regDate
    ) {
        this.productName = productName;
        this.regBy = regBy;
        this.regDate = regDate;
    }
}
