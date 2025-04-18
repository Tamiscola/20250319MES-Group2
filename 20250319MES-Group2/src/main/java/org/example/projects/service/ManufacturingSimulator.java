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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private MaterialService materialService;

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
        int increment = random.nextInt(40) + 50; // Increment between 1-10%
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
        AtomicReference<ProductionPlan> planRef = new AtomicReference<>();
        AtomicReference<Product> productRef = new AtomicReference<>();
        AtomicReference<ProductionData> productionDataRef = new AtomicReference<>();

        // STEP 1: Load the production plan
        transactionTemplate.execute(new TransactionCallback<Void>() {
            public Void doInTransaction(TransactionStatus status) {
                ProductionPlan plan = productionPlanService.getProductionPlanWithAssociations(planId);
                planRef.set(plan);
                return null;
            }
        });

        // STEP 2: Initialize and save the product properly first
        transactionTemplate.execute(new TransactionCallback<Void>() {
            public Void doInTransaction(TransactionStatus status) {
                ProductionPlan plan = planRef.get();
                Product product = initializeProduct(plan);

                // Save product first to get ID
                product = productRepository.saveAndFlush(product);

                // Now create and set up the production data
                ProductionData productionData = ProductionData.builder()
                        .product(product)
                        .productionPlan(plan)
                        .productionLine(plan.getProductionLines().iterator().next()) // Or however you select the line
                        .productName(product.getProductName())
                        .plannedQuantity(plan.getTargetQty())
                        .actualQuantity(0)
                        .status(PlanStatus.IN_PROGRESS)
                        .startTime(LocalDate.now())
                        .totalMaterialCost(0.0) // Initialize cost tracking
                        .unitCost(0.0)
                        .materialConsumptions(new ArrayList<>()) // Initialize with empty list
                        .build();

                // Set up bidirectional relationship
                product.setProductionData(productionData);

                // Save production data
                productionData = productionDataRepository.save(productionData);

                // Update the references
                productRef.set(product);
                productionDataRef.set(productionData);
                return null;
            }
        });

        ProductionPlan plan = planRef.get();
        Product product = productRef.get();
        ProductionData productionData = productionDataRef.get();

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

                // Consume materials for this process type
                transactionTemplate.execute(new TransactionCallback<Void>() {
                    public Void doInTransaction(TransactionStatus status) {
                        // Consume materials at the beginning of each process
                        if (process.getProcessType() != ProcessType.COMPLETED) {
                            double processCost = materialService.consumeMaterialsForProcess(productionData, process.getProcessType());
                            log.info("Consumed materials for process {} with cost: {}", process.getProcessType(), processCost);
                        }
                        return null;
                    }
                });

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
                // Update product cost with total material costs
                materialService.updateProductionCost(productionData);

                plan.setPlanStatus(PlanStatus.COMPLETED);
                plan.setEndDate(LocalDate.now());
                productionPlanRepository.save(plan);

                Product product = productRef.get();
                finalizeProductQuantity(product, plan);
                log.info("Production finalized for plan: {}", plan.getPlanId());
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
        // Get the first product associated with this plan
        final Product initialProduct = plan.getProducts().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No product associated with plan: " + plan.getPlanId()));

        // Ensure we have the latest version from the database
        Product product = productRepository.findById(initialProduct.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + initialProduct.getProductId()));

        // Reset quantity for simulation
        product.setQuantity(0);
        product.setManufacturedDate(LocalDate.now());

        return product;
    }

    private void updateProductQuantity(Product product, ProductionPlan plan, Task task) {
        int incrementAmount = (int) (plan.getTargetQty() * (task.getProgress() - task.getProgress()) / 100.0);
        product.setQuantity(product.getQuantity() + incrementAmount);
        productRepository.save(product);
        log.info("Updated product quantity. Current quantity: {}", product.getQuantity());
    }

    private void finalizeProductQuantity(Product product, ProductionPlan plan) {
        // Save product if not already persisted
        if (product.getProductId() == null) {
            product = productRepository.saveAndFlush(product); // Use saveAndFlush to ensure persistence
        }

        product.setQuantity(plan.getTargetQty());
        productRepository.save(product); // Persist updated product
        log.info("Finalized product quantity. Final quantity: {}", product.getQuantity());

        // Check if a ProductionResult already exists for this plan
        ProductionData existingResult = productionDataRepository.findByProductionPlan(plan)
                .orElse(null);

        if (existingResult != null) {
            // Update the existing result
            existingResult.setActualQuantity(product.getQuantity());
            existingResult.setYieldRate((double) product.getQuantity() / plan.getTargetQty() * 100);
            existingResult.setStatus(plan.getPlanStatus());
            existingResult.setEndTime(LocalDate.now()); // Update end time if completed
            productionDataRepository.save(existingResult);
            log.info("Updated production result: {}", existingResult);
        } else {
            // Create a new result if none exists
            ProductionData newResult = ProductionData.builder()
                    .productionLine(plan.getProductionLines().iterator().next())
                    .productionPlan(plan)
                    .product(product) // Associate persisted product
                    .productName(product.getProductName())
                    .plannedQuantity(plan.getTargetQty()) // Target Quantity
                    .actualQuantity(product.getQuantity()) // Actual Quantity
                    .yieldRate((double) product.getQuantity() / plan.getTargetQty() * 100) // Yield Rate
                    .status(plan.getPlanStatus()) // Status
                    .startTime(plan.getStartDate())
                    .endTime(LocalDate.now()) // End time
                    .totalMaterialCost(0.0) // Initialize material cost
                    .unitCost(0.0) // Initialize unit cost
                    .materialConsumptions(new ArrayList<>()) // Initialize with empty list
                    .build();

            productionDataRepository.save(newResult);

            log.info("Created production result: {}", newResult);
        }
    }

    // Helper method to calculate overall progress for a process
    private int calculateProcessProgress (Process process){
        return (int) process.getTasks().stream()
                .mapToDouble(Task::getProgress)
                .average()
                .orElse(0.0);
    }
}