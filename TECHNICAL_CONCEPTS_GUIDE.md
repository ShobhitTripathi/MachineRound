# Technical Concepts Guide - Complete Reference

This document explains all technical concepts, patterns, and technologies used in the Search by Params API implementation.

---

## Table of Contents

1. [Java 21 Features](#java-21-features)
2. [Java Collections & Data Structures](#java-collections--data-structures)
3. [Functional Programming in Java](#functional-programming-in-java)
4. [Spring Boot Framework](#spring-boot-framework)
5. [Spring Data JPA](#spring-data-jpa)
6. [JPA Criteria API & Specifications](#jpa-criteria-api--specifications)
7. [Database Concepts](#database-concepts)
8. [Design Patterns](#design-patterns)
9. [SOLID Principles](#solid-principles)
10. [Testing Concepts](#testing-concepts)
11. [Best Practices](#best-practices)

---

## Java 21 Features

### 1. Lambda Expressions

**What**: Anonymous functions that can be passed as arguments

**Syntax**: `(parameters) -> expression` or `(parameters) -> { statements }`

**Where Used in Our Code**:
```java
// In SupplierSpecifications.java
public static Specification<Supplier> hasCity(String city) {
    return (root, query, cb) -> {  // Lambda expression
        if (city == null || city.trim().isEmpty()) {
            return null;
        }
        return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    };
}
```

**Benefits**:
- Concise code (no need for anonymous class boilerplate)
- Enables functional programming style
- Works with functional interfaces (interfaces with single abstract method)

**Interview Tip**:
> "Lambda expressions are syntactic sugar for implementing functional interfaces. They enable passing behavior as parameters, making code more readable and functional."

---

### 2. Stream API

**What**: Process collections in a functional, declarative way

**Where Used in Our Code**:
```java
// In SupplierSearchService.java
private List<SupplierDto> mapToDtoList(List<Supplier> suppliers) {
    return suppliers.stream()                    // Create stream
            .map(this::mapToDto)                // Transform each element
            .collect(Collectors.toList());      // Collect to list
}
```

**Common Operations**:
- **map()** - Transform elements
- **filter()** - Keep only matching elements
- **collect()** - Terminal operation, gather results
- **forEach()** - Execute action on each element
- **reduce()** - Combine elements into single result

**Stream vs Loop**:
```java
// Traditional loop
List<SupplierDto> dtos = new ArrayList<>();
for (Supplier supplier : suppliers) {
    dtos.add(mapToDto(supplier));
}

// Stream (more declarative)
List<SupplierDto> dtos = suppliers.stream()
    .map(this::mapToDto)
    .collect(Collectors.toList());
```

**Benefits**:
- Declarative (what, not how)
- Chainable operations
- Lazy evaluation
- Can be parallelized easily

**Interview Tip**:
> "Streams enable functional-style operations on collections. They're lazy-evaluated, meaning intermediate operations don't execute until a terminal operation is called."

---

### 3. Optional

**What**: Container object that may or may not contain a value

**Where Used in Our Code**:
```java
// In SupplierSearchService.java
public SupplierDto findById(Long id) {
    Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
    return mapToDto(supplier);
}
```

**Common Methods**:
- **Optional.of(value)** - Create Optional with non-null value
- **Optional.empty()** - Create empty Optional
- **Optional.ofNullable(value)** - Create Optional that may be empty
- **isPresent()** - Check if value exists
- **get()** - Get value (throws if empty)
- **orElse(defaultValue)** - Get value or default
- **orElseThrow(exceptionSupplier)** - Get value or throw exception
- **map(function)** - Transform value if present
- **ifPresent(consumer)** - Execute if value present

**Why Use Optional**:
```java
// Bad: Null checks everywhere
Supplier supplier = repository.findById(id);
if (supplier == null) {
    throw new RuntimeException("Not found");
}

// Good: Optional expresses intent clearly
Supplier supplier = repository.findById(id)
    .orElseThrow(() -> new RuntimeException("Not found"));
```

**Interview Tip**:
> "Optional is a container that explicitly communicates 'this might be null'. It forces callers to handle the absence of a value, preventing NullPointerException."

---

### 4. Method References

**What**: Shorthand for lambda expressions that call a single method

**Where Used in Our Code**:
```java
// Method reference
.map(this::mapToDto)

// Equivalent lambda
.map(supplier -> this.mapToDto(supplier))
```

**Types**:
- **Static method**: `ClassName::staticMethod`
- **Instance method**: `instance::instanceMethod`
- **Instance method of arbitrary object**: `ClassName::instanceMethod`
- **Constructor**: `ClassName::new`

**Examples**:
```java
// Static method reference
Arrays.asList("a", "b").forEach(System.out::println);

// Instance method reference
suppliers.stream().map(this::mapToDto)

// Constructor reference
suppliers.stream().map(SupplierDto::new)
```

---

## Java Collections & Data Structures

### 1. List Interface

**What**: Ordered collection allowing duplicates

**Where Used in Our Code**:
```java
public List<SupplierDto> search(SupplierSearchCriteria criteria) {
    List<Supplier> suppliers = supplierRepository.findAll(spec);
    return mapToDtoList(suppliers);
}
```

**Implementations**:
- **ArrayList** - Fast random access, slow insertion/deletion
- **LinkedList** - Fast insertion/deletion, slow random access

**When to Use**:
- Need ordered collection
- Duplicates allowed
- Random access needed (use ArrayList)

---

### 2. Map Interface

**What**: Key-value pairs, unique keys

**Not directly used in our code, but important**

**Implementations**:
- **HashMap** - No ordering, O(1) lookup
- **LinkedHashMap** - Insertion order maintained
- **TreeMap** - Sorted by keys

**Common Operations**:
```java
Map<Long, Supplier> supplierMap = new HashMap<>();
supplierMap.put(1L, supplier1);
Supplier s = supplierMap.get(1L);
boolean exists = supplierMap.containsKey(1L);
```

---

### 3. Set Interface

**What**: Unordered collection, no duplicates

**Implementations**:
- **HashSet** - No ordering, O(1) operations
- **LinkedHashSet** - Insertion order maintained
- **TreeSet** - Sorted elements

**Use Cases**:
- Remove duplicates
- Check membership
- Mathematical set operations

---

## Functional Programming in Java

### 1. Functional Interfaces

**What**: Interface with exactly one abstract method

**Examples in JDK**:
- **Function<T, R>** - Takes T, returns R
- **Predicate<T>** - Takes T, returns boolean
- **Consumer<T>** - Takes T, returns void
- **Supplier<T>** - Takes nothing, returns T

**Custom Functional Interface**:
```java
@FunctionalInterface
public interface SearchService<ENTITY, DTO, CRITERIA> {
    List<DTO> search(CRITERIA criteria);
    DTO findById(Long id);
    List<DTO> findAll();
}
```

---

### 2. Specification as Functional Interface

**Where Used**:
```java
// Specification is a functional interface
@FunctionalInterface
public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}

// We implement it with lambda
public static Specification<Supplier> hasCity(String city) {
    return (root, query, cb) -> {
        // Lambda implements the single abstract method
    };
}
```

---

## Spring Boot Framework

### 1. Dependency Injection (DI)

**What**: Objects receive their dependencies from external source

**Where Used in Our Code**:
```java
@Service
@AllArgsConstructor  // Lombok generates constructor
public class SupplierSearchService {
    
    // Dependency injected via constructor
    private final SupplierRepository supplierRepository;
    
    // Spring creates SupplierRepository and injects it
}
```

**Types of DI**:
1. **Constructor Injection** (Recommended) - We use this
2. **Setter Injection** - Via setter methods
3. **Field Injection** - Via @Autowired on fields

**Why Constructor Injection**:
- Immutable dependencies (final fields)
- Required dependencies clear
- Easy to test (can pass mocks)
- No reflection magic

**Interview Tip**:
> "Constructor injection is preferred because it makes dependencies explicit, enables immutability, and simplifies testing without requiring Spring context."

---

### 2. Spring Annotations

#### @Controller
**Purpose**: Mark class as Spring MVC controller

**Where Used**:
```java
@Controller
@AllArgsConstructor
public class SupplierController {
    // Handles HTTP requests
}
```

**Alternative**: `@RestController` = `@Controller` + `@ResponseBody`

---

#### @Service
**Purpose**: Mark class as service layer component

**Where Used**:
```java
@Service
@AllArgsConstructor
public class SupplierSearchService {
    // Business logic
}
```

**Why Use**: 
- Semantic clarity (this is a service)
- Spring component scanning
- Transaction management

---

#### @Repository
**Purpose**: Mark interface as data access layer

**Where Used**:
```java
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    // Data access
}
```

**Why Use**:
- Exception translation (SQLException → DataAccessException)
- Component scanning
- Semantic clarity

---

#### @GetMapping, @PostMapping, etc.
**Purpose**: Map HTTP requests to handler methods

**Where Used**:
```java
@GetMapping("/suppliers/search")
public ResponseEntity<List<SupplierDto>> searchSuppliers(
    @RequestParam(required = false) String city
) {
    // Handle GET request to /suppliers/search
}
```

**Variants**:
- `@GetMapping` - GET requests
- `@PostMapping` - POST requests
- `@PutMapping` - PUT requests
- `@DeleteMapping` - DELETE requests
- `@PatchMapping` - PATCH requests

---

#### @RequestParam
**Purpose**: Bind query parameter to method parameter

**Where Used**:
```java
@GetMapping("/suppliers/search")
public ResponseEntity<List<SupplierDto>> search(
    @RequestParam(required = false) String city  // ?city=Berlin
) {
    // city parameter bound from query string
}
```

**Attributes**:
- **required** - Is parameter mandatory? (default true)
- **defaultValue** - Default if not provided
- **name** - Parameter name if different from variable

---

#### @PathVariable
**Purpose**: Bind path variable to method parameter

**Where Used**:
```java
@GetMapping("/suppliers/{id}")
public ResponseEntity<SupplierDto> getSupplier(
    @PathVariable Long id  // /suppliers/123 → id=123
) {
    // id extracted from URL path
}
```

---

#### @Qualifier
**Purpose**: Specify which bean to inject when multiple candidates exist

**Where Used**:
```java
@Controller
@AllArgsConstructor
public class SupplierController {
    
    @Qualifier("supplierSearchService")  // Specify which implementation
    private final SearchService<Supplier, SupplierDto, SupplierSearchCriteria> searchService;
    
    // Without @Qualifier, Spring wouldn't know whether to inject
    // ActivitySearchService or SupplierSearchService
}
```

---

### 3. Bean Lifecycle

**What Happens**:
1. Spring scans for `@Component`, `@Service`, `@Repository`, `@Controller`
2. Creates bean instances
3. Injects dependencies
4. Calls initialization methods
5. Bean ready for use
6. Application shutdown → calls destruction methods

**Bean Naming**:
- Default: Class name with lowercase first letter
- `ActivitySearchService` → bean name: `"activitySearchService"`
- `SupplierSearchService` → bean name: `"supplierSearchService"`

---

## Spring Data JPA

### 1. JpaRepository Interface

**What**: Provides CRUD operations out of the box

**Where Used**:
```java
public interface SupplierRepository extends 
        JpaRepository<Supplier, Long>,  // Entity type, ID type
        JpaSpecificationExecutor<Supplier> {
}
```

**Inherited Methods** (No implementation needed!):
- `findAll()` - Get all entities
- `findById(ID id)` - Get by primary key
- `save(Entity)` - Insert or update
- `delete(Entity)` - Delete entity
- `count()` - Count all entities
- `existsById(ID id)` - Check if exists

**Magic**: Spring generates implementation at runtime!

---

### 2. JpaSpecificationExecutor

**What**: Adds specification-based query methods

**Where Used**:
```java
public interface SupplierRepository extends 
        JpaRepository<Supplier, Long>,
        JpaSpecificationExecutor<Supplier> {  // Adds specification support
}
```

**Added Methods**:
- `findAll(Specification<T> spec)` - Query with specification
- `findOne(Specification<T> spec)` - Find single result
- `count(Specification<T> spec)` - Count matching
- `exists(Specification<T> spec)` - Check if any match

**Why Use**:
- Dynamic query building
- Type-safe queries
- Composable predicates

---

### 3. Entity Mapping

**Where Used**:
```java
@Entity
@Table(schema = "getyourguide", name = "supplier")
public class Supplier {
    
    @Id  // Primary key
    private Long id;
    
    private String name;  // Maps to "name" column
    
    @OneToMany(mappedBy = "supplier")  // One supplier has many activities
    private List<Activity> activities;
}
```

**Annotations**:
- **@Entity** - JPA entity (table)
- **@Table** - Specify table name/schema
- **@Id** - Primary key
- **@Column** - Column mapping (optional if name matches)
- **@OneToMany, @ManyToOne** - Relationships

---

## JPA Criteria API & Specifications

### 1. Criteria API Components

#### Root<T>
**What**: Represents the entity being queried (FROM clause)

**Usage**:
```java
return (root, query, cb) -> {
    root.get("city")  // Access city field
    root.get("name")  // Access name field
    root.join("supplier")  // Join to supplier table
};
```

**Think**: `FROM supplier AS root`

---

#### CriteriaQuery
**What**: Represents the overall query

**Usage**:
```java
return (root, query, cb) -> {
    query.distinct(true);  // Add DISTINCT
    query.orderBy(cb.desc(root.get("id")));  // Order by
    // Usually not needed in simple specifications
};
```

---

#### CriteriaBuilder
**What**: Factory for building predicates (WHERE conditions)

**Usage**:
```java
return (root, query, cb) -> {
    cb.equal(root.get("city"), "Berlin")  // city = 'Berlin'
    cb.like(root.get("name"), "%Tours%")  // name LIKE '%Tours%'
    cb.between(root.get("price"), 50, 200)  // price BETWEEN 50 AND 200
    cb.greaterThan(root.get("rating"), 4.0)  // rating > 4.0
    cb.and(predicate1, predicate2)  // predicate1 AND predicate2
    cb.or(predicate1, predicate2)  // predicate1 OR predicate2
};
```

---

### 2. Specification Pattern in Detail

**Concept**: Encapsulate query logic in reusable, composable objects

**Example**:
```java
// Individual specifications
Specification<Supplier> hasCity = SupplierSpecifications.hasCity("Berlin");
Specification<Supplier> hasCountry = SupplierSpecifications.hasCountry("Germany");

// Compose with AND
Specification<Supplier> spec = hasCity.and(hasCountry);

// Or use allOf (Spring Data JPA 3.5+)
Specification<Supplier> spec = Specification.allOf(hasCity, hasCountry);

// Execute
List<Supplier> suppliers = repository.findAll(spec);
```

**Generated SQL**:
```sql
SELECT * FROM supplier 
WHERE LOWER(city) LIKE '%berlin%' 
  AND LOWER(country) LIKE '%germany%'
```

---

### 3. Why Specifications > Query Methods

**Query Methods** (Spring Data):
```java
List<Supplier> findByCity(String city);
List<Supplier> findByCityAndCountry(String city, String country);
List<Supplier> findByCityAndCountryAndName(String city, String country, String name);
// With 5 optional fields: 2^5 = 32 methods needed!
```

**Specifications**:
```java
// Just 5 individual specifications
public static Specification<Supplier> hasCity(String city) { ... }
public static Specification<Supplier> hasCountry(String country) { ... }
// ... 3 more

// Combine any way you want
Specification.allOf(hasCity("Berlin"), hasCountry("Germany"));
```

**Benefits**:
- Dynamic composition
- Fewer methods
- Type-safe
- Testable independently

---

## Database Concepts

### 1. Database Indexing

**What**: Data structure that improves query speed

**How It Works**:
```
Without Index:
Table Scan - Check every row
O(N) complexity

With Index on "city":
Index Lookup - Find in B-tree
O(log N) complexity
```

**Where to Add Indexes** (for our use case):
```java
@Entity
@Table(name = "supplier", indexes = {
    @Index(name = "idx_city", columnList = "city"),
    @Index(name = "idx_country", columnList = "country"),
    @Index(name = "idx_city_country", columnList = "city, country")  // Composite
})
public class Supplier {
    // ...
}
```

**Trade-offs**:
- ✅ **Faster reads** (SELECT queries)
- ❌ **Slower writes** (INSERT, UPDATE, DELETE)
- ❌ **More storage** space

**Interview Tip**:
> "Indexes are like book indexes - they help find data quickly without scanning every page. But they need to be updated when data changes, so there's a write performance cost."

---

### 2. Query Performance Optimization

#### N+1 Query Problem

**Problem**:
```java
// 1 query to get all suppliers
List<Supplier> suppliers = repository.findAll();

// N queries (one per supplier) to get activities
for (Supplier s : suppliers) {
    s.getActivities().size();  // Triggers query!
}
// Total: 1 + N queries
```

**Solution - Fetch Join**:
```java
@Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.activities")
List<Supplier> findAllWithActivities();
// Total: 1 query
```

---

#### Database-Level vs In-Memory Filtering

**Bad (Our Original Code)**:
```java
// Load ALL suppliers
List<Supplier> all = repository.findAll();

// Filter in Java
List<Supplier> filtered = all.stream()
    .filter(s -> s.getCity().equals("Berlin"))
    .collect(Collectors.toList());

// Problem: Loaded 10,000 rows, used 1
```

**Good (Our New Code)**:
```java
// Filter in database
Specification<Supplier> spec = SupplierSpecifications.hasCity("Berlin");
List<Supplier> filtered = repository.findAll(spec);

// Only loads matching rows from database
```

**SQL Generated**:
```sql
SELECT * FROM supplier WHERE city = 'Berlin'
```

---

### 3. SQL Concepts

#### LIKE Operator
**Purpose**: Pattern matching in strings

**Usage in Our Code**:
```java
cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
```

**Generated SQL**:
```sql
WHERE LOWER(city) LIKE '%berlin%'
```

**Wildcards**:
- `%` - Any characters (0 or more)
- `_` - Single character

**Examples**:
- `LIKE 'Berlin%'` - Starts with Berlin
- `LIKE '%Berlin'` - Ends with Berlin
- `LIKE '%Berlin%'` - Contains Berlin

---

#### JOIN Operations

**Where Used**:
```java
public static Specification<Activity> hasSupplierName(String supplierName) {
    return (root, query, cb) -> {
        Join<Activity, Supplier> supplierJoin = root.join("supplier", JoinType.LEFT);
        return cb.like(cb.lower(supplierJoin.get("name")), "%" + supplierName.toLowerCase() + "%");
    };
}
```

**Generated SQL**:
```sql
SELECT a.* 
FROM activity a
LEFT JOIN supplier s ON a.supplier_id = s.id
WHERE LOWER(s.name) LIKE '%berlin%'
```

**Join Types**:
- **INNER JOIN** - Only matching rows from both tables
- **LEFT JOIN** - All from left, matching from right
- **RIGHT JOIN** - All from right, matching from left
- **FULL OUTER JOIN** - All from both

---

## Design Patterns

### 1. Specification Pattern

**Intent**: Encapsulate business rules in reusable objects

**Structure**:
```
Specification Interface
└── hasCity(), hasCountry(), etc. (concrete specifications)
```

**Benefits**:
- Reusable business rules
- Composable (AND, OR operations)
- Testable independently
- Open/Closed Principle

**Real-World Analogy**:
> "Like LEGO blocks - each specification is a block, you can combine them in different ways to build different queries."

---

### 2. Strategy Pattern

**Intent**: Define family of algorithms, make them interchangeable

**Where Used**:
```java
// Strategy interface
interface SearchService<ENTITY, DTO, CRITERIA> {
    List<DTO> search(CRITERIA criteria);
}

// Concrete strategies
class ActivitySearchService implements SearchService<Activity, ActivityDto, ActivitySearchCriteria> { }
class SupplierSearchService implements SearchService<Supplier, SupplierDto, SupplierSearchCriteria> { }

// Client uses interface
@Controller
class SupplierController {
    private final SearchService<Supplier, SupplierDto, SupplierSearchCriteria> searchService;
    // Can use any implementation
}
```

**Benefits**:
- Swap implementations at runtime
- Add new strategies without modifying clients
- Encapsulate algorithm variations

---

### 3. Builder Pattern

**Intent**: Construct complex objects step by step

**Where Used**:
```java
// Lombok @Builder generates builder
@Builder
public class SupplierSearchCriteria {
    private String name;
    private String city;
    // ...
}

// Usage
SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
    .city("Berlin")
    .country("Germany")
    .name("Tours")
    .build();
```

**Benefits**:
- Readable object construction
- Optional parameters handled elegantly
- Immutable objects possible
- No telescoping constructors

**Without Builder**:
```java
// Telescoping constructors - messy!
public SupplierSearchCriteria(String name) { }
public SupplierSearchCriteria(String name, String city) { }
public SupplierSearchCriteria(String name, String city, String country) { }
// ... many more
```

---

### 4. Generic Programming / Parametric Polymorphism

**Intent**: Write code that works with any type

**Where Used**:
```java
public interface SearchService<ENTITY, DTO, CRITERIA> {
    //                          │       │    └─ Type parameter
    //                          │       └────── Type parameter
    //                          └────────────── Type parameter
    List<DTO> search(CRITERIA criteria);
}

// Concrete types substituted
class SupplierSearchService implements 
    SearchService<Supplier, SupplierDto, SupplierSearchCriteria> {
    // ENTITY = Supplier
    // DTO = SupplierDto
    // CRITERIA = SupplierSearchCriteria
}
```

**Benefits**:
- Type safety (compile-time checking)
- Code reuse
- No casting needed
- DRY principle

---

### 5. Repository Pattern

**Intent**: Separate data access logic from business logic

**Structure**:
```
Controller → Service → Repository → Database
```

**Where Used**:
```java
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    // Abstract data access
}

@Service
public class SupplierSearchService {
    private final SupplierRepository repository;  // Depends on abstraction
    
    public List<SupplierDto> search(SupplierSearchCriteria criteria) {
        List<Supplier> suppliers = repository.findAll(spec);  // Data access delegated
        return mapToDtoList(suppliers);
    }
}
```

**Benefits**:
- Centralized data access
- Easy to swap data sources
- Testable with mocks
- Consistent data access patterns

---

## SOLID Principles

### 1. Single Responsibility Principle (SRP)

**Definition**: A class should have only one reason to change

**In Our Code**:
```java
// ✅ GOOD: Each class has one responsibility
class SupplierSpecifications {
    // Responsibility: Build query predicates
    public static Specification<Supplier> hasCity(String city) { }
}

class SupplierSearchService {
    // Responsibility: Orchestrate search operations
    public List<SupplierDto> search(SupplierSearchCriteria criteria) { }
}

class SupplierController {
    // Responsibility: Handle HTTP requests
    @GetMapping("/suppliers/search")
    public ResponseEntity<List<SupplierDto>> search(...) { }
}
```

**Interview Example**:
> "SupplierSpecifications only builds queries. It doesn't fetch data, map DTOs, or handle HTTP. If query logic changes, only this class changes. That's SRP."

---

### 2. Open/Closed Principle (OCP)

**Definition**: Open for extension, closed for modification

**In Our Code**:
```java
// ✅ GOOD: Can add new search criteria without modifying existing code
public class SupplierSpecifications {
    public static Specification<Supplier> hasCity(String city) { }
    public static Specification<Supplier> hasCountry(String country) { }
    
    // Want to add zip code search? Just add a new method:
    public static Specification<Supplier> hasZip(String zip) {
        return (root, query, cb) -> /* new logic */;
    }
    // No need to modify hasCity() or hasCountry()
}
```

**Specification Pattern Enables OCP**:
- Each specification is independent
- Add new specifications without changing existing ones
- Compose specifications in new ways

---

### 3. Liskov Substitution Principle (LSP)

**Definition**: Subtypes must be substitutable for their base types

**In Our Code**:
```java
// Interface
SearchService<Supplier, SupplierDto, SupplierSearchCriteria> searchService;

// Can be substituted with ANY implementation
searchService = new SupplierSearchService(...);  // Works
searchService = new CachedSupplierSearchService(...);  // Would also work

// Controller doesn't care which implementation
List<SupplierDto> results = searchService.search(criteria);
```

**Interview Example**:
> "The controller depends on SearchService interface. I can substitute any implementation - SupplierSearchService, a cached version, a mock for testing - and the controller code doesn't need to change. That's LSP."

---

### 4. Interface Segregation Principle (ISP)

**Definition**: Clients shouldn't depend on interfaces they don't use

**In Our Code**:
```java
// ✅ GOOD: Small, focused interface
public interface SearchService<ENTITY, DTO, CRITERIA> {
    List<DTO> search(CRITERIA criteria);  // Only 3 methods
    DTO findById(Long id);
    List<DTO> findAll();
}

// ❌ BAD: Fat interface
public interface SearchService<ENTITY, DTO, CRITERIA> {
    List<DTO> search(CRITERIA criteria);
    DTO findById(Long id);
    List<DTO> findAll();
    void save(ENTITY entity);  // Not all clients need save
    void delete(ENTITY entity);  // Not all clients need delete
    List<DTO> findByUser(Long userId);  // Specific, not general
    // ... 10 more methods
}
```

**Interview Example**:
> "Our SearchService interface has only 3 essential methods. Implementations aren't forced to implement methods they don't need. That's ISP - small, focused interfaces."

---

### 5. Dependency Inversion Principle (DIP)

**Definition**: Depend on abstractions, not concretions

**In Our Code**:
```java
// ✅ GOOD: Depend on interface
@Controller
public class SupplierController {
    private final SearchService<Supplier, SupplierDto, SupplierSearchCriteria> searchService;
    //            ^^^^^^^^^^ Interface (abstraction)
}

// ❌ BAD: Depend on concrete class
@Controller
public class SupplierController {
    private final SupplierSearchService searchService;
    //            ^^^^^^^^^^^^^^^^^^^^^ Concrete class
}
```

**Benefits**:
- Loose coupling
- Easy to swap implementations
- Testable (inject mocks)
- Flexible architecture

**Interview Example**:
> "The controller depends on SearchService interface, not SupplierSearchService concrete class. This means I can inject a different implementation (like a cached version or a mock) without changing the controller. That's DIP - depend on abstractions."

---

## Testing Concepts

### 1. Unit Tests vs Integration Tests

**Unit Tests**:
- Test single component in isolation
- Use mocks for dependencies
- Fast (milliseconds)
- No external dependencies (database, network)

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class SupplierSearchServiceTest {
    @Mock
    private SupplierRepository repository;  // Mocked
    
    @InjectMocks
    private SupplierSearchService service;
    
    @Test
    void testSearch() {
        when(repository.findAll(any())).thenReturn(List.of(supplier));
        List<SupplierDto> result = service.search(criteria);
        assertEquals(1, result.size());
    }
}
```

**Integration Tests**:
- Test multiple components together
- Real dependencies (actual database)
- Slower (seconds)
- Verify end-to-end flow

**Example**:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SupplierControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;  // Real HTTP client
    
    @Autowired
    private SupplierRepository repository;  // Real repository
    
    @Test
    void testSearchEndpoint() {
        ResponseEntity<List<SupplierDto>> response = 
            restTemplate.exchange("/suppliers/search?city=Berlin", ...);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

**Test Pyramid**:
```
      /\
     /  \  E2E Tests (few)
    /____\
   /      \  Integration Tests (some)
  /________\
 /          \ Unit Tests (many)
/____________\
```

---

### 2. Mocking with Mockito

**What**: Create fake objects that mimic real objects

**Where Used**:
```java
@Mock
private SupplierRepository repository;  // Mock repository

@Test
void testFindById() {
    // Define mock behavior
    when(repository.findById(1L)).thenReturn(Optional.of(supplier));
    
    // Call method under test
    SupplierDto result = service.findById(1L);
    
    // Verify interactions
    verify(repository, times(1)).findById(1L);
    verify(repository, never()).save(any());
}
```

**Common Mockito Methods**:
- **when(method).thenReturn(value)** - Define behavior
- **when(method).thenThrow(exception)** - Throw exception
- **verify(mock, times(n)).method()** - Verify calls
- **any(), anyString(), anyInt()** - Argument matchers

---

### 3. AAA Pattern

**Arrange-Act-Assert**: Standard test structure

**Example**:
```java
@Test
void testSearch() {
    // Arrange - Set up test data
    SupplierSearchCriteria criteria = SupplierSearchCriteria.builder()
        .city("Berlin")
        .build();
    when(repository.findAll(any())).thenReturn(List.of(supplier));
    
    // Act - Execute the code under test
    List<SupplierDto> result = service.search(criteria);
    
    // Assert - Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Berlin", result.get(0).getCity());
}
```

---

### 4. Test Doubles

**Types**:
1. **Mock** - Verifies behavior (method calls)
2. **Stub** - Returns predefined data
3. **Spy** - Partial mock (real object with some methods mocked)
4. **Fake** - Working implementation (e.g., in-memory database)
5. **Dummy** - Passed but never used

---

## Best Practices

### 1. Immutability

**Use `final` for fields**:
```java
@Service
@AllArgsConstructor
public class SupplierSearchService {
    private final SupplierRepository repository;  // Can't be reassigned
}
```

**Benefits**:
- Thread-safe
- Easier to reason about
- Prevents accidental reassignment

---

### 2. Null Safety

**Use Optional**:
```java
// Instead of returning null
public SupplierDto findById(Long id) {
    return repository.findById(id)
        .map(this::mapToDto)
        .orElseThrow(() -> new RuntimeException("Not found"));
}
```

**Check for null in specifications**:
```java
if (city == null || city.trim().isEmpty()) {
    return null;  // Skip filter
}
```

---

### 3. Separation of Concerns

**Layers**:
```
Controller  → Handle HTTP (requests/responses)
Service     → Business logic (orchestration)
Repository  → Data access (queries)
Entity      → Domain model (database mapping)
DTO         → API contract (what clients see)
```

**Each layer has clear responsibility**

---

### 4. DRY (Don't Repeat Yourself)

**Bad**:
```java
// Repeated mapping logic
ActivityDto dto1 = ActivityDto.builder().id(a.getId()).name(a.getName())...
ActivityDto dto2 = ActivityDto.builder().id(a.getId()).name(a.getName())...
```

**Good**:
```java
private ActivityDto mapToDto(Activity activity) {
    return ActivityDto.builder()...
}

// Reuse
ActivityDto dto1 = mapToDto(activity1);
ActivityDto dto2 = mapToDto(activity2);
```

---

### 5. Defensive Programming

**Validate inputs**:
```java
public List<SupplierDto> search(SupplierSearchCriteria criteria) {
    if (criteria == null) {
        throw new IllegalArgumentException("Criteria cannot be null");
    }
    // ... rest of method
}
```

**Handle edge cases**:
```java
if (title == null || title.trim().isEmpty()) {
    return null;  // Handle empty input
}
```

---

## Interview Tips

### When Discussing Lambdas:
> "Lambdas are syntactic sugar for functional interfaces. They enable functional programming paradigms in Java, making code more concise and expressive."

### When Discussing Streams:
> "Streams provide a declarative way to process collections. They're lazy-evaluated, meaning intermediate operations don't execute until a terminal operation is called. They can also be parallelized easily for performance."

### When Discussing Specifications:
> "The Specification pattern encapsulates query logic in reusable, testable units. It's more flexible than query methods and avoids the proliferation of repository methods for every query combination."

### When Discussing SOLID:
> "SOLID principles guide object-oriented design. In our code, SRP ensures each class has one responsibility, OCP allows adding features without modifying existing code, LSP ensures substitutability, ISP keeps interfaces focused, and DIP decouples high-level from low-level modules."

### When Discussing Performance:
> "We optimized performance by moving filtering from application memory to the database. Specifications generate SQL WHERE clauses, so only matching rows are loaded. With proper indexes, this is O(log N) instead of O(N)."

---

## Summary

This implementation demonstrates:
- ✅ Modern Java features (Lambdas, Streams, Optional)
- ✅ Spring Boot best practices (DI, annotations, layering)
- ✅ JPA/Hibernate expertise (Specifications, Criteria API)
- ✅ Database optimization (indexing, query performance)
- ✅ Design patterns (Specification, Strategy, Builder, Repository)
- ✅ SOLID principles (all 5 applied throughout)
- ✅ Testing strategies (unit tests, integration tests, mocking)
- ✅ Code quality (immutability, null safety, DRY)

**Total Concepts Covered**: 50+ technical concepts across 11 categories

This reference covers everything you need to confidently discuss the technical implementation in interviews!
