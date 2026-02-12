# Testing Guide - Search by Params Implementation

## What Was Implemented

### ✅ Completed Components

#### 1. **DTOs Created**
- `SupplierDto.java` - Data transfer object for supplier API responses
- `ActivitySearchCriteria.java` - Already existed
- `SupplierSearchCriteria.java` - Search criteria with 5 optional fields

#### 2. **Specifications Created**
- `ActivitySpecifications.java` - Already existed
- `SupplierSpecifications.java` - 5 search methods + fromCriteria()

#### 3. **Repositories**
- `ActivityRepository.java` - Already had JpaSpecificationExecutor
- `SupplierRepository.java` - Created with JpaSpecificationExecutor

#### 4. **Search Services**
- `SearchService.java` interface - Already existed
- `ActivitySearchService.java` - Already existed
- `SupplierSearchService.java` - NEW - Complete implementation

#### 5. **Controllers Updated**
- `ActivitiesController.java` - Fixed @Qualifier bug (was "supplierSearchService", now "activitySearchService")
- `SupplierController.java` - Completely refactored to use SearchService

---

## API Endpoints Available

### Activities

#### Get All Activities
```bash
curl http://localhost:8080/activities
```

#### Get Activity by ID
```bash
curl http://localhost:8080/activities/1
```

#### Search Activities (Multiple Criteria)
```bash
# By title
curl "http://localhost:8080/activities/search?title=museum"

# By price range
curl "http://localhost:8080/activities/search?minPrice=50&maxPrice=200"

# By rating
curl "http://localhost:8080/activities/search?minRating=4.5"

# Complex search
curl "http://localhost:8080/activities/search?title=tour&minPrice=50&maxPrice=200&minRating=4.5&currency=EUR"

# With supplier filter (JOIN query)
curl "http://localhost:8080/activities/search?supplierName=Berlin"

# Special offers only
curl "http://localhost:8080/activities/search?specialOffer=true"
```

### Suppliers (NEW)

#### Get All Suppliers
```bash
curl http://localhost:8080/suppliers
```

#### Get Supplier by ID
```bash
curl http://localhost:8080/suppliers/1
```

#### Search Suppliers (Multiple Criteria)
```bash
# By name
curl "http://localhost:8080/suppliers/search?name=Tours"

# By city
curl "http://localhost:8080/suppliers/search?city=Berlin"

# By country
curl "http://localhost:8080/suppliers/search?country=Germany"

# By zip code
curl "http://localhost:8080/suppliers/search?zip=10115"

# Complex search
curl "http://localhost:8080/suppliers/search?country=Germany&city=Berlin&name=Tours"

# By address
curl "http://localhost:8080/suppliers/search?address=Main"
```

---

## Testing Workflow

### Step 1: Start the Application
```bash
./gradlew bootRun
```

Or run from your IDE: `GetYourGuideApplication.main()`

### Step 2: Test Basic Endpoints

**Test Activities:**
```bash
# Should return all activities
curl http://localhost:8080/activities | jq

# Should return activity with ID 1
curl http://localhost:8080/activities/1 | jq
```

**Test Suppliers:**
```bash
# Should return all suppliers
curl http://localhost:8080/suppliers | jq

# Should return supplier with ID 1
curl http://localhost:8080/suppliers/1 | jq
```

### Step 3: Test Search Functionality

**Test Single Parameter Search:**
```bash
# Search activities by title
curl "http://localhost:8080/activities/search?title=museum" | jq

# Search suppliers by city
curl "http://localhost:8080/suppliers/search?city=Berlin" | jq
```

**Test Multiple Parameter Search:**
```bash
# Search activities with multiple criteria
curl "http://localhost:8080/activities/search?title=tour&minPrice=50&maxPrice=200" | jq

# Search suppliers with multiple criteria
curl "http://localhost:8080/suppliers/search?country=Germany&city=Berlin" | jq
```

**Test Empty Search (Should Return All):**
```bash
# No parameters = return all
curl "http://localhost:8080/activities/search" | jq
curl "http://localhost:8080/suppliers/search" | jq
```

### Step 4: Test Edge Cases

**Test Non-Existent Data:**
```bash
# Should return empty array []
curl "http://localhost:8080/activities/search?title=nonexistent" | jq
curl "http://localhost:8080/suppliers/search?city=nonexistent" | jq
```

**Test Invalid ID:**
```bash
# Should return 500 with error message
curl http://localhost:8080/activities/99999
curl http://localhost:8080/suppliers/99999
```

---

## Expected Responses

### Successful Search Response (Activities)
```json
[
  {
    "id": 1,
    "title": "Museum Tour",
    "price": 75,
    "currency": "EUR",
    "rating": 4.8,
    "specialOffer": true,
    "supplierName": "Berlin Tours GmbH"
  }
]
```

### Successful Search Response (Suppliers)
```json
[
  {
    "id": 1,
    "name": "Berlin Tours GmbH",
    "address": "123 Main Street",
    "zip": "10115",
    "city": "Berlin",
    "country": "Germany"
  }
]
```

### No Results
```json
[]
```

### Error Response (Not Found)
```
Activity not found with id: 99999
```
or
```
Supplier not found with id: 99999
```

