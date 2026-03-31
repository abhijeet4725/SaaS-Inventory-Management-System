import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
    Plus,
    Search,
    Eye,
    Download,
    CreditCard,
    XCircle,
    FileText,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner } from '@/components/ui';
import { invoicesApi } from '@/api/invoices';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { Invoice } from '@/types';

// Mock data for demo
const mockInvoices: Invoice[] = [
    { id: '1', invoiceNumber: 'INV-2024-001', customerId: '1', customerName: 'John Doe', items: [], subtotal: 5000, taxAmount: 900, discountAmount: 0, totalAmount: 5900, paidAmount: 5900, balanceDue: 0, status: 'PAID', dueDate: '2024-02-15', createdAt: '2024-02-01' },
    { id: '2', invoiceNumber: 'INV-2024-002', customerName: 'Jane Smith', items: [], subtotal: 8500, taxAmount: 1530, discountAmount: 500, totalAmount: 9530, paidAmount: 0, balanceDue: 9530, status: 'UNPAID', dueDate: '2024-02-20', createdAt: '2024-02-05' },
    { id: '3', invoiceNumber: 'INV-2024-003', customerName: 'Bob Wilson', items: [], subtotal: 12000, taxAmount: 2160, discountAmount: 0, totalAmount: 14160, paidAmount: 7000, balanceDue: 7160, status: 'PARTIAL', dueDate: '2024-02-25', createdAt: '2024-02-08' },
    { id: '4', invoiceNumber: 'INV-2024-004', customerName: 'Alice Brown', items: [], subtotal: 3500, taxAmount: 630, discountAmount: 0, totalAmount: 4130, paidAmount: 4130, balanceDue: 0, status: 'PAID', dueDate: '2024-02-28', createdAt: '2024-02-12' },
    { id: '5', invoiceNumber: 'INV-2024-005', customerName: 'Charlie Davis', items: [], subtotal: 6800, taxAmount: 1224, discountAmount: 200, totalAmount: 7824, paidAmount: 0, balanceDue: 7824, status: 'CANCELLED', dueDate: '2024-03-01', createdAt: '2024-02-14' },
];

