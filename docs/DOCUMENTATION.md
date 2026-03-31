# Project Documentation

Comprehensive documentation for the SaaS Inventory + Billing + POS backend template.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Entity Relationship Diagram](#entity-relationship-diagram)
4. [Authentication Flow](#authentication-flow)
5. [Multi-Tenant Architecture](#multi-tenant-architecture)
6. [API Endpoints](#api-endpoints)
7. [Event System](#event-system)
8. [What's Remaining](#whats-remaining)
9. [Improvement Suggestions](#improvement-suggestions)

---

## Architecture Overview

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        WEB["Web App<br/>(React/Vue/Angular)"]
        MOBILE["Mobile App<br/>(React Native/Flutter)"]
        POS_TERMINAL["POS Terminal"]
    end

    subgraph Gateway["API Gateway"]
        LB["Load Balancer<br/>(Nginx/AWS ALB)"]
        RATE["Rate Limiter<br/>(Bucket4j)"]
    end

    subgraph App["Spring Boot Application"]
        subgraph Security["Security Layer"]
            JWT["JWT Filter"]
            RBAC["Role-Based Access"]
            TENANT["Tenant Interceptor"]
        end

        subgraph Controllers["REST Controllers"]
            AUTH_C["AuthController"]
            INV_C["InventoryController"]
            INVOICE_C["InvoiceController"]
            POS_C["PosController"]
        end

        subgraph Services["Business Services"]
            AUTH_S["AuthService"]
            INV_S["InventoryService"]
            INVOICE_S["InvoiceService"]
            AUDIT_S["AuditService"]
        end

        subgraph Events["Event System"]
            PUB["EventPublisher"]
            LIST["EventListeners"]
        end

        SCHED["Scheduler<br/>(Background Jobs)"]
        WS["WebSocket<br/>(STOMP)"]
    end

    subgraph Data["Data Layer"]
        PG[(PostgreSQL<br/>Primary DB)]
        MONGO[(MongoDB<br/>Audit Logs)]
        REDIS[(Redis<br/>Cache/Sessions)]
    end

    WEB --> LB
    MOBILE --> LB
    POS_TERMINAL --> LB
    LB --> RATE
    RATE --> JWT
    JWT --> RBAC
    RBAC --> TENANT
    TENANT --> Controllers
    Controllers --> Services
    Services --> PUB
    PUB --> LIST
    LIST --> WS
    Services --> PG
    AUDIT_S --> MONGO
    Services --> REDIS
    WS --> Client
```

---

## Project Structure

```mermaid
flowchart LR
    subgraph src/main/java/com/saasproject
        subgraph config["config/"]
            SC[SecurityConfig]
            CC[CorsConfig]
            SWC[SwaggerConfig]
            RC[RedisConfig]
            WSC[WebSocketConfig]
            MTC[MultiTenantConfig]
            DC[DataConfig]
            RLC[RateLimitConfig]
        end

        subgraph common["common/"]
            BE[entity/BaseEntity]
            AR[api_response/ApiResponse]
            GEH[exceptions/GlobalExceptionHandler]
            CU[utils/CommonUtils]
            AC[constants/AppConstants]
        end

        subgraph tenant["tenant/"]
            TC[context/TenantContext]
            TI[interceptor/TenantInterceptor]
            TR[resolver/TenantIdentifierResolver]
            TCP[resolver/TenantConnectionProvider]
        end

        subgraph modules["modules/"]
            subgraph auth["auth/"]
                AE[entity: User, Role, RefreshToken]
                AS[service: AuthService]
                ACT[controller: AuthController]
                JWT_P[security: JwtTokenProvider]
            end
            subgraph inventory["inventory/"]
                IE[entity: Product, StockMovement]
                IS[service: InventoryService]
                ICT[controller: InventoryController]
            end
            subgraph invoice["invoice/"]
                INE[entity: Invoice, InvoiceItem]
                INS[service: InvoiceService]
                INCT[controller: InvoiceController]
            end
            subgraph pos["pos/"]
                PE[entity: Cart, CartItem]
                PCT[controller: PosController]
            end
            subgraph audit["audit/"]
                ALE[entity: AuditLog]
                ALS[service: AuditService]
            end
        end

        subgraph events["events/"]
            DE[models/DomainEvent]
            EP[publishers/EventPublisher]
            EL[listeners/InventoryEventListener]
        end

        subgraph scheduler["scheduler/"]
            SJ[ScheduledJobs]
        end
    end
```

---

## Entity Relationship Diagram

```mermaid
erDiagram
    TENANT ||--o{ USER : contains
    TENANT ||--o{ PRODUCT : contains
    TENANT ||--o{ INVOICE : contains
    TENANT ||--o{ CART : contains

    USER ||--o{ USER_ROLE : has
    USER ||--o{ REFRESH_TOKEN : has
    USER ||--o{ INVOICE : creates
    USER ||--o{ CART : operates

    USER {
        uuid id PK
        string tenant_id FK
        string email UK
        string password_hash
        string first_name
        string last_name
        string phone
        boolean enabled
        boolean email_verified
        timestamp locked_until
        int failed_login_attempts
        timestamp last_login_at
        timestamp created_at
    }

    USER_ROLE {
        uuid user_id PK,FK
        string role PK
    }

    REFRESH_TOKEN {
        uuid id PK
        string token UK
        uuid user_id FK
        string tenant_id
        timestamp expires_at
        boolean revoked
        string ip_address
    }

    PRODUCT ||--o{ STOCK_MOVEMENT : tracks
    PRODUCT ||--o{ INVOICE_ITEM : appears_in
    PRODUCT ||--o{ CART_ITEM : added_to

    PRODUCT {
        uuid id PK
        string tenant_id FK
        string name
        string description
        string sku UK
        string barcode UK
        string category
        string brand
        decimal selling_price
        decimal cost_price
        int current_stock
        int min_stock_level
        boolean is_active
        boolean track_inventory
        timestamp created_at
    }

    STOCK_MOVEMENT {
        uuid id PK
        string tenant_id FK
        uuid product_id FK
        string movement_type
        int quantity
        int stock_before
        int stock_after
        string reference_type
        string reference_id
        string notes
        timestamp created_at
    }

    INVOICE ||--|{ INVOICE_ITEM : contains

    INVOICE {
        uuid id PK
        string tenant_id FK
        string invoice_number UK
        string customer_id
        string customer_name
        string customer_email
        string status
        date invoice_date
        date due_date
        decimal subtotal
        decimal tax_amount
        decimal discount_amount
        decimal total_amount
        decimal paid_amount
        decimal balance_due
        string payment_method
        timestamp paid_at
        timestamp created_at
    }

    INVOICE_ITEM {
        uuid id PK
        uuid invoice_id FK
        uuid product_id FK
        string product_name
        int quantity
        decimal unit_price
        decimal tax_rate
        decimal amount
    }

    CART ||--|{ CART_ITEM : contains

    CART {
        uuid id PK
        string tenant_id FK
        string status
        string customer_id
        string customer_name
        decimal subtotal
        decimal tax_amount
        decimal total_amount
        uuid invoice_id
        string payment_method
        timestamp checked_out_at
        string cashier_id
        timestamp created_at
    }

    CART_ITEM {
        uuid id PK
        uuid cart_id FK
        uuid product_id FK
        string product_name
        int quantity
        decimal unit_price
        decimal amount
    }

    AUDIT_LOG {
        string id PK
        string tenant_id
        string user_id
        string action
        string entity_type
        string entity_id
        json old_value
        json new_value
        string ip_address
        timestamp timestamp
    }
```

---

## Authentication Flow

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Spring Boot API
    participant JWT as JwtTokenProvider
    participant DB as PostgreSQL
    participant Redis as Redis Cache

    rect rgb(200, 230, 200)
        Note over C,DB: User Login Flow
        C->>API: POST /v1/auth/login {email, password}
        API->>DB: Find user by email
        DB-->>API: User entity
        API->>API: Verify password (BCrypt)
        API->>JWT: Generate access token (15 min)
        API->>JWT: Generate refresh token (7 days)
        API->>DB: Save refresh token
        API-->>C: {accessToken, refreshToken, user}
    end

    rect rgb(200, 200, 230)
        Note over C,Redis: Authenticated Request
        C->>API: GET /v1/inventory/products<br/>Authorization: Bearer {accessToken}
        API->>JWT: Validate & parse token
        JWT-->>API: User details + tenantId
        API->>API: Set TenantContext
        API->>Redis: Check cache
        Redis-->>API: Cache miss
        API->>DB: Query products WHERE tenant_id = ?
        DB-->>API: Products list
        API->>Redis: Cache result
        API-->>C: {success: true, data: [...]}
    end

    rect rgb(230, 200, 200)
        Note over C,DB: Token Refresh Flow
        C->>API: POST /v1/auth/refresh {refreshToken}
        API->>DB: Validate refresh token
        DB-->>API: Valid, not revoked
        API->>DB: Revoke old refresh token
        API->>JWT: Generate new access token
        API->>JWT: Generate new refresh token
        API->>DB: Save new refresh token
        API-->>C: {accessToken, refreshToken}
    end
```

---

## Multi-Tenant Architecture

```mermaid
flowchart TB
    subgraph Request["Incoming Request"]
        REQ["HTTP Request"]
        HEAD["Headers:<br/>Authorization: Bearer JWT<br/>X-Tenant-ID: acme-corp"]
    end

    subgraph Extraction["Tenant Extraction"]
        JWT_FILTER["JwtAuthenticationFilter"]
        TENANT_INT["TenantInterceptor"]

        JWT_FILTER -->|"1. Extract JWT"| PARSE["Parse JWT Claims"]
        PARSE -->|"2. Get tenantId claim"| SET_CTX["Set TenantContext"]
        TENANT_INT -->|"Fallback: X-Tenant-ID header"| SET_CTX
    end

    subgraph Context["Thread-Local Context"]
        TC["TenantContext<br/>(ThreadLocal)"]
        MDC["MDC<br/>(Logging)"]
        SET_CTX --> TC
        SET_CTX --> MDC
    end

    subgraph Data["Data Access"]
        REPO["Repository Query"]
        TC -->|"getCurrentTenant()"| REPO
        REPO -->|"WHERE tenant_id = ?"| DB[(PostgreSQL)]
    end

    subgraph Response["Response"]
        CLEAR["Clear TenantContext"]
        RES["HTTP Response"]
    end

    REQ --> JWT_FILTER
    DB --> CLEAR
    CLEAR --> RES

    style TC fill:#f9f,stroke:#333,stroke-width:2px
    style DB fill:#bbf,stroke:#333,stroke-width:2px
```

---

## API Endpoints

### Authentication (`/v1/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | ❌ |
| POST | `/login` | Login, returns JWT | ❌ |
| POST | `/refresh` | Refresh access token | ❌ |
| POST | `/logout` | Revoke all tokens | ✅ |
| POST | `/forgot-password` | Request password reset | ❌ |
| POST | `/reset-password` | Reset with token | ❌ |
| POST | `/change-password` | Change password | ✅ |
| GET | `/me` | Get current user | ✅ |

### Inventory (`/v1/inventory`)
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/products` | Create product | ADMIN, MANAGER |
| GET | `/products` | List products | ALL |
| GET | `/products/{id}` | Get product | ALL |
| PUT | `/products/{id}` | Update product | ADMIN, MANAGER |
| DELETE | `/products/{id}` | Delete product | ADMIN, MANAGER |
| PUT | `/products/{id}/stock` | Update stock | ADMIN, MANAGER |
| GET | `/products/low-stock` | Low stock alerts | ADMIN, MANAGER |
| GET | `/products/barcode/{code}` | Lookup by barcode | ALL |
| GET | `/products/search` | Search products | ALL |

### Invoices (`/v1/invoices`)
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/` | Create invoice | ADMIN, MANAGER, CASHIER |
| GET | `/` | List invoices | ALL |
| GET | `/{id}` | Get invoice | ALL |
| POST | `/{id}/payments` | Record payment | ADMIN, MANAGER, CASHIER |
| POST | `/{id}/cancel` | Cancel invoice | ADMIN, MANAGER |
| GET | `/{id}/pdf` | Export as PDF | ALL |

### POS (`/v1/pos`)
| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/cart` | Create cart | ADMIN, MANAGER, CASHIER |
| GET | `/cart/{id}` | Get cart | ALL |
| POST | `/cart/{id}/items` | Add item | ADMIN, MANAGER, CASHIER |
| PUT | `/cart/{id}/items/{productId}` | Update quantity | ADMIN, MANAGER, CASHIER |
| DELETE | `/cart/{id}/items/{productId}` | Remove item | ADMIN, MANAGER, CASHIER |
| POST | `/cart/{id}/checkout` | Checkout | ADMIN, MANAGER, CASHIER |

---

## Event System

```mermaid
flowchart LR
    subgraph Trigger["Business Action"]
        INV_SVC["InventoryService<br/>createProduct()"]
    end

    subgraph Publish["Event Publishing"]
        EP["EventPublisher"]
        EVENT["ProductCreatedEvent<br/>{productId, name, tenantId}"]
    end

    subgraph Handle["Event Handling"]
        SPRING["Spring<br/>ApplicationEventPublisher"]
        
        subgraph Listeners["Async Listeners"]
            L1["InventoryEventListener"]
            L2["AuditEventListener"]
            L3["NotificationListener"]
        end
    end

    subgraph Actions["Side Effects"]
        AUDIT["MongoDB<br/>Audit Log"]
        WS["WebSocket<br/>Broadcast"]
        EMAIL["Email<br/>Notification"]
    end

    INV_SVC -->|"1. Create"| EP
    EP -->|"2. Wrap"| EVENT
    EVENT -->|"3. Publish"| SPRING
    SPRING -->|"@Async"| Listeners
    L1 --> AUDIT
    L1 --> WS
    L2 --> AUDIT
    L3 --> EMAIL
```

---

## What's Remaining

### 🔴 Not Implemented
| Module | Status |
|--------|--------|
| **Subscription Billing** | Payment gateway facade ready, needs billing cycle logic |
| **Printer Support** | Print job queue, receipt templates, ESC/POS commands |
| **Multi-location** | Support for multiple stores/warehouses |
| **Import/Export** | CSV/Excel import/export for products |

### ✅ Recently Implemented
| Module | Features |
|--------|----------|
| **Company/Tenant** | Entity, settings, subscription tier |
| **Supplier** | Full CRUD, search, financial details |
| **Customer** | Full CRUD, loyalty points, phone lookup |
| **Purchase Orders** | Full workflow: create, approve, send, receive, cancel |
| **User Management** | Admin CRUD, role assignment, enable/disable |
| **Reports/Dashboard** | Sales metrics, inventory metrics, analytics |
| **Email Service** | Templates for welcome, password reset, invoices, alerts |
| **Payment Gateway** | Razorpay/Stripe facade (mock mode available) |
| **Notifications** | Real-time WebSocket notifications |
| **POS** | Complete service: cart, checkout, barcode scan |

---

## Improvement Suggestions

### 🏗️ Architecture Improvements

```mermaid
mindmap
  root((Improvements))
    Architecture
      Add CQRS for read-heavy operations
      Implement Circuit Breaker with Resilience4j
      Add API versioning strategy
      Consider microservices split for scale
    Security
      Add OAuth2/OIDC support
      Implement MFA/2FA
      Add API key authentication for integrations
      Security audit logging
    Performance
      Implement query optimization
      Add database connection pooling tuning
      Implement response compression
      Add request batching
    Testing
      Add integration tests with Testcontainers
      Add contract tests
      Add performance/load tests
      Increase unit test coverage
    DevOps
      Add Kubernetes manifests
      Implement GitOps with ArgoCD
      Add Prometheus metrics
      Implement distributed tracing
```

### 📝 Specific Code Improvements

| Area | Current | Suggested Improvement |
|------|---------|----------------------|
| **DTOs** | Manual mapping | Use MapStruct generated mappers consistently |
| **Validation** | Basic Bean Validation | Add custom validators, cross-field validation |
| **Caching** | Basic Redis caching | Add cache invalidation strategy, TTL tuning |
| **Logging** | Basic SLF4J | Add correlation IDs, structured logging fields |
| **Error Codes** | String-based | Use enum-based error codes with i18n |
| **Pagination** | Basic offset | Add cursor-based pagination for large datasets |
| **Search** | LIKE queries | Integrate Elasticsearch for full-text search |
| **File Storage** | Not implemented | Add S3/MinIO for product images |

### 🔒 Security Enhancements

1. **Add Security Headers Filter**
   - X-Content-Type-Options: nosniff
   - X-Frame-Options: DENY
   - Strict-Transport-Security

2. **Implement Audit Trail**
   - Log all data mutations
   - Track who changed what and when
   - Immutable audit log

3. **Add IP Whitelisting**
   - For admin endpoints
   - Configurable per tenant

4. **Implement Request Signing**
   - For webhook callbacks
   - HMAC signature verification

### 📦 Missing Dependencies to Add

```xml
<!-- Add to pom.xml -->

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Excel Export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- Barcode Generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- Circuit Breaker -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
```

---

## Deployment Checklist

- [ ] Change JWT secret to secure random value
- [ ] Configure production database credentials
- [ ] Set up SSL/TLS certificates
- [ ] Configure CORS for production domains
- [ ] Set up monitoring (Prometheus + Grafana)
- [ ] Configure log aggregation (ELK/Loki)
- [ ] Set up backup strategy for databases
- [ ] Configure rate limiting per environment
- [ ] Review and harden security settings
- [ ] Load test with expected traffic
