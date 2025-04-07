package org.example.projects.controller;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Product;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.ProductDTO;
import org.example.projects.dto.pageDTO.PageRequestDTO;
import org.example.projects.dto.pageDTO.PageResponseDTO;
import org.example.projects.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/products")
@Log4j2
@RequiredArgsConstructor
@Data
public class ProductController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        log.info("types: {}", Arrays.toString(pageRequestDTO.getTypes()));  // types 값 확인
        log.info("keyword: {}", pageRequestDTO.getKeyword());  // keyword 값 확인

        PageResponseDTO<ProductDTO> responseDTO = productService.list(pageRequestDTO);

        log.info(responseDTO);

        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        return "product-list";
    }

    @PostMapping("/register")
    public String productRegister(@ModelAttribute @Valid ProductDTO productDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        log.info("product register");

        if(bindingResult.hasErrors()) {
            log.info("has error");

            bindingResult.getAllErrors().forEach(error -> log.info("Validation error: {}", error));

            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/products/list";
        }

        log.info("productDTO: " + productDTO);

        String productName = productService.productRegister(productDTO);

        redirectAttributes.addFlashAttribute("result", productName);

        return "redirect:/products/list";
    }

    @PostMapping("/modify")
    public String productModify(PageRequestDTO pageRequestDTO,
                         @Valid ProductDTO productDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        log.info("product modify post" + productDTO);

        if(bindingResult.hasErrors()) {
            log.info("has errors");

            String link = pageRequestDTO.getLink();

            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            redirectAttributes.addAttribute("productName", productDTO.getProductName());

            return "redirect:/product/modify?"+link;
        }
        productService.productModify(productDTO);

        redirectAttributes.addFlashAttribute("result", "modified");

        redirectAttributes.addAttribute("productName", productDTO.getProductName());

        return "redirect:/products/list";
    }

    @GetMapping("/getproduct/{id}")
    @ResponseBody
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        Product product = productService.getProductById(id);
        ProductDTO productDTO = ProductDTO.fromEntity(product);

        return ResponseEntity.ok(productDTO);
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<String> removeProduct(@RequestParam String productId) {
        boolean isDeleted = productService.productRemove(productId);

        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot delete product. It is associated with one or more production plans.");
        }

        return ResponseEntity.ok("Product deleted successfully.");
    }
}
