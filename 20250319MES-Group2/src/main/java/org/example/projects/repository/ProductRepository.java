package org.example.projects.repository;

import org.example.projects.domain.Product;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.repository.search.ProductSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String>, ProductSearch {
    Optional<Product> findByProductId(String productId);

    Optional<Product> findFirstByProductName(String productName);

    @Query("SELECT p FROM Product p JOIN p.productionPlans pp WHERE p.productName = :productName AND pp = :plan")
    Optional<Product> findByNameAndPlan(@Param("productName") String productName, @Param("plan") ProductionPlan plan);

    // 시뮬레이션으로 생선된 Product instance들 product-list.html에서 제외하기
    @Query("SELECT p FROM Product p WHERE p.manufacturedDate IS NULL OR p.manufacturedDate < :simulationStartDate")
    List<Product> findArchivedProducts(@Param("simulationStartDate") LocalDate simulationStartDate);

}
