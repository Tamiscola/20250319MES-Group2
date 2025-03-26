package org.example.projects.controller;

import org.example.projects.domain.ProductionLine;
import org.example.projects.service.ProductionLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProductionLineController {
    @Autowired
    private ProductionLineService productionLineService;

    @GetMapping("/production-lines")
    public String showProductionLines(Model model) {
        List<ProductionLine> productionLines = productionLineService.getAllProductionLines();
        model.addAttribute("productionLines", productionLines);
        return "line-monitoring";
    }
}
