import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/utils';
import {
    LayoutDashboard,
    Package,
    ShoppingCart,
    FileText,
    Users,
    Truck,
    ClipboardList,
    BarChart3,
    Printer,
    Settings,
    ChevronLeft,
    ChevronRight,
    Building2,
    UserCog,
} from 'lucide-react';
import { useState } from 'react';

interface SidebarProps {
    className?: string;
}

const navItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/inventory', icon: Package, label: 'Inventory' },
    { to: '/pos', icon: ShoppingCart, label: 'POS' },
    { to: '/invoices', icon: FileText, label: 'Invoices' },
    { to: '/customers', icon: Users, label: 'Customers' },
    { to: '/suppliers', icon: Truck, label: 'Suppliers' },
    { to: '/purchase-orders', icon: ClipboardList, label: 'Purchase Orders' },
    { to: '/reports', icon: BarChart3, label: 'Reports' },
    { to: '/printers', icon: Printer, label: 'Printers' },
    { to: '/users', icon: UserCog, label: 'User Management' },
    { to: '/company', icon: Building2, label: 'Company' },
    { to: '/settings', icon: Settings, label: 'Settings' },
];

export function Sidebar({ className }: SidebarProps) {
    const [collapsed, setCollapsed] = useState(false);

    return (
        <aside
            className={cn(
                'relative flex h-screen flex-col border-r border-border bg-card transition-all duration-300',
                collapsed ? 'w-16' : 'w-64',
                className
            )}
        >
            {/* Logo */}
            <div className="flex h-16 items-center border-b border-border px-4">
                <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground font-bold">
                        S
                    </div>
                    {!collapsed && (
                        <span className="text-lg font-semibold text-foreground">SaaSBill</span>
                    )}
                </div>
            </div>

            {/* Navigation */}
            <nav className="flex-1 space-y-1 overflow-y-auto p-2">
                {navItems.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) =>
                            cn(
                                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                                'hover:bg-accent hover:text-accent-foreground',
                                isActive
                                    ? 'bg-primary text-primary-foreground'
                                    : 'text-muted-foreground',
                                collapsed && 'justify-center px-2'
                            )
                        }
                        title={collapsed ? item.label : undefined}
                    >
                        <item.icon className="h-5 w-5 flex-shrink-0" />
                        {!collapsed && <span>{item.label}</span>}
                    </NavLink>
                ))}
            </nav>

            {/* Collapse Button */}
            <button
                onClick={() => setCollapsed(!collapsed)}
                className="absolute -right-3 top-20 flex h-6 w-6 items-center justify-center rounded-full border border-border bg-card text-muted-foreground hover:bg-accent hover:text-accent-foreground"
            >
                {collapsed ? (
                    <ChevronRight className="h-4 w-4" />
                ) : (
                    <ChevronLeft className="h-4 w-4" />
                )}
            </button>

            {/* Footer */}
            <div className="border-t border-border p-4">
                {!collapsed && (
                    <p className="text-xs text-muted-foreground">
                        &copy; 2024 SaaSBill
                    </p>
                )}
            </div>
        </aside>
    );
}
