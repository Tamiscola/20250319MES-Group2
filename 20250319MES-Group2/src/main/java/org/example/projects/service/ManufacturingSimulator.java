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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
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

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ProductionDataRepository productionDataRepository;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
    public void simulateProductionPlan(Long planId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // Use AtomicReference to hold objects that need to be accessed outside the transaction
        AtomicReference<ProductionPlan> planRef = new AtomicReference<>();
        AtomicReference<Product> productRef = new AtomicReference<>();

        transactionTemplate.execute(new TransactionCallback<Void>() {
            public Void doInTransaction(TransactionStatus status) {
                ProductionPlan plan = productionPlanService.getProductionPlanWithAssociations(planId);
                productionPlanRepository.save(plan);
                Product product = initializeProduct(plan);
                planRef.set(plan);
                productRef.set(product);
                return null;
            }
        });

        ProductionPlan plan = planRef.get();
        Product product = productRef.get();

        boolean simulationCompleted = false;

        for (ProductionLine line : plan.getProductionLines()) {
            // Reset existing processes and tasks for the line
            resetProcessesAndTasks(line);

            log.info("Processing production line: {}", line.getProductionLineCode());

            // Always create new processes for the line
            createProcessesForLine(line);

            // Reload the line to get the updated processes
            transactionTemplate.execute(new TransactionCallback<Void>() {
                public Void doInTransaction(TransactionStatus status) {
                    resetProcessesAndTasks(line);
                    createProcessesForLine(line);
                    return null;
                }
            });

            // Reload the line to get the updated processes
            ProductionLine updatedLine = transactionTemplate.execute(new TransactionCallback<ProductionLine>() {
                public ProductionLine doInTransaction(TransactionStatus status) {
                    return productionLineRepository.findById(line.getProductionLineCode()).orElseThrow();
                }
            });

            List<Process> processes = updatedLine.getProductionProcesses();

            transactionTemplate.execute(new TransactionCallback<Void>() {
                public Void doInTransaction(TransactionStatus status) {
                    plan.setPlanStatus(PlanStatus.IN_PROGRESS);
                    productionPlanRepository.save(plan);
                    return null;
                }
            });

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

                    // Process the verification task normally
                    do {
                        updateTaskProgress(finalVerificationTask);
                        process.setProgress(calculateProcessProgress(process));
                        processRepository.save(process);
                        updateProductQuantity(product, plan, finalVerificationTask);
                        taskRepository.save(finalVerificationTask);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("Simulation interrupted", e);
                        }
                    } while (!finalVerificationTask.isCompleted());

                    finalizeProductQuantity(product, plan);
                    process.setCompleted(true);
                    simulationCompleted = true;
                    // Remove the break statement to ensure process completion is saved
                    processRepository.save(process);
                }

                log.info("Number of tasks for process {}: {}", process.getProcessType(), process.getTasks().size());

                for (Task task : process.getTasks()) {
                    log.info("Starting task: {}", task.getTaskType());
                    log.info("Initial task completion status: {}", task.isCompleted());
                    log.info("Initial task progress: {}%", task.getProgress());

                    // Force the loop to run at least once
                    do {
                        transactionTemplate.execute(new TransactionCallback<Void>() {
                            public Void doInTransaction(TransactionStatus status) {
                                updateTaskProgress(task);
                                process.setProgress(calculateProcessProgress(process));
                                processRepository.save(process);
                                updateProductQuantity(product, plan, task);
                                taskRepository.save(task);
                                return null;
                            }
                        });

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

                transactionTemplate.execute(new TransactionCallback<Void>() {
                    public Void doInTransaction(TransactionStatus status) {
                        process.setCompleted(true);
                        processRepository.save(process);
                        return null;
                    }
                });
                log.info("Process {} is Completed", process.getProcessType());
            }

            // Break the main loop if simulation is completed
            if (simulationCompleted) {
                break;
            }
        }

        // Finalize the production
        transactionTemplate.execute(new TransactionCallback<Void>() {
            public Void doInTransaction(TransactionStatus status) {
                plan.setPlanStatus(PlanStatus.COMPLETED);
                plan.setEndDate(LocalDate.now());
                productionPlanRepository.save(plan);
                finalizeProductQuantity(product, plan);
                return null;
            }
        });
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

    public void createProcessesForLine(ProductionLine line) {
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
        product.setQuantity(plan.getTargetQty());
        productRepository.save(product);
        log.info("Finalized product quantity. Final quantity: {}", product.getQuantity());

        // Create ProductionResult
        ProductionData result = ProductionData.builder()
                .productionLine(product.getProductionLine())
                .productionPlan(plan)
                .productName(product.getProductName())
                .plannedQuantity(plan.getTargetQty()) // Target Quantity
                .actualQuantity(product.getQuantity()) // Actual, post-simulation
                .yieldRate((double) product.getQuantity() / plan.getTargetQty() * 100) // Rate
                .status(plan.getPlanStatus()) // Copy status from plan
                .startTime(plan.getStartDate())
                .endTime(LocalDate.now())   // Mark as completed now
                .build();

        productionDataRepository.save(result);

        log.info("Created production result: {}", result);
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
