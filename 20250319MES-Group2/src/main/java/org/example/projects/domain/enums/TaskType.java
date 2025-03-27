package org.example.projects.domain.enums;

import java.util.List;

import org.example.projects.domain.Process;

public enum TaskType {
    // Wafer Preparation tasks
    CLEANING, POLISHING, INSPECTION,

    // Fabrication Process tasks
    OXIDATION, THIN_FILM_DEPOSITION, CHEMICAL_VAPOR_DEPOSITION,
    PHYSICAL_VAPOR_DEPOSITION, ATOMIC_LAYER_DEPOSITION,

    // Photolithography tasks
    PHOTORESIST_APPLICATION, SOFT_BAKING, MASK_ALIGNMENT, EXPOSURE,
    POST_EXPOSURE_BAKE, DEVELOPMENT, HARD_BAKING,

    // Etching tasks
    WET_ETCHING, DRY_ETCHING, REACTIVE_ION_ETCHING, PLASMA_ETCHING, ION_BEAM_ETCHING,

    // Doping tasks
    ION_IMPLANTATION, DIFFUSION, IN_SITU_DOPING,

    // Annealing tasks
    RAPID_THERMAL_ANNEALING, FURNACE_ANNEALING, LASER_ANNEALING,

    // Completion task
    FINAL_PRODUCT_VERIFICATION;

    public static List<TaskType> getTasksForProcess(Process process) {
        if (process == null || process.getProcessType() == null) {
            throw new IllegalArgumentException("Process or ProcessType is null");
        }
        ProcessType processType = process.getProcessType();
        switch (processType) {
            case WAFER_PREPARATION:
                return List.of(CLEANING, POLISHING, INSPECTION);
            case FABRICATION:
                return List.of(OXIDATION, THIN_FILM_DEPOSITION, CHEMICAL_VAPOR_DEPOSITION,
                        PHYSICAL_VAPOR_DEPOSITION, ATOMIC_LAYER_DEPOSITION);
            case PHOTOLITHOGRAPHY:
                return List.of(PHOTORESIST_APPLICATION, SOFT_BAKING, MASK_ALIGNMENT, EXPOSURE,
                        POST_EXPOSURE_BAKE, DEVELOPMENT, HARD_BAKING);
            case ETCHING:
                return List.of(WET_ETCHING, DRY_ETCHING, REACTIVE_ION_ETCHING, PLASMA_ETCHING, ION_BEAM_ETCHING);
            case DOPING:
                return List.of(ION_IMPLANTATION, DIFFUSION, IN_SITU_DOPING);
            case ANNEALING:
                return List.of(RAPID_THERMAL_ANNEALING, FURNACE_ANNEALING, LASER_ANNEALING);
            case COMPLETED:
                return List.of(FINAL_PRODUCT_VERIFICATION);
            default:
                throw new IllegalArgumentException("Unknown process type: " + processType);
        }
    }

    public static boolean belongsToProcess(TaskType taskType, Process process) {
        return getTasksForProcess(process).contains(taskType);
    }
}
