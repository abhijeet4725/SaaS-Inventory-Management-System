import { useQuery } from '@tanstack/react-query';
import {
    DollarSign,
    Package,
    AlertTriangle,
    FileText,
    ShoppingCart,
    TrendingUp,
    Clock,
} from 'lucide-react';
import { formatCurrency } from '@/lib/utils';
import { reportsApi } from '@/api/reports';
import { StatCard } from './StatCard';
import { Badge, Spinner } from '@/components/ui';
import type { DashboardSummary } from '@/types';
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from 'recharts';

// Mock data for chart - replace with real API data
const mockSalesData = [
    { date: 'Mon', sales: 4000 },
    { date: 'Tue', sales: 3000 },
    { date: 'Wed', sales: 5000 },
    { date: 'Thu', sales: 2780 },
    { date: 'Fri', sales: 6890 },
    { date: 'Sat', sales: 8390 },
    { date: 'Sun', sales: 3490 },
];

const mockRecentInvoices = [
    { id: '1', number: 'INV-001', customer: 'John Doe', amount: 1250, status: 'PAID' },
    { id: '2', number: 'INV-002', customer: 'Jane Smith', amount: 890, status: 'UNPAID' },
    { id: '3', number: 'INV-003', customer: 'Bob Wilson', amount: 2340, status: 'PARTIAL' },
    { id: '4', number: 'INV-004', customer: 'Alice Brown', amount: 560, status: 'PAID' },
    { id: '5', number: 'INV-005', customer: 'Charlie Davis', amount: 1780, status: 'UNPAID' },
];

const mockLowStockProducts = [
    { id: '1', name: 'Wireless Mouse', sku: 'WM-001', quantity: 5, minLevel: 10 },
    { id: '2', name: 'USB-C Cable', sku: 'UC-002', quantity: 3, minLevel: 20 },
    { id: '3', name: 'Laptop Stand', sku: 'LS-003', quantity: 2, minLevel: 5 },
];

