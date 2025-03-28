package org.example.projects.service;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.*;
import org.example.projects.domain.Process;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.example.projects.domain.enums.TaskType;
import org.example.projects.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Log4j2
@Component
public class ManufacturingSimulator {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ProductionPlanService productionPlanService;

    private void resetTasks(ProductionLine line) {
        for (Process process : line.getProductionProcesses()) {
            for (Task task : process.getTasks()) {
                task.setCompleted(false);
                task.setProgress(0);
                taskRepository.save(task);
            }
        }
        log.info("Reset all tasks for production line {}", line.getProductionLineCode());
    }


    // Update task progress incrementally
    public void updateTaskProgress(Task task) {
        log.info("Updating progress for task: {}, current progress: {}%", task.getTaskType(), task.getProgress());

        Random random = new Random();
        int increment = random.nextInt(10) + 1; // Increment between 1-10%
        int newProgress = Math.min(task.getProgress() + increment, 100);

        log.info("Increment: {}%, New progress: {}%", increment, newProgress);

        task.setProgress(newProgress);
        taskRepository.save(task);

        if (newProgress == 100) {
            log.info("Task {} reached 100%. Marking as completed.", task.getTaskType());
            task.setCompleted(true);
            taskRepository.save(task);
        }
    }

    // Simulate production plan execution
    @Async("simulationExecutor")
    @Transactional
    public void simulateProductionPlan(Long planId) {
        ProductionPlan plan = productionPlanService.getProductionPlanWithAssociations(planId);
        productionPlanRepository.save(plan);
        Product product = initializeProduct(plan);

        boolean simulationCompleted = false;

        for (ProductionLine line : plan.getProductionLines()) {
            // Reset existing processes and tasks for the line
            resetProcessesAndTasks(line);

            log.info("Processing production line: {}", line.getProductionLineCode());

            // Always create new processes for the line
            createProcessesForLine(line);

            // Reload the line to get the updated processes
            line = productionLineRepository.findById(line.getProductionLineCode()).orElseThrow();

            List<Process> processes = line.getProductionProcesses();
            log.info("Number of processes for line {}: {}", line.getProductionLineCode(), processes.size());

            plan.setPlanStatus(PlanStatus.IN_PROGRESS);
            productionPlanRepository.save(plan);

            for (Process process : processes) {
                log.info("Process phase: {}", process.getProcessType());

                // Skip already completed processes
                if (process.isCompleted()) {
                    log.info("Process {} already completed, skipping.", process.getProcessType());
                    continue;
                }

                // Check for COMPLETED process type
                if (process.getProcessType() == ProcessType.COMPLETED) {
                    log.info("Reached COMPLETED process type. Finalizing production.");
                    Task finalVerificationTask = process.getTasks().get(0);
                    finalizeProductQuantity(product, plan);
                    finalVerificationTask.setCompleted(true);
                    process.setCompleted(true);
                    simulationCompleted = true;
                    break;
                }

                log.info("Number of tasks for process {}: {}", process.getProcessType(), process.getTasks().size());

                for (Task task : process.getTasks()) {
                    log.info("Starting task: {}", task.getTaskType());
                    log.info("Initial task completion status: {}", task.isCompleted());
                    log.info("Initial task progress: {}%", task.getProgress());

                    // Force the loop to run at least once
                    do {
                        updateTaskProgress(task);
                        process.setProgress(calculateProcessProgress(process));
                        updateProductQuantity(product, plan, task);

                        log.info("Task {} progress after update: {}%", task.getTaskType(), task.getProgress());

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("Simulation interrupted", e);
                        }
                    } while (!task.isCompleted());

                    log.info("Task {} completed. Moving to next task.", task.getTaskType());
                    taskRepository.save(task);
                }

                process.setCompleted(true);
                processRepository.save(process);
                log.info("Process {} is Completed", process.getProcessType());
            }

            // Break the main loop if simulation is completed
            if (simulationCompleted) {
                break;
            }
        }

