// API Response Types — matches backend ApiResponse.java
export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
    pageInfo?: PageInfo;
    error?: ErrorDetails;
}

export interface PageInfo {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
}

export interface ErrorDetails {
    code?: string;
    field?: string;
    details?: any;
}

// ===== Auth Types — matches AuthResponse.java, LoginRequest.java, RegisterRequest.java =====

export interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName?: string;
    phone?: string;
    avatarUrl?: string;
    roles: string[];
    tenantId?: string;
    enabled: boolean;
    emailVerified: boolean;
    lastLoginAt?: string;
    createdAt: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: AuthUserInfo;
}

export interface AuthUserInfo {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName?: string;
    tenantId?: string;
    roles: string[];
}

export interface LoginRequest {
    email: string;
    password: string;
    tenantId?: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName?: string;
    phone?: string;
    companyName?: string;
    tenantId?: string;
}

// ===== Product Types — matches ProductDto.java =====

export interface Product {
    id: string;
    name: string;
    description?: string;
    sku: string;
    barcode?: string;
    category?: string;
    brand?: string;
    unit: string;
    costPrice: number;
    sellingPrice: number;
    taxRate: number;
    priceWithTax?: number;
    currentStock: number;
    minStockLevel: number;
    lowStock: boolean;
    active: boolean;
    service: boolean;
    trackInventory: boolean;
    imageUrl?: string;
    createdAt: string;
    updatedAt: string;
}

export interface CreateProductRequest {
    name: string;
    description?: string;
    sku?: string;
    barcode?: string;
    category?: string;
    brand?: string;
    unit?: string;
    costPrice?: number;
    sellingPrice: number;
    taxRate?: number;
    currentStock?: number;
    minStockLevel?: number;
    trackInventory?: boolean;
    service?: boolean;
}

export interface UpdateProductRequest {
    name?: string;
    description?: string;
    sku?: string;
    barcode?: string;
    category?: string;
    brand?: string;
    unit?: string;
    costPrice?: number;
    sellingPrice?: number;
    taxRate?: number;
    minStockLevel?: number;
    maxStockLevel?: number;
    reorderQuantity?: number;
    imageUrl?: string;
    active?: boolean;
}

export interface StockUpdateRequest {
    quantity: number;
    reason: string;
    notes?: string;
}

// ===== Invoice Types — matches InvoiceDto.java =====

export interface Invoice {
    id: string;
    invoiceNumber: string;
    customerId?: string;
    customerName?: string;
    customerEmail?: string;
    customerPhone?: string;
    status: string;
    invoiceDate?: string;
    dueDate?: string;
    items: InvoiceItem[];
    subtotal: number;
    taxAmount: number;
    discountAmount: number;
    totalAmount: number;
    paidAmount: number;
    balanceDue: number;
    paymentMethod?: string;
    paymentReference?: string;
    paidAt?: string;
    notes?: string;
    createdAt: string;
}

export interface InvoiceItem {
    id: string;
    productId?: string;
    productName: string;
    productSku?: string;
    description?: string;
    quantity: number;
    unitPrice: number;
    discountPercent?: number;
    taxRate: number;
    taxAmount: number;
    amount: number;
}

export interface CreateInvoiceRequest {
    customerId?: string;
    customerName?: string;
    customerEmail?: string;
    customerPhone?: string;
    customerAddress?: string;
    invoiceDate: string;
    dueDate?: string;
    items: CreateInvoiceItemRequest[];
    discountAmount?: number;
    notes?: string;
}

export interface CreateInvoiceItemRequest {
    productId?: string;
    productName: string;
    productSku?: string;
    description?: string;
    quantity: number;
    unitPrice: number;
    discountPercent?: number;
    taxRate?: number;
}

export interface PaymentRequest {
    amount: number;
    paymentMethod: string;
    reference?: string;
}

// ===== POS Types — matches PosDto.java =====

export interface Cart {
    id: string;
    status: string;
    customerName?: string;
    customerPhone?: string;
    items: CartItem[];
    subtotal: number;
    taxAmount: number;
    discountAmount: number;
    totalAmount: number;
    itemCount: number;
    createdAt: string;
}

export interface CartItem {
    productId: string;
    productName: string;
    productSku: string;
    quantity: number;
    unitPrice: number;
    taxRate: number;
    taxAmount: number;
    amount: number;
}

export interface AddItemRequest {
    productId: string;
    quantity: number;
}

export interface CheckoutRequest {
    paymentMethod: string;
    receivedAmount?: number;
    paymentReference?: string;
}

