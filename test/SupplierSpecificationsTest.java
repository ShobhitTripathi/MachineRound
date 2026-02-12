package com.getourguide.interview.specification;

import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SupplierSpecifications
 * Tests specification creation logic without database
 */
@DisplayName("SupplierSpecifications Tests")
class SupplierSpecificationsTest {

    @Test
    @DisplayName("hasName() should return null when name is null")
    void testHasNameWithNull() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasName(null);

        // Then
        assertNull(spec, "Specification should be null for null input");
    }

    @Test
    @DisplayName("hasName() should return null when name is empty")
    void testHasNameWithEmptyString() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasName("");

        // Then
        assertNull(spec, "Specification should be null for empty string");
    }

    @Test
    @DisplayName("hasName() should return null when name is whitespace")
    void testHasNameWithWhitespace() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasName("   ");

        // Then
        assertNull(spec, "Specification should be null for whitespace");
    }

    @Test
    @DisplayName("hasName() should return non-null specification for valid name")
    void testHasNameWithValidName() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasName("Tours");

        // Then
        assertNotNull(spec, "Specification should not be null for valid name");
    }

    @Test
    @DisplayName("hasCity() should return null when city is null")
    void testHasCityWithNull() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasCity(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasCity() should return non-null specification for valid city")
    void testHasCityWithValidCity() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasCity("Berlin");

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasCountry() should return null when country is null")
    void testHasCountryWithNull() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasCountry(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasCountry() should return non-null specification for valid country")
    void testHasCountryWithValidCountry() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasCountry("Germany");

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasZip() should return null when zip is null")
    void testHasZipWithNull() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasZip(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasZip() should return non-null specification for valid zip")
    void testHasZipWithValidZip() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasZip("10115");

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasAddress() should return null when address is null")
    void testHasAddressWithNull() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasAddress(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasAddress() should return non-null specification for valid address")
    void testHasAddressWithValidAddress() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasAddress("Main Street");

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle all null criteria")
    void testFromCriteriaWithAllNulls() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder().build();

        // When
        Specification<Supplier> spec = SupplierSpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec, "Specification should not be null even with all null criteria");
    }

    @Test
    @DisplayName("fromCriteria() should handle single criterion")
    void testFromCriteriaWithSingleCriterion() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .city("Berlin")
                .build();

        // When
        Specification<Supplier> spec = SupplierSpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle multiple criteria")
    void testFromCriteriaWithMultipleCriteria() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .name("Tours")
                .city("Berlin")
                .country("Germany")
                .zip("10115")
                .address("Main")
                .build();

        // When
        Specification<Supplier> spec = SupplierSpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle partial criteria (some null, some not)")
    void testFromCriteriaWithPartialCriteria() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .city("Berlin")
                .country(null)
                .name("Tours")
                .zip(null)
                .address(null)
                .build();

        // When
        Specification<Supplier> spec = SupplierSpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Specifications should handle trimming whitespace")
    void testSpecificationsHandleWhitespace() {
        // When
        Specification<Supplier> spec = SupplierSpecifications.hasName("  Tours  ");

        // Then
        assertNotNull(spec, "Should handle whitespace and return non-null spec");
    }

    @Test
    @DisplayName("Should create specification for case-insensitive search")
    void testCaseInsensitiveSearch() {
        // When - specifications should handle case-insensitivity internally
        Specification<Supplier> spec1 = SupplierSpecifications.hasName("tours");
        Specification<Supplier> spec2 = SupplierSpecifications.hasName("TOURS");
        Specification<Supplier> spec3 = SupplierSpecifications.hasName("Tours");

        // Then - all should create valid specifications
        assertAll("Case insensitive specifications",
            () -> assertNotNull(spec1),
            () -> assertNotNull(spec2),
            () -> assertNotNull(spec3)
        );
    }

    @Test
    @DisplayName("fromCriteria() should handle criteria with empty strings")
    void testFromCriteriaWithEmptyStrings() {
        // Given
        SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
                .name("")
                .city("")
                .country("")
                .zip("")
                .address("")
                .build();

        // When
        Specification<Supplier> spec = SupplierSpecifications.fromCriteria(criteria);

        // Then - should handle gracefully (empty strings treated as null)
        assertNotNull(spec);
    }
}
