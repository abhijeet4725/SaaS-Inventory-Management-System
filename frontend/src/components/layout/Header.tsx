import { Bell, LogOut, Moon, Search, Sun, User } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Avatar, Button } from '@/components/ui';
import { useAuthStore } from '@/stores/auth.store';
import { getInitials } from '@/lib/utils';

export function Header() {
    const navigate = useNavigate();
    const { user, logout } = useAuthStore();
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    const [darkMode, setDarkMode] = useState(true);

    const toggleDarkMode = () => {
        setDarkMode(!darkMode);
        document.documentElement.classList.toggle('dark');
        document.documentElement.classList.toggle('light');
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="flex h-16 items-center justify-between border-b border-border bg-card px-6">
            {/* Search */}
            <div className="relative max-w-md flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <input
                    type="text"
                    placeholder="Search products, invoices, customers..."
                    className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                />
            </div>

            {/* Actions */}
            <div className="flex items-center gap-2">
                {/* Theme Toggle */}
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={toggleDarkMode}
                    title={darkMode ? 'Light mode' : 'Dark mode'}
                >
                    {darkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
                </Button>

                {/* Notifications */}
                <Button variant="ghost" size="icon" className="relative">
                    <Bell className="h-5 w-5" />
                    <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-destructive" />
                </Button>

                {/* User Menu */}
                <div className="relative">
                    <button
                        onClick={() => setUserMenuOpen(!userMenuOpen)}
                        className="flex items-center gap-2 rounded-lg p-1.5 hover:bg-accent"
                    >
                        <Avatar
                            fallback={user ? getInitials(`${user.firstName} ${user.lastName}`) : 'U'}
                            size="sm"
                        />
                        <div className="hidden text-left md:block">
                            <p className="text-sm font-medium text-foreground">
                                {user ? `${user.firstName} ${user.lastName}` : 'User'}
                            </p>
                            <p className="text-xs text-muted-foreground">{user?.roles?.[0] || 'Admin'}</p>
                        </div>
                    </button>

                    {/* Dropdown */}
                    {userMenuOpen && (
                        <>
                            <div
                                className="fixed inset-0 z-40"
                                onClick={() => setUserMenuOpen(false)}
                            />
                            <div className="absolute right-0 top-full z-50 mt-2 w-48 rounded-lg border border-border bg-card py-1 shadow-lg">
                                <button
                                    className="flex w-full items-center gap-2 px-4 py-2 text-sm text-foreground hover:bg-accent"
                                    onClick={() => {
                                        setUserMenuOpen(false);
                                        navigate('/settings');
                                    }}
                                >
                                    <User className="h-4 w-4" />
                                    Profile
                                </button>
                                <hr className="my-1 border-border" />
                                <button
                                    className="flex w-full items-center gap-2 px-4 py-2 text-sm text-destructive hover:bg-accent"
                                    onClick={handleLogout}
                                >
                                    <LogOut className="h-4 w-4" />
                                    Logout
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </header>
    );
}
