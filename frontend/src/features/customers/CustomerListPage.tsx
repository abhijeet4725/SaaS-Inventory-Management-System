import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Edit2, Trash2, Users, Phone, Mail } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner } from '@/components/ui';
import { customersApi } from '@/api/customers';
import type { Customer } from '@/types';

// Mock data
const mockCustomers: Customer[] = [
    { id: '1', name: 'John Doe', email: 'john@example.com', phone: '9876543210', addressLine1: '123 Main St', city: 'Mumbai', state: 'MH', postalCode: '400001', active: true, createdAt: '2024-01-15' },
    { id: '2', name: 'Jane Smith', email: 'jane@example.com', phone: '9876543211', addressLine1: '456 Oak Ave', city: 'Delhi', state: 'DL', postalCode: '110001', active: true, createdAt: '2024-01-20' },
    { id: '3', name: 'Bob Wilson', phone: '9876543212', addressLine1: '789 Pine Rd', city: 'Bangalore', state: 'KA', postalCode: '560001', active: true, createdAt: '2024-02-01' },
    { id: '4', name: 'Alice Brown', email: 'alice@example.com', phone: '9876543213', city: 'Chennai', state: 'TN', postalCode: '600001', active: false, createdAt: '2024-02-10' },
];

export function CustomerListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['customers', search],
        queryFn: () => customersApi.getCustomers({ search }),
        placeholderData: {
            customers: mockCustomers,
            page: { page: 0, size: 20, totalElements: mockCustomers.length, totalPages: 1, hasNext: false, hasPrevious: false },
        },
    });

    const deleteMutation = useMutation({
        mutationFn: customersApi.deleteCustomer,
        onSuccess: () => {
            toast.success('Customer deleted');
            queryClient.invalidateQueries({ queryKey: ['customers'] });
        },
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Customers</h1>
                    <p className="text-muted-foreground">Manage your customer directory</p>
                </div>
                <Button onClick={() => navigate('/customers/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Customer
                </Button>
            </div>

            <div className="relative max-w-md">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <input
                    type="text"
                    placeholder="Search customers..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                />
            </div>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {isLoading ? (
                    <div className="col-span-full flex h-64 items-center justify-center">
                        <Spinner size="lg" />
                    </div>
                ) : data?.customers.length === 0 ? (
                    <div className="col-span-full flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <Users className="mb-2 h-8 w-8" />
                        <p>No customers found</p>
                    </div>
                ) : (
                    data?.customers.map((customer) => (
                        <div
                            key={customer.id}
                            className="rounded-lg border border-border bg-card p-4 transition-shadow hover:shadow-lg"
                        >
                            <div className="flex items-start justify-between">
                                <div className="flex items-center gap-3">
                                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-primary-foreground font-medium">
                                        {customer.name.charAt(0)}
                                    </div>
                                    <div>
                                        <p className="font-medium text-foreground">{customer.name}</p>
                                        <Badge variant={customer.active ? 'success' : 'secondary'} className="mt-1">
                                            {customer.active ? 'Active' : 'Inactive'}
                                        </Badge>
                                    </div>
                                </div>
                                <div className="flex gap-1">
                                    <Button variant="ghost" size="icon" onClick={() => navigate(`/customers/${customer.id}/edit`)}>
                                        <Edit2 className="h-4 w-4" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        onClick={() => {
                                            if (confirm(`Delete ${customer.name}?`)) deleteMutation.mutate(customer.id);
                                        }}
                                    >
                                        <Trash2 className="h-4 w-4 text-destructive" />
                                    </Button>
                                </div>
                            </div>
                            <div className="mt-4 space-y-2 text-sm">
                                <div className="flex items-center gap-2 text-muted-foreground">
                                    <Phone className="h-4 w-4" />
                                    {customer.phone}
                                </div>
                                {customer.email && (
                                    <div className="flex items-center gap-2 text-muted-foreground">
                                        <Mail className="h-4 w-4" />
                                        {customer.email}
                                    </div>
                                )}
                                {customer.city && (
                                    <p className="text-muted-foreground">
                                        {customer.city}, {customer.state}
                                    </p>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export { CustomerListPage as default };