        // Finalize the production
        plan.setPlanStatus(PlanStatus.COMPLETED);
        plan.setEndDate(LocalDate.now());
        productionPlanRepository.save(plan);

        finalizeProductQuantity(product, plan);
    }

    private void resetProcessesAndTasks(ProductionLine line) {
        // Delete existing tasks
        List<Task> existingTasks = taskRepository.findByProcessProductionLine(line);
        taskRepository.deleteAll(existingTasks);

        // Delete existing processes
        List<Process> existingProcesses = processRepository.findByProductionLine(line);
        processRepository.deleteAll(existingProcesses);

        // Clear the reference in the production line
        line.setProductionProcesses(new ArrayList<>());
        productionLineRepository.save(line);

        log.info("Reset processes and tasks for production line: {}", line.getProductionLineCode());
    }

    private void createProcessesForLine(ProductionLine line) {
        List<Process> newProcesses = new ArrayList<>();

        // Create processes for each process type
        for (ProcessType processType : ProcessType.values()) {
            Process process = new Process();
            process.setProcessType(processType);
            process.setProductionLine(line);
            process.setCompleted(false);
            process.setProgress(0);

            // Create tasks for this process
            List<TaskType> tasksForProcess = TaskType.getTasksForProcess(process);
            List<Task> processTasks = tasksForProcess.stream()
                    .map(taskType -> {
                        Task task = new Task();
                        task.setTaskType(taskType);
                        task.setProcess(process);
                        task.setCompleted(false);
                        task.setProgress(0);
                        return task;
                    })
                    .collect(Collectors.toList());

            process.setTasks(processTasks);
            newProcesses.add(process);
        }

        // Save all new processes (and cascading tasks)
        processRepository.saveAll(newProcesses);

        // Update the production line with new processes
        line.setProductionProcesses(newProcesses);
        productionLineRepository.save(line);

        log.info("Created {} processes for production line: {}",
                newProcesses.size(), line.getProductionLineCode());
    }

    private Product initializeProduct(ProductionPlan plan) {
        Product product = Product.builder()
                .productName(plan.getProductName())
                .productionPlan(plan)
                .productionLine(plan.getProductionLines().iterator().next()) // Assuming one line for simplicity
                .productStatus(Status.NORMAL)
                .manufacturedDate(LocalDate.now())
                .regBy(plan.getManager())
                .quantity(0) // Start with 0 quantity
                .build();
        return productRepository.save(product);
    }

    private void updateProductQuantity(Product product, ProductionPlan plan, Task task) {
        int incrementAmount = (int) (plan.getTargetQty() * (task.getProgress() - task.getProgress()) / 100.0);
        product.setQuantity(product.getQuantity() + incrementAmount);
        productRepository.save(product);
        log.info("Updated product quantity. Current quantity: {}", product.getQuantity());
    }

    private void finalizeProductQuantity(Product product, ProductionPlan plan) {
        product.setQuantity(plan.getTargetQty());  // Ensure final quantity matches target
        productRepository.save(product);
        log.info("Finalized product quantity. Final quantity: {}", product.getQuantity());
    }

    // Helper method to calculate overall progress for a process
    private int calculateProcessProgress(Process process) {
        return (int) process.getTasks().stream()
                .mapToDouble(Task::getProgress)
                .average()
                .orElse(0.0);
    }

    // Create products based on the production plan
    private void createProducts(ProductionPlan plan) {
        Product product = Product.builder()
                .productName(plan.getProductName())
                .productionPlan(plan)
                .productionLine(plan.getProductionLines().iterator().next()) // Assuming one line for simplicity
                .productStatus(Status.NORMAL)
                .manufacturedDate(LocalDate.now())
                .regBy(plan.getManager())
                .quantity(plan.getTargetQty())  // Set the quantity to the target quantity
                .build();

        productRepository.save(product);

        log.info("Created product batch for production plan {}. Quantity: {}", plan.getPlanId(), plan.getTargetQty());
    }
}
