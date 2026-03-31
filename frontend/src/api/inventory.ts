import api from './client';
import type { ApiResponse, Product, CreateProductRequest, UpdateProductRequest, StockUpdateRequest, PageInfo } from '@/types';

interface ProductListParams {
    page?: number;
    size?: number;
    sort?: string;
    search?: string;
}

interface ProductListResponse {
    products: Product[];
    page: PageInfo;
}

export const inventoryApi = {
    getProducts: async (params: ProductListParams = {}): Promise<ProductListResponse> => {
        const response = await api.get<ApiResponse<Product[]>>('/v1/inventory/products', { params });
        return {
            products: response.data.data,
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

    getProduct: async (id: string): Promise<Product> => {
        const response = await api.get<ApiResponse<Product>>(`/v1/inventory/products/${id}`);
        return response.data.data;
    },

    getProductByBarcode: async (barcode: string): Promise<Product> => {
        const response = await api.get<ApiResponse<Product>>(`/v1/inventory/products/barcode/${barcode}`);
        return response.data.data;
    },

    searchProducts: async (query: string): Promise<Product[]> => {
        const response = await api.get<ApiResponse<Product[]>>('/v1/inventory/products/search', {
            params: { query },
        });
        return response.data.data;
    },

    createProduct: async (data: CreateProductRequest): Promise<Product> => {
        const response = await api.post<ApiResponse<Product>>('/v1/inventory/products', data);
        return response.data.data;
    },

    updateProduct: async (id: string, data: UpdateProductRequest): Promise<Product> => {
        const response = await api.put<ApiResponse<Product>>(`/v1/inventory/products/${id}`, data);
        return response.data.data;
    },

    deleteProduct: async (id: string): Promise<void> => {
        await api.delete(`/v1/inventory/products/${id}`);
    },

    updateStock: async (id: string, data: StockUpdateRequest): Promise<Product> => {
        const response = await api.put<ApiResponse<Product>>(`/v1/inventory/products/${id}/stock`, data);
        return response.data.data;
    },

    getLowStockProducts: async (): Promise<Product[]> => {
        const response = await api.get<ApiResponse<Product[]>>('/v1/inventory/products/low-stock');
        return response.data.data;
    },
};
