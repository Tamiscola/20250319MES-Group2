package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Material;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.MaterialDTO;
import org.example.projects.service.MaterialService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/material")
public class MaterialController {
    private final MaterialService materialService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(defaultValue = "mId") String sort,
                       @RequestParam(defaultValue = "DESC") String direction,
                       @PageableDefault(size = 10) Pageable pageable) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<MaterialDTO> materialDTOPage = materialService.getAllMaterials(sortedPageable);

        model.addAttribute("materials", materialDTOPage);

        return "material";
    }

//    @PreAuthorize("hasRoles('USER')")
    @PostMapping("/create")
    public String createMaterial(@ModelAttribute MaterialDTO materialDTO) {
        try {
            materialService.createMaterial(materialDTO);
            return "redirect:/material/list";
        } catch (Exception e) {
            log.error("Error creating material");
            return "error";
        }
    }

    @GetMapping("/getmaterial/{id}")
    @ResponseBody
    public ResponseEntity<MaterialDTO> getMaterial(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        MaterialDTO materialDTO = MaterialDTO.fromEntity(material);
        log.info("Fetched materialDTO: {}", materialDTO); // 로그 추가
        return ResponseEntity.ok(materialDTO);
    }

//    @PreAuthorize("hasRoles('USER')")
    @PostMapping("/modify")
    public String modifyMaterial(@ModelAttribute MaterialDTO materialDTO){
        log.info("Received materialDTO: {}", materialDTO);  // ID와 기타 필드 로그 출력
        try{
            materialService.modifyMaterial(materialDTO);
            return "redirect:/material/list";
        } catch (Exception e){
            log.error("Error modifying material", e);
            return "error";
        }
    }

//    @PreAuthorize("hasRoles('USER')")
    @PostMapping("/delete/{id}")
    public String deleteMaterial(@PathVariable Long id){
        try {
            materialService.deleteMaterial(id);
            return "redirect:/material/list";
        }catch(Exception e){
            log.error("Error deleting material", e);
            return "error";
        }
    }

    @GetMapping("/search")
    public String searchMaterials(@RequestParam(required = false) String mProcess,
                                  @RequestParam(required = false) String mStatus,
                                  @RequestParam(defaultValue = "mId") String sort,
                                  @RequestParam(defaultValue = "DESC") String direction,
                                  @PageableDefault(size = 10) Pageable pageable,
                                  Model model){

        Status statusEnum = null;

        if (mStatus != null && !mStatus.isEmpty()) {
            try {
                statusEnum = Status.valueOf(mStatus);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Invalid status value");
            }
        }

        ProcessType processEnum = null;

        if (mProcess != null && !mProcess.isEmpty()) {
            try {
                processEnum = ProcessType.valueOf(mProcess);
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Invalid process value");
            }
        }

        // Create a properly sorted pageable
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Page<MaterialDTO> searchResult = materialService.searchMaterials(mProcess, mStatus, sortedPageable);

        model.addAttribute("materials", searchResult);
        model.addAttribute("selectedMProcess", mProcess);
        model.addAttribute("selectedMStatus", mStatus);
        return "material";

    }
}
