import api from './client';
import type { ApiResponse, Supplier, CreateSupplierRequest, UpdateSupplierRequest, PageInfo } from '@/types';

interface SupplierListParams {
    page?: number;
    size?: number;
    search?: string;
}

interface SupplierListResponse {
    suppliers: Supplier[];
    page: PageInfo;
}

export const suppliersApi = {
    getSuppliers: async (params: SupplierListParams = {}): Promise<SupplierListResponse> => {
        const response = await api.get<ApiResponse<Supplier[]>>('/v1/suppliers', { params });
        return {
            suppliers: response.data.data,
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

    getSupplier: async (id: string): Promise<Supplier> => {
        const response = await api.get<ApiResponse<Supplier>>(`/v1/suppliers/${id}`);
        return response.data.data;
    },

    getActiveSuppliers: async (): Promise<Supplier[]> => {
        const response = await api.get<ApiResponse<Supplier[]>>('/v1/suppliers/active');
        return response.data.data;
    },

    searchSuppliers: async (query: string): Promise<Supplier[]> => {
        const response = await api.get<ApiResponse<Supplier[]>>('/v1/suppliers/search', {
            params: { query },
        });
        return response.data.data;
    },

    createSupplier: async (data: CreateSupplierRequest): Promise<Supplier> => {
        const response = await api.post<ApiResponse<Supplier>>('/v1/suppliers', data);
        return response.data.data;
    },

    updateSupplier: async (id: string, data: UpdateSupplierRequest): Promise<Supplier> => {
        const response = await api.put<ApiResponse<Supplier>>(`/v1/suppliers/${id}`, data);
        return response.data.data;
    },

    deleteSupplier: async (id: string): Promise<void> => {
        await api.delete(`/v1/suppliers/${id}`);
    },
};
