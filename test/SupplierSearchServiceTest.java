package com.getourguide.interview.service.search;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
import com.getourguide.interview.repository.SupplierRepository;
import com.getourguide.interview.specification.SupplierSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.getourguide.interview.helpers.SupplierHelper.createSupplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SupplierSearchService
 * Tests the service layer in isolation using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierSearchService Tests")
class SupplierSearchServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierSearchService supplierSearchService;

    private Supplier testSupplier1;
    private Supplier testSupplier2;

    @BeforeEach
    void setup() {
        testSupplier1 = createSupplier(1L, "Berlin Tours GmbH", "123 Main St", "10115", "Berlin", "Germany");
        testSupplier2 = createSupplier(2L, "Munich Adventures", "456 Oak Ave", "80331", "Munich", "Germany");
    }

    @Test
    @DisplayName("Should return all suppliers when calling findAll()")
    void testFindAll() {
        // Given
        when(supplierRepository.findAll()).thenReturn(Arrays.asList(testSupplier1, testSupplier2));

        // When
        List<SupplierDto> result = supplierSearchService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Berlin Tours GmbH", result.get(0).getName());
        assertEquals("Munich Adventures", result.get(1).getName());
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no suppliers exist")
    void testFindAllEmpty() {
        // Given
        when(supplierRepository.findAll()).thenReturn(List.of());

        // When
        List<SupplierDto> result = supplierSearchService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find supplier by ID successfully")
    void testFindById() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier1));

        // When
        SupplierDto result = supplierSearchService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Berlin Tours GmbH", result.getName());
        assertEquals("Berlin", result.getCity());
        verify(supplierRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when supplier not found by ID")
    void testFindByIdNotFound() {
        // Given
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierSearchService.findById(999L);
        });
        
        assertTrue(exception.getMessage().contains("Supplier not found with id: 999"));
        verify(supplierRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should search suppliers by city criteria")
    void testSearchByCity() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .city("Berlin")
                .build();
        
        when(supplierRepository.findAll(any(Specification.class))).thenReturn(List.of(testSupplier1));

        // When
        List<SupplierDto> result = supplierSearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Berlin", result.get(0).getCity());
        verify(supplierRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search suppliers by multiple criteria")
    void testSearchByMultipleCriteria() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .country("Germany")
                .city("Berlin")
                .name("Tours")
                .build();
        
        when(supplierRepository.findAll(any(Specification.class))).thenReturn(List.of(testSupplier1));

        // When
        List<SupplierDto> result = supplierSearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Berlin Tours GmbH", result.get(0).getName());
        verify(supplierRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should return empty list when search has no matches")
    void testSearchNoMatches() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .city("NonExistentCity")
                .build();
        
        when(supplierRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        List<SupplierDto> result = supplierSearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search by zip code (exact match)")
    void testSearchByZip() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .zip("10115")
                .build();
        
        when(supplierRepository.findAll(any(Specification.class))).thenReturn(List.of(testSupplier1));

        // When
        List<SupplierDto> result = supplierSearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("10115", result.get(0).getZip());
        verify(supplierRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should map entity to DTO correctly")
    void testMappingEntityToDto() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier1));

        // When
        SupplierDto result = supplierSearchService.findById(1L);

        // Then
        assertAll("DTO mapping",
            () -> assertEquals(testSupplier1.getId(), result.getId()),
            () -> assertEquals(testSupplier1.getName(), result.getName()),
            () -> assertEquals(testSupplier1.getAddress(), result.getAddress()),
            () -> assertEquals(testSupplier1.getZip(), result.getZip()),
            () -> assertEquals(testSupplier1.getCity(), result.getCity()),
            () -> assertEquals(testSupplier1.getCountry(), result.getCountry())
        );
    }

    @Test
    @DisplayName("Should handle null criteria fields gracefully")
    void testSearchWithNullCriteria() {
        // Given - all null criteria should return all suppliers
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder().build();
        
        when(supplierRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(testSupplier1, testSupplier2));

        // When
        List<SupplierDto> result = supplierSearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(supplierRepository, times(1)).findAll(any(Specification.class));
    }
}
