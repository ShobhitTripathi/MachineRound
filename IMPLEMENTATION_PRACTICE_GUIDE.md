# Search by Params API - Implementation Practice Guide

This guide provides step-by-step instructions with exact code examples for implementing the search-by-params API design. Follow this order for a logical, incremental build.

---

## Implementation Order & Rationale

1. **DTOs first** → Define the data contracts
2. **Mappers next** → Eliminate duplication early
3. **Specifications** → Core query building logic
4. **Repository enhancement** → Enable specification execution
5. **Service refactoring** → Use the new components
6. **Controller updates** → Expose new endpoints
7. **Validation** → Add input validation
8. **Error handling** → Polish the implementation

---

## Step 1: Create Search Criteria DTOs

### 1.1 Create Package
```
src/main/java/com/getourguide/interview/dto/search/
```

### 1.2 Create `ActivitySearchCriteria.java`

**File**: `src/main/java/com/getourguide/interview/dto/search/ActivitySearchCriteria.java`

```java
package com.getourguide.interview.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search criteria for filtering activities.
 * All fields are optional - null values are ignored in query building.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySearchCriteria {
    
    /**
     * Search in activity title (partial match, case-insensitive)
     */
    private String title;
    
    /**
     * Minimum price filter (inclusive)
     */
    private Integer minPrice;
    
    /**
     * Maximum price filter (inclusive)
     */
    private Integer maxPrice;
    
    /**
     * Minimum rating filter (inclusive)
     */
    private Double minRating;
    
    /**
     * Currency filter (exact match, case-insensitive)
     */
    private String currency;
    
    /**
     * Special offer filter (true = only special offers, false = only non-special, null = all)
     */
    private Boolean specialOffer;
    
    /**
     * Search in supplier name (partial match, case-insensitive)
     */
    private String supplierName;
    
    /**
     * Check if any search criteria is provided
     */
    public boolean isEmpty() {
        return title == null 
            && minPrice == null 
            && maxPrice == null 
            && minRating == null 
            && currency == null 
            && specialOffer == null 
            && supplierName == null;
    }
}
```

**Key Points to Practice:**
- `@Builder` enables clean object construction
- All fields are `private` with no defaults (null means "don't filter")
- `isEmpty()` utility method for validation
- JavaDoc explains each field's behavior

### 1.3 Create `SupplierSearchCriteria.java`

**File**: `src/main/java/com/getourguide/interview/dto/search/SupplierSearchCriteria.java`

```java
package com.getourguide.interview.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search criteria for filtering suppliers.
 * All fields are optional - null values are ignored in query building.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSearchCriteria {
    
    /**
     * Search in supplier name (partial match, case-insensitive)
     */
    private String name;
    
    /**
     * Search in address (partial match, case-insensitive)
     */
    private String address;
    
    /**
     * Zip code filter (exact match)
     */
    private String zip;
    
    /**
     * Search in city (partial match, case-insensitive)
     */
    private String city;
    
    /**
     * Search in country (partial match, case-insensitive)
     */
    private String country;
    
    /**
     * Check if any search criteria is provided
     */
    public boolean isEmpty() {
        return name == null 
            && address == null 
            && zip == null 
            && city == null 
            && country == null;
    }
}
```

**Practice Tip**: Notice the pattern similarity - this makes the architecture consistent and predictable.

---

## Step 2: Create Mappers (Eliminate Code Duplication)

### 2.1 Create Package
```
src/main/java/com/getourguide/interview/mapper/
```

### 2.2 Create `ActivityMapper.java`

**File**: `src/main/java/com/getourguide/interview/mapper/ActivityMapper.java`

