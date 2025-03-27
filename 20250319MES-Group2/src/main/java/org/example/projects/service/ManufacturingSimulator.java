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

        for (ProductionLine line : plan.getProductionLines()) {
            resetTasks(line);
            log.info("Processing production line: {}", line.getProductionLineCode());

            if (line.getProductionProcesses() == null || line.getProductionProcesses().isEmpty()) {
                log.info("No processes found for line {}. Creating processes...", line.getProductionLineCode());
                createProcessesForLine(line);
                // Reload the line to get the updated processes and tasks
                line = productionLineRepository.findById(line.getProductionLineCode()).orElseThrow();
            }

            List<Process> processes = line.getProductionProcesses();
            log.info("Number of processes for line {}: {}", line.getProductionLineCode(), processes.size());

            for (Process process : processes) {
                log.info("Process phase: {}", process.getProcessType());
                log.info("Number of tasks for process {}: {}", process.getProcessType(), process.getTasks().size());

                plan.setPlanStatus(PlanStatus.IN_PROGRESS);
                productionPlanRepository.save(plan);

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
                        // Force a refresh of the line to get updated task progress
                        line = productionLineRepository.findById(line.getProductionLineCode()).orElseThrow();

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
        }

        plan.setPlanStatus(PlanStatus.COMPLETED);
        plan.setEndDate(LocalDate.now());
        productionPlanRepository.save(plan);

        finalizeProductQuantity(product, plan);
    }

    // Create processes and tasks for a production line
    private void createProcessesForLine(ProductionLine line) {
        List<Process> processes = new ArrayList<>();

        for (ProcessType processType : ProcessType.values()) {
            if (processType != ProcessType.COMPLETED) {
                Process process = Process.builder()
                        .processType(processType)
                        .productionLine(line)
                        .completed(false)
                        .progress(0)
                        .build();

                process = processRepository.save(process);

                List<Task> tasks = createTasksForProcess(process);
                process.setTasks(tasks);
                processes.add(process);
            }
        }

        line.setProductionProcesses(processes);
        productionLineRepository.save(line);
        log.info("Created {} processes for production line {}", processes.size(), line.getProductionLineCode());
    }

    // Helper method to create tasks for a process
    private List<Task> createTasksForProcess(Process process) {
        List<Task> tasks = new ArrayList<>();
        List<TaskType> taskTypes = TaskType.getTasksForProcess(process);

        for (TaskType taskType : taskTypes) {
            Task task = Task.builder()
                    .taskType(taskType)
                    .process(process)
                    .completed(false)  // Explicitly set to false
                    .progress(0)       // Explicitly set to 0
                    .build();

            task = taskRepository.save(task);
            tasks.add(task);
            log.info("Created task {} for process {} with initial progress 0% and completed status false", taskType, process.getProcessType());
        }

        return tasks;
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