export interface CheckoutResponse {
    cartId: string;
    invoiceId: string;
    invoiceNumber: string;
    totalAmount: number;
    paidAmount: number;
    change: number;
    paymentMethod: string;
    status: string;
}

// ===== Customer Types — matches CustomerDto.java =====

export interface Customer {
    id: string;
    customerCode?: string;
    name: string;
    email?: string;
    phone?: string;
    altPhone?: string;
    customerType?: string;
    companyName?: string;
    taxId?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    fullAddress?: string;
    creditLimit?: number;
    outstandingBalance?: number;
    totalPurchases?: number;
    loyaltyPoints?: number;
    notes?: string;
    active: boolean;
    createdAt: string;
}

export interface CreateCustomerRequest {
    name: string;
    email?: string;
    phone?: string;
    customerType?: string;
    companyName?: string;
    taxId?: string;
    addressLine1?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    creditLimit?: number;
    notes?: string;
}

export interface UpdateCustomerRequest {
    customerCode?: string;
    name?: string;
    email?: string;
    phone?: string;
    altPhone?: string;
    customerType?: string;
    companyName?: string;
    taxId?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    creditLimit?: number;
    notes?: string;
    active?: boolean;
}

// ===== Supplier Types — matches SupplierDto.java =====

export interface Supplier {
    id: string;
    supplierCode?: string;
    name: string;
    contactPerson?: string;
    email?: string;
    phone?: string;
    altPhone?: string;
    website?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    fullAddress?: string;
    taxId?: string;
    paymentTerms?: string;
    creditLimit?: number;
    outstandingBalance?: number;
    bankName?: string;
    notes?: string;
    active: boolean;
    createdAt: string;
    updatedAt?: string;
}

export interface CreateSupplierRequest {
    supplierCode?: string;
    name: string;
    contactPerson?: string;
    email?: string;
    phone?: string;
    website?: string;
    addressLine1?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    taxId?: string;
    paymentTerms?: string;
    creditLimit?: number;
    notes?: string;
}

export interface UpdateSupplierRequest {
    supplierCode?: string;
    name?: string;
    contactPerson?: string;
    email?: string;
    phone?: string;
    altPhone?: string;
    website?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    taxId?: string;
    paymentTerms?: string;
    creditLimit?: number;
    bankName?: string;
    bankAccount?: string;
    notes?: string;
    active?: boolean;
}

// ===== Purchase Order Types — matches PurchaseOrderDto.java =====

export interface PurchaseOrder {
    id: string;
    poNumber: string;
    supplierId: string;
    supplierName: string;
    supplierEmail?: string;
    orderDate?: string;
    expectedDate?: string;
    receivedDate?: string;
    status: string;
    items: PurchaseOrderItem[];
    subtotal: number;
    taxAmount: number;
    shippingCost?: number;
    discountAmount?: number;
    totalAmount: number;
    shippingAddress?: string;
    notes?: string;
    approvedBy?: string;
    createdAt: string;
    updatedAt?: string;
}

export interface PurchaseOrderSummary {
    id: string;
    poNumber: string;
    supplierName: string;
    orderDate?: string;
    status: string;
    totalAmount: number;
    itemCount: number;
}

export interface PurchaseOrderItem {
    id: string;
    productId?: string;
    productName: string;
    productSku?: string;
    description?: string;
    quantity: number;
    receivedQuantity: number;
    pendingQuantity: number;
    unitCost: number;
    taxRate?: number;
    taxAmount?: number;
    amount: number;
}

export interface CreatePurchaseOrderRequest {
    supplierId: string;
    expectedDate?: string;
    shippingCost?: number;
    discountAmount?: number;
    shippingAddress?: string;
    notes?: string;
    items: CreatePOItemRequest[];
}

export interface CreatePOItemRequest {
    productId?: string;
    productName: string;
    productSku?: string;
    description?: string;
    quantity: number;
    unitCost: number;
    taxRate?: number;
}

export interface ReceiveItemsRequest {
    items: { itemId: string; quantity: number }[];
}

// ===== Report Types — matches ReportDto.java =====

export interface DashboardSummary {
    sales: SalesMetrics;
    inventory: InventoryMetrics;
    customers: CustomerMetrics;
    purchases: PurchaseMetrics;
    topProducts: TopProduct[];
    recentSales: RecentSale[];
}

export interface SalesMetrics {
    todaySales: number;
    weekSales: number;
    monthSales: number;
    yearSales: number;
    todayTransactions: number;
    monthTransactions: number;
    averageOrderValue: number;
    growthPercent: number;
}