export function DashboardPage() {
    const { data: summary, isLoading } = useQuery({
        queryKey: ['dashboard-summary'],
        queryFn: reportsApi.getDashboardSummary,
        // Use placeholder data if API fails
        placeholderData: {
            sales: { todaySales: 12500, weekSales: 87500, monthSales: 345000, yearSales: 0, todayTransactions: 0, monthTransactions: 0, averageOrderValue: 0, growthPercent: 12.5 },
            inventory: { totalProducts: 256, lowStockCount: 8, outOfStockCount: 0, inventoryValue: 0, categoriesCount: 0 },
            customers: { totalCustomers: 0, newCustomersThisMonth: 0, activeCustomers: 0, totalRevenue: 0 },
            purchases: { pendingOrders: 5, overdueOrders: 0, pendingValue: 0, monthPurchases: 0 },
            topProducts: [],
            recentSales: [],
        } as DashboardSummary,
    });

    const getStatusBadge = (status: string) => {
        const variants: Record<string, 'success' | 'warning' | 'destructive' | 'secondary'> = {
            PAID: 'success',
            PARTIAL: 'warning',
            UNPAID: 'destructive',
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
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
                <p className="text-muted-foreground">
                    Welcome back! Here's what's happening today.
                </p>
            </div>

            {/* Stats Grid */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <StatCard
                    title="Today's Sales"
                    value={formatCurrency(summary?.sales?.todaySales || 0)}
                    icon={DollarSign}
                    trend={{ value: summary?.sales?.growthPercent || 0, isPositive: (summary?.sales?.growthPercent || 0) >= 0 }}
                />
                <StatCard
                    title="Total Products"
                    value={summary?.inventory?.totalProducts || 0}
                    subtitle="Active in inventory"
                    icon={Package}
                />
                <StatCard
                    title="Low Stock Items"
                    value={summary?.inventory?.lowStockCount || 0}
                    subtitle="Need attention"
                    icon={AlertTriangle}
                    className={summary?.inventory?.lowStockCount ? 'border-warning' : ''}
                />
                <StatCard
                    title="Pending Orders"
                    value={summary?.purchases?.pendingOrders || 0}
                    subtitle="Awaiting fulfillment"
                    icon={FileText}
                />
            </div>

            {/* Charts and Lists Row */}
            <div className="grid gap-6 lg:grid-cols-3">
                {/* Sales Chart */}
                <div className="rounded-lg border border-border bg-card p-6 lg:col-span-2">
                    <div className="mb-4 flex items-center justify-between">
                        <div>
                            <h2 className="text-lg font-semibold text-foreground">Sales Overview</h2>
                            <p className="text-sm text-muted-foreground">Weekly sales performance</p>
                        </div>
                        <div className="flex items-center gap-2 text-sm">
                            <TrendingUp className="h-4 w-4 text-success" />
                            <span className="font-medium text-success">+18.2%</span>
                        </div>
                    </div>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={mockSalesData}>
                                <defs>
                                    <linearGradient id="salesGradient" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="hsl(217.2 91.2% 59.8%)" stopOpacity={0.3} />
                                        <stop offset="95%" stopColor="hsl(217.2 91.2% 59.8%)" stopOpacity={0} />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="hsl(217.2 32.6% 17.5%)" />
                                <XAxis dataKey="date" stroke="hsl(215 20.2% 65.1%)" fontSize={12} />
                                <YAxis stroke="hsl(215 20.2% 65.1%)" fontSize={12} />
                                <Tooltip
                                    contentStyle={{
                                        backgroundColor: 'hsl(222.2 84% 7%)',
                                        border: '1px solid hsl(217.2 32.6% 17.5%)',
                                        borderRadius: '8px',
                                    }}
                                    labelStyle={{ color: 'hsl(210 40% 98%)' }}
                                />
                                <Area
                                    type="monotone"
                                    dataKey="sales"
                                    stroke="hsl(217.2 91.2% 59.8%)"
                                    fillOpacity={1}
                                    fill="url(#salesGradient)"
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Low Stock Alert */}
                <div className="rounded-lg border border-border bg-card p-6">
                    <div className="mb-4 flex items-center justify-between">
                        <h2 className="text-lg font-semibold text-foreground">Low Stock Alert</h2>
                        <AlertTriangle className="h-5 w-5 text-warning" />
                    </div>
                    <div className="space-y-4">
                        {mockLowStockProducts.map((product) => (
                            <div
                                key={product.id}
                                className="flex items-center justify-between border-b border-border pb-3 last:border-0"
                            >
                                <div>
                                    <p className="font-medium text-foreground">{product.name}</p>
                                    <p className="text-sm text-muted-foreground">{product.sku}</p>
                                </div>
                                <div className="text-right">
                                    <p className="font-semibold text-destructive">{product.quantity}</p>
                                    <p className="text-xs text-muted-foreground">Min: {product.minLevel}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                    <button className="mt-4 w-full rounded-lg border border-border py-2 text-sm font-medium text-foreground hover:bg-accent">
                        View All
                    </button>
                </div>
            </div>

            {/* Recent Invoices */}
            <div className="rounded-lg border border-border bg-card">
                <div className="flex items-center justify-between border-b border-border p-6">
                    <h2 className="text-lg font-semibold text-foreground">Recent Invoices</h2>
                    <button className="text-sm text-primary hover:underline">View All</button>
                </div>
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
                                    Amount
                                </th>
                                <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">
                                    Status
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {mockRecentInvoices.map((invoice) => (
                                <tr
                                    key={invoice.id}
                                    className="border-b border-border last:border-0 hover:bg-muted/30"
                                >
                                    <td className="px-6 py-4 font-medium text-foreground">{invoice.number}</td>
                                    <td className="px-6 py-4 text-muted-foreground">{invoice.customer}</td>
                                    <td className="px-6 py-4 font-medium text-foreground">
                                        {formatCurrency(invoice.amount)}
                                    </td>
                                    <td className="px-6 py-4">{getStatusBadge(invoice.status)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="grid gap-4 md:grid-cols-4">
                <button className="flex items-center gap-3 rounded-lg border border-border bg-card p-4 transition-colors hover:bg-accent">
                    <div className="rounded-lg bg-primary/10 p-2">
                        <ShoppingCart className="h-5 w-5 text-primary" />
                    </div>
                    <div className="text-left">
                        <p className="font-medium text-foreground">New Sale</p>
                        <p className="text-sm text-muted-foreground">Open POS</p>
                    </div>
                </button>
                <button className="flex items-center gap-3 rounded-lg border border-border bg-card p-4 transition-colors hover:bg-accent">
                    <div className="rounded-lg bg-success/10 p-2">
                        <Package className="h-5 w-5 text-success" />
                    </div>
                    <div className="text-left">
                        <p className="font-medium text-foreground">Add Product</p>
                        <p className="text-sm text-muted-foreground">New inventory</p>
                    </div>
                </button>
                <button className="flex items-center gap-3 rounded-lg border border-border bg-card p-4 transition-colors hover:bg-accent">
                    <div className="rounded-lg bg-warning/10 p-2">
                        <FileText className="h-5 w-5 text-warning" />
                    </div>
                    <div className="text-left">
                        <p className="font-medium text-foreground">Create Invoice</p>
                        <p className="text-sm text-muted-foreground">Manual billing</p>
                    </div>
                </button>
                <button className="flex items-center gap-3 rounded-lg border border-border bg-card p-4 transition-colors hover:bg-accent">
                    <div className="rounded-lg bg-destructive/10 p-2">
                        <Clock className="h-5 w-5 text-destructive" />
                    </div>
                    <div className="text-left">
                        <p className="font-medium text-foreground">View Reports</p>
                        <p className="text-sm text-muted-foreground">Analytics</p>
                    </div>
                </button>
            </div>
        </div>
    );
}
