package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.*;
import org.example.projects.domain.Process;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.TaskType;
import org.example.projects.repository.*;
import org.example.projects.service.ManufacturingSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProductionDataRepository productionDataRepository;

    @GetMapping("/list")
    public String list(Model model) {
        List<ProductionLine> lines = productionLineRepository.findAll();
        model.addAttribute("productionLines", lines);
        return "line-monitoring";
    }

    @Transactional(readOnly = true)
    @GetMapping("/api/lines/progress/all")
    @ResponseBody
    public List<Map<String, Object>> getAllLinesProgress() {
        List<ProductionLine> lines = productionLineRepository.findAll();

        return lines.stream().map(line -> {
            // Get the first production plan for this line
            ProductionPlan plan = line.getProductionPlans().stream()
                    .findFirst()
                    .orElse(null);

            PlanStatus planStatus = plan != null ?
                    plan.getPlanStatus() :
                    PlanStatus.STANDBY;

            Map<String, Object> progressData = new HashMap<>();
            progressData.put("productionLineCode", line.getProductionLineCode());
            progressData.put("productionLineName", line.getProductionLineName());
            progressData.put("planStatus", planStatus.name());
            progressData.put("capacity", line.getCapacity());
            progressData.put("progress", calculateOverallProgressForLine(line));
            progressData.put("todayQty", calculateTodayProduction(line));

            // Include current process and task details
            Process currentProcess = line.getProductionProcesses().stream()
                    .filter(p -> !p.isCompleted())
                    .findFirst()
                    .orElse(null);

            Task currentTask = null;
            if (currentProcess != null) {
                currentTask = currentProcess.getTasks().stream()
                        .filter(t -> !t.isCompleted())
                        .findFirst()
                        .orElse(null);
            }

            progressData.put("currentProcessType", currentProcess != null ? currentProcess.getProcessType().name() : "N/A");
            progressData.put("currentTaskType", currentTask != null ? currentTask.getTaskType().name() : "N/A");
            progressData.put("currentTaskProgress", currentTask != null ? currentTask.getProgress() : 0);

            return progressData;
        }).collect(Collectors.toList());
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
    @Transactional(readOnly = true)
    @GetMapping("/api/lines/progress/{lineCode}")
    @ResponseBody
    public Map<String, Object> getLineProgress(@PathVariable String lineCode) {
        ProductionLine line = productionLineRepository.findById(lineCode)
                .orElseThrow(() -> new RuntimeException("Production line not found"));

        double progress = calculateOverallProgressForLine(line);
        log.info("Calculated progress for line {}: {}%", lineCode, progress);

        // Find the current process and task
        Process currentProcess = line.getProductionProcesses().stream()
                .filter(p -> !p.isCompleted())
                .findFirst()
                .orElse(null);

        Task currentTask = null;
        if (currentProcess != null) {
            currentTask = currentProcess.getTasks().stream()
                    .filter(t -> !t.isCompleted())
                    .findFirst()
                    .orElse(null);
        }

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("productionLineCode", line.getProductionLineCode());
        progressData.put("productionLineName", line.getProductionLineName());
        progressData.put("status", line.getProductionLineStatus());
        progressData.put("capacity", line.getCapacity());
        progressData.put("progress", progress);
        progressData.put("todayQty", calculateTodayProduction(line));
        progressData.put("currentProcessType", currentProcess != null ? currentProcess.getProcessType().name() : "N/A");
        progressData.put("currentTaskType", currentTask != null ? currentTask.getTaskType().name() : "N/A");
        progressData.put("currentTaskProgress", currentTask != null ? currentTask.getProgress() : 0);

        return progressData;
    }

    // Helper method to calculate overall progress for a production line
    private double calculateOverallProgressForLine(ProductionLine line) {
        line = productionLineRepository.findById(line.getProductionLineCode())
                .orElseThrow(() -> new RuntimeException("Production line not found"));

        List<Process> processes = line.getProductionProcesses();

        if (processes == null || processes.isEmpty()) {
            simulator.createProcessesForLine(line);
            processes = line.getProductionProcesses(); // Get newly created processes
        }

        double totalProgress = 0.0;
        int totalTasks = 0;

        for (Process process : processes) {
            List<Task> tasks = process.getTasks();
            totalTasks += tasks.size();  // Count actual existing tasks
            log.info("Processing process: {}", process.getProcessType());
            for (Task task : tasks) {
                log.info("Task type: {}, Current progress: {}", task.getTaskType(), task.getProgress());
                totalProgress += task.getProgress(); // Add task progress directly
            }
        }

        if (totalTasks == 0) {
            return 0.0; // Avoid division by zero
        }

        // Calculate the average progress across all tasks
        double overallProgress = (totalProgress / totalTasks);
        log.info("Overall progress calculation: totalProgress={}, totalTasks={}, result={}%",
                totalProgress, totalTasks, overallProgress);
        return Math.min(overallProgress, 100.0);  // Ensure maximum 100%
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

    @PostMapping("/reset")
    public ResponseEntity<String> resetProgress() {
        try {
            // Reset all production plans
            List<ProductionPlan> plans = productionPlanRepository.findAll();
            plans.forEach(plan -> {
                plan.setPlanStatus(PlanStatus.STANDBY);
                plan.setEndDate(null);
            });
            productionPlanRepository.saveAll(plans);

            // Reset all tasks
            List<Task> tasks = taskRepository.findAll();
            tasks.forEach(task -> {
                task.setProgress(0);
                task.setCompleted(false);
            });
            taskRepository.saveAll(tasks);

            // Reset all processes
            List<Process> processes = processRepository.findAll();
            processes.forEach(process -> {
                process.setProgress(0);
                process.setCompleted(false);
            });
            processRepository.saveAll(processes);

            // Reset all production lines
            List<ProductionLine> lines = productionLineRepository.findAll();
            lines.forEach(line -> line.setProgress(0.0));
            productionLineRepository.saveAll(lines);

            // Delete incomplete production results
            productionDataRepository.deleteByStatus(PlanStatus.IN_PROGRESS);

            return ResponseEntity.ok("All progress has been reset.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset progress.");
        }
    }
}



