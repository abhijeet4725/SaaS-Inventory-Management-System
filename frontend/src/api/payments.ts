import api from './client';
import type {
    ApiResponse,
    CreatePaymentOrderRequest,
    PaymentOrder,
    VerifyPaymentRequest,
    RefundRequest,
    RefundResult,
} from '@/types';

export const paymentsApi = {
    createOrder: async (data: CreatePaymentOrderRequest): Promise<PaymentOrder> => {
        const response = await api.post<ApiResponse<PaymentOrder>>('/v1/payments/create-order', data);
        return response.data.data;
    },

    verifyPayment: async (data: VerifyPaymentRequest): Promise<{ verified: boolean; orderId: string }> => {
        const response = await api.post<ApiResponse<{ verified: boolean; orderId: string }>>('/v1/payments/verify', data);
        return response.data.data;
    },

    processRefund: async (data: RefundRequest): Promise<RefundResult> => {
        const response = await api.post<ApiResponse<RefundResult>>('/v1/payments/refund', data);
        return response.data.data;
    },
};
