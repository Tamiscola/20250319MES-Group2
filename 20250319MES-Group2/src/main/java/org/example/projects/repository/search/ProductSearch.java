package org.example.projects.repository.search;

import org.example.projects.domain.Product;
import org.example.projects.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearch {
    Page<Product> searchAll(String[] types, String keyword, Pageable pageable);
}