```java
package com.getourguide.interview.mapper;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.entity.Activity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maps Activity entity to ActivityDto.
 * Eliminates code duplication from ActivityService.
 */
@Component
public class ActivityMapper {
    
    /**
     * Convert single Activity entity to DTO
     */
    public ActivityDto toDto(Activity activity) {
        if (activity == null) {
            return null;
        }
        
        return ActivityDto.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .price(activity.getPrice())
                .currency(activity.getCurrency())
                .rating(activity.getRating())
                .specialOffer(activity.isSpecialOffer())
                .supplierName(Objects.isNull(activity.getSupplier()) 
                    ? "" 
                    : activity.getSupplier().getName())
                .build();
    }
    
    /**
     * Convert list of Activity entities to DTOs
     */
    public List<ActivityDto> toDtoList(List<Activity> activities) {
        if (activities == null) {
            return List.of();
        }
        
        return activities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
```

**Key Points:**
- `@Component` makes it a Spring bean for injection
- Null-safe operations
- Single responsibility: only mapping logic
- Replaces 3 duplicated code blocks in current `ActivityService`

### 2.3 Create `SupplierMapper.java`

**File**: `src/main/java/com/getourguide/interview/mapper/SupplierMapper.java`

First, you need to create `SupplierDto` if it doesn't exist:

**File**: `src/main/java/com/getourguide/interview/dto/SupplierDto.java`

```java
package com.getourguide.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private Long id;
    private String name;
    private String address;
    private String zip;
    private String city;
    private String country;
}
```

Now create the mapper:

**File**: `src/main/java/com/getourguide/interview/mapper/SupplierMapper.java`

```java
package com.getourguide.interview.mapper;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.entity.Supplier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Supplier entity to SupplierDto.
 */
@Component
public class SupplierMapper {
    
    /**
     * Convert single Supplier entity to DTO
     */
    public SupplierDto toDto(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        
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
     * Convert list of Supplier entities to DTOs
     */
    public List<SupplierDto> toDtoList(List<Supplier> suppliers) {
        if (suppliers == null) {
            return List.of();
        }
        
        return suppliers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
```

---

## Step 3: Create Specifications (Core Query Logic)

### 3.1 Create Package
```
src/main/java/com/getourguide/interview/specification/
```

### 3.2 Create `ActivitySpecifications.java`

**File**: `src/main/java/com/getourguide/interview/specification/ActivitySpecifications.java`

```java
package com.getourguide.interview.specification;

import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.entity.Supplier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for Activity entity queries.
 * Each method returns a reusable, composable query fragment.
 * 
 * Usage:
 *   Specification<Activity> spec = ActivitySpecifications
 *       .hasTitle("museum")
 *       .and(ActivitySpecifications.hasPriceRange(50, 200));
 */
public class ActivitySpecifications {
    
    /**
     * Filter by title (partial match, case-insensitive)
     * SQL: WHERE LOWER(title) LIKE '%value%'
     */
    public static Specification<Activity> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.trim().isEmpty()) {
                return null; // null means "no filtering"
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")),
                "%" + title.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by price range (inclusive)
     * SQL: WHERE price BETWEEN minPrice AND maxPrice
     */
    public static Specification<Activity> hasPriceRange(Integer minPrice, Integer maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            }
            
            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
        };
    }
    
    /**
     * Filter by minimum rating
     * SQL: WHERE rating >= minRating
     */
    public static Specification<Activity> hasMinRating(Double minRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
        };
    }
    
    /**
     * Filter by currency (exact match, case-insensitive)
     * SQL: WHERE LOWER(currency) = 'value'
     */
    public static Specification<Activity> hasCurrency(String currency) {
        return (root, query, criteriaBuilder) -> {
            if (currency == null || currency.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(
                criteriaBuilder.lower(root.get("currency")),
                currency.toLowerCase().trim()
            );
        };
    }
    
    /**
     * Filter by special offer status
     * SQL: WHERE specialOffer = value
     */
    public static Specification<Activity> hasSpecialOffer(Boolean specialOffer) {
        return (root, query, criteriaBuilder) -> {
            if (specialOffer == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("specialOffer"), specialOffer);
        };
    }
    
    /**
     * Filter by supplier name (partial match, case-insensitive, with JOIN)
     * SQL: WHERE LOWER(supplier.name) LIKE '%value%'
     * Note: This performs a JOIN with the supplier table
     */
    public static Specification<Activity> hasSupplierName(String supplierName) {
        return (root, query, criteriaBuilder) -> {
            if (supplierName == null || supplierName.trim().isEmpty()) {
                return null;
            }
            
            // Join with Supplier entity
            Join<Activity, Supplier> supplierJoin = root.join("supplier", JoinType.LEFT);
            
            return criteriaBuilder.like(
                criteriaBuilder.lower(supplierJoin.get("name")),
                "%" + supplierName.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Combine all criteria into a single Specification
     * This is a convenience method that applies all non-null criteria
     */
    public static Specification<Activity> withCriteria(
            String title,
            Integer minPrice,
            Integer maxPrice,
            Double minRating,
            String currency,
            Boolean specialOffer,
            String supplierName) {
        
        return Specification
                .where(hasTitle(title))
                .and(hasPriceRange(minPrice, maxPrice))
                .and(hasMinRating(minRating))
                .and(hasCurrency(currency))
                .and(hasSpecialOffer(specialOffer))
                .and(hasSupplierName(supplierName));
    }
}
```

