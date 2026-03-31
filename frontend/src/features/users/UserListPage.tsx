import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Edit2, Trash2, Shield, UserCheck, UserX, Key } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Button, Input, Badge, Spinner, Card, CardContent } from '@/components/ui';
import { usersApi } from '@/api/users';
import { formatDate } from '@/lib/utils';
import type { User } from '@/types';

// Mock users for demo
const mockUsers: User[] = [
    {
        id: '1',
        email: 'admin@example.com',
        firstName: 'Admin',
        lastName: 'User',
        phone: '9876543210',
        roles: ['ADMIN'],
        enabled: true,
        emailVerified: true,
        createdAt: '2024-01-01T10:00:00',
    },
    {
        id: '2',
        email: 'manager@example.com',
        firstName: 'Store',
        lastName: 'Manager',
        phone: '9876543211',
        roles: ['MANAGER'],
        enabled: true,
        emailVerified: true,
        createdAt: '2024-01-15T10:00:00',
    },
    {
        id: '3',
        email: 'cashier@example.com',
        firstName: 'John',
        lastName: 'Cashier',
        phone: '9876543212',
        roles: ['CASHIER'],
        enabled: true,
        emailVerified: false,
        createdAt: '2024-02-01T10:00:00',
    },
    {
        id: '4',
        email: 'disabled@example.com',
        firstName: 'Disabled',
        lastName: 'User',
        phone: '9876543213',
        roles: ['CASHIER'],
        enabled: false,
        emailVerified: true,
        createdAt: '2024-01-20T10:00:00',
    },
];

export function UserListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [search, setSearch] = useState('');
    const [showResetModal, setShowResetModal] = useState<string | null>(null);
    const [newPassword, setNewPassword] = useState('');

    const { data: usersData, isLoading } = useQuery({
        queryKey: ['users'],
        queryFn: () => usersApi.getUsers(),
        placeholderData: { users: mockUsers, page: { page: 0, size: 20, totalElements: mockUsers.length, totalPages: 1, hasNext: false, hasPrevious: false } },
    });

    const users = usersData?.users || mockUsers;

    const enableMutation = useMutation({
        mutationFn: usersApi.enableUser,
        onSuccess: () => {
            toast.success('User enabled');
            queryClient.invalidateQueries({ queryKey: ['users'] });
        },
    });

    const disableMutation = useMutation({
        mutationFn: usersApi.disableUser,
        onSuccess: () => {
            toast.success('User disabled');
            queryClient.invalidateQueries({ queryKey: ['users'] });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: usersApi.deleteUser,
        onSuccess: () => {
            toast.success('User deleted');
            queryClient.invalidateQueries({ queryKey: ['users'] });
        },
    });

    const resetPasswordMutation = useMutation({
        mutationFn: ({ id, password }: { id: string; password: string }) =>
            usersApi.resetPassword(id, password),
        onSuccess: () => {
            toast.success('Password reset successfully');
            setShowResetModal(null);
            setNewPassword('');
        },
    });

    const getRoleBadge = (role: string) => {
        const variants: Record<string, 'destructive' | 'warning' | 'secondary'> = {
            ADMIN: 'destructive',
            MANAGER: 'warning',
            CASHIER: 'secondary',
        };
        return (
            <Badge key={role} variant={variants[role] || 'secondary'}>
                {role}
            </Badge>
        );
    };

    const filteredUsers = users.filter(
        (user) =>
            user.email.toLowerCase().includes(search.toLowerCase()) ||
            user.firstName.toLowerCase().includes(search.toLowerCase()) ||
            user.lastName.toLowerCase().includes(search.toLowerCase())
    );

    if (isLoading) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">User Management</h1>
                    <p className="text-muted-foreground">Manage users and their roles</p>
                </div>
                <Button onClick={() => navigate('/users/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add User
                </Button>
            </div>

            {/* Search */}
            <div className="max-w-md">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                        type="text"
                        placeholder="Search users..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="h-10 w-full rounded-lg border border-input bg-background pl-10 pr-4 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                </div>
            </div>

            {/* Users Table */}
            <Card>
                <CardContent className="p-0">
                    <table className="w-full">
                        <thead>
                            <tr className="border-b border-border bg-muted/50">
                                <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">User</th>
                                <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Roles</th>
                                <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Status</th>
                                <th className="px-6 py-3 text-left text-sm font-medium text-muted-foreground">Created</th>
                                <th className="px-6 py-3 text-right text-sm font-medium text-muted-foreground">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredUsers.map((user) => (
                                <tr key={user.id} className="border-b border-border last:border-0 hover:bg-muted/30">
                                    <td className="px-6 py-4">
                                        <div>
                                            <p className="font-medium text-foreground">
                                                {user.firstName} {user.lastName}
                                            </p>
                                            <p className="text-sm text-muted-foreground">{user.email}</p>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex gap-1">{user.roles.map(getRoleBadge)}</div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <Badge variant={user.enabled ? 'success' : 'secondary'}>
                                            {user.enabled ? 'Active' : 'Disabled'}
                                        </Badge>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-muted-foreground">{formatDate(user.createdAt)}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex justify-end gap-2">
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => navigate(`/users/${user.id}/edit`)}
                                                title="Edit"
                                            >
                                                <Edit2 className="h-4 w-4" />
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => navigate(`/users/${user.id}/roles`)}
                                                title="Manage Roles"
                                            >
                                                <Shield className="h-4 w-4" />
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => setShowResetModal(user.id)}
                                                title="Reset Password"
                                            >
                                                <Key className="h-4 w-4" />
                                            </Button>
                                            {user.enabled ? (
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => disableMutation.mutate(user.id)}
                                                    title="Disable"
                                                >
                                                    <UserX className="h-4 w-4" />
                                                </Button>
                                            ) : (
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => enableMutation.mutate(user.id)}
                                                    title="Enable"
                                                >
                                                    <UserCheck className="h-4 w-4" />
                                                </Button>
                                            )}
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => {
                                                    if (confirm('Are you sure you want to delete this user?')) {
                                                        deleteMutation.mutate(user.id);
                                                    }
                                                }}
                                                title="Delete"
                                            >
                                                <Trash2 className="h-4 w-4 text-destructive" />
                                            </Button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </CardContent>
            </Card>

            {/* Reset Password Modal */}
            {showResetModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                    <Card className="w-full max-w-md">
                        <CardContent className="p-6 space-y-4">
                            <h3 className="text-lg font-semibold text-foreground">Reset Password</h3>
                            <Input
                                type="password"
                                label="New Password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                placeholder="Enter new password"
                            />
                            <div className="flex justify-end gap-2">
                                <Button variant="outline" onClick={() => setShowResetModal(null)}>
                                    Cancel
                                </Button>
                                <Button
                                    onClick={() =>
                                        resetPasswordMutation.mutate({ id: showResetModal, password: newPassword })
                                    }
                                    isLoading={resetPasswordMutation.isPending}
                                >
                                    Reset Password
                                </Button>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            )}
        </div>
    );
}
