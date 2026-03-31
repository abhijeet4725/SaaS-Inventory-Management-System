import api from './client';
import type { ApiResponse, Customer, CreateCustomerRequest, UpdateCustomerRequest, PageInfo } from '@/types';

interface CustomerListParams {
    page?: number;
    size?: number;
    search?: string;
}

interface CustomerListResponse {
    customers: Customer[];
    page: PageInfo;
}

export const customersApi = {
    getCustomers: async (params: CustomerListParams = {}): Promise<CustomerListResponse> => {
        const response = await api.get<ApiResponse<Customer[]>>('/v1/customers', { params });
        return {
            customers: response.data.data,
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

    getCustomer: async (id: string): Promise<Customer> => {
        const response = await api.get<ApiResponse<Customer>>(`/v1/customers/${id}`);
        return response.data.data;
    },

    searchCustomers: async (query: string): Promise<Customer[]> => {
        const response = await api.get<ApiResponse<Customer[]>>('/v1/customers/search', {
            params: { query },
        });
        return response.data.data;
    },

    findByPhone: async (phone: string): Promise<Customer> => {
        const response = await api.get<ApiResponse<Customer>>(`/v1/customers/phone/${phone}`);
        return response.data.data;
    },

    createCustomer: async (data: CreateCustomerRequest): Promise<Customer> => {
        const response = await api.post<ApiResponse<Customer>>('/v1/customers', data);
        return response.data.data;
    },

    updateCustomer: async (id: string, data: UpdateCustomerRequest): Promise<Customer> => {
        const response = await api.put<ApiResponse<Customer>>(`/v1/customers/${id}`, data);
        return response.data.data;
    },

    deleteCustomer: async (id: string): Promise<void> => {
        await api.delete(`/v1/customers/${id}`);
    },
};