**Key Learning Points:**
- Each method returns `Specification<Activity>` - a functional interface
- Lambdas: `(root, query, criteriaBuilder) -> ...`
  - `root`: The entity being queried (Activity)
  - `query`: The overall query being built
  - `criteriaBuilder`: Helper for building SQL predicates
- Returning `null` means "skip this filter"
- `.and()` and `.or()` allow combining specifications
- `JOIN` example in `hasSupplierName()` shows relationship traversal

### 3.3 Create `SupplierSpecifications.java`

**File**: `src/main/java/com/getourguide/interview/specification/SupplierSpecifications.java`

```java
package com.getourguide.interview.specification;

import com.getourguide.interview.entity.Supplier;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for Supplier entity queries.
 */
public class SupplierSpecifications {
    
    /**
     * Filter by supplier name (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + name.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by address (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasAddress(String address) {
        return (root, query, criteriaBuilder) -> {
            if (address == null || address.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("address")),
                "%" + address.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by zip code (exact match)
     */
    public static Specification<Supplier> hasZip(String zip) {
        return (root, query, criteriaBuilder) -> {
            if (zip == null || zip.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("zip"), zip.trim());
        };
    }
    
    /**
     * Filter by city (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (city == null || city.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("city")),
                "%" + city.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Filter by country (partial match, case-insensitive)
     */
    public static Specification<Supplier> hasCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (country == null || country.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("country")),
                "%" + country.toLowerCase().trim() + "%"
            );
        };
    }
    
    /**
     * Combine all criteria into a single Specification
     */
    public static Specification<Supplier> withCriteria(
            String name,
            String address,
            String zip,
            String city,
            String country) {
        
        return Specification
                .where(hasName(name))
                .and(hasAddress(address))
                .and(hasZip(zip))
                .and(hasCity(city))
                .and(hasCountry(country));
    }
}
```

---

## Step 4: Enhance Repositories

### 4.1 Modify `ActivityRepository.java`

**File**: `src/main/java/com/getourguide/interview/repository/ActivityRepository.java`

**BEFORE:**
```java
package com.getourguide.interview.repository;

import com.getourguide.interview.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
```

**AFTER (add JpaSpecificationExecutor):**
```java
package com.getourguide.interview.repository;

import com.getourguide.interview.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends 
        JpaRepository<Activity, Long>, 
        JpaSpecificationExecutor<Activity> {
    // JpaSpecificationExecutor provides:
    // - findAll(Specification<Activity> spec)
    // - findOne(Specification<Activity> spec)
    // - count(Specification<Activity> spec)
    // - exists(Specification<Activity> spec)
}
```