export function InvoiceListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('');
    const [page, _setPage] = useState(0);
    const [showPaymentModal, setShowPaymentModal] = useState<Invoice | null>(null);
    const [paymentAmount, setPaymentAmount] = useState('');
    const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD' | 'UPI' | 'BANK_TRANSFER'>('CASH');

    const { data, isLoading } = useQuery({
        queryKey: ['invoices', page, search, statusFilter],
        queryFn: () => invoicesApi.getInvoices({ page, size: 20, search, status: statusFilter }),
        placeholderData: {
            invoices: mockInvoices,
            page: { page: 0, size: 20, totalElements: mockInvoices.length, totalPages: 1, hasNext: false, hasPrevious: false },
        },
    });

    const paymentMutation = useMutation({
        mutationFn: ({ id, amount, method }: { id: string; amount: number; method: string }) =>
            invoicesApi.recordPayment(id, { amount, paymentMethod: method as any }),
        onSuccess: () => {
            toast.success('Payment recorded');
            queryClient.invalidateQueries({ queryKey: ['invoices'] });
            setShowPaymentModal(null);
            setPaymentAmount('');
        },
        onError: () => {
            toast.error('Failed to record payment');
        },
    });

    const cancelMutation = useMutation({
        mutationFn: invoicesApi.cancelInvoice,
        onSuccess: () => {
            toast.success('Invoice cancelled');
            queryClient.invalidateQueries({ queryKey: ['invoices'] });
        },
        onError: () => {
            toast.error('Failed to cancel invoice');
        },
    });

    const handleRecordPayment = () => {
        if (!showPaymentModal || !paymentAmount) return;
        const amount = parseFloat(paymentAmount);
        if (isNaN(amount) || amount <= 0) {
            toast.error('Invalid amount');
            return;
        }
        paymentMutation.mutate({ id: showPaymentModal.id, amount, method: paymentMethod });
    };

    const handleCancelInvoice = (invoice: Invoice) => {
        if (confirm(`Cancel invoice ${invoice.invoiceNumber}?`)) {
            cancelMutation.mutate(invoice.id);
        }
    };

    const getStatusBadge = (status: string) => {
        const variants: Record<string, 'success' | 'warning' | 'destructive' | 'secondary'> = {
            PAID: 'success',
            PARTIAL: 'warning',
            UNPAID: 'destructive',
            DRAFT: 'secondary',
            CANCELLED: 'secondary',
        };
        return <Badge variant={variants[status] || 'secondary'}>{status}</Badge>;
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Invoices</h1>
                    <p className="text-muted-foreground">Manage billing and payments</p>
                </div>
                <Button onClick={() => navigate('/invoices/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    New Invoice
                </Button>
            </div>

            {/* Filters */}
            <div className="flex flex-col gap-4 rounded-lg border border-border bg-card p-4 sm:flex-row sm:items-center">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                        type="text"
                        placeholder="Search by invoice number or customer..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                </div>
                <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="h-10 rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                >
                    <option value="">All Status</option>
                    <option value="PAID">Paid</option>
                    <option value="UNPAID">Unpaid</option>
                    <option value="PARTIAL">Partial</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>

            {/* Invoices Table */}
            <div className="rounded-lg border border-border bg-card">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <Spinner size="lg" />
                    </div>
                ) : data?.invoices.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <FileText className="mb-2 h-8 w-8" />
                        <p>No invoices found</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-border bg-muted/50">
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        Invoice
                                    </th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        Customer
                                    </th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        Date
                                    </th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">
                                        Amount
                                    </th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">
                                        Paid
                                    </th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        Status
                                    </th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.invoices.map((invoice) => (
                                    <tr
                                        key={invoice.id}
                                        className="border-b border-border last:border-0 hover:bg-muted/30"
                                    >
                                        <td className="px-6 py-4 font-medium text-foreground">
                                            {invoice.invoiceNumber}
                                        </td>
                                        <td className="px-6 py-4 text-muted-foreground">
                                            {invoice.customerName || 'Walk-in Customer'}
                                        </td>
                                        <td className="px-6 py-4 text-muted-foreground">
                                            {formatDate(invoice.createdAt)}
                                        </td>
                                        <td className="px-6 py-4 text-right font-medium text-foreground">
                                            {formatCurrency(invoice.totalAmount)}
                                        </td>
                                        <td className="px-6 py-4 text-right text-muted-foreground">
                                            {formatCurrency(invoice.paidAmount)}
                                        </td>
                                        <td className="px-6 py-4">{getStatusBadge(invoice.status)}</td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center justify-end gap-1">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => navigate(`/invoices/${invoice.id}`)}
                                                    title="View"
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                {(invoice.status === 'UNPAID' || invoice.status === 'PARTIAL') && (
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        onClick={() => {
                                                            setShowPaymentModal(invoice);
                                                            setPaymentAmount((invoice.totalAmount - invoice.paidAmount).toString());
                                                        }}
                                                        title="Record Payment"
                                                    >
                                                        <CreditCard className="h-4 w-4 text-success" />
                                                    </Button>
                                                )}
                                                <Button variant="ghost" size="icon" title="Download PDF">
                                                    <Download className="h-4 w-4" />
                                                </Button>
                                                {invoice.status !== 'CANCELLED' && invoice.status !== 'PAID' && (
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        onClick={() => handleCancelInvoice(invoice)}
                                                        title="Cancel"
                                                    >
                                                        <XCircle className="h-4 w-4 text-destructive" />
                                                    </Button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Payment Modal */}
            {showPaymentModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                    <div className="w-full max-w-md rounded-lg border border-border bg-card p-6">
                        <h2 className="mb-4 text-xl font-semibold text-foreground">Record Payment</h2>
                        <p className="mb-4 text-muted-foreground">
                            Invoice: {showPaymentModal.invoiceNumber}
                            <br />
                            Balance Due: {formatCurrency(showPaymentModal.balanceDue)}
                        </p>

                        <div className="mb-4">
                            <label className="mb-2 block text-sm font-medium text-foreground">Amount</label>
                            <input
                                type="number"
                                step="0.01"
                                value={paymentAmount}
                                onChange={(e) => setPaymentAmount(e.target.value)}
                                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                            />
                        </div>

                        <div className="mb-6">
                            <label className="mb-2 block text-sm font-medium text-foreground">Payment Method</label>
                            <select
                                value={paymentMethod}
                                onChange={(e) => setPaymentMethod(e.target.value as any)}
                                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                            >
                                <option value="CASH">Cash</option>
                                <option value="CARD">Card</option>
                                <option value="UPI">UPI</option>
                                <option value="BANK_TRANSFER">Bank Transfer</option>
                            </select>
                        </div>

                        <div className="flex gap-3">
                            <Button variant="outline" className="flex-1" onClick={() => setShowPaymentModal(null)}>
                                Cancel
                            </Button>
                            <Button
                                className="flex-1"
                                onClick={handleRecordPayment}
                                isLoading={paymentMutation.isPending}
                            >
                                Record Payment
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
