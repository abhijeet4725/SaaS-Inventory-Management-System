import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Building2, Save } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input, Card, CardContent, Spinner } from '@/components/ui';
import { companyApi } from '@/api/company';

const companySchema = z.object({
    name: z.string().min(1, 'Company name is required'),
    legalName: z.string().optional(),
    taxId: z.string().optional(),
    registrationNumber: z.string().optional(),
    addressLine1: z.string().optional(),
    city: z.string().optional(),
    state: z.string().optional(),
    postalCode: z.string().optional(),
    country: z.string().optional(),
    phone: z.string().optional(),
    email: z.string().email().optional().or(z.literal('')),
    website: z.string().url().optional().or(z.literal('')),
    invoicePrefix: z.string().optional(),
    invoiceFooter: z.string().optional(),
    defaultTaxRate: z.coerce.number().min(0).max(100).optional(),
    currency: z.string().optional(),
    timezone: z.string().optional(),
});

type CompanyFormData = z.infer<typeof companySchema>;

// Mock data
const mockCompany = {
    id: '1',
    name: 'My Store',
    legalName: 'My Store Pvt Ltd',
    taxId: '22AAAAA0000A1Z5',
    registrationNumber: 'AAAAA0000A',
    addressLine1: '123 Business Street',
    city: 'Mumbai',
    state: 'Maharashtra',
    postalCode: '400001',
    country: 'India',
    phone: '9876543210',
    email: 'contact@mystore.com',
    website: 'https://mystore.com',
    invoicePrefix: 'INV-',
    invoiceFooter: 'Thank you for your business!',
    defaultTaxRate: 18,
    currency: 'INR',
    timezone: 'Asia/Kolkata',
};

export function CompanySettingsPage() {
    const queryClient = useQueryClient();

    const { data: company, isLoading } = useQuery({
        queryKey: ['company'],
        queryFn: companyApi.getCompany,
        placeholderData: mockCompany,
    });

    const {
        register,
        handleSubmit,
        formState: { errors, isDirty },
    } = useForm<CompanyFormData>({
        resolver: zodResolver(companySchema),
        values: company,
    });

    const updateMutation = useMutation({
        mutationFn: companyApi.updateCompany,
        onSuccess: () => {
            toast.success('Company settings saved');
            queryClient.invalidateQueries({ queryKey: ['company'] });
        },
        onError: () => toast.error('Failed to save settings'),
    });

    const onSubmit = (data: CompanyFormData) => {
        updateMutation.mutate(data);
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
            {/* Header */}
            <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
                    <Building2 className="h-6 w-6 text-primary" />
                </div>
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Company Settings</h1>
                    <p className="text-muted-foreground">Manage your business information</p>
                </div>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                {/* Basic Information */}
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Basic Information</h2>

                        <div className="grid gap-4 md:grid-cols-2">
                            <Input
                                label="Company Name *"
                                {...register('name')}
                                error={errors.name?.message}
                            />
                            <Input
                                label="Legal Name"
                                {...register('legalName')}
                                error={errors.legalName?.message}
                            />
                        </div>
                    </CardContent>
                </Card>

                {/* Tax Information */}
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Tax Information</h2>

                        <div className="grid gap-4 md:grid-cols-2">
                            <Input
                                label="Tax ID / GST Number"
                                {...register('taxId')}
                                placeholder="22AAAAA0000A1Z5"
                            />
                            <Input label="Registration Number" {...register('registrationNumber')} placeholder="AAAAA0000A" />
                        </div>
                    </CardContent>
                </Card>

                {/* Address */}
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Address</h2>

                        <div className="grid gap-4">
                            <Input label="Address" {...register('addressLine1')} />
                            <div className="grid gap-4 md:grid-cols-4">
                                <Input label="City" {...register('city')} />
                                <Input label="State" {...register('state')} />
                                <Input label="Postal Code" {...register('postalCode')} />
                                <Input label="Country" {...register('country')} />
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Contact Information */}
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Contact Information</h2>

                        <div className="grid gap-4 md:grid-cols-3">
                            <Input label="Phone" {...register('phone')} />
                            <Input
                                label="Email"
                                type="email"
                                {...register('email')}
                                error={errors.email?.message}
                            />
                            <Input
                                label="Website"
                                type="url"
                                {...register('website')}
                                error={errors.website?.message}
                            />
                        </div>
                    </CardContent>
                </Card>

                {/* Invoice Settings */}
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Invoice Settings</h2>

                        <div className="grid gap-4 md:grid-cols-3">
                            <Input
                                label="Invoice Prefix"
                                {...register('invoicePrefix')}
                                placeholder="INV-"
                            />
                            <Input
                                label="Tax Rate (%)"
                                type="number"
                                {...register('defaultTaxRate')}
                                min={0}
                                max={100}
                            />
                            <Input label="Currency" {...register('currency')} placeholder="INR" />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-foreground mb-2">
                                Invoice Footer
                            </label>
                            <textarea
                                {...register('invoiceFooter')}
                                rows={3}
                                placeholder="Thank you for your business!"
                                className="w-full px-3 py-2 border border-border rounded-lg bg-background text-foreground resize-none focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
                            />
                        </div>
                    </CardContent>
                </Card>

                {/* Regional Settings */}
                <Card>
                    <CardContent className="p-6 space-y-6">
                        <h2 className="text-lg font-semibold text-foreground">Regional Settings</h2>

                        <div className="grid gap-4 md:grid-cols-2">
                            <div>
                                <label className="block text-sm font-medium text-foreground mb-2">
                                    Timezone
                                </label>
                                <select
                                    {...register('timezone')}
                                    className="w-full px-3 py-2 border border-border rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
                                >
                                    <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                                    <option value="America/New_York">America/New_York (EST)</option>
                                    <option value="Europe/London">Europe/London (GMT)</option>
                                    <option value="Asia/Dubai">Asia/Dubai (GST)</option>
                                </select>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Actions */}
                <div className="flex justify-end gap-4">
                    <Button
                        type="submit"
                        isLoading={updateMutation.isPending}
                        disabled={!isDirty}
                    >
                        <Save className="mr-2 h-4 w-4" />
                        Save Changes
                    </Button>
                </div>
            </form>
        </div>
    );
}
