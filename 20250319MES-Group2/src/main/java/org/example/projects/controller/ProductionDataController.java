package org.example.projects.controller;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionData;
import org.example.projects.service.ProductionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Log4j2
@RequestMapping("/data")
public class ProductionDataController {
    @Autowired
    private ProductionDataService productionDataService;

    @GetMapping("/list")
    public String list(Model model) {
        List<ProductionData> results = productionDataService.getAllProductionResults();
        model.addAttribute("results", results);
        return "production-data";
    }
}
