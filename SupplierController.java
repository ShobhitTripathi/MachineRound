package com.getourguide.interview.controller;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
import com.getourguide.interview.service.search.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class SupplierController {
    
    @Qualifier("supplierSearchService")
    private final SearchService<Supplier, SupplierDto, SupplierSearchCriteria> searchService;
    
    /**
     * Get all suppliers
     * GET /suppliers
     */
    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        return ResponseEntity.ok(searchService.findAll());
    }
    
    /**
     * Get supplier by ID
     * GET /suppliers/{id}
     */
    @GetMapping("/suppliers/{id}")
    public ResponseEntity<SupplierDto> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(searchService.findById(id));
    }
    
    /**
     * Search suppliers with query parameters
     * GET /suppliers/search?name=X&city=Y&country=Z&zip=P&address=A
     * 
     * Example requests:
     * - /suppliers/search?city=Berlin
     * - /suppliers/search?country=Germany&city=Munich
     * - /suppliers/search?name=Tours&city=Berlin
     * 
     * All parameters are optional
     */
    @GetMapping("/suppliers/search")
    public ResponseEntity<List<SupplierDto>> searchSuppliers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country) {
        
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .name(name)
                .address(address)
                .zip(zip)
                .city(city)
                .country(country)
                .build();
        
        return ResponseEntity.ok(searchService.search(criteria));
    }
}
