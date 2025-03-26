package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.dto.ProductionLineDTO;
import org.example.projects.dto.ProductionPlanDTO;
import org.example.projects.service.ProductionLineService;
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
@RequestMapping("/line")
public class ProductionLineController {
    private final ProductionLineService productionLineService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(defaultValue = "productionLineCode") String sort,
                       @RequestParam(defaultValue = "DESC") String direction,
                       @PageableDefault(size = 10) Pageable pageable){

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<ProductionLineDTO> lineDTOPage = productionLineService.getAllLines(sortedPageable);
        model.addAttribute("lines", lineDTOPage);
        return "production-line";
    }

    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
    @PostMapping("/create")
    public String createLine(@ModelAttribute ProductionLineDTO productionLineDTO) {
        try {
            productionLineService.createProductionLine(productionLineDTO);
            return "redirect:/line/list";
        } catch (Exception e) {
            log.error("Error creating production plan", e);
            return "error";
        }
    }

    @GetMapping("/getline/{code}")
    @ResponseBody
    public ResponseEntity<ProductionLineDTO> getLine(@PathVariable String code) {
        ProductionLine productionLine = productionLineService.getProductionLineByCode(code);
        ProductionLineDTO lineDTO = ProductionLineDTO.fromEntity(productionLine);
        return ResponseEntity.ok(lineDTO);
    }

    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
    @PostMapping("/modify")
    public String modifyLine(@RequestBody ProductionLineDTO productionLineDTO) {
        try {
            productionLineService.modifyProductionLine(productionLineDTO);
            return "redirect:/line/list";
        } catch (Exception e) {
            log.error("Error modifying production line", e);
            return "error";
        }
    }

}
