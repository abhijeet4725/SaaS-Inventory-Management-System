import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Check, Send, Package, XCircle, Truck } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner, Card, CardHeader, CardTitle, CardContent } from '@/components/ui';
import { purchaseOrdersApi } from '@/api/purchase-orders';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { PurchaseOrder } from '@/types';

// Mock PO for demo
const mockPO: PurchaseOrder = {
    id: '1',
    poNumber: 'PO-2024-001',
    supplierId: 's1',
    supplierName: 'Tech Distributors Ltd',
    items: [
        { id: 'i1', productId: 'p1', productName: 'Wireless Mouse', productSku: 'WM-001', quantity: 50, unitCost: 399, amount: 19950, receivedQuantity: 0, pendingQuantity: 50 },
        { id: 'i2', productId: 'p2', productName: 'USB-C Cable', productSku: 'UC-002', quantity: 100, unitCost: 149, amount: 14900, receivedQuantity: 0, pendingQuantity: 100 },
    ],
    subtotal: 34850,
    taxAmount: 6273,
    totalAmount: 41123,
    status: 'PENDING',
    notes: 'Priority order - restock before month end',
    expectedDate: '2024-02-15',
    createdAt: '2024-02-01T10:00:00',
    updatedAt: '2024-02-01T10:00:00',
};

export function PODetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data: po = mockPO, isLoading } = useQuery({
        queryKey: ['purchase-order', id],
        queryFn: () => purchaseOrdersApi.getPurchaseOrder(id!),
        enabled: !!id,
        placeholderData: mockPO,
    });

    const approveMutation = useMutation({
        mutationFn: () => purchaseOrdersApi.approvePurchaseOrder(id!),
        onSuccess: () => {
            toast.success('Purchase order approved');
            queryClient.invalidateQueries({ queryKey: ['purchase-order', id] });
        },
    });

    const sendMutation = useMutation({
        mutationFn: () => purchaseOrdersApi.sendPurchaseOrder(id!),
        onSuccess: () => {
            toast.success('Purchase order sent to supplier');
            queryClient.invalidateQueries({ queryKey: ['purchase-order', id] });
        },
    });

    const cancelMutation = useMutation({
        mutationFn: () => purchaseOrdersApi.cancelPurchaseOrder(id!),
        onSuccess: () => {
            toast.success('Purchase order cancelled');
            queryClient.invalidateQueries({ queryKey: ['purchase-order', id] });
        },
    });

    const getStatusBadge = (status: string) => {
        const variants: Record<string, 'secondary' | 'warning' | 'success' | 'destructive'> = {
            DRAFT: 'secondary',
            PENDING: 'warning',
            APPROVED: 'success',
            SENT: 'success',
            PARTIAL: 'warning',
            RECEIVED: 'success',
            CANCELLED: 'destructive',
        };
        return <Badge variant={variants[status] || 'secondary'}>{status}</Badge>;
    };

    if (isLoading) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-4xl space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="icon" onClick={() => navigate('/purchase-orders')}>
                        <ArrowLeft className="h-5 w-5" />
                    </Button>
                    <div>
                        <div className="flex items-center gap-3">
                            <h1 className="text-3xl font-bold text-foreground">{po.poNumber}</h1>
                            {getStatusBadge(po.status)}
                        </div>
                        <p className="text-muted-foreground">Created {formatDate(po.createdAt)}</p>
                    </div>
                </div>
                <div className="flex gap-2">
                    {po.status === 'DRAFT' && (
                        <Button onClick={() => approveMutation.mutate()}>
                            <Check className="mr-2 h-4 w-4" />
                            Approve
                        </Button>
                    )}
                    {po.status === 'APPROVED' && (
                        <Button onClick={() => sendMutation.mutate()}>
                            <Send className="mr-2 h-4 w-4" />
                            Send to Supplier
                        </Button>
                    )}
                    {po.status === 'SENT' && (
                        <Button onClick={() => navigate(`/purchase-orders/${id}/receive`)}>
                            <Package className="mr-2 h-4 w-4" />
                            Receive Items
                        </Button>
                    )}
                    {po.status !== 'CANCELLED' && po.status !== 'RECEIVED' && (
                        <Button variant="outline" onClick={() => cancelMutation.mutate()}>
                            <XCircle className="mr-2 h-4 w-4" />
                            Cancel
                        </Button>
                    )}
                </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-3">
                {/* Order Details */}
                <div className="lg:col-span-2 space-y-6">
                    {/* Supplier Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Truck className="h-5 w-5" />
                                Supplier
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <p className="font-medium text-foreground">{po.supplierName}</p>
                            <p className="text-sm text-muted-foreground">
                                Expected Delivery: {po.expectedDate ? formatDate(po.expectedDate) : 'Not specified'}
                            </p>
                        </CardContent>
                    </Card>

                    {/* Line Items */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Order Items</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <table className="w-full">
                                <thead>
                                    <tr className="border-b border-border">
                                        <th className="pb-3 text-left text-sm font-medium text-muted-foreground">Product</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Qty</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Received</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Unit Cost</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {po.items.map((item, index) => (
                                        <tr key={index} className="border-b border-border last:border-0">
                                            <td className="py-3">
                                                <p className="font-medium text-foreground">{item.productName}</p>
                                                <p className="text-sm text-muted-foreground">{item.productSku}</p>
                                            </td>
                                            <td className="py-3 text-right text-foreground">{item.quantity}</td>
                                            <td className="py-3 text-right">
                                                <Badge variant={item.receivedQuantity >= item.quantity ? 'success' : 'secondary'}>
                                                    {item.receivedQuantity || 0}
                                                </Badge>
                                            </td>
                                            <td className="py-3 text-right text-foreground">{formatCurrency(item.unitCost)}</td>
                                            <td className="py-3 text-right font-medium text-foreground">{formatCurrency(item.amount)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>

                    {/* Notes */}
                    {po.notes && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Notes</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-muted-foreground">{po.notes}</p>
                            </CardContent>
                        </Card>
                    )}
                </div>

                {/* Summary */}
                <div className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Order Summary</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3">
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">Subtotal</span>
                                <span className="text-foreground">{formatCurrency(po.subtotal)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">Tax (GST)</span>
                                <span className="text-foreground">{formatCurrency(po.taxAmount)}</span>
                            </div>
                            <div className="border-t border-border pt-3">
                                <div className="flex justify-between text-lg font-bold">
                                    <span className="text-foreground">Total</span>
                                    <span className="text-primary">{formatCurrency(po.totalAmount)}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>Timeline</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-3 text-sm">
                                <div className="flex items-center gap-2">
                                    <div className="h-2 w-2 rounded-full bg-success" />
                                    <span className="text-muted-foreground">Created</span>
                                    <span className="ml-auto text-foreground">{formatDate(po.createdAt)}</span>
                                </div>
                                {po.status !== 'DRAFT' && (
                                    <div className="flex items-center gap-2">
                                        <div className="h-2 w-2 rounded-full bg-success" />
                                        <span className="text-muted-foreground">Approved</span>
                                        <span className="ml-auto text-foreground">{po.updatedAt ? formatDate(po.updatedAt) : '-'}</span>
                                    </div>
                                )}
                                {(po.status === 'SENT' || po.status === 'RECEIVED') && (
                                    <div className="flex items-center gap-2">
                                        <div className="h-2 w-2 rounded-full bg-success" />
                                        <span className="text-muted-foreground">Sent</span>
                                        <span className="ml-auto text-foreground">{po.updatedAt ? formatDate(po.updatedAt) : '-'}</span>
                                    </div>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}
