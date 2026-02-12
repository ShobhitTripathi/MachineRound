# Error Handling Guide - Best Practices & Interview Points

## Current Error Handling in the Implementation

### 1. Repository Layer - Optional Pattern

**Current Implementation:**
```java
// In ActivitySearchService.java
public ActivityDto findById(Long id) {
    Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));
    return mapToDto(activity);
}
```

**What's Good:**
- Uses `Optional` to handle absence of data
- Provides descriptive error message
- Prevents NullPointerException

**What Could Be Improved:**
- Using generic `RuntimeException` instead of custom exception
- No HTTP status code mapping
- No structured error response

---

### 2. Specification Layer - Null Safety

**Current Implementation:**
```java
public static Specification<Activity> hasTitle(String title) {
    return (root, query, cb) -> {
        if (title == null || title.trim().isEmpty()) {
            return null;  // Gracefully handle null/empty
        }
        // Build predicate
    };
}
```

**What's Good:**
- Defensive programming (checks for null/empty)
- Graceful degradation (returns null instead of throwing)
- No crash on invalid input

**What Could Be Improved:**
- Could validate and throw meaningful exception for truly invalid input
- No logging of skipped criteria

---

### 3. Controller Layer - Basic Error Responses

**Current Implementation:**
```java
@GetMapping("/activities/{id}")
public ResponseEntity<ActivityDto> getActivity(@PathVariable Long id) {
    return ResponseEntity.ok(activityService.findById(id));
}
```

**What Happens on Error:**
- Exception bubbles up
- Global error handler catches it (if configured)
- Returns generic 500 error

**What Could Be Improved:**
- No validation of input parameters
- No specific error handling per endpoint
- No custom error responses

---

## Error Handling Best Practices

### 1. Exception Hierarchy

**Best Practice:** Create a hierarchy of custom exceptions

```java
// Base exception
public abstract class SearchApiException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> details;
    
    public SearchApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    public SearchApiException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    // Getters and methods to add details
}

// Specific exceptions
public class ResourceNotFoundException extends SearchApiException {
    public ResourceNotFoundException(String resource, Long id) {
        super(
            String.format("%s not found with id: %s", resource, id),
            "RESOURCE_NOT_FOUND"
        );
        addDetail("resource", resource);
        addDetail("id", id);
    }
}

public class InvalidSearchCriteriaException extends SearchApiException {
    public InvalidSearchCriteriaException(String message, List<String> errors) {
        super(message, "INVALID_CRITERIA");
        addDetail("validationErrors", errors);
    }
}

public class DatabaseException extends SearchApiException {
    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
    }
}
```

**Benefits:**
- Clear exception types
- Structured error information
- Easy to handle differently based on type
- Supports error codes for API consumers

---

### 2. Global Exception Handler

