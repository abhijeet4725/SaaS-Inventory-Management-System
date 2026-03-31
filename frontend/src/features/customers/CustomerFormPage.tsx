import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input, Card, CardHeader, CardTitle, CardContent, Spinner } from '@/components/ui';
import { customersApi } from '@/api/customers';

const customerSchema = z.object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    email: z.string().email('Invalid email').optional().or(z.literal('')),
    phone: z.string().min(10, 'Phone must be at least 10 digits'),
    addressLine1: z.string().optional(),
    city: z.string().optional(),
    state: z.string().optional(),
    postalCode: z.string().optional(),
});

type CustomerFormData = z.infer<typeof customerSchema>;

export function CustomerFormPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const queryClient = useQueryClient();
    const isEditing = Boolean(id);

    const { data: customer, isLoading: isLoadingCustomer } = useQuery({
        queryKey: ['customer', id],
        queryFn: () => customersApi.getCustomer(id!),
        enabled: isEditing,
    });

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<CustomerFormData>({
        resolver: zodResolver(customerSchema),
        values: customer
            ? {
                name: customer.name,
                email: customer.email || '',
                phone: customer.phone || '',
                addressLine1: customer.addressLine1 || '',
                city: customer.city || '',
                state: customer.state || '',
                postalCode: customer.postalCode || '',
            }
            : undefined,
    });

    const createMutation = useMutation({
        mutationFn: customersApi.createCustomer,
        onSuccess: () => {
            toast.success('Customer created successfully');
            queryClient.invalidateQueries({ queryKey: ['customers'] });
            navigate('/customers');
        },
        onError: () => {
            toast.error('Failed to create customer');
        },
    });

    const updateMutation = useMutation({
        mutationFn: (data: CustomerFormData) => customersApi.updateCustomer(id!, data),
        onSuccess: () => {
            toast.success('Customer updated successfully');
            queryClient.invalidateQueries({ queryKey: ['customers'] });
            queryClient.invalidateQueries({ queryKey: ['customer', id] });
            navigate('/customers');
        },
        onError: () => {
            toast.error('Failed to update customer');
        },
    });

    const onSubmit = (data: CustomerFormData) => {
        if (isEditing) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    if (isEditing && isLoadingCustomer) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => navigate('/customers')}>
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold text-foreground">
                        {isEditing ? 'Edit Customer' : 'Add Customer'}
                    </h1>
                    <p className="text-muted-foreground">
                        {isEditing ? 'Update customer details' : 'Add a new customer to your directory'}
                    </p>
                </div>
            </div>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Card>
                    <CardHeader>
                        <CardTitle>Customer Information</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <Input
                            label="Full Name *"
                            placeholder="e.g., John Doe"
                            error={errors.name?.message}
                            {...register('name')}
                        />

                        <div className="grid gap-4 sm:grid-cols-2">
                            <Input
                                label="Phone *"
                                placeholder="e.g., 9876543210"
                                error={errors.phone?.message}
                                {...register('phone')}
                            />
                            <Input
                                label="Email"
                                type="email"
                                placeholder="e.g., john@example.com"
                                error={errors.email?.message}
                                {...register('email')}
                            />
                        </div>

                        <Input
                            label="Address"
                            placeholder="e.g., 123 Main Street"
                            error={errors.addressLine1?.message}
                            {...register('addressLine1')}
                        />

                        <div className="grid gap-4 sm:grid-cols-3">
                            <Input
                                label="City"
                                placeholder="e.g., Mumbai"
                                error={errors.city?.message}
                                {...register('city')}
                            />
                            <Input
                                label="State"
                                placeholder="e.g., Maharashtra"
                                error={errors.state?.message}
                                {...register('state')}
                            />
                            <Input
                                label="Postal Code"
                                placeholder="e.g., 400001"
                                error={errors.postalCode?.message}
                                {...register('postalCode')}
                            />
                        </div>

                        <div className="flex justify-end gap-4 border-t border-border pt-6">
                            <Button type="button" variant="outline" onClick={() => navigate('/customers')}>
                                Cancel
                            </Button>
                            <Button type="submit" isLoading={isSubmitting}>
                                {isEditing ? 'Update Customer' : 'Create Customer'}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </form>
        </div>
    );
}
