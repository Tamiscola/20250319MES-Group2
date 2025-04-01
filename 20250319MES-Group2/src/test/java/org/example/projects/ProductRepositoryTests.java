package org.example.projects;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Product;
import org.example.projects.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Log4j2
public class ProductRepositoryTests {
    @Autowired
    private ProductRepository productRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Prevent rollback for testing purposes
    public void createProducts() {
        List<String> productNames = List.of(
                "14nm FinFET Chip",
                "5nm AI Processor",
                "7nm Automotive SoC",
                "3nm High-Performance Computing Chip",
                "10nm IoT Sensor Chip"
        );

        productNames.forEach(name -> {
            Product product = Product.builder()
                    .productName(name)
                    .regBy("Admin") // Replace with actual user or system registering the product
                    .regDate(LocalDate.now())
                    .quantity(100) // Set default quantity (required field)
                    .build();

            productRepository.save(product);
            log.info("Saved product: {}", product);
        });

        // Verify saved products
        List<Product> savedProducts = productRepository.findAll();
        log.info("Total products saved: {}", savedProducts.size());
        savedProducts.forEach(product -> log.info("Product: {}", product));
    }
}