**Best Practice:** Use `@ControllerAdvice` for centralized error handling

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(ex.getDetails())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidSearchCriteriaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCriteria(
            InvalidSearchCriteriaException ex,
            WebRequest request) {
        
        log.warn("Invalid search criteria: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(ex.getDetails())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            DataAccessException ex,
            WebRequest request) {
        
        log.error("Database error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An error occurred while accessing the database")
                .errorCode("DATABASE_ERROR")
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Benefits:**
- Centralized error handling
- Consistent error responses
- Proper HTTP status codes
- Logging at appropriate levels
- No exception details leaked to clients (security)

---

### 3. Structured Error Response

**Best Practice:** Define a consistent error response structure

```java
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private Map<String, Object> details;
    private String path;
}
```

**Example Response:**
```json
{
  "timestamp": "2026-02-11T15:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Activity not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND",
  "details": {
    "resource": "Activity",
    "id": 999
  },
  "path": "/api/activities/999"
}
```

**Benefits:**
- Consistent structure
- Easy for clients to parse
- Includes debugging information
- Machine-readable error codes

---

### 4. Input Validation

**Best Practice:** Validate input at controller layer

```java
@PostMapping("/activities/search")
public ResponseEntity<List<ActivityDto>> searchActivities(
        @Valid @RequestBody ActivitySearchRequest request) {
    // Validation happens automatically via @Valid
}

// In ActivitySearchRequest
public class ActivitySearchRequest {
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;
    
    @Min(value = 0, message = "Minimum price must be 0 or greater")
    private Integer minPrice;
    
    @Min(value = 0, message = "Maximum price must be 0 or greater")
    private Integer maxPrice;
    
    @DecimalMin(value = "0.0", message = "Rating must be between 0.0 and 5.0")
    @DecimalMax(value = "5.0", message = "Rating must be between 0.0 and 5.0")
    private Double minRating;
    
    @AssertTrue(message = "Maximum price must be greater than or equal to minimum price")
    private boolean isPriceRangeValid() {
        if (minPrice != null && maxPrice != null) {
            return maxPrice >= minPrice;
        }
        return true;
    }
}
```

**Validation Exception Handler:**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request) {
    
    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> 
        validationErrors.put(error.getField(), error.getDefaultMessage())
    );
    
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input parameters")
            .errorCode("VALIDATION_ERROR")
            .details(Map.of("fieldErrors", validationErrors))
            .path(request.getDescription(false))
            .build();
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

### 5. Service Layer Error Handling

**Best Practice:** Let exceptions bubble up, but add context

```java
@Service
public class ActivitySearchService {
    
    public ActivityDto findById(Long id) {
        return activityRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
    }
    
    public List<ActivityDto> search(ActivitySearchCriteria criteria) {
        try {
            validateCriteria(criteria);
            Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);
            List<Activity> activities = activityRepository.findAll(spec);
            return mapToDtoList(activities);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to search activities", ex);
        }
    }
    
    private void validateCriteria(ActivitySearchCriteria criteria) {
        List<String> errors = new ArrayList<>();
        
        if (criteria.getMinPrice() != null && criteria.getMinPrice() < 0) {
            errors.add("Minimum price must be 0 or greater");
        }
        
        if (criteria.getMaxPrice() != null && criteria.getMaxPrice() < 0) {
            errors.add("Maximum price must be 0 or greater");
        }
        
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null 
                && criteria.getMinPrice() > criteria.getMaxPrice()) {
            errors.add("Minimum price must be less than or equal to maximum price");
        }
        
        if (criteria.getMinRating() != null 
                && (criteria.getMinRating() < 0.0 || criteria.getMinRating() > 5.0)) {
            errors.add("Rating must be between 0.0 and 5.0");
        }
        
        if (!errors.isEmpty()) {
            throw new InvalidSearchCriteriaException("Invalid search criteria", errors);
        }
    }
}
```

---

## HTTP Status Codes to Use

| Status Code | When to Use | Example |
|-------------|-------------|---------|
| **200 OK** | Successful GET request | Found activities matching criteria |
| **201 Created** | Successful POST (resource created) | Created new activity |
| **204 No Content** | Successful DELETE | Deleted activity |
| **400 Bad Request** | Invalid input data | Invalid price range, malformed JSON |
| **404 Not Found** | Resource doesn't exist | Activity ID 999 doesn't exist |
| **409 Conflict** | Business rule violation | Duplicate activity title |
| **422 Unprocessable Entity** | Valid format but semantic errors | Price is negative |
| **500 Internal Server Error** | Server-side error | Database connection failed |
| **503 Service Unavailable** | Service temporarily down | Database is down |

---

## Error Handling Strategies

### 1. Fail Fast

**Concept:** Validate inputs early and throw exceptions immediately

```java
public ActivityDto findById(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("Activity ID must be positive");
    }
    // Continue with logic
}
```

**Benefits:**
- Prevents invalid state propagation
- Easier to debug (fail at source)
- Clear error messages

---

### 2. Graceful Degradation

**Concept:** Handle errors without crashing, provide fallback

```java
public static Specification<Activity> hasTitle(String title) {
    return (root, query, cb) -> {
        if (title == null || title.trim().isEmpty()) {
            return null;  // Gracefully skip this filter
        }
        // Build predicate
    };
}
```

**Benefits:**
- System remains functional
- Better user experience
- Partial results better than no results

---

### 3. Circuit Breaker Pattern

**Concept:** Prevent cascading failures in distributed systems

```java
@CircuitBreaker(name = "activityService", fallbackMethod = "getFallbackActivities")
public List<ActivityDto> search(ActivitySearchCriteria criteria) {
    // Normal search logic
}

private List<ActivityDto> getFallbackActivities(ActivitySearchCriteria criteria, Exception ex) {
    log.warn("Search failed, returning empty list", ex);
    return List.of();
}
```

**When to Use:**
- Calling external services
- Database is frequently unavailable
- Need high availability

---

### 4. Retry Logic

**Concept:** Automatically retry failed operations

```java
@Retryable(
    value = { DataAccessException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000)
)
public List<ActivityDto> search(ActivitySearchCriteria criteria) {
    // Search logic
}
```

**When to Use:**
- Transient network errors
- Database connection timeouts
- Rate-limited APIs

---

## Logging Best Practices

### Log Levels

```java
// DEBUG - Detailed debugging information
log.debug("Searching activities with criteria: {}", criteria);

// INFO - Normal operation events
log.info("Activity search completed. Found {} results", results.size());

// WARN - Unexpected but handled situations
log.warn("Activity not found with id: {}", id);

// ERROR - Error conditions requiring attention
log.error("Database error while searching activities", ex);

// CRITICAL - System-threatening errors (use ERROR with specific markers)
log.error("CRITICAL: Database connection pool exhausted", ex);
```

### Structured Logging

```java
log.info("Activity search completed", 
    kv("criteria", criteria),
    kv("resultCount", results.size()),
    kv("executionTimeMs", executionTime),
    kv("userId", userId)
);
```

### What to Log

**Do Log:**
- Request IDs for tracing
- User IDs (if applicable)
- Input parameters (sanitized)
- Execution time
- Error stack traces
- External API calls

**Don't Log:**
- Sensitive data (passwords, tokens)
- Full credit card numbers
- Personal identifiable information (PII)
- Entire request/response bodies (unless debugging)

---

## Testing Error Scenarios

### Unit Tests

```java
@Test
@DisplayName("Should throw ResourceNotFoundException when activity not found")
void testFindByIdNotFound() {
    // Given
    when(activityRepository.findById(999L)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> {
        activitySearchService.findById(999L);
    });
}

@Test
@DisplayName("Should throw InvalidSearchCriteriaException when minPrice > maxPrice")
void testInvalidPriceRange() {
    // Given
    ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
            .minPrice(200)
            .maxPrice(50)
            .build();
    
    // When & Then
    assertThrows(InvalidSearchCriteriaException.class, () -> {
        activitySearchService.search(criteria);
    });
}
```

### Integration Tests

```java
@Test
@DisplayName("Should return 404 when activity not found")
void testGetActivityByIdNotFound() {
    // When
    ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
        baseUrl + "/activities/99999",
        ErrorResponse.class
    );
    
    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
}

@Test
@DisplayName("Should return 400 when search criteria is invalid")
void testSearchWithInvalidCriteria() {
    // When
    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        baseUrl + "/activities/search?minPrice=200&maxPrice=50",
        HttpMethod.GET,
        null,
        ErrorResponse.class
    );
    
    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("INVALID_CRITERIA", response.getBody().getErrorCode());
}
```

---

## Security Considerations

### 1. Don't Leak Sensitive Information

**Bad:**
```java
catch (SQLException ex) {
    return ResponseEntity.status(500)
        .body("SQL Error: " + ex.getMessage());  // Leaks database structure
}
```

**Good:**
```java
catch (SQLException ex) {
    log.error("Database error", ex);  // Log details server-side
    return ResponseEntity.status(500)
        .body(new ErrorResponse("An error occurred while processing your request"));
}
```

### 2. Sanitize Error Messages

**Bad:**
```java
throw new RuntimeException("User email john@example.com not found");  // Leaks user data
```

**Good:**
```java
throw new ResourceNotFoundException("User", userId);  // No sensitive data
```

### 3. Rate Limiting on Errors

```java
@RateLimiter(name = "searchApi")
public List<ActivityDto> search(ActivitySearchCriteria criteria) {
    // Prevent abuse through error responses
}
```

---

## Interview Talking Points

### "How do you handle errors in your API?"

> "I use a hierarchical exception structure with custom exceptions for different error types - ResourceNotFoundException, InvalidSearchCriteriaException, and DatabaseException. Each exception includes structured information like error codes and contextual details. I have a global exception handler using @ControllerAdvice that catches these exceptions, logs them appropriately, and returns consistent error responses with proper HTTP status codes. This ensures clients receive predictable, parseable error messages."

### "What HTTP status codes do you use?"

> "I use 404 for resources not found, 400 for invalid input like bad search criteria, 422 for semantic validation errors, and 500 for server errors. I always include a structured error response with a timestamp, error code, message, and relevant details. This helps API consumers handle errors programmatically rather than parsing error messages."

### "How do you validate input?"

> "I use Bean Validation (JSR-303) annotations at the DTO level for basic validation like @Min, @Max, @Size. For complex business rules like 'minPrice must be less than maxPrice', I use custom validators or service-level validation. Validation errors are caught by @ControllerAdvice and returned as 400 Bad Request with detailed field-level error messages."

### "How do you handle database errors?"

> "I catch DataAccessException from Spring Data JPA and wrap it in a custom DatabaseException. This prevents database implementation details from leaking to clients. I log the full stack trace server-side for debugging but return a generic error message to the client. For transient errors, I can use @Retryable to automatically retry the operation."

### "What about logging?"

> "I use structured logging with appropriate log levels. DEBUG for detailed flow, INFO for normal operations, WARN for handled exceptions like resources not found, and ERROR for unexpected errors. I include context like request IDs, user IDs, and execution time but never log sensitive data. This makes troubleshooting easier and supports log aggregation tools."

---

## Recommended Improvements for Current Code

### Priority 1: Custom Exceptions

Create exception hierarchy:
- `ResourceNotFoundException` for 404 errors
- `InvalidSearchCriteriaException` for 400 errors
- `DatabaseException` for 500 errors

### Priority 2: Global Exception Handler

Implement `@ControllerAdvice` with handlers for:
- Custom exceptions
- `DataAccessException`
- `MethodArgumentNotValidException`
- Generic `Exception`

### Priority 3: Structured Error Response

Create `ErrorResponse` DTO with:
- Timestamp
- HTTP status
- Error message
- Error code
- Details map
- Request path

### Priority 4: Input Validation

Add validation to search criteria:
- Price range validation
- Rating range validation
- String length limits

### Priority 5: Logging

Add structured logging:
- Log all exceptions
- Log search criteria (sanitized)
- Log execution time
- Include correlation IDs

---

## Example: Complete Error Handling Flow

```
1. Client Request
   GET /activities/search?minPrice=200&maxPrice=50
   
2. Controller
   Receives request, binds parameters
   
3. Service Layer
   Validates criteria → Throws InvalidSearchCriteriaException
   
4. Global Exception Handler
   Catches InvalidSearchCriteriaException
   Logs warning
   Creates ErrorResponse
   
5. HTTP Response
   Status: 400 Bad Request
   Body: {
     "timestamp": "2026-02-11T15:30:45",
     "status": 400,
     "error": "Bad Request",
     "message": "Invalid search criteria",
     "errorCode": "INVALID_CRITERIA",
     "details": {
       "validationErrors": [
         "Minimum price must be less than or equal to maximum price"
       ]
     },
     "path": "/activities/search"
   }
```

---

## Summary

**Key Principles:**
1. Fail fast with clear error messages
2. Use custom exceptions with structured data
3. Centralize error handling with @ControllerAdvice
4. Return consistent error responses
5. Use appropriate HTTP status codes
6. Log errors with context
7. Never leak sensitive information
8. Validate input early
9. Test error scenarios
10. Handle both expected and unexpected errors

**For Production:**
- Add retry logic for transient failures
- Implement circuit breakers for external services
- Add correlation IDs for request tracing
- Monitor error rates and patterns
- Set up alerts for critical errors

**For Interviews:**
- Explain exception hierarchy
- Discuss HTTP status code choices
- Demonstrate validation strategies
- Show logging best practices
- Discuss security considerations

Error handling is not an afterthought - it's a core part of building reliable, maintainable APIs!
