package com.getourguide.interview.specification;

import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.entity.Activity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActivitySpecifications
 * Tests specification creation logic without database
 */
@DisplayName("ActivitySpecifications Tests")
class ActivitySpecificationsTest {

    @Test
    @DisplayName("hasTitle() should return null when title is null")
    void testHasTitleWithNull() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasTitle(null);

        // Then
        assertNull(spec, "Specification should be null for null input");
    }

    @Test
    @DisplayName("hasTitle() should return null when title is empty")
    void testHasTitleWithEmptyString() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasTitle("");

        // Then
        assertNull(spec, "Specification should be null for empty string");
    }

    @Test
    @DisplayName("hasTitle() should return null when title is whitespace")
    void testHasTitleWithWhitespace() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasTitle("   ");

        // Then
        assertNull(spec, "Specification should be null for whitespace");
    }

    @Test
    @DisplayName("hasTitle() should return non-null specification for valid title")
    void testHasTitleWithValidTitle() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasTitle("Museum");

        // Then
        assertNotNull(spec, "Specification should not be null for valid title");
    }

    @Test
    @DisplayName("hasPriceRange() should return null when both prices are null")
    void testHasPriceRangeWithBothNull() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasPriceRange(null, null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasPriceRange() should return non-null when only minPrice provided")
    void testHasPriceRangeWithMinOnly() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasPriceRange(50, null);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasPriceRange() should return non-null when only maxPrice provided")
    void testHasPriceRangeWithMaxOnly() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasPriceRange(null, 200);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasPriceRange() should return non-null when both prices provided")
    void testHasPriceRangeWithBothPrices() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasPriceRange(50, 200);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasMinRating() should return null when rating is null")
    void testHasMinRatingWithNull() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasMinRating(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasMinRating() should return non-null specification for valid rating")
    void testHasMinRatingWithValidRating() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasMinRating(4.5);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasCurrency() should return null when currency is null")
    void testHasCurrencyWithNull() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasCurrency(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasCurrency() should return null when currency is empty")
    void testHasCurrencyWithEmptyString() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasCurrency("");

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasCurrency() should return non-null specification for valid currency")
    void testHasCurrencyWithValidCurrency() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasCurrency("EUR");

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasSpecialOffer() should return null when specialOffer is null")
    void testHasSpecialOfferWithNull() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasSpecialOffer(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasSpecialOffer() should return non-null specification for true")
    void testHasSpecialOfferWithTrue() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasSpecialOffer(true);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasSpecialOffer() should return non-null specification for false")
    void testHasSpecialOfferWithFalse() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasSpecialOffer(false);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("hasSupplierName() should return null when supplierName is null")
    void testHasSupplierNameWithNull() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasSupplierName(null);

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasSupplierName() should return null when supplierName is empty")
    void testHasSupplierNameWithEmptyString() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasSupplierName("");

        // Then
        assertNull(spec);
    }

    @Test
    @DisplayName("hasSupplierName() should return non-null specification for valid supplier name")
    void testHasSupplierNameWithValidName() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasSupplierName("Berlin Tours");

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle all null criteria")
    void testFromCriteriaWithAllNulls() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec, "Specification should not be null even with all null criteria");
    }

    @Test
    @DisplayName("fromCriteria() should handle single criterion")
    void testFromCriteriaWithSingleCriterion() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("Museum")
                .build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle multiple criteria")
    void testFromCriteriaWithMultipleCriteria() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("Museum")
                .minPrice(50)
                .maxPrice(200)
                .minRating(4.5)
                .currency("EUR")
                .specialOffer(true)
                .supplierName("Berlin")
                .build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle partial criteria (some null, some not)")
    void testFromCriteriaWithPartialCriteria() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("Museum")
                .minPrice(null)
                .maxPrice(200)
                .minRating(null)
                .currency("EUR")
                .specialOffer(null)
                .supplierName(null)
                .build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Specifications should handle trimming whitespace")
    void testSpecificationsHandleWhitespace() {
        // When
        Specification<Activity> spec = ActivitySpecifications.hasTitle("  Museum  ");

        // Then
        assertNotNull(spec, "Should handle whitespace and return non-null spec");
    }

    @Test
    @DisplayName("Should create specification for case-insensitive search")
    void testCaseInsensitiveSearch() {
        // When - specifications should handle case-insensitivity internally
        Specification<Activity> spec1 = ActivitySpecifications.hasTitle("museum");
        Specification<Activity> spec2 = ActivitySpecifications.hasTitle("MUSEUM");
        Specification<Activity> spec3 = ActivitySpecifications.hasTitle("Museum");

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
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("")
                .currency("")
                .supplierName("")
                .build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then - should handle gracefully (empty strings treated as null)
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Price range specifications should handle edge cases")
    void testPriceRangeEdgeCases() {
        // When
        Specification<Activity> spec1 = ActivitySpecifications.hasPriceRange(0, 0);
        Specification<Activity> spec2 = ActivitySpecifications.hasPriceRange(100, 100);
        Specification<Activity> spec3 = ActivitySpecifications.hasPriceRange(1, 10000);

        // Then - all should create valid specifications
        assertAll("Price range edge cases",
            () -> assertNotNull(spec1, "Zero range should be valid"),
            () -> assertNotNull(spec2, "Same min/max should be valid"),
            () -> assertNotNull(spec3, "Large range should be valid")
        );
    }

    @Test
    @DisplayName("Rating specification should handle edge cases")
    void testRatingEdgeCases() {
        // When
        Specification<Activity> spec1 = ActivitySpecifications.hasMinRating(0.0);
        Specification<Activity> spec2 = ActivitySpecifications.hasMinRating(5.0);
        Specification<Activity> spec3 = ActivitySpecifications.hasMinRating(2.5);

        // Then - all should create valid specifications
        assertAll("Rating edge cases",
            () -> assertNotNull(spec1, "Minimum rating should be valid"),
            () -> assertNotNull(spec2, "Maximum rating should be valid"),
            () -> assertNotNull(spec3, "Mid-range rating should be valid")
        );
    }

    @Test
    @DisplayName("fromCriteria() should handle only price criteria")
    void testFromCriteriaWithOnlyPriceCriteria() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .minPrice(50)
                .maxPrice(200)
                .build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("fromCriteria() should handle complex combination")
    void testFromCriteriaComplexCombination() {
        // Given - realistic search scenario
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("tour")
                .minPrice(30)
                .maxPrice(150)
                .minRating(4.0)
                .currency("EUR")
                .specialOffer(true)
                .build();

        // When
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Then
        assertNotNull(spec);
    }
}
