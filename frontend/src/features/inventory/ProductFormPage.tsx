import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input, Card, CardHeader, CardTitle, CardContent, Spinner } from '@/components/ui';
import { inventoryApi } from '@/api/inventory';

const productSchema = z.object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    sku: z.string().min(1, 'SKU is required'),
    barcode: z.string().optional(),
    description: z.string().optional(),
    category: z.string().optional(),
    costPrice: z.coerce.number().min(0, 'Cost price must be positive'),
    sellingPrice: z.coerce.number().min(0, 'Selling price must be positive'),
    taxRate: z.coerce.number().min(0).max(100).optional(),
    currentStock: z.coerce.number().min(0, 'Quantity must be positive'),
    minStockLevel: z.coerce.number().min(0).optional(),
    unit: z.string().optional(),
});

type ProductFormData = z.infer<typeof productSchema>;

export function ProductFormPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const queryClient = useQueryClient();
    const isEditing = Boolean(id);

    const { data: product, isLoading: isLoadingProduct } = useQuery({
        queryKey: ['product', id],
        queryFn: () => inventoryApi.getProduct(id!),
        enabled: isEditing,
    });

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<ProductFormData>({
        resolver: zodResolver(productSchema),
        values: product
            ? {
                name: product.name,
                sku: product.sku,
                barcode: product.barcode || '',
                description: product.description || '',
                category: product.category || '',
                costPrice: product.costPrice,
                sellingPrice: product.sellingPrice,
                taxRate: product.taxRate || 0,
                currentStock: product.currentStock,
                minStockLevel: product.minStockLevel || 0,
                unit: product.unit || '',
            }
            : undefined,
    });

    const createMutation = useMutation({
        mutationFn: inventoryApi.createProduct,
        onSuccess: () => {
            toast.success('Product created successfully');
            queryClient.invalidateQueries({ queryKey: ['products'] });
            navigate('/inventory');
        },
        onError: () => {
            toast.error('Failed to create product');
        },
    });

    const updateMutation = useMutation({
        mutationFn: (data: ProductFormData) => inventoryApi.updateProduct(id!, data),
        onSuccess: () => {
            toast.success('Product updated successfully');
            queryClient.invalidateQueries({ queryKey: ['products'] });
            queryClient.invalidateQueries({ queryKey: ['product', id] });
            navigate('/inventory');
        },
        onError: () => {
            toast.error('Failed to update product');
        },
    });

    const onSubmit = (data: ProductFormData) => {
        if (isEditing) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    if (isEditing && isLoadingProduct) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => navigate('/inventory')}>
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold text-foreground">
                        {isEditing ? 'Edit Product' : 'Add Product'}
                    </h1>
                    <p className="text-muted-foreground">
                        {isEditing ? 'Update product details' : 'Add a new product to your inventory'}
                    </p>
                </div>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit(onSubmit)}>
                <Card>
                    <CardHeader>
                        <CardTitle>Product Details</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        {/* Basic Info */}
                        <div className="grid gap-4 sm:grid-cols-2">
                            <Input
                                label="Product Name *"
                                placeholder="e.g., Wireless Mouse"
                                error={errors.name?.message}
                                {...register('name')}
                            />
                            <Input
                                label="SKU *"
                                placeholder="e.g., WM-001"
                                error={errors.sku?.message}
                                {...register('sku')}
                            />
                        </div>

                        <div className="grid gap-4 sm:grid-cols-2">
                            <Input
                                label="Barcode"
                                placeholder="e.g., 1234567890123"
                                error={errors.barcode?.message}
                                {...register('barcode')}
                            />
                            <Input
                                label="Category"
                                placeholder="e.g., Electronics"
                                error={errors.category?.message}
                                {...register('category')}
                            />
                        </div>

                        <div>
                            <label className="mb-2 block text-sm font-medium text-foreground">Description</label>
                            <textarea
                                className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                                rows={3}
                                placeholder="Product description..."
                                {...register('description')}
                            />
                        </div>

                        {/* Pricing */}
                        <div className="border-t border-border pt-6">
                            <h3 className="mb-4 font-semibold text-foreground">Pricing</h3>
                            <div className="grid gap-4 sm:grid-cols-3">
                                <Input
                                    label="Cost Price *"
                                    type="number"
                                    step="0.01"
                                    placeholder="0.00"
                                    error={errors.costPrice?.message}
                                    {...register('costPrice')}
                                />
                                <Input
                                    label="Selling Price *"
                                    type="number"
                                    step="0.01"
                                    placeholder="0.00"
                                    error={errors.sellingPrice?.message}
                                    {...register('sellingPrice')}
                                />
                                <Input
                                    label="Tax Rate (%)"
                                    type="number"
                                    step="0.01"
                                    placeholder="18"
                                    error={errors.taxRate?.message}
                                    {...register('taxRate')}
                                />
                            </div>
                        </div>

                        {/* Inventory */}
                        <div className="border-t border-border pt-6">
                            <h3 className="mb-4 font-semibold text-foreground">Inventory</h3>
                            <div className="grid gap-4 sm:grid-cols-3">
                                <Input
                                    label="Current Stock *"
                                    type="number"
                                    placeholder="0"
                                    error={errors.currentStock?.message}
                                    {...register('currentStock')}
                                />
                                <Input
                                    label="Min Stock Level"
                                    type="number"
                                    placeholder="10"
                                    error={errors.minStockLevel?.message}
                                    {...register('minStockLevel')}
                                />
                                <Input
                                    label="Unit"
                                    placeholder="e.g., pcs, kg"
                                    error={errors.unit?.message}
                                    {...register('unit')}
                                />
                            </div>
                        </div>

                        {/* Actions */}
                        <div className="flex justify-end gap-4 border-t border-border pt-6">
                            <Button type="button" variant="outline" onClick={() => navigate('/inventory')}>
                                Cancel
                            </Button>
                            <Button type="submit" isLoading={isSubmitting}>
                                {isEditing ? 'Update Product' : 'Create Product'}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </form>
        </div>
    );
}
