import { useState, useRef, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Search,
    Plus,
    Minus,
    Trash2,
    CreditCard,
    Banknote,
    Smartphone,
    User,
    X,
    Receipt,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input } from '@/components/ui';
import { inventoryApi } from '@/api/inventory';
import { useCartStore } from '@/stores/cart.store';
import { formatCurrency } from '@/lib/utils';
import type { Product } from '@/types';

// Mock products for demo
const mockProducts: Product[] = [
    { id: '1', name: 'Wireless Mouse', sku: 'WM-001', barcode: '123456789', description: '', category: 'Electronics', costPrice: 300, sellingPrice: 599, taxRate: 18, currentStock: 50, minStockLevel: 10, lowStock: false, unit: 'pcs', active: true, service: false, trackInventory: true, createdAt: '', updatedAt: '' },
    { id: '2', name: 'USB-C Cable', sku: 'UC-002', barcode: '234567890', description: '', category: 'Electronics', costPrice: 100, sellingPrice: 249, taxRate: 18, currentStock: 100, minStockLevel: 20, lowStock: false, unit: 'pcs', active: true, service: false, trackInventory: true, createdAt: '', updatedAt: '' },
    { id: '3', name: 'Laptop Stand', sku: 'LS-003', barcode: '345678901', description: '', category: 'Accessories', costPrice: 800, sellingPrice: 1499, taxRate: 18, currentStock: 25, minStockLevel: 5, lowStock: false, unit: 'pcs', active: true, service: false, trackInventory: true, createdAt: '', updatedAt: '' },
    { id: '4', name: 'Webcam HD', sku: 'WC-004', barcode: '456789012', description: '', category: 'Electronics', costPrice: 1500, sellingPrice: 2999, taxRate: 18, currentStock: 15, minStockLevel: 5, lowStock: false, unit: 'pcs', active: true, service: false, trackInventory: true, createdAt: '', updatedAt: '' },
    { id: '5', name: 'Mechanical Keyboard', sku: 'MK-005', barcode: '567890123', description: '', category: 'Electronics', costPrice: 2000, sellingPrice: 3999, taxRate: 18, currentStock: 20, minStockLevel: 5, lowStock: false, unit: 'pcs', active: true, service: false, trackInventory: true, createdAt: '', updatedAt: '' },
    { id: '6', name: 'Monitor Arm', sku: 'MA-006', barcode: '678901234', description: '', category: 'Accessories', costPrice: 1200, sellingPrice: 2299, taxRate: 18, currentStock: 12, minStockLevel: 5, lowStock: false, unit: 'pcs', active: true, service: false, trackInventory: true, createdAt: '', updatedAt: '' },
];

