package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.ProductDTO;
import org.example.projects.dto.pageDTO.PageRequestDTO;
import org.example.projects.dto.pageDTO.PageResponseDTO;
import org.example.projects.repository.ProductRepository;
import org.example.projects.repository.ProductionLineRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public String productRegister(ProductDTO productDTO) {
        Product product = Product.builder()
                .productName(productDTO.getProductName())
                .regBy(productDTO.getRegBy())
                .regDate(LocalDate.now())
                .quantity(0)
                .build();

        String productName = productRepository.save(product).getProductName();

        return productName;
    }

    @Transactional
    public void productModify(ProductDTO productDTO) {
        Optional<Product> result = productRepository.findByProductId(productDTO.getProductId());

        Product product = result.orElseThrow();
        log.info("Product Retrieved: " + product);

        product.change(productDTO.getProductName(),
                productDTO.getRegBy(),
                productDTO.getRegDate()
        );

        productRepository.save(product);
    }

    @Transactional
    public boolean productRemove(String productId) {
        // Fetch the product by ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        // Check if the product is associated with any production plans
        if (!product.getProductionPlans().isEmpty()) {
            // Return false to indicate that deletion is not allowed
            return false;
        }

        // If no associations exist, delete the product
        productRepository.deleteById(productId);
        return true;
    }

    @Transactional
    public PageResponseDTO<ProductDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("productCode");

        Page<Product> result = productRepository.searchAll(types, keyword, pageable);

        List<ProductDTO> dtoList = result.getContent().stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    log.info("매핑된 ProductDTO: {}", dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<ProductDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    public Product getProductById(String id) {
        Product product = productRepository.findById(id).orElse(null);

        return product;
    }
}
