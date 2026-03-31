import api from './client';
import type { ApiResponse, Cart, AddItemRequest, CheckoutRequest, CheckoutResponse } from '@/types';

export const posApi = {
    createCart: async (customer?: { name?: string; phone?: string }): Promise<Cart> => {
        const response = await api.post<ApiResponse<Cart>>('/v1/pos/cart', customer || null);
        return response.data.data;
    },

    getCart: async (cartId: string): Promise<Cart> => {
        const response = await api.get<ApiResponse<Cart>>(`/v1/pos/cart/${cartId}`);
        return response.data.data;
    },

    addItem: async (cartId: string, data: AddItemRequest): Promise<Cart> => {
        const response = await api.post<ApiResponse<Cart>>(`/v1/pos/cart/${cartId}/items`, data);
        return response.data.data;
    },

    addItemByBarcode: async (cartId: string, barcode: string, quantity: number = 1): Promise<Cart> => {
        const response = await api.post<ApiResponse<Cart>>(
            `/v1/pos/cart/${cartId}/items/barcode`,
            null,
            { params: { barcode, quantity } }
        );
        return response.data.data;
    },

    updateItemQuantity: async (cartId: string, productId: string, quantity: number): Promise<Cart> => {
        const response = await api.put<ApiResponse<Cart>>(
            `/v1/pos/cart/${cartId}/items/${productId}`,
            null,
            { params: { quantity } }
        );
        return response.data.data;
    },

    removeItem: async (cartId: string, productId: string): Promise<Cart> => {
        const response = await api.delete<ApiResponse<Cart>>(
            `/v1/pos/cart/${cartId}/items/${productId}`
        );
        return response.data.data;
    },

    applyDiscount: async (cartId: string, amount: number): Promise<Cart> => {
        const response = await api.post<ApiResponse<Cart>>(
            `/v1/pos/cart/${cartId}/discount`,
            null,
            { params: { amount } }
        );
        return response.data.data;
    },

    checkout: async (cartId: string, data: CheckoutRequest): Promise<CheckoutResponse> => {
        const response = await api.post<ApiResponse<CheckoutResponse>>(
            `/v1/pos/cart/${cartId}/checkout`,
            data
        );
        return response.data.data;
    },

    voidCart: async (cartId: string): Promise<void> => {
        await api.post(`/v1/pos/cart/${cartId}/void`);
    },
};
