import api from './client';
import type { ApiResponse, Printer, PrinterRequest, PrintJob, SystemPrinterInfo } from '@/types';

export const printersApi = {
    // ===== Printer Management (under /v1/printers) =====

    getPrinters: async (): Promise<Printer[]> => {
        const response = await api.get<ApiResponse<Printer[]>>('/v1/printers');
        return response.data.data;
    },

    getPrinter: async (id: string): Promise<Printer> => {
        const response = await api.get<ApiResponse<Printer>>(`/v1/printers/${id}`);
        return response.data.data;
    },

    createPrinter: async (data: PrinterRequest): Promise<Printer> => {
        const response = await api.post<ApiResponse<Printer>>('/v1/printers', data);
        return response.data.data;
    },

    updatePrinter: async (id: string, data: PrinterRequest): Promise<Printer> => {
        const response = await api.put<ApiResponse<Printer>>(`/v1/printers/${id}`, data);
        return response.data.data;
    },

    deletePrinter: async (id: string): Promise<void> => {
        await api.delete(`/v1/printers/${id}`);
    },

    // ===== System Printer Discovery (under /v1/printers/system) =====

    discoverSystemPrinters: async (): Promise<SystemPrinterInfo[]> => {
        const response = await api.get<ApiResponse<SystemPrinterInfo[]>>('/v1/printers/system');
        return response.data.data;
    },

    getDefaultSystemPrinter: async (): Promise<SystemPrinterInfo | null> => {
        const response = await api.get<ApiResponse<SystemPrinterInfo>>('/v1/printers/system/default');
        return response.data.data;
    },

    checkPrinterAvailable: async (printerName: string): Promise<boolean> => {
        const response = await api.get<ApiResponse<boolean>>(`/v1/printers/system/check/${printerName}`);
        return response.data.data;
    },

    // ===== Print Operations (under /v1/print) =====

    printInvoice: async (invoiceId: string, printerId?: string): Promise<PrintJob> => {
        const response = await api.post<ApiResponse<PrintJob>>(
            `/v1/print/invoice/${invoiceId}`,
            null,
            printerId ? { params: { printerId } } : undefined
        );
        return response.data.data;
    },

    printCart: async (cartId: string, printerId?: string): Promise<PrintJob> => {
        const response = await api.post<ApiResponse<PrintJob>>(
            `/v1/print/cart/${cartId}`,
            null,
            printerId ? { params: { printerId } } : undefined
        );
        return response.data.data;
    },

    testPrint: async (printerId: string, message?: string): Promise<PrintJob> => {
        const response = await api.post<ApiResponse<PrintJob>>('/v1/print/test', {
            printerId,
            message,
        });
        return response.data.data;
    },

    // ===== Print Jobs (under /v1/print/jobs) =====

    getPrintJobs: async (params: { page?: number; size?: number } = {}): Promise<PrintJob[]> => {
        const response = await api.get<ApiResponse<PrintJob[]>>('/v1/print/jobs', { params });
        return response.data.data;
    },

    getPrintJob: async (id: string): Promise<PrintJob> => {
        const response = await api.get<ApiResponse<PrintJob>>(`/v1/print/jobs/${id}`);
        return response.data.data;
    },

    retryPrintJob: async (id: string): Promise<PrintJob> => {
        const response = await api.post<ApiResponse<PrintJob>>(`/v1/print/jobs/${id}/retry`);
        return response.data.data;
    },

    // ===== Preview =====

    previewInvoice: async (invoiceId: string): Promise<{ content: string }> => {
        const response = await api.get<ApiResponse<{ content: string }>>(`/v1/print/preview/invoice/${invoiceId}`);
        return response.data.data;
    },
};
