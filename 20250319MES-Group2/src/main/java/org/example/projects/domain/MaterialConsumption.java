package org.example.projects.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.enums.ProcessType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MaterialConsumption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material; // Material being consumed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", nullable = false)
    private ProductionPlan productionPlan; // Production plan under which material was consumed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_data_id", nullable = false)
    private ProductionData productionData; // ProductionData associated with this consumption

    private Integer quantityUsed; // Quantity of material used

    @Enumerated(EnumType.STRING)
    private ProcessType processType; // Process type where material was used

    private LocalDateTime consumptionDate; // Date and time of consumption

    private Double consumptionCost; // Cost of consumed materials (quantity * unit price)
}
