import api from './client';
import type { ApiResponse, PurchaseOrder, PurchaseOrderSummary, CreatePurchaseOrderRequest, ReceiveItemsRequest, PageInfo } from '@/types';

interface POListParams {
    page?: number;
    size?: number;
    status?: string;
    search?: string;
}

interface POListResponse {
    purchaseOrders: PurchaseOrderSummary[];
    page: PageInfo;
}

export const purchaseOrdersApi = {
    getPurchaseOrders: async (params: POListParams = {}): Promise<POListResponse> => {
        const response = await api.get<ApiResponse<PurchaseOrderSummary[]>>('/v1/purchase-orders', { params });
        return {
            purchaseOrders: response.data.data,
            page: response.data.pageInfo || {
                page: 0,
                size: 20,
                totalElements: response.data.data.length,
                totalPages: 1,
                hasNext: false,
                hasPrevious: false,
            },
        };
    },

    getPurchaseOrder: async (id: string): Promise<PurchaseOrder> => {
        const response = await api.get<ApiResponse<PurchaseOrder>>(`/v1/purchase-orders/${id}`);
        return response.data.data;
    },

    createPurchaseOrder: async (data: CreatePurchaseOrderRequest): Promise<PurchaseOrder> => {
        const response = await api.post<ApiResponse<PurchaseOrder>>('/v1/purchase-orders', data);
        return response.data.data;
    },

    approvePurchaseOrder: async (id: string): Promise<PurchaseOrder> => {
        const response = await api.post<ApiResponse<PurchaseOrder>>(`/v1/purchase-orders/${id}/approve`);
        return response.data.data;
    },

    receiveItems: async (id: string, data: ReceiveItemsRequest): Promise<PurchaseOrder> => {
        const response = await api.post<ApiResponse<PurchaseOrder>>(`/v1/purchase-orders/${id}/receive`, data);
        return response.data.data;
    },

    cancelPurchaseOrder: async (id: string): Promise<void> => {
        await api.post(`/v1/purchase-orders/${id}/cancel`);
    },

    sendPurchaseOrder: async (id: string): Promise<PurchaseOrder> => {
        const response = await api.post<ApiResponse<PurchaseOrder>>(`/v1/purchase-orders/${id}/send`);
        return response.data.data;
    },
};