export interface InventoryMetrics {
    totalProducts: number;
    lowStockCount: number;
    outOfStockCount: number;
    inventoryValue: number;
    categoriesCount: number;
}

export interface CustomerMetrics {
    totalCustomers: number;
    newCustomersThisMonth: number;
    activeCustomers: number;
    totalRevenue: number;
}

export interface PurchaseMetrics {
    pendingOrders: number;
    overdueOrders: number;
    pendingValue: number;
    monthPurchases: number;
}

export interface TopProduct {
    productId: string;
    productName: string;
    productSku: string;
    quantitySold: number;
    revenue: number;
}

export interface RecentSale {
    invoiceId: string;
    invoiceNumber: string;
    customerName: string;
    amount: number;
    date: string;
    paymentMethod: string;
}

export interface SalesReport {
    period?: string;
    startDate: string;
    endDate: string;
    totalSales: number;
    totalTax: number;
    totalDiscount: number;
    netSales: number;
    transactionCount: number;
    averageOrderValue: number;
    salesByPaymentMethod?: Record<string, number>;
    dailyBreakdown: DailySales[];
}

export interface DailySales {
    date: string;
    sales: number;
    transactions: number;
}

export interface InventoryReport {
    totalProducts: number;
    totalValue: number;
    lowStockItems: number;
    outOfStockItems: number;
    products: ProductStock[];
}

export interface ProductStock {
    productId: string;
    productName: string;
    sku: string;
    category: string;
    currentStock: number;
    reorderLevel: number;
    costPrice: number;
    sellingPrice: number;
    stockValue: number;
    status: string;
}

// ===== Printer Types — matches PrinterDto.java =====

export interface Printer {
    id: string;
    name: string;
    description?: string;
    printerType: string;
    connectionType: string;
    ipAddress?: string;
    port?: number;
    usbPath?: string;
    paperWidth?: number;
    isDefault: boolean;
    active: boolean;
    createdAt?: string;
}

export interface PrinterRequest {
    name: string;
    description?: string;
    printerType?: string;
    connectionType: string;
    ipAddress?: string;
    port?: number;
    usbPath?: string;
    paperWidth?: number;
    isDefault?: boolean;
}

export interface PrintJob {
    id: string;
    printerId?: string;
    printerName?: string;
    jobType: string;
    status: string;
    referenceType?: string;
    referenceId?: string;
    contentPreview?: string;
    errorMessage?: string;
    retryCount?: number;
    createdAt: string;
    printedAt?: string;
}

export interface SystemPrinterInfo {
    name: string;
    isDefault: boolean;
    supportsRaw: boolean;
    connectionType: string;
}

// ===== Company Types — matches CompanyDto.java =====

export interface Company {
    id: string;
    tenantId?: string;
    name: string;
    legalName?: string;
    registrationNumber?: string;
    taxId?: string;
    email?: string;
    phone?: string;
    website?: string;
    logoUrl?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    fullAddress?: string;
    currency?: string;
    timezone?: string;
    dateFormat?: string;
    defaultTaxRate?: number;
    invoicePrefix?: string;
    invoiceFooter?: string;
    subscriptionTier?: string;
    active?: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface CompanyUpdateRequest {
    name?: string;
    legalName?: string;
    registrationNumber?: string;
    taxId?: string;
    email?: string;
    phone?: string;
    website?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    postalCode?: string;
    country?: string;
    logoUrl?: string;
}

export interface CompanySettingsRequest {
    currency?: string;
    timezone?: string;
    dateFormat?: string;
    defaultTaxRate?: number;
    invoicePrefix?: string;
    invoiceFooter?: string;
}

// ===== Payment Types — matches PaymentController.java =====

export interface CreatePaymentOrderRequest {
    amount: number;
    currency?: string;
    description?: string;
    metadata?: Record<string, string>;
}

export interface PaymentOrder {
    orderId: string;
    amount: number;
    currency: string;
    status: string;
}

export interface VerifyPaymentRequest {
    orderId: string;
    paymentId: string;
    signature: string;
}

export interface RefundRequest {
    paymentId: string;
    amount: number;
    reason?: string;
}

export interface RefundResult {
    refundId: string;
    amount: number;
    status: string;
}

// ===== User Management Types — matches UserDto.java =====

export interface UserCreateRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
    roles: string[];
}

export interface UserUpdateRequest {
    firstName?: string;
    lastName?: string;
    phone?: string;
    avatarUrl?: string;
}

export interface UserRolesRequest {
    roles: string[];
}
