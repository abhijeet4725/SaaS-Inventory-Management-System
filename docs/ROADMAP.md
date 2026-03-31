# Project Roadmap

What's remaining to implement and future improvement plans for the SaaS Inventory + Billing + POS system.

---

## Current Status

✅ **Completed** | 🚧 **Partial** | ❌ **Not Started**

| Module | Status | Notes |
|--------|--------|-------|
| Project Setup | ✅ | Spring Boot 3.2, Maven, profiles |
| Configuration | ✅ | Security, CORS, Redis, WebSocket, Swagger |
| Multi-Tenancy | ✅ | Shared DB with tenant_id (DISCRIMINATOR) |
| Auth Module | ✅ | JWT, login, register, refresh, password |
| Inventory Module | ✅ | Product CRUD, stock, low stock alerts |
| Invoice Module | ✅ | Create, payment, cancel |
 | POS Module | 🚧 | Controller done, service placeholders |
| Audit Module | ✅ | MongoDB logging, async |
| Events System | ✅ | Publisher, listeners, WebSocket |
| Background Jobs | ✅ | Scheduler with cleanup jobs |
| Docker/DevOps | ✅ | Dockerfile, docker-compose |
| Documentation | ✅ | API docs, dev guide, architecture |

---

## What's Left to Implement

### 🔴 Phase 1 - Core Business (High Priority)

#### 1. Company/Tenant Module
**Why Needed:** Tenants need to manage their own settings, subscription tier, and business details.

```
modules/company/
├── entity/
│   ├── Company.java              # Company details, logo, address
│   ├── CompanySettings.java      # Tax settings, currency, timezone
│   └── Branch.java               # Multi-location support
├── dto/CompanyDto.java
├── repository/CompanyRepository.java
├── service/CompanyService.java
└── controller/CompanyController.java
```

**Endpoints:**
- `GET /v1/company` - Get current company
- `PUT /v1/company` - Update company settings
- `POST /v1/company/logo` - Upload logo

---

#### 2. User Management Module
**Why Needed:** Admins need to manage users separately from authentication.

```
modules/user/
├── dto/UserDto.java
├── service/UserManagementService.java
└── controller/UserController.java
```

**Endpoints:**
- `GET /v1/users` - List users
- `POST /v1/users` - Create user (admin)
- `PUT /v1/users/{id}` - Update user
- `PUT /v1/users/{id}/roles` - Assign roles
- `DELETE /v1/users/{id}` - Deactivate user

---

#### 3. Supplier Module
**Why Needed:** Track vendors for purchase orders and inventory sourcing.

```
modules/supplier/
├── entity/Supplier.java
├── dto/SupplierDto.java
├── repository/SupplierRepository.java
├── service/SupplierService.java
└── controller/SupplierController.java
```

**Fields:** name, email, phone, address, contactPerson, paymentTerms, active

---

#### 4. Purchase Order Module
**Why Needed:** Track orders to suppliers, receive goods, update inventory.

```
modules/purchase/
├── entity/
│   ├── PurchaseOrder.java
│   ├── PurchaseOrderItem.java
│   └── GoodsReceipt.java
├── dto/PurchaseOrderDto.java
├── service/PurchaseOrderService.java
└── controller/PurchaseOrderController.java
```

**Workflow:**
1. Create PO → Status: DRAFT
2. Submit PO → Status: SUBMITTED
3. Approve PO → Status: APPROVED
4. Receive goods → Create GoodsReceipt → Update Stock

---

#### 5. Complete POS Service
**Why Needed:** Controller has placeholders, needs full service implementation.

```java
// PosService.java methods to implement:
- createCart()
- addItemByBarcode()
- addItemByProductId()
- removeItem()
- updateQuantity()
- applyDiscount()
- checkout() → Create Invoice → Deduct Stock
- voidCart()
```

---

### 🟡 Phase 2 - Payments & Subscriptions (Medium Priority)

#### 6. Subscription/Billing Module
**Why Needed:** SaaS billing with payment gateway integration.

```
modules/subscription/
├── entity/
│   ├── Plan.java                 # Subscription plans
│   ├── Subscription.java         # Tenant subscriptions
│   └── PaymentTransaction.java   # Payment history
├── service/
│   ├── SubscriptionService.java
│   ├── RazorpayService.java      # Or StripeService
│   └── WebhookService.java
└── controller/
    ├── SubscriptionController.java
    └── PaymentWebhookController.java
```

**Integration Points:**
- Razorpay/Stripe SDK
- Webhook handlers for payment status
- Grace period handling
- Auto-renewal logic

---

#### 7. Customer Module
**Why Needed:** Track customers for invoicing, loyalty, and CRM.

```
modules/customer/
├── entity/
│   ├── Customer.java
│   └── CustomerAddress.java
├── service/CustomerService.java
└── controller/CustomerController.java
```

**Features:**
- Customer CRUD
- Purchase history
- Outstanding balance
- Loyalty points (future)

---

#### 8. Email Service
**Why Needed:** Password reset, invoice emails, notifications.

```
common/email/
├── EmailService.java
├── EmailTemplateService.java
└── templates/
    ├── password-reset.html
    ├── invoice.html
    └── low-stock-alert.html
```

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

