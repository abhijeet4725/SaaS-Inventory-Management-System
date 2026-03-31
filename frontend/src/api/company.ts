import api from './client';
import type { ApiResponse, Company, CompanyUpdateRequest, CompanySettingsRequest } from '@/types';

export const companyApi = {
    getCompany: async (): Promise<Company> => {
        const response = await api.get<ApiResponse<Company>>('/v1/company');
        return response.data.data;
    },

    updateCompany: async (data: CompanyUpdateRequest): Promise<Company> => {
        const response = await api.put<ApiResponse<Company>>('/v1/company', data);
        return response.data.data;
    },

    updateSettings: async (data: CompanySettingsRequest): Promise<Company> => {
        const response = await api.put<ApiResponse<Company>>('/v1/company/settings', data);
        return response.data.data;
    },
};
