package org.example.projects;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.example.projects.domain.Product;
import org.example.projects.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.example.projects.domain.enums.Status;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Log4j2
public class ProductRepositoryTests {
    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Should successfully save products with dummy data")
    void shouldSaveDummyProducts() {
        // Given: Dummy data for products
        List<Product> dummyProducts = List.of(
                Product.builder()
                        .productName("14nm FinFET Chip")
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .productStatus(Status.NORMAL)
                        .build(),
                Product.builder()
                        .productName("3nm High-Performance Computing Chip")
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .productStatus(Status.NORMAL)
                        .build(),
                Product.builder()
                        .productName("10nm IoT Sensor Chip")
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .productStatus(Status.NORMAL)
                        .build(),
                Product.builder()
                        .productName("7nm Automotive SoC")
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .productStatus(Status.NORMAL)
                        .build(),
                Product.builder()
                        .productName("5nm AI Processor")
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .productStatus(Status.NORMAL)
                        .build()
        );

        // When: Save all dummy products
        productRepository.saveAll(dummyProducts);

        // Then: Verify the products are saved successfully
        Assertions.assertThat(productRepository.findAll())
                .hasSize(dummyProducts.size())
                .extracting(Product::getProductName)
                .containsExactlyInAnyOrder(
                        "14nm FinFET Chip",
                        "3nm High-Performance Computing Chip",
                        "10nm IoT Sensor Chip",
                        "7nm Automotive SoC",
                        "5nm AI Processor"
                );
    }
}
