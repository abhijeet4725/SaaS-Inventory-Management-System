import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Save } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input, Card, CardContent, Spinner } from '@/components/ui';
import { usersApi } from '@/api/users';

const userSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(8, 'Password must be at least 8 characters').optional(),
    firstName: z.string().min(1, 'First name is required'),
    lastName: z.string().min(1, 'Last name is required'),
    phone: z.string().optional(),
    roles: z.array(z.string()).min(1, 'At least one role is required'),
});

type UserFormData = z.infer<typeof userSchema>;

const ROLES = ['ADMIN', 'MANAGER', 'CASHIER'];

export function UserFormPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const isEditing = Boolean(id);
    const queryClient = useQueryClient();
    const [selectedRoles, setSelectedRoles] = useState<string[]>(['CASHIER']);

    const { data: user, isLoading: isLoadingUser } = useQuery({
        queryKey: ['user', id],
        queryFn: () => usersApi.getUser(id!),
        enabled: isEditing,
    });

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<UserFormData>({
        resolver: zodResolver(userSchema),
        values: user
            ? {
                email: user.email,
                firstName: user.firstName,
                lastName: user.lastName,
                phone: user.phone || '',
                roles: user.roles,
            }
            : undefined,
    });

    const createMutation = useMutation({
        mutationFn: usersApi.createUser,
        onSuccess: () => {
            toast.success('User created successfully');
            queryClient.invalidateQueries({ queryKey: ['users'] });
            navigate('/users');
        },
        onError: () => toast.error('Failed to create user'),
    });

    const updateMutation = useMutation({
        mutationFn: (data: any) => usersApi.updateUser(id!, data),
        onSuccess: () => {
            toast.success('User updated successfully');
            queryClient.invalidateQueries({ queryKey: ['users'] });
            navigate('/users');
        },
        onError: () => toast.error('Failed to update user'),
    });

    const onSubmit = (data: UserFormData) => {
        const submitData = { ...data, roles: selectedRoles };
        if (isEditing) {
            updateMutation.mutate(submitData);
        } else {
            createMutation.mutate(submitData as any);
        }
    };

    const toggleRole = (role: string) => {
        setSelectedRoles((prev) =>
            prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]
        );
    };

    if (isLoadingUser) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <Button variant="ghost" onClick={() => navigate('/users')}>
                    <ArrowLeft className="h-4 w-4" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold text-foreground">
                        {isEditing ? 'Edit User' : 'Add New User'}
                    </h1>
                    <p className="text-muted-foreground">
                        {isEditing ? 'Update user details' : 'Create a new user account'}
                    </p>
                </div>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Account Information</h2>

                        <div className="grid gap-4 md:grid-cols-2">
                            <Input
                                label="Email *"
                                type="email"
                                {...register('email')}
                                error={errors.email?.message}
                                disabled={isEditing}
                            />
                            {!isEditing && (
                                <Input
                                    label="Password *"
                                    type="password"
                                    {...register('password')}
                                    error={errors.password?.message}
                                />
                            )}
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Personal Information</h2>

                        <div className="grid gap-4 md:grid-cols-2">
                            <Input
                                label="First Name *"
                                {...register('firstName')}
                                error={errors.firstName?.message}
                            />
                            <Input
                                label="Last Name *"
                                {...register('lastName')}
                                error={errors.lastName?.message}
                            />
                            <Input label="Phone" {...register('phone')} error={errors.phone?.message} />
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Roles</h2>
                        <p className="text-sm text-muted-foreground">
                            Select the roles for this user. Roles determine access permissions.
                        </p>

                        <div className="flex flex-wrap gap-3">
                            {ROLES.map((role) => (
                                <button
                                    key={role}
                                    type="button"
                                    onClick={() => toggleRole(role)}
                                    className={`px-4 py-2 rounded-lg border transition-colors ${selectedRoles.includes(role)
                                            ? 'border-primary bg-primary/10 text-primary'
                                            : 'border-border bg-background text-muted-foreground hover:border-primary/50'
                                        }`}
                                >
                                    {role}
                                </button>
                            ))}
                        </div>

                        <div className="text-sm text-muted-foreground space-y-1">
                            <p>
                                <strong>ADMIN:</strong> Full access to all features
                            </p>
                            <p>
                                <strong>MANAGER:</strong> Manage inventory, invoices, and reports
                            </p>
                            <p>
                                <strong>CASHIER:</strong> POS and basic customer operations
                            </p>
                        </div>
                    </CardContent>
                </Card>

                {/* Actions */}
                <div className="flex justify-end gap-4">
                    <Button type="button" variant="outline" onClick={() => navigate('/users')}>
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        isLoading={createMutation.isPending || updateMutation.isPending}
                    >
                        <Save className="mr-2 h-4 w-4" />
                        {isEditing ? 'Update User' : 'Create User'}
                    </Button>
                </div>
            </form>
        </div>
    );
}
