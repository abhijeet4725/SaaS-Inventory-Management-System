import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/stores/auth.store';

// Create axios instance
export const api = axios.create({
    baseURL: import.meta.env.DEV ? '/api' : 'https://api.example.com',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 30000,
});

// Request interceptor - add auth token
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = useAuthStore.getState().accessToken;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor - handle errors and token refresh
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<{ message?: string }>) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // Handle 401 - try to refresh token
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            const refreshToken = useAuthStore.getState().refreshToken;
            if (refreshToken) {
                try {
                    const response = await axios.post('/api/v1/auth/refresh', {
                        refreshToken,
                    });

                    const { accessToken, refreshToken: newRefreshToken } = response.data.data;
                    useAuthStore.getState().setTokens(accessToken, newRefreshToken);

                    originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                    return api(originalRequest);
                } catch (refreshError) {
                    // Refresh failed - logout user
                    useAuthStore.getState().logout();
                    window.location.href = '/login';
                    return Promise.reject(refreshError);
                }
            } else {
                // No refresh token - redirect to login
                useAuthStore.getState().logout();
                window.location.href = '/login';
            }
        }

        // Handle other errors
        const message = error.response?.data?.message || error.message || 'An error occurred';

        // Don't show toast for 401 (handled above) or auth endpoints
        if (error.response?.status !== 401 && !originalRequest.url?.includes('/auth/')) {
            toast.error(message);
        }

        return Promise.reject(error);
    }
);

export default api;
