//package org.example.projects.domain;
//
//import lombok.*;
//import org.example.projects.domain.enums.TaskType;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "tasks")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@ToString(exclude = "process")
//public class Task {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)      // 한 Process 안 많은 Task들
//    @JoinColumn(name = "process_id", nullable = false)
//    private Process process;
//
//    @Column(nullable = false)
//    private TaskType taskType;
//
//    private boolean completed;
//
//    @Column(nullable = false, columnDefinition = "int default 0")
//    private int progress;
//}
