import { BarChart3, Calendar, TrendingUp, TrendingDown } from 'lucide-react';
import { useState } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui';
import { formatCurrency, formatDate } from '@/lib/utils';
import {
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    LineChart,
    Line,
    PieChart,
    Pie,
    Cell,
} from 'recharts';

const COLORS = ['#3b82f6', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

// Mock data
const mockSalesData = [
    { date: '2024-02-01', sales: 12500, orders: 15 },
    { date: '2024-02-02', sales: 8900, orders: 11 },
    { date: '2024-02-03', sales: 15200, orders: 18 },
    { date: '2024-02-04', sales: 9800, orders: 12 },
    { date: '2024-02-05', sales: 21000, orders: 25 },
    { date: '2024-02-06', sales: 18500, orders: 22 },
    { date: '2024-02-07', sales: 16700, orders: 20 },
];

const mockCategoryData = [
    { name: 'Electronics', value: 45000 },
    { name: 'Accessories', value: 28000 },
    { name: 'Furniture', value: 15000 },
    { name: 'Stationery', value: 8000 },
];

const mockTopProducts = [
    { name: 'Wireless Mouse', quantity: 125, revenue: 74875 },
    { name: 'USB-C Cable', quantity: 98, revenue: 24402 },
    { name: 'Mechanical Keyboard', quantity: 45, revenue: 179955 },
    { name: 'Laptop Stand', quantity: 38, revenue: 56962 },
    { name: 'Webcam HD', quantity: 32, revenue: 95968 },
];

export function ReportsPage() {
    const [dateRange, setDateRange] = useState({ start: '2024-02-01', end: '2024-02-07' });

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Reports</h1>
                    <p className="text-muted-foreground">Analytics and insights</p>
                </div>
                <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                    <input
                        type="date"
                        value={dateRange.start}
                        onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
                        className="h-10 rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                    <span className="text-muted-foreground">to</span>
                    <input
                        type="date"
                        value={dateRange.end}
                        onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
                        className="h-10 rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                </div>
            </div>

            {/* Summary Cards */}
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardContent className="p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-muted-foreground">Total Revenue</p>
                                <p className="text-2xl font-bold text-foreground">{formatCurrency(102600)}</p>
                                <p className="flex items-center text-sm text-success">
                                    <TrendingUp className="mr-1 h-4 w-4" />
                                    +12.5% vs last period
                                </p>
                            </div>
                            <div className="rounded-lg bg-primary/10 p-3">
                                <BarChart3 className="h-6 w-6 text-primary" />
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardContent className="p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-muted-foreground">Total Orders</p>
                                <p className="text-2xl font-bold text-foreground">123</p>
                                <p className="flex items-center text-sm text-success">
                                    <TrendingUp className="mr-1 h-4 w-4" />
                                    +8.2% vs last period
                                </p>
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardContent className="p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-muted-foreground">Average Order</p>
                                <p className="text-2xl font-bold text-foreground">{formatCurrency(834)}</p>
                                <p className="flex items-center text-sm text-success">
                                    <TrendingUp className="mr-1 h-4 w-4" />
                                    +3.8% vs last period
                                </p>
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardContent className="p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-muted-foreground">Inventory Value</p>
                                <p className="text-2xl font-bold text-foreground">{formatCurrency(850000)}</p>
                                <p className="flex items-center text-sm text-destructive">
                                    <TrendingDown className="mr-1 h-4 w-4" />
                                    -2.1% vs last period
                                </p>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Charts Row */}
            <div className="grid gap-6 lg:grid-cols-2">
                {/* Sales Trend */}
                <Card>
                    <CardHeader>
                        <CardTitle>Sales Trend</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={mockSalesData}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(217.2 32.6% 17.5%)" />
                                    <XAxis dataKey="date" stroke="hsl(215 20.2% 65.1%)" fontSize={12} tickFormatter={(v) => formatDate(v, { day: 'numeric', month: 'short' })} />
                                    <YAxis stroke="hsl(215 20.2% 65.1%)" fontSize={12} tickFormatter={(v) => `₹${v / 1000}k`} />
                                    <Tooltip
                                        contentStyle={{
                                            backgroundColor: 'hsl(222.2 84% 7%)',
                                            border: '1px solid hsl(217.2 32.6% 17.5%)',
                                            borderRadius: '8px',
                                        }}
                                        formatter={(value: number) => [formatCurrency(value), 'Sales']}
                                    />
                                    <Line type="monotone" dataKey="sales" stroke="#3b82f6" strokeWidth={2} dot={{ fill: '#3b82f6' }} />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>

                {/* Category Breakdown */}
                <Card>
                    <CardHeader>
                        <CardTitle>Sales by Category</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={mockCategoryData}
                                        cx="50%"
                                        cy="50%"
                                        labelLine={false}
                                        outerRadius={80}
                                        fill="#8884d8"
                                        dataKey="value"
                                        label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                    >
                                        {mockCategoryData.map((_, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip formatter={(value: number) => formatCurrency(value)} />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Top Products */}
            <Card>
                <CardHeader>
                    <CardTitle>Top Selling Products</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="border-b border-border">
                                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">#</th>
                                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Product</th>
                                    <th className="px-4 py-3 text-right text-sm font-medium text-muted-foreground">Quantity</th>
                                    <th className="px-4 py-3 text-right text-sm font-medium text-muted-foreground">Revenue</th>
                                </tr>
                            </thead>
                            <tbody>
                                {mockTopProducts.map((product, index) => (
                                    <tr key={product.name} className="border-b border-border last:border-0">
                                        <td className="px-4 py-3 text-muted-foreground">{index + 1}</td>
                                        <td className="px-4 py-3 font-medium text-foreground">{product.name}</td>
                                        <td className="px-4 py-3 text-right text-muted-foreground">{product.quantity}</td>
                                        <td className="px-4 py-3 text-right font-medium text-foreground">{formatCurrency(product.revenue)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}

export { ReportsPage as default };