---

## Postman Collection

If using Postman, create these requests:

### Activities Collection

**1. Get All Activities**
- Method: GET
- URL: `http://localhost:8080/activities`

**2. Get Activity by ID**
- Method: GET
- URL: `http://localhost:8080/activities/1`

**3. Search Activities**
- Method: GET
- URL: `http://localhost:8080/activities/search`
- Params:
  - title: museum
  - minPrice: 50
  - maxPrice: 200

### Suppliers Collection

**1. Get All Suppliers**
- Method: GET
- URL: `http://localhost:8080/suppliers`

**2. Get Supplier by ID**
- Method: GET
- URL: `http://localhost:8080/suppliers/1`

**3. Search Suppliers**
- Method: GET
- URL: `http://localhost:8080/suppliers/search`
- Params:
  - city: Berlin
  - country: Germany

---

## Verifying Database-Level Filtering

To verify that queries are happening at the database level (not in-memory), check your application logs. You should see SQL like:

**Activity Search:**
```sql
SELECT * FROM getyourguide.activity 
WHERE LOWER(title) LIKE '%museum%' 
  AND price BETWEEN 50 AND 200
```

**Supplier Search:**
```sql
SELECT * FROM getyourguide.supplier 
WHERE LOWER(city) LIKE '%berlin%' 
  AND LOWER(country) LIKE '%germany%'
```

---

## Key Improvements Over Original Implementation

### Before (Supplier Controller)
- ❌ Used EntityManager with native SQL
- ❌ Loaded ALL suppliers then filtered in Java
- ❌ Returned entity directly (no DTO)
- ❌ Poor search logic (only returned first match)

### After (Supplier Controller)
- ✅ Uses Spring Data JPA with Specifications
- ✅ Database-level filtering with WHERE clauses
- ✅ Returns SupplierDto (proper API contract)
- ✅ Returns all matches, not just first one
- ✅ Flexible multi-criteria search

---

## Architecture Highlights

### Design Patterns Used
1. **Specification Pattern** - Dynamic query building
2. **Strategy Pattern** - SearchService implementations
3. **Builder Pattern** - Criteria objects
4. **Generic Programming** - SearchService<ENTITY, DTO, CRITERIA>

### SOLID Principles
1. **Single Responsibility** - Each class has one job
2. **Open/Closed** - Easy to add new search criteria
3. **Liskov Substitution** - SearchService implementations are interchangeable
4. **Interface Segregation** - Small, focused SearchService interface
5. **Dependency Inversion** - Controllers depend on SearchService interface

---

## Next Steps (Optional)

If you want to enhance further:

### 1. Add Pagination
```java
Page<DTO> search(CRITERIA criteria, Pageable pageable);
```

### 2. Add Validation
```java
@Min(0)
private Integer minPrice;

@Max(10000)
private Integer maxPrice;
```

### 3. Add Custom Exceptions
```java
public class ResourceNotFoundException extends RuntimeException { }
public class InvalidSearchCriteriaException extends RuntimeException { }
```

### 4. Add API Documentation
```java
@Operation(summary = "Search activities by multiple criteria")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful search"),
    @ApiResponse(responseCode = "400", description = "Invalid criteria")
})
```

---

## Summary

✅ **Supplier search fully implemented** following the same pattern as Activities  
✅ **Both entities use generic SearchService interface**  
✅ **Specification pattern for flexible, composable queries**  
✅ **Database-level filtering (no in-memory operations)**  
✅ **Clean separation of concerns (Controller → Service → Repository)**  
✅ **DTOs for API responses (not exposing entities)**  
✅ **All query parameters are optional**  

The implementation is complete and ready for testing!

---

## Automated Tests

### Running Tests

**Run all tests:**
```bash
./gradlew test
```

**Run specific test class:**
```bash
./gradlew test --tests SupplierSearchServiceTest
./gradlew test --tests SupplierSpecificationsTest
./gradlew test --tests SupplierControllerIntegrationTest
```

**Run tests with output:**
```bash
./gradlew test --info
```

### Test Coverage

**Created Test Files:**

**Activities:**
1. `ActivitySearchServiceTest.java` - 15 unit tests for service layer
2. `ActivitySpecificationsTest.java` - 29 unit tests for specifications
3. `ActivityControllerIntegrationTest.java` - 21 integration tests

**Suppliers:**
4. `SupplierSearchServiceTest.java` - 10 unit tests for service layer
5. `SupplierSpecificationsTest.java` - 17 unit tests for specifications
6. `SupplierControllerIntegrationTest.java` - 14 integration tests

**Total**: 106 tests covering all aspects of Activity and Supplier search functionality

**What's Tested:**
- ✅ Service layer business logic (with mocks)
- ✅ Specification creation and composition
- ✅ HTTP endpoints and status codes
- ✅ Database queries and filtering
- ✅ DTO mapping
- ✅ Error handling (not found scenarios)
- ✅ Case-insensitive search
- ✅ Partial matching
- ✅ Multiple criteria combinations
- ✅ Edge cases (null, empty, whitespace)

See `TEST_CASES_SUMMARY.md` for detailed test documentation.
