import api from './client';
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '@/types';

export const authApi = {
    login: async (data: LoginRequest): Promise<AuthResponse> => {
        const response = await api.post<ApiResponse<AuthResponse>>('/v1/auth/login', data);
        return response.data.data;
    },

    register: async (data: RegisterRequest): Promise<AuthResponse> => {
        const response = await api.post<ApiResponse<AuthResponse>>('/v1/auth/register', data);
        return response.data.data;
    },

    refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
        const response = await api.post<ApiResponse<AuthResponse>>('/v1/auth/refresh', { refreshToken });
        return response.data.data;
    },

    logout: async (): Promise<void> => {
        await api.post('/v1/auth/logout');
    },

    forgotPassword: async (email: string): Promise<void> => {
        await api.post('/v1/auth/forgot-password', { email });
    },

    resetPassword: async (token: string, newPassword: string): Promise<void> => {
        await api.post('/v1/auth/reset-password', { token, newPassword });
    },

    changePassword: async (currentPassword: string, newPassword: string): Promise<void> => {
        await api.post('/v1/auth/change-password', { currentPassword, newPassword });
    },

    getCurrentUser: async () => {
        const response = await api.get('/v1/auth/me');
        return response.data.data;
    },
};