export function PosPage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [barcodeInput, setBarcodeInput] = useState('');
    const [showCheckout, setShowCheckout] = useState(false);
    const [selectedPayment, setSelectedPayment] = useState<'CASH' | 'CARD' | 'UPI'>('CASH');
    const [tenderedAmount, setTenderedAmount] = useState('');
    const barcodeRef = useRef<HTMLInputElement>(null);

    const {
        localItems,
        addLocalItem,
        updateLocalItemQuantity,
        removeLocalItem,
        clearLocalItems,
        getLocalSubtotal,
        getLocalTax,
        getLocalTotal,
    } = useCartStore();

    // Focus barcode input on mount
    useEffect(() => {
        barcodeRef.current?.focus();
    }, []);

    // Search products
    const { data: searchResults = [] } = useQuery({
        queryKey: ['product-search', searchQuery],
        queryFn: () => inventoryApi.searchProducts(searchQuery),
        enabled: searchQuery.length >= 2,
        placeholderData: mockProducts.filter(
            (p) =>
                p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                p.sku.toLowerCase().includes(searchQuery.toLowerCase())
        ),
    });

    const handleBarcodeSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!barcodeInput.trim()) return;

        // Find product by barcode
        const product = mockProducts.find((p) => p.barcode === barcodeInput);
        if (product) {
            addLocalItem(product);
            toast.success(`Added ${product.name}`);
        } else {
            toast.error('Product not found');
        }
        setBarcodeInput('');
        barcodeRef.current?.focus();
    };

    const handleAddProduct = (product: Product) => {
        addLocalItem(product);
        toast.success(`Added ${product.name}`);
        setSearchQuery('');
    };

    const handleCheckout = () => {
        if (localItems.length === 0) {
            toast.error('Cart is empty');
            return;
        }

        const total = getLocalTotal();
        const tendered = parseFloat(tenderedAmount) || total;

        if (selectedPayment === 'CASH' && tendered < total) {
            toast.error('Insufficient amount');
            return;
        }

        const change = tendered - total;

        toast.success(
            `Payment successful! ${selectedPayment === 'CASH' && change > 0 ? `Change: ${formatCurrency(change)}` : ''}`
        );
        clearLocalItems();
        setShowCheckout(false);
        setTenderedAmount('');
        barcodeRef.current?.focus();
    };

    const subtotal = getLocalSubtotal();
    const tax = getLocalTax();
    const total = getLocalTotal();

    return (
        <div className="flex h-[calc(100vh-7rem)] gap-6">
            {/* Left Panel - Products */}
            <div className="flex flex-1 flex-col overflow-hidden">
                {/* Barcode Scanner Input */}
                <form onSubmit={handleBarcodeSubmit} className="mb-4">
                    <div className="relative">
                        <div className="absolute left-3 top-1/2 -translate-y-1/2 rounded bg-primary px-2 py-0.5 text-xs font-medium text-primary-foreground">
                            SCAN
                        </div>
                        <input
                            ref={barcodeRef}
                            type="text"
                            value={barcodeInput}
                            onChange={(e) => setBarcodeInput(e.target.value)}
                            placeholder="Scan barcode or enter manually..."
                            className="h-12 w-full rounded-lg border border-primary bg-background pl-20 pr-4 text-lg font-mono placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                        />
                    </div>
                </form>

                {/* Product Search */}
                <div className="mb-4">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            placeholder="Search products by name or SKU..."
                            className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                        />
                    </div>

                    {/* Search Results */}
                    {searchQuery.length >= 2 && (
                        <div className="mt-2 max-h-48 overflow-y-auto rounded-lg border border-border bg-card">
                            {searchResults.length === 0 ? (
                                <p className="p-4 text-center text-muted-foreground">No products found</p>
                            ) : (
                                searchResults.map((product) => (
                                    <button
                                        key={product.id}
                                        onClick={() => handleAddProduct(product)}
                                        className="flex w-full items-center justify-between p-3 text-left hover:bg-accent"
                                    >
                                        <div>
                                            <p className="font-medium text-foreground">{product.name}</p>
                                            <p className="text-sm text-muted-foreground">{product.sku}</p>
                                        </div>
                                        <div className="text-right">
                                            <p className="font-medium text-foreground">
                                                {formatCurrency(product.sellingPrice)}
                                            </p>
                                            <p className="text-sm text-muted-foreground">Qty: {product.currentStock}</p>
                                        </div>
                                    </button>
                                ))
                            )}
                        </div>
                    )}
                </div>

                {/* Quick Products Grid */}
                <div className="flex-1 overflow-y-auto">
                    <p className="mb-3 text-sm font-medium text-muted-foreground">Quick Add</p>
                    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
                        {mockProducts.map((product) => (
                            <button
                                key={product.id}
                                onClick={() => handleAddProduct(product)}
                                className="flex flex-col items-center rounded-lg border border-border bg-card p-4 text-center transition-colors hover:bg-accent"
                            >
                                <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
                                    <span className="text-lg font-bold text-primary">
                                        {product.name.charAt(0)}
                                    </span>
                                </div>
                                <p className="text-sm font-medium text-foreground line-clamp-1">{product.name}</p>
                                <p className="text-sm font-bold text-primary">
                                    {formatCurrency(product.sellingPrice)}
                                </p>
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* Right Panel - Cart */}
            <div className="flex w-96 flex-col rounded-lg border border-border bg-card">
                {/* Cart Header */}
                <div className="flex items-center justify-between border-b border-border p-4">
                    <h2 className="text-lg font-semibold text-foreground">Current Sale</h2>
                    <div className="flex gap-2">
                        <Button variant="ghost" size="sm">
                            <User className="mr-1 h-4 w-4" />
                            Customer
                        </Button>
                        {localItems.length > 0 && (
                            <Button variant="ghost" size="sm" onClick={clearLocalItems}>
                                <Trash2 className="h-4 w-4 text-destructive" />
                            </Button>
                        )}
                    </div>
                </div>

                {/* Cart Items */}
                <div className="flex-1 overflow-y-auto p-4">
                    {localItems.length === 0 ? (
                        <div className="flex h-full flex-col items-center justify-center text-muted-foreground">
                            <Receipt className="mb-2 h-12 w-12 opacity-50" />
                            <p>Cart is empty</p>
                            <p className="text-sm">Scan or search for products</p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {localItems.map((item, index) => (
                                <div
                                    key={`${item.productId}-${index}`}
                                    className="rounded-lg border border-border p-3"
                                >
                                    <div className="flex items-start justify-between">
                                        <div className="flex-1">
                                            <p className="font-medium text-foreground">{item.productName}</p>
                                            <p className="text-sm text-muted-foreground">
                                                {formatCurrency(item.unitPrice)} × {item.quantity}
                                            </p>
                                        </div>
                                        <p className="font-semibold text-foreground">
                                            {formatCurrency(item.amount)}
                                        </p>
                                    </div>
                                    <div className="mt-2 flex items-center gap-2">
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="h-8 w-8"
                                            onClick={() => updateLocalItemQuantity(index, item.quantity - 1)}
                                        >
                                            <Minus className="h-4 w-4" />
                                        </Button>
                                        <span className="w-8 text-center font-medium">{item.quantity}</span>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="h-8 w-8"
                                            onClick={() => updateLocalItemQuantity(index, item.quantity + 1)}
                                        >
                                            <Plus className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            className="ml-auto h-8 w-8"
                                            onClick={() => removeLocalItem(index)}
                                        >
                                            <X className="h-4 w-4 text-destructive" />
                                        </Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Cart Summary */}
                <div className="border-t border-border p-4">
                    <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                            <span className="text-muted-foreground">Subtotal</span>
                            <span className="text-foreground">{formatCurrency(subtotal)}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-muted-foreground">Tax (GST)</span>
                            <span className="text-foreground">{formatCurrency(tax)}</span>
                        </div>
                        <div className="flex justify-between border-t border-border pt-2 text-lg font-bold">
                            <span className="text-foreground">Total</span>
                            <span className="text-primary">{formatCurrency(total)}</span>
                        </div>
                    </div>

                    {/* Checkout Button */}
                    <Button
                        className="mt-4 w-full"
                        size="lg"
                        disabled={localItems.length === 0}
                        onClick={() => setShowCheckout(true)}
                    >
                        Checkout
                    </Button>
                </div>
            </div>

            {/* Checkout Modal */}
            {showCheckout && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                    <div className="w-full max-w-md rounded-lg border border-border bg-card p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-xl font-semibold text-foreground">Checkout</h2>
                            <Button variant="ghost" size="icon" onClick={() => setShowCheckout(false)}>
                                <X className="h-5 w-5" />
                            </Button>
                        </div>

                        {/* Amount */}
                        <div className="mb-6 text-center">
                            <p className="text-muted-foreground">Total Amount</p>
                            <p className="text-4xl font-bold text-primary">{formatCurrency(total)}</p>
                        </div>

                        {/* Payment Methods */}
                        <div className="mb-6">
                            <p className="mb-3 text-sm font-medium text-muted-foreground">Payment Method</p>
                            <div className="grid grid-cols-3 gap-3">
                                <button
                                    onClick={() => setSelectedPayment('CASH')}
                                    className={`flex flex-col items-center gap-2 rounded-lg border p-4 transition-colors ${selectedPayment === 'CASH'
                                        ? 'border-primary bg-primary/10'
                                        : 'border-border hover:bg-accent'
                                        }`}
                                >
                                    <Banknote className="h-6 w-6" />
                                    <span className="text-sm font-medium">Cash</span>
                                </button>
                                <button
                                    onClick={() => setSelectedPayment('CARD')}
                                    className={`flex flex-col items-center gap-2 rounded-lg border p-4 transition-colors ${selectedPayment === 'CARD'
                                        ? 'border-primary bg-primary/10'
                                        : 'border-border hover:bg-accent'
                                        }`}
                                >
                                    <CreditCard className="h-6 w-6" />
                                    <span className="text-sm font-medium">Card</span>
                                </button>
                                <button
                                    onClick={() => setSelectedPayment('UPI')}
                                    className={`flex flex-col items-center gap-2 rounded-lg border p-4 transition-colors ${selectedPayment === 'UPI'
                                        ? 'border-primary bg-primary/10'
                                        : 'border-border hover:bg-accent'
                                        }`}
                                >
                                    <Smartphone className="h-6 w-6" />
                                    <span className="text-sm font-medium">UPI</span>
                                </button>
                            </div>
                        </div>

                        {/* Cash Tendered */}
                        {selectedPayment === 'CASH' && (
                            <div className="mb-6">
                                <Input
                                    label="Amount Tendered"
                                    type="number"
                                    step="0.01"
                                    placeholder={total.toString()}
                                    value={tenderedAmount}
                                    onChange={(e) => setTenderedAmount(e.target.value)}
                                />
                                {tenderedAmount && parseFloat(tenderedAmount) >= total && (
                                    <p className="mt-2 text-sm text-success">
                                        Change: {formatCurrency(parseFloat(tenderedAmount) - total)}
                                    </p>
                                )}
                            </div>
                        )}

                        {/* Actions */}
                        <div className="flex gap-3">
                            <Button variant="outline" className="flex-1" onClick={() => setShowCheckout(false)}>
                                Cancel
                            </Button>
                            <Button className="flex-1" onClick={handleCheckout}>
                                <Receipt className="mr-2 h-4 w-4" />
                                Complete Sale
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
