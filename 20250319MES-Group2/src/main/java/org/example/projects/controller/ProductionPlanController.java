package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.dto.ProductionPlanDTO;
import org.example.projects.service.ProductionPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/plan")
public class ProductionPlanController {
    private final ProductionPlanService productionPlanService;

    @GetMapping("/productionplan")
    public String list(Model model, @PageableDefault(size = 10, sort = "planId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductionPlan> planPage = productionPlanService.getAllPlans(pageable);
        Page<ProductionPlanDTO> planDTOPage = planPage.map(ProductionPlanDTO::fromEntity);
        model.addAttribute("plans", planDTOPage);
        return "production-plan";
    }
}

