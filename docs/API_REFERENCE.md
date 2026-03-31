# API Reference

Complete API reference with request/response examples.

---

## Base URL

```
Development: http://localhost:8080/api
Staging:     https://staging.example.com/api
Production:  https://api.example.com
```

---

## Authentication

All endpoints (except public auth endpoints) require JWT Bearer token:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Standard Response Format

**Success:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

**Paginated:**
```json
{
  "success": true,
  "message": "Success",
  "data": [ ... ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

**Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "field": "email",
    "details": { "email": "Invalid email format" }
  },
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

---

## Authentication Endpoints

### Register User

```http
POST /v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecureP@ss123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "tenantId": "acme-corp"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Created successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe",
      "tenantId": "acme-corp",
      "roles": ["CASHIER"]
    }
  }
}
```

---

### Login

```http
POST /v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecureP@ss123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe",
      "tenantId": "acme-corp",
      "roles": ["ADMIN", "MANAGER"]
    }
  }
}
```

---

### Refresh Token

```http
POST /v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## Inventory Endpoints

### Create Product

```http
POST /v1/inventory/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "sku": "WM-001",
  "barcode": "1234567890123",
  "category": "Electronics",
  "brand": "TechBrand",
  "unit": "piece",
  "costPrice": 15.00,
  "sellingPrice": 29.99,
  "taxRate": 10.00,
  "currentStock": 100,
  "minStockLevel": 10,
  "trackInventory": true
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Wireless Mouse",
    "sku": "WM-001",
    "barcode": "1234567890123",
    "category": "Electronics",
    "brand": "TechBrand",
    "sellingPrice": 29.99,
    "taxRate": 10.00,
    "priceWithTax": 32.99,
    "currentStock": 100,
    "minStockLevel": 10,
    "lowStock": false,
    "active": true,
    "trackInventory": true,
    "createdAt": "2024-01-15 10:30:00"
  }
}
```

---

### List Products

```http
GET /v1/inventory/products?page=0&size=20&sortBy=name&sortDir=asc
Authorization: Bearer {token}
```

---

### Update Stock

```http
PUT /v1/inventory/products/{id}/stock
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 50,
  "reason": "PURCHASE",
  "notes": "Received from supplier XYZ"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Stock updated",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Wireless Mouse",
    "currentStock": 150,
    "lowStock": false
  }
}
```

---

### Get Low Stock Products

```http
GET /v1/inventory/products/low-stock
Authorization: Bearer {token}
```

---

## Invoice Endpoints

### Create Invoice

```http
POST /v1/invoices
Authorization: Bearer {token}
Content-Type: application/json

{
  "customerId": "cust-123",
  "customerName": "Acme Corporation",
  "customerEmail": "billing@acme.com",
  "customerPhone": "+1234567890",
  "invoiceDate": "2024-01-15",
  "dueDate": "2024-02-15",
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440001",
      "productName": "Wireless Mouse",
      "productSku": "WM-001",
      "quantity": 5,
      "unitPrice": 29.99,
      "taxRate": 10.00
    },
    {
      "productName": "Installation Service",
      "quantity": 1,
      "unitPrice": 50.00,
      "taxRate": 0
    }
  ],
  "discountAmount": 10.00,
  "notes": "Net 30 payment terms"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "invoiceNumber": "INV-20240115-1234",
    "customerName": "Acme Corporation",
    "status": "PENDING",
    "invoiceDate": "2024-01-15",
    "dueDate": "2024-02-15",
    "items": [
      {
        "productName": "Wireless Mouse",
        "quantity": 5,
        "unitPrice": 29.99,
        "taxRate": 10.00,
        "taxAmount": 15.00,
        "amount": 149.95
      }
    ],
    "subtotal": 199.95,
    "taxAmount": 15.00,
    "discountAmount": 10.00,
    "totalAmount": 204.95,
    "paidAmount": 0,
    "balanceDue": 204.95
  }
}
```

---

### Record Payment

```http
POST /v1/invoices/{id}/payments
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 204.95,
  "paymentMethod": "CARD",
  "reference": "TXN-123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment recorded",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "invoiceNumber": "INV-20240115-1234",
    "status": "PAID",
    "totalAmount": 204.95,
    "paidAmount": 204.95,
    "balanceDue": 0,
    "paymentMethod": "CARD",
    "paidAt": "2024-01-15 11:00:00"
  }
}
```

---

## POS Endpoints

### Create Cart

```http
POST /v1/pos/cart
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Walk-in Customer",
  "phone": "+1234567890"
}
```

---

### Add Item to Cart

```http
POST /v1/pos/cart/{cartId}/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": "550e8400-e29b-41d4-a716-446655440001",
  "quantity": 2
}
```

---

### Checkout

```http
POST /v1/pos/cart/{cartId}/checkout
Authorization: Bearer {token}
Content-Type: application/json

{
  "paymentMethod": "CASH",
  "receivedAmount": 100.00
}
```

**Response:**
```json
{
  "success": true,
  "message": "Checkout complete",
  "data": {
    "cartId": "550e8400-e29b-41d4-a716-446655440003",
    "invoiceId": "550e8400-e29b-41d4-a716-446655440004",
    "invoiceNumber": "INV-20240115-1235",
    "totalAmount": 65.98,
    "paymentMethod": "CASH",
    "change": 34.02,
    "status": "COMPLETED"
  }
}
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `UNAUTHORIZED` | 401 | Missing or invalid token |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `RESOURCE_NOT_FOUND` | 404 | Entity not found |
| `DUPLICATE_SKU` | 400 | SKU already exists |
| `DUPLICATE_BARCODE` | 400 | Barcode already exists |
| `INSUFFICIENT_STOCK` | 400 | Not enough stock |
| `ALREADY_PAID` | 400 | Invoice already paid |
| `INVOICE_CANCELLED` | 400 | Cannot operate on cancelled invoice |
| `TENANT_ACCESS_DENIED` | 403 | Cross-tenant access attempt |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## WebSocket Events

Connect to: `ws://localhost:8080/api/ws`

### Subscribe Topics

```javascript
// Inventory updates for tenant
stomp.subscribe('/topic/inventory/acme-corp', (message) => {
  console.log(JSON.parse(message.body));
});

// POS updates for tenant
stomp.subscribe('/topic/pos/acme-corp', (message) => {
  console.log(JSON.parse(message.body));
});

// User notifications
stomp.subscribe('/topic/notifications/acme-corp', (message) => {
  console.log(JSON.parse(message.body));
});
```

### Event Payloads

**Stock Updated:**
```json
{
  "type": "STOCK_UPDATED",
  "productId": "550e8400-e29b-41d4-a716-446655440001",
  "productName": "Wireless Mouse",
  "currentStock": 150,
  "change": 50
}
```

**Low Stock Alert:**
```json
{
  "type": "LOW_STOCK_ALERT",
  "productId": "550e8400-e29b-41d4-a716-446655440001",
  "productName": "Wireless Mouse",
  "currentStock": 5,
  "minStockLevel": 10,
  "severity": "WARNING"
}
```
