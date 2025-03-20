package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.ProcessType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "processes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "productionLine")
public class Process {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProcessType processType;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)    // Process 안에 여러개의 task (task1, task2, task3 task4)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_code", nullable = false)
    private ProductionLine productionLine;
}
