package org.example.projects.domain;

import lombok.*;
import org.example.projects.domain.enums.TaskType;

import javax.persistence.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "process")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @Builder.Default
    private boolean completed = false;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int progress = 0;
}
