import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
    Plus,
    Search,
    Edit2,
    Trash2,
    Package,
    AlertTriangle,
    Filter,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner } from '@/components/ui';
import { inventoryApi } from '@/api/inventory';
import { formatCurrency } from '@/lib/utils';
import type { Product } from '@/types';

export function ProductListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);

    const { data, isLoading, error } = useQuery({
        queryKey: ['products', page, search],
        queryFn: () => inventoryApi.getProducts({ page, size: 20, search: search || undefined }),
        placeholderData: {
            products: [],
            page: { page: 0, size: 20, totalElements: 0, totalPages: 0, hasNext: false, hasPrevious: false },
        },
    });

    const deleteMutation = useMutation({
        mutationFn: inventoryApi.deleteProduct,
        onSuccess: () => {
            toast.success('Product deleted');
            queryClient.invalidateQueries({ queryKey: ['products'] });
        },
        onError: () => {
            toast.error('Failed to delete product');
        },
    });

    const handleDelete = (id: string, name: string) => {
        if (confirm(`Are you sure you want to delete "${name}"?`)) {
            deleteMutation.mutate(id);
        }
    };

    const getStockBadge = (product: Product) => {
        if (product.currentStock <= 0) {
            return <Badge variant="destructive">Out of Stock</Badge>;
        }
        if (product.currentStock <= product.minStockLevel) {
            return <Badge variant="warning">Low Stock</Badge>;
        }
        return <Badge variant="success">In Stock</Badge>;
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Products</h1>
                    <p className="text-muted-foreground">Manage your inventory</p>
                </div>
                <Button onClick={() => navigate('/inventory/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Product
                </Button>
            </div>

            {/* Filters */}
            <div className="flex flex-col gap-4 rounded-lg border border-border bg-card p-4 sm:flex-row sm:items-center">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                        type="text"
                        placeholder="Search by name, SKU, or barcode..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                </div>
                <Button variant="outline">
                    <Filter className="mr-2 h-4 w-4" />
                    Filters
                </Button>
            </div>

            {/* Products Table */}
            <div className="rounded-lg border border-border bg-card">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <Spinner size="lg" />
                    </div>
                ) : error ? (
                    <div className="flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <AlertTriangle className="mb-2 h-8 w-8" />
                        <p>Failed to load products</p>
                    </div>
                ) : data?.products.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <Package className="mb-2 h-8 w-8" />
                        <p>No products found</p>
                        <Button variant="link" onClick={() => navigate('/inventory/new')}>
                            Add your first product
                        </Button>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-border bg-muted/50">
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        Product
                                    </th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        SKU
                                    </th>
                                    <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                        Category
                                    </th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">
                                        Price
                                    </th>
                                    <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">
                                        Stock
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
                                {data?.products.map((product) => (
                                    <tr
                                        key={product.id}
                                        className="border-b border-border last:border-0 hover:bg-muted/30"
                                    >
                                        <td className="px-6 py-4">
                                            <div>
                                                <p className="font-medium text-foreground">{product.name}</p>
                                                {product.barcode && (
                                                    <p className="text-sm text-muted-foreground">{product.barcode}</p>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 font-mono text-sm text-foreground">{product.sku}</td>
                                        <td className="px-6 py-4 text-muted-foreground">{product.category || '-'}</td>
                                        <td className="px-6 py-4 text-right font-medium text-foreground">
                                            {formatCurrency(product.sellingPrice)}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <span
                                                className={
                                                    product.currentStock <= product.minStockLevel
                                                        ? 'font-semibold text-destructive'
                                                        : 'text-foreground'
                                                }
                                            >
                                                {product.currentStock}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">{getStockBadge(product)}</td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => navigate(`/inventory/${product.id}/edit`)}
                                                >
                                                    <Edit2 className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => handleDelete(product.id, product.name)}
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

                {/* Pagination */}
                {data && data.page.totalPages > 1 && (
                    <div className="flex items-center justify-between border-t border-border px-6 py-4">
                        <p className="text-sm text-muted-foreground">
                            Showing {page * data.page.size + 1} to{' '}
                            {Math.min((page + 1) * data.page.size, data.page.totalElements)} of{' '}
                            {data.page.totalElements} products
                        </p>
                        <div className="flex gap-2">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setPage(page - 1)}
                                disabled={!data.page.hasPrevious}
                            >
                                Previous
                            </Button>
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setPage(page + 1)}
                                disabled={!data.page.hasNext}
                            >
                                Next
                            </Button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
