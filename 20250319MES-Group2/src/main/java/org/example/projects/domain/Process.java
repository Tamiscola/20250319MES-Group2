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
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "productionLine")
@Builder
public class Process {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProcessType processType;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)    // Process 안에 여러개의 task (task1, task2, task3 task4)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_code")
    private ProductionLine productionLine;

    @Builder.Default
    private boolean completed = false;

    @Column(nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int progress = 0;
}
