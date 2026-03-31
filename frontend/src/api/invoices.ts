import api from './client';
import type { ApiResponse, Invoice, CreateInvoiceRequest, PaymentRequest, PageInfo } from '@/types';

interface InvoiceListParams {
    page?: number;
    size?: number;
    status?: string;
    search?: string;
}

interface InvoiceListResponse {
    invoices: Invoice[];
    page: PageInfo;
}

export const invoicesApi = {
    getInvoices: async (params: InvoiceListParams = {}): Promise<InvoiceListResponse> => {
        const response = await api.get<ApiResponse<Invoice[]>>('/v1/invoices', { params });
        return {
            invoices: response.data.data,
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

    getInvoice: async (id: string): Promise<Invoice> => {
        const response = await api.get<ApiResponse<Invoice>>(`/v1/invoices/${id}`);
        return response.data.data;
    },

    createInvoice: async (data: CreateInvoiceRequest): Promise<Invoice> => {
        const response = await api.post<ApiResponse<Invoice>>('/v1/invoices', data);
        return response.data.data;
    },

    recordPayment: async (id: string, data: PaymentRequest): Promise<Invoice> => {
        const response = await api.post<ApiResponse<Invoice>>(`/v1/invoices/${id}/payments`, data);
        return response.data.data;
    },

    cancelInvoice: async (id: string): Promise<void> => {
        await api.post(`/v1/invoices/${id}/cancel`);
    },

    downloadPdf: async (id: string): Promise<Blob> => {
        const response = await api.get(`/v1/invoices/${id}/pdf`, {
            responseType: 'blob',
        });
        return response.data;
    },
};
