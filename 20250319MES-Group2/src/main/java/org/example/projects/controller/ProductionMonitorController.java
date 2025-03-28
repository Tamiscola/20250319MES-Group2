package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.*;
import org.example.projects.domain.Process;
import org.example.projects.domain.enums.Status;
import org.example.projects.domain.enums.TaskType;
import org.example.projects.repository.ProductionLineRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.example.projects.service.ManufacturingSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/monitor")
public class ProductionMonitorController {
    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ManufacturingSimulator simulator;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @GetMapping("/list")
    public String list(Model model) {
        List<ProductionLine> lines = productionLineRepository.findAll();
        model.addAttribute("productionLines", lines);
        return "line-monitoring";
    }

    @GetMapping("/simulate/{productionLineCode}")
    @ResponseBody
    public ResponseEntity<String> simulateProductionLine(@PathVariable String productionLineCode) {
        log.info("Simulating production for line with code: {}", productionLineCode);

        ProductionLine line = productionLineRepository.findById(productionLineCode)
                .orElseThrow(() -> new RuntimeException("Production line not found"));
        log.info("Production line found: {} : {}", productionLineCode, line);

        // Get the first production plan for this line
        ProductionPlan plan = line.getProductionPlans().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No production plan found for this line"));

        // Start simulation in background thread
        simulator.simulateProductionPlan(plan.getPlanId());

        return ResponseEntity.ok("Simulation started");
    }


    // API to get progress of all production lines
    @GetMapping("/api/lines/progress/{lineCode}")
    @ResponseBody
    public Map<String, Object> getLineProgress(@PathVariable String lineCode) {
        ProductionLine line = productionLineRepository.findById(lineCode)
                .orElseThrow(() -> new RuntimeException("Production line not found"));

        double progress = calculateOverallProgressForLine(line);
        log.info("Calculated progress for line {}: {}%", lineCode, progress);

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("productionLineCode", line.getProductionLineCode());
        progressData.put("productionLineName", line.getProductionLineName());
        progressData.put("status", line.getProductionLineStatus());
        progressData.put("capacity", line.getCapacity());
        progressData.put("progress", calculateOverallProgressForLine(line));
        progressData.put("todayQty", calculateTodayProduction(line));

        return progressData;
    }

    // Helper method to calculate overall progress for a production line
    private double calculateOverallProgressForLine(ProductionLine line) {
        List<Process> processes = line.getProductionProcesses();

        if (processes == null || processes.isEmpty()) {
            return 0.0;
        }

        int totalTasks = TaskType.values().length;
        double totalProgress = 0.0;

        log.info("Total number of task types: {}", totalTasks);

        for (Process process : processes) {
            log.info("Processing process: {}", process.getProcessType());
            for (Task task : process.getTasks()) {
                log.info("Task type: {}, Current progress: {}", task.getTaskType(), task.getProgress());
                totalProgress += task.getProgress(); // Add task progress directly
            }
        }

        if (totalTasks == 0) {
            return 0.0; // Avoid division by zero
        }

        // Calculate the average progress across all tasks
        double overallProgress = totalProgress / totalTasks;
        log.info("Overall progress calculation: totalProgress={}, totalTasks={}, result={}%",
                totalProgress, totalTasks, overallProgress);
        return overallProgress; // Return the average progress as a percentage
    }

    private int calculateTodayProduction(ProductionLine line) {
        int todayProduction = 0;
        LocalDate today = LocalDate.now();

        for (ProductionPlan plan : line.getProductionPlans()) {
            if (plan.getStartDate().isEqual(today) || (plan.getStartDate().isBefore(today) && plan.getEndDate().isAfter(today))) {
                for (Product product : plan.getProducts()) {
                    todayProduction += product.getQuantity();
                }
            }
        }

        return todayProduction;
    }
}


