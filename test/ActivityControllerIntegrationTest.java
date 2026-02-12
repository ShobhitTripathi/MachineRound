package com.getourguide.interview.controller;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.entity.Supplier;
import com.getourguide.interview.repository.ActivityRepository;
import com.getourguide.interview.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.getourguide.interview.helpers.ActivityHelper.createActivity;
import static com.getourguide.interview.helpers.SupplierHelper.createSupplier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ActivityController
 * Tests the full stack: Controller -> Service -> Repository -> Database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("ActivityController Integration Tests")
class ActivityControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    private String baseUrl;
    private Activity testActivity1;
    private Activity testActivity2;
    private Supplier testSupplier1;
    private Supplier testSupplier2;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        
        // Clean database before each test
        activityRepository.deleteAll();
        supplierRepository.deleteAll();
        
        // Create test suppliers
        testSupplier1 = createSupplier(null, "Berlin Tours GmbH", "123 Main St", "10115", "Berlin", "Germany");
        testSupplier2 = createSupplier(null, "Munich Adventures", "456 Oak Ave", "80331", "Munich", "Germany");
        testSupplier1 = supplierRepository.save(testSupplier1);
        testSupplier2 = supplierRepository.save(testSupplier2);
        
        // Create test activities
        testActivity1 = createActivity(null, "Museum Tour", 75, "EUR", 4.8, true, testSupplier1);
        testActivity2 = createActivity(null, "City Walk", 50, "USD", 4.5, false, testSupplier2);
        testActivity1 = activityRepository.save(testActivity1);
        testActivity2 = activityRepository.save(testActivity2);
    }

    @Test
    @DisplayName("GET /activities should return all activities")
    void testGetAllActivities() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("GET /activities/{id} should return activity by ID")
    void testGetActivityById() {
        // When
        ResponseEntity<ActivityDto> response = restTemplate.getForEntity(
            baseUrl + "/activities/" + testActivity1.getId(),
            ActivityDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Museum Tour", response.getBody().getTitle());
        assertEquals(75, response.getBody().getPrice());
    }

    @Test
    @DisplayName("GET /activities/{id} should return 500 when activity not found")
    void testGetActivityByIdNotFound() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/activities/99999",
            String.class
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Activity not found"));
    }

    @Test
    @DisplayName("GET /activities/search?title=Museum should filter by title")
    void testSearchActivitiesByTitle() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?title=Museum",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getTitle().contains("Museum"));
    }

    @Test
    @DisplayName("GET /activities/search?minPrice=60 should filter by minimum price")
    void testSearchActivitiesByMinPrice() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?minPrice=60",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getPrice() >= 60);
    }

    @Test
    @DisplayName("GET /activities/search with price range should filter correctly")
    void testSearchActivitiesByPriceRange() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?minPrice=40&maxPrice=60",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(50, response.getBody().get(0).getPrice());
    }

    @Test
    @DisplayName("GET /activities/search?minRating=4.7 should filter by rating")
    void testSearchActivitiesByRating() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?minRating=4.7",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getRating() >= 4.7);
    }

    @Test
    @DisplayName("GET /activities/search?currency=EUR should filter by currency")
    void testSearchActivitiesByCurrency() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?currency=EUR",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("EUR", response.getBody().get(0).getCurrency());
    }

    @Test
    @DisplayName("GET /activities/search?specialOffer=true should filter by special offer")
    void testSearchActivitiesBySpecialOffer() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?specialOffer=true",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).isSpecialOffer());
    }

    @Test
    @DisplayName("GET /activities/search?supplierName=Berlin should filter by supplier name (JOIN)")
    void testSearchActivitiesBySupplierName() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?supplierName=Berlin",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getSupplierName().contains("Berlin"));
    }

    @Test
    @DisplayName("GET /activities/search with multiple criteria should apply AND logic")
    void testSearchActivitiesMultipleCriteria() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?title=Museum&minPrice=50&maxPrice=100&minRating=4.5&currency=EUR&specialOffer=true",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        ActivityDto result = response.getBody().get(0);
        assertEquals("Museum Tour", result.getTitle());
        assertTrue(result.getPrice() >= 50 && result.getPrice() <= 100);
        assertTrue(result.getRating() >= 4.5);
        assertEquals("EUR", result.getCurrency());
        assertTrue(result.isSpecialOffer());
    }

    @Test
    @DisplayName("GET /activities/search with no matches should return empty list")
    void testSearchActivitiesNoMatches() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?title=NonExistentActivity",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("GET /activities/search with no parameters should return all activities")
    void testSearchActivitiesNoParameters() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Search should be case-insensitive")
    void testSearchCaseInsensitive() {
        // When - search with different case
        ResponseEntity<List<ActivityDto>> response1 = restTemplate.exchange(
            baseUrl + "/activities/search?title=museum",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );
        
        ResponseEntity<List<ActivityDto>> response2 = restTemplate.exchange(
            baseUrl + "/activities/search?title=MUSEUM",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then - both should find the same result
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(1, response1.getBody().size());
        assertEquals(1, response2.getBody().size());
        assertEquals(response1.getBody().get(0).getId(), response2.getBody().get(0).getId());
    }

    @Test
    @DisplayName("Search should support partial matches")
    void testSearchPartialMatch() {
        // When - search with partial title
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?title=Mus",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then - should find Museum Tour
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getTitle().contains("Museum"));
    }

    @Test
    @DisplayName("DTO should include supplier name from JOIN")
    void testDtoIncludesSupplierName() {
        // When
        ResponseEntity<ActivityDto> response = restTemplate.getForEntity(
            baseUrl + "/activities/" + testActivity1.getId(),
            ActivityDto.class
        );

        // Then
        ActivityDto dto = response.getBody();
        assertNotNull(dto);
        assertNotNull(dto.getSupplierName());
        assertEquals("Berlin Tours GmbH", dto.getSupplierName());
    }

    @Test
    @DisplayName("Search by supplier name should perform JOIN correctly")
    void testSearchBySupplierNamePerformsJoin() {
        // When - search by supplier name should use JOIN
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?supplierName=Munich",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("City Walk", response.getBody().get(0).getTitle());
        assertTrue(response.getBody().get(0).getSupplierName().contains("Munich"));
    }

    @Test
    @DisplayName("Search should handle special offer = false correctly")
    void testSearchBySpecialOfferFalse() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?specialOffer=false",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then - should only return non-special offers
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isSpecialOffer());
    }

    @Test
    @DisplayName("Search with extreme price range should work")
    void testSearchWithExtremePriceRange() {
        // When
        ResponseEntity<List<ActivityDto>> response = restTemplate.exchange(
            baseUrl + "/activities/search?minPrice=0&maxPrice=10000",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then - should return all activities
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Currency search should be case-insensitive")
    void testCurrencySearchCaseInsensitive() {
        // When
        ResponseEntity<List<ActivityDto>> response1 = restTemplate.exchange(
            baseUrl + "/activities/search?currency=eur",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );
        
        ResponseEntity<List<ActivityDto>> response2 = restTemplate.exchange(
            baseUrl + "/activities/search?currency=EUR",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActivityDto>>() {}
        );

        // Then - both should find the same results
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(response1.getBody().size(), response2.getBody().size());
    }
}
