package org.example.projects.repository.search;

import lombok.RequiredArgsConstructor;
import org.example.projects.domain.Product;
import org.example.projects.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchImpl implements ProductSearch {
    private final EntityManager entityManager;

    @Override
    public Page<Product> searchAll(String[] types, String keyword, Pageable pageable) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE p.productId IS NOT NULL");
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasType = types != null && types.length > 0;

        if (hasKeyword && hasType) {
            jpql.append(" AND (");
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                switch (type) {
                    case "n":
                        jpql.append("p.productName LIKE :keyword");
                        break;
                    case "r":
                        jpql.append("p.regBy LIKE :keyword");
                        break;
                }
                if (i < types.length - 1) {
                    jpql.append(" OR ");
                }
            }
            jpql.append(")");
        }

        jpql.append(" ORDER BY p.productName ASC");

        System.out.println("Generated JPQL: " + jpql);
        System.out.println("Keyword parameter: " + keyword);

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(
                jpql.toString().replace("SELECT p", "SELECT COUNT(p)"), Long.class);

        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword + "%");
            countQuery.setParameter("keyword", "%" + keyword + "%"); // 추가
        }


        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Product> list = query.getResultList();
        long count = countQuery.getSingleResult();

        return new PageImpl<>(list, pageable, count);
    }
}
