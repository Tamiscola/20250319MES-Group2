package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.dto.ProductionPlanDTO;
import org.example.projects.service.ProductionPlanService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public String list(Model model,
                       @RequestParam(defaultValue = "planId") String sort,
                       @RequestParam(defaultValue = "DESC") String direction,
                       @PageableDefault(size = 10) Pageable pageable) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<ProductionPlanDTO> planDTOPage = productionPlanService.getAllPlans(sortedPageable);
        model.addAttribute("plans", planDTOPage);
        return "production-plan";
    }

    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
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

    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
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

    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
    @PostMapping("/delete/{id}")
    public String deletePlan(@PathVariable Long id) {
        try {
            productionPlanService.deleteProductionPlan(id);
            return "redirect:/plan/list";
        } catch (Exception e) {
            log.error("Error deleting production plan", e);
            return "error";
        }
    }

    @GetMapping("/search")
    public String searchPlans(@RequestParam(required = false, defaultValue = "") String keyword,
                              @RequestParam(required = false, defaultValue = "") String priority,
                              @RequestParam(required = false, defaultValue = "") String status,
                              @RequestParam(defaultValue = "planId") String sort,
                              @RequestParam(defaultValue = "DESC") String direction,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {

        // Create a properly sorted pageable
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<ProductionPlanDTO> searchResult = productionPlanService.searchPlans(keyword, priority, status, sortedPageable);
        model.addAttribute("plans", searchResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedStatus", status);
        return "production-plan";
    }
}