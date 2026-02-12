package com.getourguide.interview.specification;

import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for Supplier entity.
 * Each method returns a reusable, composable Specification.
 */
public class SupplierSpecifications {
    
    /**
     * Filter by supplier name (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                cb.lower(root.get("name")),
                "%" + name.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by address (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasAddress(String address) {
        return (root, query, cb) -> {
            if (address == null || address.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                cb.lower(root.get("address")),
                "%" + address.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by zip code (exact match)
     */
    public static Specification<Supplier> hasZip(String zip) {
        return (root, query, cb) -> {
            if (zip == null || zip.trim().isEmpty()) {
                return null;
            }
            return cb.equal(root.get("zip"), zip.trim());
        };
    }
    
    /**
     * Filter by city (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                cb.lower(root.get("city")),
                "%" + city.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by country (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasCountry(String country) {
        return (root, query, cb) -> {
            if (country == null || country.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                cb.lower(root.get("country")),
                "%" + country.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Combine all criteria from SupplierSearchCriteria object
     * Uses Spring Data JPA 3.5+ Specification.allOf() approach
     */
    public static Specification<Supplier> fromCriteria(SupplierSearchCriteria criteria) {
        return Specification.allOf(
            hasName(criteria.getName()),
            hasAddress(criteria.getAddress()),
            hasZip(criteria.getZip()),
            hasCity(criteria.getCity()),
            hasCountry(criteria.getCountry())
        );
    }
}
