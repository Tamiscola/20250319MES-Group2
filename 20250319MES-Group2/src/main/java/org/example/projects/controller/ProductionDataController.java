package org.example.projects.controller;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionLine;
import org.example.projects.dto.ProductionDataDTO;
import org.example.projects.repository.ProductRepository;
import org.example.projects.repository.ProductionLineRepository;
import org.example.projects.service.ProductionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@Log4j2
@RequestMapping("/data")
public class ProductionDataController {
    @Autowired
    private ProductionDataService productionDataService;

    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) String productionLineName,
                       @RequestParam(required = false) String productName,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                       @RequestParam(required = false) String status,
                       @PageableDefault(size = 10) Pageable pageable,
                       Model model) {
        List<ProductionLine> productionLines = productionLineRepository.findAll();
        List<Product> products = productRepository.findAll();

        Page<ProductionDataDTO> results = productionDataService.filterProductionData(
                productionLineName, productName, startDate, endDate, status, pageable);

        model.addAttribute("productionLines", productionLines);
        model.addAttribute("products", products);
        model.addAttribute("results", results);
        return "production-data";
    }
}
