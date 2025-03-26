package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.Process;
import org.example.projects.domain.Task;
import org.example.projects.domain.enums.Status;
import org.example.projects.repository.ProductionLineRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.example.projects.service.ManufacturingSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @GetMapping("/list")
    public String list(Model model) {
        List<ProductionLine> lines = productionLineRepository.findAll();
        model.addAttribute("productionLines", lines);
        return "line-monitoring";
    }

    // Simulate production for a specific production line
    @GetMapping("/simulate/{productionLineCode}")
    public String simulateProductionLine(@PathVariable String productionLineCode) {
        log.info("Simulating production for line with code: {}", productionLineCode);

        ProductionLine line = productionLineRepository.findById(productionLineCode)
                .orElseThrow(() -> new RuntimeException("Production line not found"));
        log.info("Production line found: {} : {}", productionLineCode, line);

        // Simulate production for the given line
        simulator.simulateProductionPlan(line.getProductionPlans().iterator().next());
        log.info("Simulation completed");

        return "redirect:/monitor/list";
    }

    // API to get progress of all production lines
    @GetMapping("/api/lines/progress")
    @ResponseBody
    public List<Map<String, Object>> getLinesProgress() {
        List<ProductionLine> lines = productionLineRepository.findAll();
        return lines.stream()
                .map(line -> {
                    Map<String, Object> progressData = new HashMap<>();
                    progressData.put("productionLineCode", line.getProductionLineCode());
                    progressData.put("productionLineName", line.getProductionLineName());
                    progressData.put("status", line.getProductionLineStatus());
                    progressData.put("capacity", line.getCapacity());

                    // Calculate overall progress for the line based on its processes and tasks
                    double overallProgress = calculateOverallProgressForLine(line);
                    progressData.put("progress", overallProgress);

                    return progressData;
                })
                .collect(Collectors.toList());
    }

    // Helper method to calculate overall progress for a production line
    private double calculateOverallProgressForLine(ProductionLine line) {
        List<Process> processes = line.getProductionProcesses();

        if (processes == null || processes.isEmpty()) {
            return 0.0;
        }

        int totalTasks = 0;
        double totalProgress = 0.0;

        for (Process process : processes) {
            for (Task task : process.getTasks()) {
                totalTasks++;
                totalProgress += task.getProgress();
            }
        }

        if (totalTasks == 0) {
            return 0.0;
        }

        return (totalProgress / (totalTasks * 100)) * 100; // Return percentage progress
    }
}



