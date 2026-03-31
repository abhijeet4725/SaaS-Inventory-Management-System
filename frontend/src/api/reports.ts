import api from './client';
import type { ApiResponse, DashboardSummary, SalesReport, InventoryReport } from '@/types';

export const reportsApi = {
    getDashboardSummary: async (): Promise<DashboardSummary> => {
        const response = await api.get<ApiResponse<DashboardSummary>>('/v1/reports/dashboard');
        return response.data.data;
    },

    getSalesReport: async (startDate: string, endDate: string): Promise<SalesReport> => {
        const response = await api.get<ApiResponse<SalesReport>>('/v1/reports/sales', {
            params: { startDate, endDate },
        });
        return response.data.data;
    },

    getInventoryReport: async (): Promise<InventoryReport> => {
        const response = await api.get<ApiResponse<InventoryReport>>('/v1/reports/inventory');
        return response.data.data;
    },
};
