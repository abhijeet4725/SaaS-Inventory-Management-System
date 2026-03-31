import api from './client';
import type { ApiResponse, User, UserCreateRequest, UserUpdateRequest, UserRolesRequest, PageInfo } from '@/types';

interface UserListParams {
    page?: number;
    size?: number;
    search?: string;
}

interface UserListResponse {
    users: User[];
    page: PageInfo;
}

export const usersApi = {
    getUsers: async (params: UserListParams = {}): Promise<UserListResponse> => {
        const response = await api.get<ApiResponse<User[]>>('/v1/users', { params });
        return {
            users: response.data.data,
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

    getUser: async (id: string): Promise<User> => {
        const response = await api.get<ApiResponse<User>>(`/v1/users/${id}`);
        return response.data.data;
    },

    createUser: async (data: UserCreateRequest): Promise<User> => {
        const response = await api.post<ApiResponse<User>>('/v1/users', data);
        return response.data.data;
    },

    updateUser: async (id: string, data: UserUpdateRequest): Promise<User> => {
        const response = await api.put<ApiResponse<User>>(`/v1/users/${id}`, data);
        return response.data.data;
    },

    updateRoles: async (id: string, data: UserRolesRequest): Promise<User> => {
        const response = await api.put<ApiResponse<User>>(`/v1/users/${id}/roles`, data);
        return response.data.data;
    },

    enableUser: async (id: string): Promise<User> => {
        const response = await api.post<ApiResponse<User>>(`/v1/users/${id}/enable`);
        return response.data.data;
    },

    disableUser: async (id: string): Promise<User> => {
        const response = await api.post<ApiResponse<User>>(`/v1/users/${id}/disable`);
        return response.data.data;
    },

    deleteUser: async (id: string): Promise<void> => {
        await api.delete(`/v1/users/${id}`);
    },

    resetPassword: async (id: string, newPassword: string): Promise<void> => {
        await api.post(`/v1/users/${id}/reset-password`, { newPassword });
    },
};
