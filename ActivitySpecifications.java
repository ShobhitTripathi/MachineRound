package com.getourguide.interview.specification;

import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.entity.Activity;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for Activity entity.
 * Each method returns a reusable, composable Specification.
 *
 * Demonstrates:
 * - Specification Pattern for dynamic query building
 * - Open/Closed Principle (add criteria without modifying existing code)
 * - Single Responsibility Principle (each method handles one criterion)
 */
public class ActivitySpecifications {

    /**
     * Filter by title (partial match, case-insensitive)
     */
    public static Specification<Activity> hasTitle(String title) {
        return (root, query, cb) -> {
            if (title == null || title.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + title.toLowerCase().trim() + "%"
            );
        };
    }

    /**
     * Filter by price range
     */
    public static Specification<Activity> hasPriceRange(Integer minPrice, Integer maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            }
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("price"), minPrice, maxPrice);
            }
            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    /**
     * Filter by minimum rating
     */
    public static Specification<Activity> hasMinRating(Double minRating) {
        return (root, query, cb) -> {
            if (minRating == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("rating"), minRating);
        };
    }

    /**
     * Filter by currency (exact match, case-insensitive)
     */
    public static Specification<Activity> hasCurrency(String currency) {
        return (root, query, cb) -> {
            if (currency == null || currency.trim().isEmpty()) {
                return null;
            }
            return cb.equal(
                    cb.lower(root.get("currency")),
                    currency.toLowerCase().trim()
            );
        };
    }

    /**
     * Filter by special offer flag
     */
    public static Specification<Activity> hasSpecialOffer(Boolean specialOffer) {
        return (root, query, cb) -> {
            if (specialOffer == null) {
                return null;
            }
            return cb.equal(root.get("specialOffer"), specialOffer);
        };
    }

    /**
     * Filter by supplier name (requires JOIN)
     * This demonstrates relationship traversal in specifications
     */
//    public static Specification<Activity> hasSupplierName(String supplierName) {
//        return (root, query, cb) -> {
//            if (supplierName == null || supplierName.trim().isEmpty()) {
//                return null;
//            }
//            Join<Activity, Supplier> supplierJoin = root.join("supplier", JoinType.LEFT);
//            return cb.like(
//                    cb.lower(supplierJoin.get("name")),
//                    "%" + supplierName.toLowerCase().trim() + "%"
//            );
//        };
//    }

    /**
     * Combine all criteria from SearchCriteria object
     * This is the main entry point used by the service
     */
    public static Specification<Activity> fromCriteria(ActivitySearchCriteria criteria) {
        return Specification
                .allOf(hasTitle(criteria.getTitle()))
                .and(hasPriceRange(criteria.getMinPrice(), criteria.getMaxPrice()))
                .and(hasMinRating(criteria.getMinRating()))
                .and(hasCurrency(criteria.getCurrency()))
                .and(hasSpecialOffer(criteria.getSpecialOffer()));
//                .and(hasSupplierName(criteria.getSupplierName()));
    }
}