**What Changed:**
- Added `JpaSpecificationExecutor<Activity>` interface
- This gives us `findAll(Specification)` method automatically
- No need to write any implementation code!

### 4.2 Modify `SupplierRepository.java` (or create if missing)

**File**: `src/main/java/com/getourguide/interview/repository/SupplierRepository.java`

```java
package com.getourguide.interview.repository;

import com.getourguide.interview.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends 
        JpaRepository<Supplier, Long>, 
        JpaSpecificationExecutor<Supplier> {
}
```

---

## Step 5: Refactor Services

### 5.1 Refactor `ActivityService.java`

**File**: `src/main/java/com/getourguide/interview/service/ActivityService.java`

**BEFORE:** 67 lines with duplication and in-memory filtering

**AFTER:**

```java
package com.getourguide.interview.service;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.mapper.ActivityMapper;
import com.getourguide.interview.repository.ActivityRepository;
import com.getourguide.interview.specification.ActivitySpecifications;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Activity operations.
 * Orchestrates repository calls and entity-to-DTO mapping.
 */
@Service
@AllArgsConstructor
public class ActivityService {
    
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    
    /**
     * Get all activities
     */
    public List<ActivityDto> getActivities() {
        List<Activity> activities = activityRepository.findAll();
        return activityMapper.toDtoList(activities);
    }
    
    /**
     * Get activity by ID
     * @throws RuntimeException if activity not found
     */
    public ActivityDto getActivity(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + activityId));
        return activityMapper.toDto(activity);
    }
    
    /**
     * Search activities by title (legacy method - keep for backward compatibility)
     */
    public List<ActivityDto> searchActivitiesByTitle(String title) {
        Specification<Activity> spec = ActivitySpecifications.hasTitle(title);
        List<Activity> activities = activityRepository.findAll(spec);
        return activityMapper.toDtoList(activities);
    }
    
    /**
     * Search activities by multiple criteria (NEW METHOD)
     * This is the main search method using query parameters
     */
    public List<ActivityDto> searchActivities(ActivitySearchCriteria criteria) {
        // Build composite specification from all criteria
        Specification<Activity> spec = ActivitySpecifications.withCriteria(
                criteria.getTitle(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating(),
                criteria.getCurrency(),
                criteria.getSpecialOffer(),
                criteria.getSupplierName()
        );
        
        // Execute query with specifications (database-level filtering)
        List<Activity> activities = activityRepository.findAll(spec);
        
        // Map to DTOs
        return activityMapper.toDtoList(activities);
    }
}
```

**Key Improvements:**
- Lines reduced from 67 to ~50
- No code duplication (mapper handles all conversions)
- No in-memory filtering (`findAll()` then stream filter)
- Proper `Optional` handling with `orElseThrow()`
- Clean separation: service orchestrates, doesn't implement logic

### 5.2 Create `SupplierService.java`

**File**: `src/main/java/com/getourguide/interview/service/SupplierService.java`

```java
package com.getourguide.interview.service;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.entity.Supplier;
import com.getourguide.interview.mapper.SupplierMapper;
import com.getourguide.interview.repository.SupplierRepository;
import com.getourguide.interview.specification.SupplierSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Supplier operations.
 */
@Service
@AllArgsConstructor
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    
    /**
     * Get all suppliers
     */
    public List<SupplierDto> getSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return supplierMapper.toDtoList(suppliers);
    }
    
    /**
     * Get supplier by ID
     */
    public SupplierDto getSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + supplierId));
        return supplierMapper.toDto(supplier);
    }
    
    /**
     * Search suppliers by multiple criteria
     */
    public List<SupplierDto> searchSuppliers(SupplierSearchCriteria criteria) {
        Specification<Supplier> spec = SupplierSpecifications.withCriteria(
                criteria.getName(),
                criteria.getAddress(),
                criteria.getZip(),
                criteria.getCity(),
                criteria.getCountry()
        );
        
        List<Supplier> suppliers = supplierRepository.findAll(spec);
        return supplierMapper.toDtoList(suppliers);
    }
}
```

