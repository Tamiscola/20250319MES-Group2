package org.example.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.Task;
import org.example.projects.domain.enums.TaskType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private Long id;
    private TaskType taskType;
    private boolean completed;
    private int progress;

    public static TaskDTO fromEntity(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .taskType(task.getTaskType())
                .completed(task.isCompleted())
                .progress(task.getProgress())
                .build();
    }
}

