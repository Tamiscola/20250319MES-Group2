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
    @DisplayName("Should save dummy products to the database")
    void saveDummyProducts() {
        // Given
        List<Product> products = List.of(
                Product.builder()
                        .productName("14nm FinFET Chip")
                        .productStatus(Status.NORMAL)
                        .manufacturedDate(LocalDate.of(2025, 4, 1))
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .build(),
                Product.builder()
                        .productName("3nm High-Performance Computing Chip")
                        .productStatus(Status.NORMAL)
                        .manufacturedDate(LocalDate.of(2025, 4, 2))
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .build(),
                Product.builder()
                        .productName("10nm IoT Sensor Chip")
                        .productStatus(Status.NORMAL)
                        .manufacturedDate(LocalDate.of(2025, 4, 3))
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .build(),
                Product.builder()
                        .productName("7nm Automotive SoC")
                        .productStatus(Status.NORMAL)
                        .manufacturedDate(LocalDate.of(2025, 4, 4))
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .build(),
                Product.builder()
                        .productName("5nm AI Processor")
                        .productStatus(Status.NORMAL)
                        .manufacturedDate(LocalDate.of(2025, 4, 5))
                        .regBy("admin")
                        .regDate(LocalDate.now())
                        .build()
        );

        // When
        productRepository.saveAll(products);

        // Then
        List<Product> savedProducts = productRepository.findAll();
        Assertions.assertThat(savedProducts).hasSize(products.size());
    }
}
