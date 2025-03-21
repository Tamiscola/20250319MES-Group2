package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.dto.ProductionPlanDTO;
import org.example.projects.service.ProductionPlanService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/plan")
public class ProductionPlanController {
    private final ProductionPlanService productionPlanService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 10, sort = "planId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductionPlanDTO> planDTOPage = productionPlanService.getAllPlans(pageable);
        model.addAttribute("plans", planDTOPage);
        return "production-plan";
    }

    @PostMapping("/create")
    public String createPlan(@ModelAttribute ProductionPlanDTO productionPlanDTO,
                             @RequestParam("productionLineName") String productionLineName,
                             @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            productionPlanService.createProductionPlan(productionPlanDTO, productionLineName, file);
            return "redirect:/plan/list";
        } catch (Exception e) {
            log.error("Error creating production plan", e);
            return "error";
        }
    }

    @GetMapping("/getplan/{id}")
    @ResponseBody
    public ResponseEntity<ProductionPlanDTO> getPlan(@PathVariable Long id) {
        ProductionPlan productionPlan = productionPlanService.getProductionPlanById(id);
        ProductionPlanDTO planDTO = ProductionPlanDTO.fromEntity(productionPlan);
        return ResponseEntity.ok(planDTO);
    }

    @PostMapping("/modify")
    public String modifyPlan(@ModelAttribute ProductionPlanDTO productionPlanDTO,
                             @RequestParam("productionLineName") String productionLineName,
                             @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            productionPlanService.modifyProductionPlan(productionPlanDTO, productionLineName, file);
            return "redirect:/plan/list";
        } catch (Exception e) {
            log.error("Error modifying production plan", e);
            return "error";
        }
    }
}

