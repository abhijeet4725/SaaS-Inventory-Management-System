import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Eye, Check, Send, Package, ClipboardList } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner } from '@/components/ui';
import { purchaseOrdersApi } from '@/api/purchase-orders';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { PurchaseOrderSummary } from '@/types';

// Mock data
const mockPOs: PurchaseOrderSummary[] = [
    { id: '1', poNumber: 'PO-2024-001', supplierName: 'Tech Distributors Ltd', totalAmount: 59000, itemCount: 3, status: 'APPROVED', orderDate: '2024-02-01' },
    { id: '2', poNumber: 'PO-2024-002', supplierName: 'Electronics Hub', totalAmount: 94400, itemCount: 5, status: 'SENT', orderDate: '2024-02-05' },
    { id: '3', poNumber: 'PO-2024-003', supplierName: 'Tech Distributors Ltd', totalAmount: 29500, itemCount: 2, status: 'PARTIAL', orderDate: '2024-02-08' },
    { id: '4', poNumber: 'PO-2024-004', supplierName: 'Office Supplies Co', totalAmount: 17700, itemCount: 4, status: 'RECEIVED', orderDate: '2024-02-10' },
    { id: '5', poNumber: 'PO-2024-005', supplierName: 'Electronics Hub', totalAmount: 41300, itemCount: 2, status: 'PENDING', orderDate: '2024-02-12' },
];

export function POListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['purchase-orders', search, statusFilter],
        queryFn: () => purchaseOrdersApi.getPurchaseOrders({ search, status: statusFilter }),
        placeholderData: {
            purchaseOrders: mockPOs,
            page: { page: 0, size: 20, totalElements: mockPOs.length, totalPages: 1, hasNext: false, hasPrevious: false },
        },
    });

    const approveMutation = useMutation({
        mutationFn: purchaseOrdersApi.approvePurchaseOrder,
        onSuccess: () => {
            toast.success('Purchase order approved');
            queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
        },
    });

    const sendMutation = useMutation({
        mutationFn: purchaseOrdersApi.sendPurchaseOrder,
        onSuccess: () => {
            toast.success('Purchase order sent to supplier');
            queryClient.invalidateQueries({ queryKey: ['purchase-orders'] });
        },
    });

    const getStatusBadge = (status: string) => {
        const variants: Record<string, 'success' | 'warning' | 'destructive' | 'secondary' | 'default'> = {
            DRAFT: 'secondary',
            PENDING: 'warning',
            APPROVED: 'default',
            SENT: 'default',
            PARTIAL: 'warning',
            RECEIVED: 'success',
            CANCELLED: 'destructive',
        };
        return <Badge variant={(variants[status] || 'secondary') as any}>{status}</Badge>;
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Purchase Orders</h1>
                    <p className="text-muted-foreground">Manage procurement orders</p>
                </div>
                <Button onClick={() => navigate('/purchase-orders/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    Create PO
                </Button>
            </div>

            <div className="flex flex-col gap-4 sm:flex-row">
                <div className="relative flex-1 max-w-md">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                        type="text"
                        placeholder="Search by PO number or supplier..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                </div>
                <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="h-10 rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                >
                    <option value="">All Status</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                    <option value="SENT">Sent</option>
                    <option value="PARTIAL">Partial</option>
                    <option value="RECEIVED">Received</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>

            <div className="rounded-lg border border-border bg-card">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <Spinner size="lg" />
                    </div>
                ) : data?.purchaseOrders.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <ClipboardList className="mb-2 h-8 w-8" />
                        <p>No purchase orders found</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-border bg-muted/50">
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">PO Number</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Supplier</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Date</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Expected</th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">Total</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Status</th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.purchaseOrders.map((po) => (
                                    <tr key={po.id} className="border-b border-border last:border-0 hover:bg-muted/30">
                                        <td className="px-6 py-4 font-medium text-foreground">{po.poNumber}</td>
                                        <td className="px-6 py-4 text-muted-foreground">{po.supplierName}</td>
                                        <td className="px-6 py-4 text-muted-foreground">
                                            {po.orderDate ? formatDate(po.orderDate) : '-'}
                                        </td>
                                        <td className="px-6 py-4 text-muted-foreground">
                                            -
                                        </td>
                                        <td className="px-6 py-4 text-right font-medium text-foreground">
                                            {formatCurrency(po.totalAmount)}
                                        </td>
                                        <td className="px-6 py-4">{getStatusBadge(po.status)}</td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center justify-end gap-1">
                                                <Button variant="ghost" size="icon" onClick={() => navigate(`/purchase-orders/${po.id}`)} title="View">
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                {po.status === 'PENDING' && (
                                                    <Button variant="ghost" size="icon" onClick={() => approveMutation.mutate(po.id)} title="Approve">
                                                        <Check className="h-4 w-4 text-success" />
                                                    </Button>
                                                )}
                                                {po.status === 'APPROVED' && (
                                                    <Button variant="ghost" size="icon" onClick={() => sendMutation.mutate(po.id)} title="Send">
                                                        <Send className="h-4 w-4 text-primary" />
                                                    </Button>
                                                )}
                                                {(po.status === 'SENT' || po.status === 'PARTIAL') && (
                                                    <Button variant="ghost" size="icon" onClick={() => navigate(`/purchase-orders/${po.id}/receive`)} title="Receive">
                                                        <Package className="h-4 w-4 text-warning" />
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
        </div>
    );
}

export { POListPage as default };