---

## Step 6: Update Controllers

### 6.1 Update `ActivitiesController.java`

**File**: `src/main/java/com/getourguide/interview/controller/ActivitiesController.java`

**Add the new search endpoint:**

```java
package com.getourguide.interview.controller;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class ActivitiesController {

    private final ActivityService activityService;

    /**
     * Get all activities
     * GET /activities
     */
    @GetMapping("/activities")
    public ResponseEntity<List<ActivityDto>> activities() {
        return ResponseEntity.ok(activityService.getActivities());
    }

    /**
     * Get activity by ID
     * GET /activities/{id}
     */
    @GetMapping("/activities/{id}")
    public ResponseEntity<ActivityDto> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    /**
     * Legacy search by title (path variable)
     * GET /activities/search/{search}
     * Keep for backward compatibility
     */
    @GetMapping("/activities/search/{search}")
    public ResponseEntity<List<ActivityDto>> searchByTitle(@PathVariable String search) {
        return ResponseEntity.ok(activityService.searchActivitiesByTitle(search));
    }

    /**
     * NEW: Advanced search with query parameters
     * GET /activities/search?title=X&minPrice=Y&maxPrice=Z&minRating=R&currency=C&specialOffer=true&supplierName=S
     * 
     * Example requests:
     * - /activities/search?title=museum
     * - /activities/search?minPrice=50&maxPrice=200
     * - /activities/search?title=tour&minRating=4.5&currency=EUR
     * - /activities/search?specialOffer=true&supplierName=Berlin
     * 
     * All parameters are optional - only non-null values are used for filtering
     */
    @GetMapping("/activities/search")
    public ResponseEntity<List<ActivityDto>> searchActivities(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Boolean specialOffer,
            @RequestParam(required = false) String supplierName) {
        
        // Build search criteria from query parameters
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title(title)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .currency(currency)
                .specialOffer(specialOffer)
                .supplierName(supplierName)
                .build();
        
        return ResponseEntity.ok(activityService.searchActivities(criteria));
    }
}
```

**Note:** Spring will now route:
- `/activities/search/{search}` → `searchByTitle()` (path variable)
- `/activities/search?param=value` → `searchActivities()` (query params)

### 6.2 Update `SupplierController.java`

**File**: `src/main/java/com/getourguide/interview/controller/SupplierController.java`

**Replace current implementation with:**

```java
package com.getourguide.interview.controller;

import com.getourguide.interview.dto.SupplierDto;
import com.getourguide.interview.dto.search.SupplierSearchCriteria;
import com.getourguide.interview.service.SupplierService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class SupplierController {
    
    private final SupplierService supplierService;
    
    /**
     * Get all suppliers
     * GET /suppliers
     */
    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierDto>> getSuppliers() {
        return ResponseEntity.ok(supplierService.getSuppliers());
    }
    
    /**
     * Get supplier by ID
     * GET /suppliers/{id}
     */
    @GetMapping("/suppliers/{id}")
    public ResponseEntity<SupplierDto> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplier(id));
    }
    
    /**
     * Search suppliers with query parameters
     * GET /suppliers/search?name=X&city=Y&country=Z&zip=P&address=A
     * 
     * Example requests:
     * - /suppliers/search?city=Berlin
     * - /suppliers/search?country=Germany&city=Munich
     * - /suppliers/search?name=Tours&city=Berlin
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
        
        return ResponseEntity.ok(supplierService.searchSuppliers(criteria));
    }
}
```

---

## Step 7: Add Validation (Optional but Recommended)

### 7.1 Create Package
```
src/main/java/com/getourguide/interview/validation/
```

