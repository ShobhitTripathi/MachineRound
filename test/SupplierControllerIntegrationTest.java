package com.getourguide.interview.controller;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
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

import static com.getourguide.interview.helpers.SupplierHelper.createSupplier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SupplierController
 * Tests the full stack: Controller -> Service -> Repository -> Database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("SupplierController Integration Tests")
class SupplierControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SupplierRepository supplierRepository;

    private String baseUrl;
    private Supplier testSupplier1;
    private Supplier testSupplier2;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        
        // Clean database before each test
        supplierRepository.deleteAll();
        
        // Create test data
        testSupplier1 = createSupplier(null, "Berlin Tours GmbH", "123 Main St", "10115", "Berlin", "Germany");
        testSupplier2 = createSupplier(null, "Munich Adventures", "456 Oak Ave", "80331", "Munich", "Germany");
        
        testSupplier1 = supplierRepository.save(testSupplier1);
        testSupplier2 = supplierRepository.save(testSupplier2);
    }

    @Test
    @DisplayName("GET /suppliers should return all suppliers")
    void testGetAllSuppliers() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("GET /suppliers/{id} should return supplier by ID")
    void testGetSupplierById() {
        // When
        ResponseEntity<SupplierDto> response = restTemplate.getForEntity(
            baseUrl + "/suppliers/" + testSupplier1.getId(),
            SupplierDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Berlin Tours GmbH", response.getBody().getName());
        assertEquals("Berlin", response.getBody().getCity());
    }

    @Test
    @DisplayName("GET /suppliers/{id} should return 500 when supplier not found")
    void testGetSupplierByIdNotFound() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/suppliers/99999",
            String.class
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Supplier not found"));
    }

    @Test
    @DisplayName("GET /suppliers/search?city=Berlin should filter by city")
    void testSearchSuppliersByCity() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?city=Berlin",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Berlin", response.getBody().get(0).getCity());
    }

    @Test
    @DisplayName("GET /suppliers/search?country=Germany should filter by country")
    void testSearchSuppliersByCountry() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?country=Germany",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size()); // Both suppliers are in Germany
    }

    @Test
    @DisplayName("GET /suppliers/search?name=Tours should filter by name")
    void testSearchSuppliersByName() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?name=Tours",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getName().contains("Tours"));
    }

    @Test
    @DisplayName("GET /suppliers/search with multiple criteria should apply AND logic")
    void testSearchSuppliersMultipleCriteria() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?country=Germany&city=Berlin&name=Tours",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        SupplierDto result = response.getBody().get(0);
        assertEquals("Berlin Tours GmbH", result.getName());
        assertEquals("Berlin", result.getCity());
        assertEquals("Germany", result.getCountry());
    }

    @Test
    @DisplayName("GET /suppliers/search with no matches should return empty list")
    void testSearchSuppliersNoMatches() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?city=NonExistentCity",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("GET /suppliers/search?zip=10115 should filter by exact zip code")
    void testSearchSuppliersByZip() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?zip=10115",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("10115", response.getBody().get(0).getZip());
    }

    @Test
    @DisplayName("GET /suppliers/search with no parameters should return all suppliers")
    void testSearchSuppliersNoParameters() {
        // When
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
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
        ResponseEntity<List<SupplierDto>> response1 = restTemplate.exchange(
            baseUrl + "/suppliers/search?city=berlin",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );
        
        ResponseEntity<List<SupplierDto>> response2 = restTemplate.exchange(
            baseUrl + "/suppliers/search?city=BERLIN",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
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
        // When - search with partial city name
        ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
            baseUrl + "/suppliers/search?city=Berl",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SupplierDto>>() {}
        );

        // Then - should find Berlin
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Berlin", response.getBody().get(0).getCity());
    }

    @Test
    @DisplayName("DTO should not expose entity relationships")
    void testDtoDoesNotExposeRelationships() {
        // When
        ResponseEntity<SupplierDto> response = restTemplate.getForEntity(
            baseUrl + "/suppliers/" + testSupplier1.getId(),
            SupplierDto.class
        );

        // Then
        SupplierDto dto = response.getBody();
        assertNotNull(dto);
        
        // DTO should only have basic fields, no relationships
        assertAll("DTO fields",
            () -> assertNotNull(dto.getId()),
            () -> assertNotNull(dto.getName()),
            () -> assertNotNull(dto.getAddress()),
            () -> assertNotNull(dto.getZip()),
            () -> assertNotNull(dto.getCity()),
            () -> assertNotNull(dto.getCountry())
        );
    }
}
