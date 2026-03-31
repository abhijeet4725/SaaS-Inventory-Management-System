import { Routes, Route, Navigate } from 'react-router-dom';

// Layout
import { AppShell, PrivateRoute } from '@/components/layout';

// Auth Pages
import { LoginPage, RegisterPage, ForgotPasswordPage } from '@/features/auth';

// Feature Pages
import { DashboardPage } from '@/features/dashboard';
import { ProductListPage, ProductFormPage } from '@/features/inventory';
import { PosPage } from '@/features/pos';
import { InvoiceListPage, InvoiceDetailPage } from '@/features/invoices';
import { CustomerListPage, CustomerFormPage } from '@/features/customers';
import { SupplierListPage, SupplierFormPage } from '@/features/suppliers';
import { POListPage, PODetailPage } from '@/features/purchase-orders';
import { ReportsPage } from '@/features/reports';
import { PrinterListPage, PrinterFormPage } from '@/features/printers';
import { SettingsPage } from '@/features/settings';
import { UserListPage, UserFormPage } from '@/features/users';
import { CompanySettingsPage } from '@/features/company';


export default function App() {
    return (
        <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />

            {/* Protected Routes */}
            <Route
                element={
                    <PrivateRoute>
                        <AppShell />
                    </PrivateRoute>
                }
            >
                {/* Dashboard */}
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={<DashboardPage />} />

                {/* Inventory */}
                <Route path="/inventory" element={<ProductListPage />} />
                <Route path="/inventory/new" element={<ProductFormPage />} />
                <Route path="/inventory/:id/edit" element={<ProductFormPage />} />

                {/* POS */}
                <Route path="/pos" element={<PosPage />} />

                {/* Invoices */}
                <Route path="/invoices" element={<InvoiceListPage />} />
                <Route path="/invoices/:id" element={<InvoiceDetailPage />} />

                {/* Customers */}
                <Route path="/customers" element={<CustomerListPage />} />
                <Route path="/customers/new" element={<CustomerFormPage />} />
                <Route path="/customers/:id/edit" element={<CustomerFormPage />} />

                {/* Suppliers */}
                <Route path="/suppliers" element={<SupplierListPage />} />
                <Route path="/suppliers/new" element={<SupplierFormPage />} />
                <Route path="/suppliers/:id/edit" element={<SupplierFormPage />} />

                {/* Purchase Orders */}
                <Route path="/purchase-orders" element={<POListPage />} />
                <Route path="/purchase-orders/:id" element={<PODetailPage />} />

                {/* Reports */}
                <Route path="/reports" element={<ReportsPage />} />

                {/* Printers */}
                <Route path="/printers" element={<PrinterListPage />} />
                <Route path="/printers/new" element={<PrinterFormPage />} />
                <Route path="/printers/:id/edit" element={<PrinterFormPage />} />

                {/* Settings */}
                <Route path="/settings" element={<SettingsPage />} />

                {/* User Management (Admin) */}
                <Route path="/users" element={<UserListPage />} />
                <Route path="/users/new" element={<UserFormPage />} />
                <Route path="/users/:id/edit" element={<UserFormPage />} />

                {/* Company Settings */}
                <Route path="/company" element={<CompanySettingsPage />} />
            </Route>

            {/* 404 */}
            <Route
                path="*"
                element={
                    <div className="flex h-screen items-center justify-center bg-background">
                        <div className="text-center">
                            <h1 className="text-6xl font-bold text-primary">404</h1>
                            <p className="mt-4 text-xl text-muted-foreground">Page not found</p>
                            <a href="/dashboard" className="mt-4 inline-block text-primary hover:underline">
                                Go to Dashboard
                            </a>
                        </div>
                    </div>
                }
            />
        </Routes>
    );
}
