package com.getourguide.interview.service.search;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.repository.ActivityRepository;
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

import static com.getourguide.interview.helpers.ActivityHelper.createActivity;
import static com.getourguide.interview.helpers.SupplierHelper.createSupplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ActivitySearchService
 * Tests the service layer in isolation using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ActivitySearchService Tests")
class ActivitySearchServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivitySearchService activitySearchService;

    private Activity testActivity1;
    private Activity testActivity2;

    @BeforeEach
    void setup() {
        testActivity1 = createActivity(
            1L,
            "Museum Tour",
            75,
            "EUR",
            4.8,
            true,
            createSupplier(1L, "Berlin Tours GmbH", "123 Main St", "10115", "Berlin", "Germany")
        );
        
        testActivity2 = createActivity(
            2L,
            "City Walk",
            50,
            "EUR",
            4.5,
            false,
            createSupplier(2L, "Munich Adventures", "456 Oak Ave", "80331", "Munich", "Germany")
        );
    }

    @Test
    @DisplayName("Should return all activities when calling findAll()")
    void testFindAll() {
        // Given
        when(activityRepository.findAll()).thenReturn(Arrays.asList(testActivity1, testActivity2));

        // When
        List<ActivityDto> result = activitySearchService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Museum Tour", result.get(0).getTitle());
        assertEquals("City Walk", result.get(1).getTitle());
        verify(activityRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no activities exist")
    void testFindAllEmpty() {
        // Given
        when(activityRepository.findAll()).thenReturn(List.of());

        // When
        List<ActivityDto> result = activitySearchService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(activityRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find activity by ID successfully")
    void testFindById() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity1));

        // When
        ActivityDto result = activitySearchService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Museum Tour", result.getTitle());
        assertEquals(75, result.getPrice());
        assertEquals("Berlin Tours GmbH", result.getSupplierName());
        verify(activityRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when activity not found by ID")
    void testFindByIdNotFound() {
        // Given
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            activitySearchService.findById(999L);
        });
        
        assertTrue(exception.getMessage().contains("Activity not found with id: 999"));
        verify(activityRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should search activities by title criteria")
    void testSearchByTitle() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("Museum")
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity1));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains("Museum"));
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search activities by price range")
    void testSearchByPriceRange() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .minPrice(40)
                .maxPrice(60)
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity2));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getPrice());
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search activities by multiple criteria")
    void testSearchByMultipleCriteria() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("Museum")
                .minPrice(50)
                .maxPrice(100)
                .minRating(4.5)
                .currency("EUR")
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity1));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Museum Tour", result.get(0).getTitle());
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should return empty list when search has no matches")
    void testSearchNoMatches() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title("NonExistentActivity")
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search by special offer flag")
    void testSearchBySpecialOffer() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .specialOffer(true)
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity1));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isSpecialOffer());
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search by rating")
    void testSearchByRating() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .minRating(4.7)
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity1));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRating() >= 4.7);
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should map entity to DTO correctly")
    void testMappingEntityToDto() {
        // Given
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity1));

        // When
        ActivityDto result = activitySearchService.findById(1L);

        // Then
        assertAll("DTO mapping",
            () -> assertEquals(testActivity1.getId(), result.getId()),
            () -> assertEquals(testActivity1.getTitle(), result.getTitle()),
            () -> assertEquals(testActivity1.getPrice(), result.getPrice()),
            () -> assertEquals(testActivity1.getCurrency(), result.getCurrency()),
            () -> assertEquals(testActivity1.getRating(), result.getRating()),
            () -> assertEquals(testActivity1.isSpecialOffer(), result.isSpecialOffer()),
            () -> assertEquals(testActivity1.getSupplier().getName(), result.getSupplierName())
        );
    }

    @Test
    @DisplayName("Should handle null supplier gracefully in mapping")
    void testMappingWithNullSupplier() {
        // Given
        Activity activityWithoutSupplier = createActivity(3L, "Solo Activity", 30, "EUR", 4.0, false, null);
        when(activityRepository.findById(3L)).thenReturn(Optional.of(activityWithoutSupplier));

        // When
        ActivityDto result = activitySearchService.findById(3L);

        // Then
        assertNotNull(result);
        assertEquals("", result.getSupplierName());  // Should default to empty string
    }

    @Test
    @DisplayName("Should handle null criteria fields gracefully")
    void testSearchWithNullCriteria() {
        // Given - all null criteria should return all activities
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().build();
        
        when(activityRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(testActivity1, testActivity2));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search by supplier name (JOIN query)")
    void testSearchBySupplierName() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .supplierName("Berlin")
                .build();
        
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity1));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getSupplierName().contains("Berlin"));
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search by currency")
    void testSearchByCurrency() {
        // Given
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .currency("EUR")
                .build();
        
        when(activityRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(testActivity1, testActivity2));

        // When
        List<ActivityDto> result = activitySearchService.search(criteria);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> "EUR".equals(dto.getCurrency())));
        verify(activityRepository, times(1)).findAll(any(Specification.class));
    }
}
