import { create } from 'zustand';
import type { Cart, CartItem, Product } from '@/types';

interface CartState {
    cart: Cart | null;
    isLoading: boolean;

    // Actions
    setCart: (cart: Cart | null) => void;
    setLoading: (loading: boolean) => void;
    clearCart: () => void;

    // Local cart management (for offline/demo mode)
    localItems: CartItem[];
    addLocalItem: (product: Product, quantity?: number) => void;
    updateLocalItemQuantity: (index: number, quantity: number) => void;
    removeLocalItem: (index: number) => void;
    clearLocalItems: () => void;
    getLocalSubtotal: () => number;
    getLocalTax: () => number;
    getLocalTotal: () => number;
}

export const useCartStore = create<CartState>((set, get) => ({
    cart: null,
    isLoading: false,
    localItems: [],

    setCart: (cart) => set({ cart }),
    setLoading: (isLoading) => set({ isLoading }),
    clearCart: () => set({ cart: null }),

    addLocalItem: (product, quantity = 1) => {
        const items = get().localItems;
        const existingIndex = items.findIndex((item) => item.productId === product.id);

        if (existingIndex >= 0) {
            // Update quantity
            const newItems = [...items];
            const item = newItems[existingIndex];
            const newQuantity = item.quantity + quantity;
            const taxAmount = item.unitPrice * (item.taxRate / 100) * newQuantity;
            newItems[existingIndex] = {
                ...item,
                quantity: newQuantity,
                taxAmount,
                amount: item.unitPrice * newQuantity,
            };
            set({ localItems: newItems });
        } else {
            // Add new item
            const taxAmount = product.sellingPrice * (product.taxRate / 100) * quantity;
            const newItem: CartItem = {
                productId: product.id,
                productName: product.name,
                productSku: product.sku,
                quantity,
                unitPrice: product.sellingPrice,
                taxRate: product.taxRate,
                taxAmount,
                amount: product.sellingPrice * quantity,
            };
            set({ localItems: [...items, newItem] });
        }
    },

    updateLocalItemQuantity: (index, quantity) => {
        const items = get().localItems;
        if (index < 0 || index >= items.length) return;

        if (quantity <= 0) {
            // Remove item
            set({ localItems: items.filter((_, i) => i !== index) });
        } else {
            // Update quantity
            const newItems = [...items];
            const item = newItems[index];
            const taxAmount = item.unitPrice * (item.taxRate / 100) * quantity;
            newItems[index] = {
                ...item,
                quantity,
                taxAmount,
                amount: item.unitPrice * quantity,
            };
            set({ localItems: newItems });
        }
    },

    removeLocalItem: (index) => {
        const items = get().localItems;
        set({ localItems: items.filter((_, i) => i !== index) });
    },

    clearLocalItems: () => set({ localItems: [] }),

    getLocalSubtotal: () => {
        return get().localItems.reduce((sum, item) => sum + item.amount, 0);
    },

    getLocalTax: () => {
        return get().localItems.reduce((sum, item) => sum + item.taxAmount, 0);
    },

    getLocalTotal: () => {
        const subtotal = get().getLocalSubtotal();
        const tax = get().getLocalTax();
        return subtotal + tax;
    },
}));
