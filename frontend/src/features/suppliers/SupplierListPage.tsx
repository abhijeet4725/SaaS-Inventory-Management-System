import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Edit2, Trash2, Truck, Phone, Mail, Building } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner } from '@/components/ui';
import { suppliersApi } from '@/api/suppliers';
import type { Supplier } from '@/types';

// Mock data
const mockSuppliers: Supplier[] = [
    { id: '1', name: 'Tech Distributors Ltd', supplierCode: 'TD-001', email: 'orders@techdist.com', phone: '9876543210', addressLine1: '123 Industrial Area', contactPerson: 'Raj Kumar', paymentTerms: 'Net 30', active: true, createdAt: '2024-01-15' },
    { id: '2', name: 'Electronics Hub', supplierCode: 'EH-002', email: 'sales@ehub.com', phone: '9876543211', contactPerson: 'Priya Singh', paymentTerms: 'Net 15', active: true, createdAt: '2024-01-20' },
    { id: '3', name: 'Office Supplies Co', supplierCode: 'OS-003', email: 'info@officesupplies.com', phone: '9876543212', paymentTerms: 'COD', active: true, createdAt: '2024-02-01' },
    { id: '4', name: 'Global Imports', supplierCode: 'GI-004', phone: '9876543213', active: false, createdAt: '2024-02-10' },
];

export function SupplierListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['suppliers', search],
        queryFn: () => suppliersApi.getSuppliers({ search }),
        placeholderData: {
            suppliers: mockSuppliers,
            page: { page: 0, size: 20, totalElements: mockSuppliers.length, totalPages: 1, hasNext: false, hasPrevious: false },
        },
    });

    const deleteMutation = useMutation({
        mutationFn: suppliersApi.deleteSupplier,
        onSuccess: () => {
            toast.success('Supplier deleted');
            queryClient.invalidateQueries({ queryKey: ['suppliers'] });
        },
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Suppliers</h1>
                    <p className="text-muted-foreground">Manage your vendor directory</p>
                </div>
                <Button onClick={() => navigate('/suppliers/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Supplier
                </Button>
            </div>

            <div className="relative max-w-md">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <input
                    type="text"
                    placeholder="Search suppliers..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                />
            </div>

            <div className="rounded-lg border border-border bg-card">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <Spinner size="lg" />
                    </div>
                ) : data?.suppliers.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <Truck className="mb-2 h-8 w-8" />
                        <p>No suppliers found</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-border bg-muted/50">
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Supplier</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Code</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Contact</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Payment Terms</th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Status</th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.suppliers.map((supplier) => (
                                    <tr key={supplier.id} className="border-b border-border last:border-0 hover:bg-muted/30">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                                                    <Building className="h-5 w-5 text-primary" />
                                                </div>
                                                <div>
                                                    <p className="font-medium text-foreground">{supplier.name}</p>
                                                    {supplier.contactPerson && (
                                                        <p className="text-sm text-muted-foreground">{supplier.contactPerson}</p>
                                                    )}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 font-mono text-sm text-foreground">{supplier.supplierCode}</td>
                                        <td className="px-6 py-4">
                                            <div className="space-y-1 text-sm text-muted-foreground">
                                                {supplier.phone && <div className="flex items-center gap-1"><Phone className="h-3 w-3" />{supplier.phone}</div>}
                                                {supplier.email && <div className="flex items-center gap-1"><Mail className="h-3 w-3" />{supplier.email}</div>}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-muted-foreground">{supplier.paymentTerms || '-'}</td>
                                        <td className="px-6 py-4">
                                            <Badge variant={supplier.active ? 'success' : 'secondary'}>
                                                {supplier.active ? 'Active' : 'Inactive'}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center justify-end gap-1">
                                                <Button variant="ghost" size="icon" onClick={() => navigate(`/suppliers/${supplier.id}/edit`)}>
                                                    <Edit2 className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => {
                                                        if (confirm(`Delete ${supplier.name}?`)) deleteMutation.mutate(supplier.id);
                                                    }}
                                                >
                                                    <Trash2 className="h-4 w-4 text-destructive" />
                                                </Button>
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

export { SupplierListPage as default };