### 🟢 Phase 3 - Advanced Features (Lower Priority)

#### 9. Reports & Analytics

| Report | Description |
|--------|-------------|
| Sales Summary | Daily/weekly/monthly sales |
| Inventory Report | Stock levels, valuation |
| Profit & Loss | Cost vs Revenue |
| Tax Report | GST/VAT summary |
| Product Performance | Best/worst sellers |

```
modules/reports/
├── service/ReportService.java
├── dto/ReportRequest.java
└── controller/ReportController.java
```

---

#### 10. PDF Invoice Generation
**Current:** Stub returning placeholder bytes
**Needed:** Actual iText PDF generation

```java
// InvoiceService.exportPdf() implementation
- Company header with logo
- Invoice details
- Line items table
- Tax breakdown
- Payment info
- Terms & conditions
```

---

#### 11. Printer/Receipt Module
**Why Needed:** POS terminals need receipt printing.

```
modules/printer/
├── entity/PrintJob.java
├── service/
│   ├── PrintService.java
│   ├── ReceiptTemplateService.java
│   └── EscPosBuilder.java        # ESC/POS commands
└── controller/PrinterController.java
```

---

#### 12. Import/Export

| Feature | Format |
|---------|--------|
| Product Import | CSV, Excel |
| Product Export | CSV, Excel, PDF |
| Invoice Export | PDF, Excel |
| Bulk Stock Update | CSV |

---

#### 13. Multi-Location Support

```java
// Add to Product entity
@ManyToOne
private Location location;

// Add Location entity
@Entity
public class Location {
    private String name;
    private String address;
    private String type; // WAREHOUSE, STORE
}
```

---

## Future Improvements

### 🏗️ Architecture

| Improvement | Why | How |
|-------------|-----|-----|
| **CQRS Pattern** | Separate read/write for scale | Separate query services |
| **Event Sourcing** | Audit trail, replay | Store events, derive state |
| **Microservices** | Independent scaling | Split inventory, billing, auth |
| **Message Queue** | Async processing | RabbitMQ/Kafka for events |

### ⚡ Performance

| Improvement | Why | How |
|-------------|-----|-----|
| **Connection Pooling** | DB efficiency | HikariCP tuning |
| **Query Optimization** | Faster responses | Analyze slow queries, add indexes |
| **Caching Strategy** | Reduce DB load | Cache invalidation rules |
| **Elasticsearch** | Full-text search | Product search integration |
| **CDN** | Static assets | S3 + CloudFront |

### 🔒 Security

| Improvement | Why | How |
|-------------|-----|-----|
| **OAuth2/OIDC** | Enterprise SSO | Google, Azure AD integration |
| **MFA/2FA** | Account security | TOTP, SMS verification |
| **API Keys** | External integrations | Per-client keys with scopes |
| **IP Whitelisting** | Admin protection | Configurable per tenant |
| **Request Signing** | Webhook security | HMAC verification |

### 🧪 Testing

| Type | Coverage Target | Tools |
|------|-----------------|-------|
| Unit Tests | 80% | JUnit 5, Mockito |
| Integration Tests | Key flows | Testcontainers |
| Contract Tests | API contracts | Spring Cloud Contract |
| Load Tests | 1000 req/sec | Gatling, k6 |
| Security Tests | OWASP Top 10 | OWASP ZAP |

### 📊 Observability

| Tool | Purpose |
|------|---------|
| **Prometheus** | Metrics collection |
| **Grafana** | Dashboards |
| **Jaeger/Zipkin** | Distributed tracing |
| **ELK Stack** | Log aggregation |
| **Sentry** | Error tracking |

### 🚀 DevOps

| Improvement | Description |
|-------------|-------------|
| **Kubernetes** | K8s manifests for container orchestration |
| **Helm Charts** | Package manager for K8s |
| **GitOps** | ArgoCD for deployments |
| **CI/CD** | GitHub Actions / GitLab CI |
| **Terraform** | Infrastructure as Code |

---

## Implementation Priority Matrix

```
                    IMPACT
                High    │    Low
          ┌─────────────┼─────────────┐
    High  │  DO FIRST   │   SCHEDULE  │
          │  - POS      │   - Reports │
  EFFORT  │  - Supplier │   - Export  │
          │  - Purchase │             │
          ├─────────────┼─────────────┤
    Low   │  QUICK WINS │   DON'T DO  │
          │  - Email    │   (or later)│
          │  - PDF      │             │
          └─────────────┴─────────────┘
```

---

## Suggested Next Steps

1. **Complete POS Module** - Most impactful for MVP
2. **Add Supplier + Purchase Orders** - Core inventory workflow
3. **Implement Email Service** - Needed for password reset
4. **Add PDF Generation** - Invoice printing
5. **Payment Gateway Integration** - For SaaS monetization
6. **Write Integration Tests** - Ensure stability
7. **Deploy to Staging** - Real-world testing

---

## Contributing

When implementing new features:

1. Follow existing package structure
2. Use `BaseEntity` for all JPA entities
3. Wrap responses in `ApiResponse<T>`
4. Add Swagger annotations
5. Write unit tests
6. Add Flyway migration
7. Update documentation