### 7.2 Create `SearchCriteriaValidator.java`

**File**: `src/main/java/com/getourguide/interview/validation/SearchCriteriaValidator.java`

```java
package com.getourguide.interview.validation;

import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates search criteria to ensure data integrity
 */
@Component
public class SearchCriteriaValidator {
    
    /**
     * Validate ActivitySearchCriteria
     * @return List of validation error messages (empty if valid)
     */
    public List<String> validate(ActivitySearchCriteria criteria) {
        List<String> errors = new ArrayList<>();
        
        // Price validation
        if (criteria.getMinPrice() != null && criteria.getMinPrice() < 0) {
            errors.add("minPrice must be >= 0");
        }
        
        if (criteria.getMaxPrice() != null && criteria.getMaxPrice() < 0) {
            errors.add("maxPrice must be >= 0");
        }
        
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null 
                && criteria.getMinPrice() > criteria.getMaxPrice()) {
            errors.add("minPrice must be <= maxPrice");
        }
        
        // Rating validation
        if (criteria.getMinRating() != null) {
            if (criteria.getMinRating() < 0.0 || criteria.getMinRating() > 5.0) {
                errors.add("minRating must be between 0.0 and 5.0");
            }
        }
        
        return errors;
    }
    
    /**
     * Check if criteria is valid
     */
    public boolean isValid(ActivitySearchCriteria criteria) {
        return validate(criteria).isEmpty();
    }
}
```

### 7.3 Use Validator in Service

**Update `ActivityService.searchActivities()` to include validation:**

```java
public List<ActivityDto> searchActivities(ActivitySearchCriteria criteria) {
    // Validate criteria
    List<String> validationErrors = validator.validate(criteria);
    if (!validationErrors.isEmpty()) {
        throw new IllegalArgumentException("Invalid search criteria: " + String.join(", ", validationErrors));
    }
    
    // Rest of the method...
    Specification<Activity> spec = ActivitySpecifications.withCriteria(...);
    // ...
}
```

---

## Step 8: Improve Error Handling

### 8.1 Create Custom Exceptions

**File**: `src/main/java/com/getourguide/interview/exception/ResourceNotFoundException.java`

```java
package com.getourguide.interview.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**File**: `src/main/java/com/getourguide/interview/exception/InvalidSearchCriteriaException.java`

```java
package com.getourguide.interview.exception;

import java.util.List;

public class InvalidSearchCriteriaException extends RuntimeException {
    private final List<String> errors;
    
