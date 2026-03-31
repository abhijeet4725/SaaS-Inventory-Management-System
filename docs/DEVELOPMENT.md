# Development Guide

Guide for developers working on this project.

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- IDE: IntelliJ IDEA / VS Code with Java extensions

---

## Local Setup

### 1. Clone and Start Infrastructure

```bash
# Start databases
docker-compose up -d postgres redis mongo

# Verify services are running
docker-compose ps
```

### 2. Run Application

```bash
# With Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Verify

- **API:** http://localhost:8080/api/actuator/health
- **Swagger:** http://localhost:8080/api/swagger-ui.html
- **Docs:** http://localhost:8080/api/docs

---

## Project Conventions

### Package Structure

```
com.saasproject.modules.{module}/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── dto/            # Request/Response DTOs
└── mapper/         # Entity-DTO mappers
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Entity | Singular noun | `Product`, `Invoice` |
| Repository | `{Entity}Repository` | `ProductRepository` |
| Service | `{Module}Service` | `InventoryService` |
| Controller | `{Module}Controller` | `InventoryController` |
| DTO | `{Entity}Dto.{Type}` | `ProductDto.CreateRequest` |

### API Conventions

- Prefix: `/v1/{module}`
- Use plural nouns for collections
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
- Always return `ApiResponse<T>` wrapper

---

## Adding a New Module

### 1. Create Package Structure

```bash
mkdir -p src/main/java/com/saasproject/modules/{module}/{controller,service,repository,entity,dto,mapper}
```

### 2. Create Entity

```java
@Entity
@Table(name = "{plural_name}")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class {Entity} extends BaseEntity {
    // Fields
}
```

### 3. Create Repository

```java
@Repository
public interface {Entity}Repository extends JpaRepository<{Entity}, UUID> {
    
    @Query("SELECT e FROM {Entity} e WHERE e.tenantId = :tenantId AND e.deleted = false")
    Page<{Entity}> findByTenant(@Param("tenantId") String tenantId, Pageable pageable);
}
```

### 4. Create DTOs

```java
public class {Entity}Dto {
    @Data @Builder
    public static class CreateRequest { ... }
    
    @Data @Builder
    public static class UpdateRequest { ... }
    
    @Data @Builder
    public static class Response { ... }
}
```

### 5. Create Service

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class {Module}Service {
    
    private final {Entity}Repository repository;
    
    @Transactional
    public {Entity}Dto.Response create({Entity}Dto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        // Implementation
    }
}
```

### 6. Create Controller

```java
@RestController
@RequestMapping("/v1/{module}")
@RequiredArgsConstructor
@Tag(name = "{Module}", description = "...")
public class {Module}Controller {
    
    private final {Module}Service service;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<{Entity}Dto.Response>> create(...) {
        // Implementation
    }
}
```

### 7. Add Flyway Migration

Create `V{N}__{Description}.sql` in `src/main/resources/db/migration/`

---

## Testing

### Run Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=AuthServiceTest

# With coverage
mvn test jacoco:report
```

### Test Structure

```
src/test/java/com/saasproject/
├── modules/
│   ├── auth/
│   │   ├── controller/AuthControllerTest.java
│   │   └── service/AuthServiceTest.java
│   └── inventory/
│       └── service/InventoryServiceTest.java
└── integration/
    └── InventoryIntegrationTest.java
```

---

## Common Tasks

### Generate JWT Secret

```bash
openssl rand -base64 32
```

### View Logs

```bash
# Application logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f app
```

### Database Operations

```bash
# Connect to PostgreSQL
docker exec -it saas-postgres psql -U postgres -d saas_inventory

# Run migrations manually
mvn flyway:migrate

# Reset database
mvn flyway:clean flyway:migrate
```

### Redis CLI

```bash
docker exec -it saas-redis redis-cli
# KEYS *
# GET "products::123"
# FLUSHALL
```

---

## Troubleshooting

### Application Won't Start

1. Check if ports are in use: `netstat -an | findstr 8080`
2. Verify database connection: `docker-compose ps`
3. Check logs: `mvn spring-boot:run` with `-X` flag

### JWT Token Issues

1. Verify token format: https://jwt.io
2. Check expiration time
3. Ensure secret matches configuration

### Database Connection Issues

1. Check Docker containers: `docker-compose ps`
2. Verify credentials in `application.yml`
3. Test connection: `docker exec -it saas-postgres pg_isready`

---

## IDE Setup

### IntelliJ IDEA

1. Import as Maven project
2. Enable annotation processing (Lombok)
3. Set Java 21 as SDK
4. Install plugins: Lombok, MapStruct Support

### VS Code

Install extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support
