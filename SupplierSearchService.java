package com.getourguide.interview.service.search;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
import com.getourguide.interview.repository.SupplierRepository;
import com.getourguide.interview.specification.SupplierSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Search service implementation for Supplier entity.
 * Implements SearchService with specific types.
 */
@Service
@AllArgsConstructor
public class SupplierSearchService implements SearchService<Supplier, SupplierDto, SupplierSearchCriteria> {
    
    private final SupplierRepository supplierRepository;
    
    @Override
    public List<SupplierDto> search(SupplierSearchCriteria criteria) {
        // Build specification from criteria
        Specification<Supplier> spec = SupplierSpecifications.fromCriteria(criteria);
        
        // Execute query (database-level filtering)
        List<Supplier> suppliers = supplierRepository.findAll(spec);
        
        // Map to DTOs
        return mapToDtoList(suppliers);
    }
    
    @Override
    public SupplierDto findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        return mapToDto(supplier);
    }
    
    @Override
    public List<SupplierDto> findAll() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return mapToDtoList(suppliers);
    }
    
    /**
     * Map single Supplier entity to DTO
     * Private helper - keeps mapping logic in one place
     */
    private SupplierDto mapToDto(Supplier supplier) {
        return SupplierDto.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .address(supplier.getAddress())
                .zip(supplier.getZip())
                .city(supplier.getCity())
                .country(supplier.getCountry())
                .build();
    }
    
    /**
     * Map list of entities to DTOs
     */
    private List<SupplierDto> mapToDtoList(List<Supplier> suppliers) {
        return suppliers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
