package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.ProductionLineDTO;
import org.example.projects.service.ProductionLineService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

        List<String> productionLineNames = productionLineService.getAllProductionLineName();

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<ProductionLineDTO> lineDTOPage = productionLineService.getAllLines(sortedPageable);

        model.addAttribute("productionLineNames", productionLineNames);
        model.addAttribute("lines", lineDTOPage);

        return "production-line";
    }

//    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
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

//    @PreAuthorize("hasRole('USER')") // ROLE_USER인가(권한)을 가진 사람만 사용 가능
    @PostMapping("/modify")
    public String modifyLine(@ModelAttribute ProductionLineDTO productionLineDTO) {
        try {
            productionLineService.modifyProductionLine(productionLineDTO);
            return "redirect:/line/list";
        } catch (Exception e) {
            log.error("Error modifying production line", e);
            return "error";
        }
    }

//    @PreAuthorize("hasRole('USER')")
    @PostMapping("/delete/{code}")
    public String deletedLine(@PathVariable String code){
        try{
            productionLineService.deleteProductionLine(code);
            return "redirect:/line/list";
        }catch (Exception e){
            log.error("Error modifying production line", e);
            return "error";
        }
    }

    @GetMapping("/search")
    public String searchLines(@RequestParam(required = false, defaultValue = "") String productionLineName,
                              @RequestParam(required = false, defaultValue = "") String productionLineStatus,
                              @RequestParam(required = false)
                              @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate regDate,
                              @RequestParam(defaultValue = "productionLineCode") String sort,
                              @RequestParam(defaultValue = "DESC") String direction,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {

        Status statusEnum = null;

        if (productionLineStatus != null && !productionLineStatus.isEmpty()) {
            try {
                statusEnum = Status.valueOf(productionLineStatus);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Invalid status value");
            }
        }

        // 정렬 설정
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        // service에서 데이터를 가져옴
        Page<ProductionLineDTO> searchResult = productionLineService.searchLines(productionLineName, productionLineStatus, regDate, sortedPageable);

        // DB에서 모든 생산라인 이름 목록을 가져옵니다.
        List<String> productionLineNames = productionLineService.getAllProductionLineName();

        // 뷰에 전달할 데이터 설정
        model.addAttribute("lines", searchResult);
        model.addAttribute("productionLineNames", productionLineNames); // 생산라인 이름 목록 // 드롭다운에 사용할 생산라인 목록
        model.addAttribute("selectedProductionLineName", productionLineName);
        model.addAttribute("selectedProductionLineStatus", productionLineStatus);
        model.addAttribute("selectedRegDate", regDate);

        return "production-line";  // JSP 또는 Thymeleaf 뷰 이름
    }


}
