import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input, Card, CardHeader, CardTitle, CardContent, Spinner } from '@/components/ui';
import { suppliersApi } from '@/api/suppliers';

const supplierSchema = z.object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    supplierCode: z.string().min(1, 'Supplier code is required'),
    email: z.string().email('Invalid email').optional().or(z.literal('')),
    phone: z.string().optional(),
    addressLine1: z.string().optional(),
    contactPerson: z.string().optional(),
    paymentTerms: z.string().optional(),
    taxId: z.string().optional(),
    bankName: z.string().optional(),
});

type SupplierFormData = z.infer<typeof supplierSchema>;

export function SupplierFormPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const queryClient = useQueryClient();
    const isEditing = Boolean(id);

    const { data: supplier, isLoading: isLoadingSupplier } = useQuery({
        queryKey: ['supplier', id],
        queryFn: () => suppliersApi.getSupplier(id!),
        enabled: isEditing,
    });

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<SupplierFormData>({
        resolver: zodResolver(supplierSchema),
        values: supplier
            ? {
                name: supplier.name,
                supplierCode: supplier.supplierCode || '',
                email: supplier.email || '',
                phone: supplier.phone || '',
                addressLine1: supplier.addressLine1 || '',
                contactPerson: supplier.contactPerson || '',
                paymentTerms: supplier.paymentTerms || '',
                taxId: supplier.taxId || '',
                bankName: supplier.bankName || '',
            }
            : undefined,
    });

    const createMutation = useMutation({
        mutationFn: suppliersApi.createSupplier,
        onSuccess: () => {
            toast.success('Supplier created successfully');
            queryClient.invalidateQueries({ queryKey: ['suppliers'] });
            navigate('/suppliers');
        },
        onError: () => {
            toast.error('Failed to create supplier');
        },
    });

    const updateMutation = useMutation({
        mutationFn: (data: SupplierFormData) => suppliersApi.updateSupplier(id!, data),
        onSuccess: () => {
            toast.success('Supplier updated successfully');
            queryClient.invalidateQueries({ queryKey: ['suppliers'] });
            queryClient.invalidateQueries({ queryKey: ['supplier', id] });
            navigate('/suppliers');
        },
        onError: () => {
            toast.error('Failed to update supplier');
        },
    });

    const onSubmit = (data: SupplierFormData) => {
        if (isEditing) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    if (isEditing && isLoadingSupplier) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => navigate('/suppliers')}>
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold text-foreground">
                        {isEditing ? 'Edit Supplier' : 'Add Supplier'}
                    </h1>
                    <p className="text-muted-foreground">
                        {isEditing ? 'Update supplier details' : 'Add a new vendor to your directory'}
                    </p>
                </div>
            </div>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Card>
                    <CardHeader>
                        <CardTitle>Supplier Information</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="grid gap-4 sm:grid-cols-2">
                            <Input
                                label="Company Name *"
                                placeholder="e.g., Tech Distributors Ltd"
                                error={errors.name?.message}
                                {...register('name')}
                            />
                            <Input
                                label="Supplier Code *"
                                placeholder="e.g., TD-001"
                                error={errors.supplierCode?.message}
                                {...register('supplierCode')}
                            />
                        </div>

                        <div className="grid gap-4 sm:grid-cols-2">
                            <Input
                                label="Contact Person"
                                placeholder="e.g., Raj Kumar"
                                error={errors.contactPerson?.message}
                                {...register('contactPerson')}
                            />
                            <Input
                                label="Phone"
                                placeholder="e.g., 9876543210"
                                error={errors.phone?.message}
                                {...register('phone')}
                            />
                        </div>

                        <Input
                            label="Email"
                            type="email"
                            placeholder="e.g., orders@supplier.com"
                            error={errors.email?.message}
                            {...register('email')}
                        />

                        <Input
                            label="Address"
                            placeholder="e.g., 123 Industrial Area"
                            error={errors.addressLine1?.message}
                            {...register('addressLine1')}
                        />

                        <div className="border-t border-border pt-6">
                            <h3 className="mb-4 font-semibold text-foreground">Payment & Tax</h3>
                            <div className="grid gap-4 sm:grid-cols-2">
                                <Input
                                    label="Payment Terms"
                                    placeholder="e.g., Net 30"
                                    error={errors.paymentTerms?.message}
                                    {...register('paymentTerms')}
                                />
                                <Input
                                    label="Tax ID / GST Number"
                                    placeholder="e.g., GSTIN1234567890"
                                    error={errors.taxId?.message}
                                    {...register('taxId')}
                                />
                            </div>
                        </div>

                        <div>
                            <label className="mb-2 block text-sm font-medium text-foreground">Bank Name</label>
                            <textarea
                                className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                rows={3}
                                placeholder="Bank name..."
                                {...register('bankName')}
                            />
                        </div>

                        <div className="flex justify-end gap-4 border-t border-border pt-6">
                            <Button type="button" variant="outline" onClick={() => navigate('/suppliers')}>
                                Cancel
                            </Button>
                            <Button type="submit" isLoading={isSubmitting}>
                                {isEditing ? 'Update Supplier' : 'Create Supplier'}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </form>
        </div>
    );
}
