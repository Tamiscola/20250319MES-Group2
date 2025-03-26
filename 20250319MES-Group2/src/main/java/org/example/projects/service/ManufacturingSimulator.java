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
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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

    // Update task progress
    public void updateTaskProgress(Task task) {
        log.info("Starting updateTaskProgress for task type: {}, current progress: {}",
                task.getTaskType(), task.getProgress());

        Random random = new Random();
        int increment = random.nextInt(50) + 1; // More controlled increment
        int newProgress = Math.min(task.getProgress() + increment, 100);

        log.info("Increment: {}, New progress: {}", increment, newProgress);

        task.setProgress(newProgress);
        taskRepository.save(task);

        if (newProgress == 100) {
            log.info("Task reached 100%. Marking as completed.");
            task.setCompleted(true);
            taskRepository.save(task);
        }
    }

    // Simulate production plan execution
    public void simulateProductionPlan(ProductionPlan plan) {
        productionPlanRepository.save(plan);
        Product product = initializeProduct(plan);

        for (ProductionLine line : plan.getProductionLines()) {
            List<Process> processes = line.getProductionProcesses();

            for (Process process : processes) {
                log.info("Process phase : {}", process.getProcessType());
                plan.setPlanStatus(PlanStatus.IN_PROGRESS);
                productionPlanRepository.save(plan);

                for (Task task : process.getTasks()) {
                    while (!task.isCompleted()) {
                        updateTaskProgress(task);
                        process.setProgress(calculateProcessProgress(process));

                        // Update product quantity based on task progress
                        updateProductQuantity(product, plan, task);

                        if (task.isCompleted()) {
                            log.info("Task {} completed. Moving to next task.", task.getTaskType());
                            break;
                        }

                        try {
                            Thread.sleep(1000);  // Simulate time passing
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("Simulation interrupted", e);
                        }
                    }
                }
                process.setCompleted(true);
                processRepository.save(process);
                log.info("Process {} is Completed", process.getProcessType());
            }
        }

        plan.setPlanStatus(PlanStatus.COMPLETED);
        plan.setEndDate(LocalDate.now());
        productionPlanRepository.save(plan);

        // Ensure final product quantity matches target quantity
        finalizeProductQuantity(product, plan);
    }

    private Product initializeProduct(ProductionPlan plan) {
        Product product = Product.builder()
                .productName(plan.getProductName())
                .productionPlan(plan)
                .productionLine(plan.getProductionLines().iterator().next()) // Assuming one line for simplicity
                .productStatus(Status.NORMAL)
                .manufacturedDate(LocalDate.now())
                .regBy(plan.getManager())
                .quantity(0)  // Start with 0 quantity
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


    // Calculate process progress
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