    public InvalidSearchCriteriaException(List<String> errors) {
        super("Invalid search criteria: " + String.join(", ", errors));
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
```

### 8.2 Update Error Handler

**File**: `src/main/java/com/getourguide/interview/error/ErrorHandler.java`

**Add new exception handlers:**

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException e) {
    log.error("Resource not found", e);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
}

@ExceptionHandler(InvalidSearchCriteriaException.class)
public ResponseEntity<Map<String, Object>> handleInvalidSearchCriteria(InvalidSearchCriteriaException e) {
    log.error("Invalid search criteria", e);
    Map<String, Object> response = new HashMap<>();
    response.put("error", "Invalid search criteria");
    response.put("details", e.getErrors());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
}
```

### 8.3 Use Custom Exceptions in Services

**Update service methods:**

```java
public ActivityDto getActivity(Long activityId) {
    Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));
    return activityMapper.toDto(activity);
}

public List<ActivityDto> searchActivities(ActivitySearchCriteria criteria) {
    List<String> validationErrors = validator.validate(criteria);
    if (!validationErrors.isEmpty()) {
        throw new InvalidSearchCriteriaException(validationErrors);
    }
    // ... rest of method
}
```

---

## Testing Your Implementation

### Manual Testing with cURL/Postman

**1. Get all activities:**
```bash
curl http://localhost:8080/activities
```

**2. Search by title:**
```bash
curl "http://localhost:8080/activities/search?title=museum"
```

**3. Search by price range:**
```bash
curl "http://localhost:8080/activities/search?minPrice=50&maxPrice=200"
```

**4. Complex search:**
```bash
curl "http://localhost:8080/activities/search?title=tour&minRating=4.5&currency=EUR&specialOffer=true"
```

**5. Search with supplier filter:**
```bash
curl "http://localhost:8080/activities/search?supplierName=Berlin"
```

**6. Search suppliers:**
```bash
curl "http://localhost:8080/suppliers/search?city=Berlin&country=Germany"
```

### Expected SQL Queries

When you search with: `?title=museum&minPrice=50&maxPrice=200`

JPA generates (conceptually):
```sql
SELECT a.* 
FROM getyourguide.activity a
WHERE LOWER(a.title) LIKE '%museum%'
  AND a.price BETWEEN 50 AND 200;
```

When searching with supplier: `?supplierName=Berlin`

```sql
SELECT a.* 
FROM getyourguide.activity a
LEFT JOIN getyourguide.supplier s ON a.supplier_id = s.id
WHERE LOWER(s.name) LIKE '%berlin%';
```

---

## Interview Discussion Points

### When Explaining Your Implementation:

**1. SOLID Principles Applied:**
- **SRP**: Each class has one responsibility (mapper maps, specification queries, service orchestrates)
- **OCP**: Can add new search criteria without modifying existing code
- **LSP**: Specification interface works for any entity type
- **ISP**: Small interfaces (JpaSpecificationExecutor, JpaRepository)
- **DIP**: Depend on repository interfaces, not concrete implementations

**2. Design Patterns Used:**
- **Specification Pattern**: Dynamic, composable queries
- **Builder Pattern**: Clean criteria object construction
- **Mapper Pattern**: Separation of entity and DTO layers
- **Strategy Pattern**: Different search strategies per entity

**3. Performance Considerations:**
- Database-level filtering (not in-memory)
- Proper indexing on searchable columns
- LEFT JOIN for optional relationships
- No N+1 query problems

**4. Extensibility:**
- "To add pagination, I'd change `findAll(spec)` to `findAll(spec, pageable)` and return `Page<DTO>`"
- "To add sorting, Pageable includes Sort or we can add explicit Sort parameter"
- "To add OR logic, Specifications support `.or()` combinations"

**5. Trade-offs:**
- More files/classes vs. simpler monolithic service
- Type-safety vs. dynamic query strings
- Reusability vs. entity-specific optimizations

---

## Checklist for Interview Implementation

- [ ] Create search criteria DTOs with Builder pattern
- [ ] Create mappers to eliminate duplication
- [ ] Create specifications for each searchable field
- [ ] Add JpaSpecificationExecutor to repositories
- [ ] Refactor services to use specifications and mappers
- [ ] Add query parameter endpoints to controllers
- [ ] Add validation for input criteria
- [ ] Improve error handling with custom exceptions
- [ ] Test with multiple query parameter combinations
- [ ] Prepare to explain SOLID principles and patterns used

---

## Summary

This implementation demonstrates:
- **Clean Architecture**: Clear separation of layers
- **SOLID Principles**: Applied throughout the design
- **Design Patterns**: Specification, Builder, Mapper, Strategy
- **Performance**: Database-level filtering, no in-memory operations
- **Maintainability**: Easy to extend with new criteria
- **Testability**: Each component mockable and testable
- **Interview-Ready**: 30-45 minutes to implement with clear explanations

**Implementation Time Estimate:**
- DTOs: 10 minutes
- Mappers: 10 minutes
- Specifications: 15 minutes
- Repository/Service updates: 10 minutes
- Controller updates: 10 minutes
- Validation & Error handling: 10 minutes (optional)
- **Total: ~45 minutes (basic) to 65 minutes (with validation)**

Good luck with your interview practice!
