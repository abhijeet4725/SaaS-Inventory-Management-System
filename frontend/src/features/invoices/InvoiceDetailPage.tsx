import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Download, Printer, CreditCard, XCircle, Mail } from 'lucide-react';
import { Button, Badge, Spinner, Card, CardHeader, CardTitle, CardContent } from '@/components/ui';
import { invoicesApi } from '@/api/invoices';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { Invoice } from '@/types';

// Mock invoice for demo
const mockInvoice: Invoice = {
    id: '1',
    invoiceNumber: 'INV-2024-001',
    customerId: '1',
    customerName: 'John Doe',
    customerEmail: 'john@example.com',
    customerPhone: '9876543210',
    items: [
        { id: '1', productId: '1', productName: 'Wireless Mouse', productSku: 'WM-001', quantity: 2, unitPrice: 599, taxRate: 18, taxAmount: 215.64, amount: 1198 },
        { id: '2', productId: '2', productName: 'USB-C Cable', productSku: 'UC-002', quantity: 3, unitPrice: 249, taxRate: 18, taxAmount: 134.46, amount: 747 },
        { id: '3', productId: '3', productName: 'Laptop Stand', productSku: 'LS-003', quantity: 1, unitPrice: 1499, taxRate: 18, taxAmount: 269.82, amount: 1499 },
    ],
    subtotal: 3444,
    taxAmount: 619.92,
    discountAmount: 0,
    totalAmount: 4063.92,
    paidAmount: 4063.92,
    balanceDue: 0,
    status: 'PAID',
    dueDate: '2024-02-15',
    notes: 'Thank you for your business!',
    createdAt: '2024-02-01T10:30:00',
};

export function InvoiceDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const { data: invoice = mockInvoice, isLoading } = useQuery({
        queryKey: ['invoice', id],
        queryFn: () => invoicesApi.getInvoice(id!),
        enabled: !!id,
        placeholderData: mockInvoice,
    });

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
                    <Button variant="ghost" size="icon" onClick={() => navigate('/invoices')}>
                        <ArrowLeft className="h-5 w-5" />
                    </Button>
                    <div>
                        <div className="flex items-center gap-3">
                            <h1 className="text-3xl font-bold text-foreground">{invoice.invoiceNumber}</h1>
                            {getStatusBadge(invoice.status)}
                        </div>
                        <p className="text-muted-foreground">Created {formatDate(invoice.createdAt)}</p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline">
                        <Mail className="mr-2 h-4 w-4" />
                        Email
                    </Button>
                    <Button variant="outline">
                        <Printer className="mr-2 h-4 w-4" />
                        Print
                    </Button>
                    <Button variant="outline">
                        <Download className="mr-2 h-4 w-4" />
                        PDF
                    </Button>
                    {(invoice.status === 'UNPAID' || invoice.status === 'PARTIAL') && (
                        <Button>
                            <CreditCard className="mr-2 h-4 w-4" />
                            Record Payment
                        </Button>
                    )}
                </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-3">
                {/* Invoice Details */}
                <div className="lg:col-span-2 space-y-6">
                    {/* Customer Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Bill To</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <p className="font-medium text-foreground">{invoice.customerName || 'Walk-in Customer'}</p>
                            {invoice.customerEmail && <p className="text-muted-foreground">{invoice.customerEmail}</p>}
                            {invoice.customerPhone && <p className="text-muted-foreground">{invoice.customerPhone}</p>}
                        </CardContent>
                    </Card>

                    {/* Line Items */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Items</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <table className="w-full">
                                <thead>
                                    <tr className="border-b border-border">
                                        <th className="pb-3 text-left text-sm font-medium text-muted-foreground">Item</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Qty</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Price</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Tax</th>
                                        <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {invoice.items.map((item, index) => (
                                        <tr key={index} className="border-b border-border last:border-0">
                                            <td className="py-3">
                                                <p className="font-medium text-foreground">{item.productName}</p>
                                                <p className="text-sm text-muted-foreground">{item.productSku}</p>
                                            </td>
                                            <td className="py-3 text-right text-foreground">{item.quantity}</td>
                                            <td className="py-3 text-right text-foreground">{formatCurrency(item.unitPrice)}</td>
                                            <td className="py-3 text-right text-muted-foreground">{formatCurrency(item.taxAmount)}</td>
                                            <td className="py-3 text-right font-medium text-foreground">{formatCurrency(item.amount + item.taxAmount)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>

                    {/* Notes */}
                    {invoice.notes && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Notes</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-muted-foreground">{invoice.notes}</p>
                            </CardContent>
                        </Card>
                    )}
                </div>

                {/* Summary */}
                <div className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Summary</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3">
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">Subtotal</span>
                                <span className="text-foreground">{formatCurrency(invoice.subtotal)}</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-muted-foreground">Tax (GST)</span>
                                <span className="text-foreground">{formatCurrency(invoice.taxAmount)}</span>
                            </div>
                            {invoice.discountAmount > 0 && (
                                <div className="flex justify-between text-sm">
                                    <span className="text-muted-foreground">Discount</span>
                                    <span className="text-success">-{formatCurrency(invoice.discountAmount)}</span>
                                </div>
                            )}
                            <div className="border-t border-border pt-3">
                                <div className="flex justify-between text-lg font-bold">
                                    <span className="text-foreground">Total</span>
                                    <span className="text-primary">{formatCurrency(invoice.totalAmount)}</span>
                                </div>
                            </div>
                            <div className="border-t border-border pt-3">
                                <div className="flex justify-between text-sm">
                                    <span className="text-muted-foreground">Paid</span>
                                    <span className="text-success">{formatCurrency(invoice.paidAmount)}</span>
                                </div>
                                <div className="flex justify-between text-sm font-medium">
                                    <span className="text-muted-foreground">Balance Due</span>
                                    <span className={invoice.balanceDue > 0 ? 'text-destructive' : 'text-foreground'}>
                                        {formatCurrency(invoice.balanceDue)}
                                    </span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>Details</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3 text-sm">
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Invoice Date</span>
                                <span className="text-foreground">{formatDate(invoice.createdAt)}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Due Date</span>
                                <span className="text-foreground">{invoice.dueDate ? formatDate(invoice.dueDate) : 'N/A'}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Status</span>
                                {getStatusBadge(invoice.status)}
                            </div>
                        </CardContent>
                    </Card>

                    {invoice.status !== 'CANCELLED' && invoice.status !== 'PAID' && (
                        <Button variant="destructive" className="w-full">
                            <XCircle className="mr-2 h-4 w-4" />
                            Cancel Invoice
                        </Button>
                    )}
                </div>
            </div>
        </div>
    );
}
