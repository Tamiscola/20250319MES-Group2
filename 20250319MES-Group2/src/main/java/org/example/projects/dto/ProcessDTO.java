package org.example.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.projects.domain.Process;
import org.example.projects.domain.enums.ProcessType;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDTO {
    private Long id;
    private ProcessType processType;
    private List<TaskDTO> tasks;
    private String productionLineCode;

    public static ProcessDTO fromEntity(Process process) {
        return ProcessDTO.builder()
                .id(process.getId())
                .processType(process.getProcessType())
                .tasks(process.getTasks().stream().map(TaskDTO::fromEntity).collect(Collectors.toList()))
                .productionLineCode(process.getProductionLine().getProductionLineCode())
                .build();
    }
}


