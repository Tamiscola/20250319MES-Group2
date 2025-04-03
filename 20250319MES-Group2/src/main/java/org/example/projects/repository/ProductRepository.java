package org.example.projects.repository;

import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.repository.search.ProductSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String>, ProductSearch {
    Optional<Product> findByProductId(String productId);

    Optional<Product> findFirstByProductName(String productName);

    Optional<Product> findByProductionPlan(ProductionPlan productionPlan);
}
