import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Wifi, Usb } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Input, Card, CardHeader, CardTitle, CardContent, Spinner } from '@/components/ui';
import { printersApi } from '@/api/printers';

const printerSchema = z.object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    description: z.string().optional(),
    printerType: z.enum(['THERMAL_80MM', 'THERMAL_58MM', 'LABEL', 'STANDARD']),
    connectionType: z.enum(['NETWORK', 'USB']),
    ipAddress: z.string().optional(),
    port: z.coerce.number().optional(),
    usbPath: z.string().optional(),
    paperWidth: z.coerce.number().min(20).max(200),
    isDefault: z.boolean().default(false),
});

type PrinterFormData = z.infer<typeof printerSchema>;

export function PrinterFormPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const queryClient = useQueryClient();
    const isEditing = Boolean(id);

    const { data: printer, isLoading: isLoadingPrinter } = useQuery({
        queryKey: ['printer', id],
        queryFn: () => printersApi.getPrinter(id!),
        enabled: isEditing,
    });

    const {
        register,
        handleSubmit,
        watch,
        formState: { errors, isSubmitting },
    } = useForm<PrinterFormData>({
        resolver: zodResolver(printerSchema),
        defaultValues: {
            printerType: 'THERMAL_80MM',
            connectionType: 'NETWORK',
            port: 9100,
            paperWidth: 80,
            isDefault: false,
        },
        values: printer
            ? {
                name: printer.name,
                description: printer.description || '',
                printerType: printer.printerType as PrinterFormData['printerType'],
                connectionType: printer.connectionType as PrinterFormData['connectionType'],
                ipAddress: printer.ipAddress || '',
                port: printer.port || 9100,
                usbPath: printer.usbPath || '',
                paperWidth: printer.paperWidth ?? 80,
                isDefault: printer.isDefault,
            }
            : undefined,
    });

    const connectionType = watch('connectionType');

    const createMutation = useMutation({
        mutationFn: (data: PrinterFormData) => printersApi.createPrinter(data),
        onSuccess: () => {
            toast.success('Printer added successfully');
            queryClient.invalidateQueries({ queryKey: ['printers'] });
            navigate('/printers');
        },
        onError: () => {
            toast.error('Failed to add printer');
        },
    });

    const updateMutation = useMutation({
        mutationFn: (data: PrinterFormData) => printersApi.updatePrinter(id!, data),
        onSuccess: () => {
            toast.success('Printer updated successfully');
            queryClient.invalidateQueries({ queryKey: ['printers'] });
            navigate('/printers');
        },
        onError: () => {
            toast.error('Failed to update printer');
        },
    });

    const onSubmit = (data: PrinterFormData) => {
        if (isEditing) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    if (isEditing && isLoadingPrinter) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => navigate('/printers')}>
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold text-foreground">
                        {isEditing ? 'Edit Printer' : 'Add Printer'}
                    </h1>
                    <p className="text-muted-foreground">
                        {isEditing ? 'Update printer settings' : 'Configure a new printer'}
                    </p>
                </div>
            </div>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Card>
                    <CardHeader>
                        <CardTitle>Printer Configuration</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <Input
                            label="Printer Name *"
                            placeholder="e.g., Reception Printer"
                            error={errors.name?.message}
                            {...register('name')}
                        />

                        <Input
                            label="Description"
                            placeholder="e.g., Main receipt printer at front desk"
                            error={errors.description?.message}
                            {...register('description')}
                        />

                        <div className="grid gap-4 sm:grid-cols-2">
                            <div>
                                <label className="mb-2 block text-sm font-medium text-foreground">Printer Type *</label>
                                <select
                                    className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                                    {...register('printerType')}
                                >
                                    <option value="THERMAL_80MM">Thermal 80mm (Receipts)</option>
                                    <option value="THERMAL_58MM">Thermal 58mm (Small Receipts)</option>
                                    <option value="LABEL">Label Printer</option>
                                    <option value="STANDARD">Standard Printer</option>
                                </select>
                            </div>

                            <Input
                                label="Paper Width (mm) *"
                                type="number"
                                placeholder="80"
                                error={errors.paperWidth?.message}
                                {...register('paperWidth')}
                            />
                        </div>

                        <div className="border-t border-border pt-6">
                            <h3 className="mb-4 font-semibold text-foreground">Connection Settings</h3>

                            <div className="mb-4 flex gap-4">
                                <label className="flex cursor-pointer items-center gap-2">
                                    <input type="radio" value="NETWORK" {...register('connectionType')} />
                                    <Wifi className="h-4 w-4" />
                                    <span className="text-foreground">Network (IP)</span>
                                </label>
                                <label className="flex cursor-pointer items-center gap-2">
                                    <input type="radio" value="USB" {...register('connectionType')} />
                                    <Usb className="h-4 w-4" />
                                    <span className="text-foreground">USB</span>
                                </label>
                            </div>

                            {connectionType === 'NETWORK' ? (
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Input
                                        label="IP Address *"
                                        placeholder="e.g., 192.168.1.100"
                                        error={errors.ipAddress?.message}
                                        {...register('ipAddress')}
                                    />
                                    <Input
                                        label="Port"
                                        type="number"
                                        placeholder="9100"
                                        error={errors.port?.message}
                                        {...register('port')}
                                    />
                                </div>
                            ) : (
                                <Input
                                    label="USB Path *"
                                    placeholder="e.g., /dev/usb/lp0"
                                    error={errors.usbPath?.message}
                                    {...register('usbPath')}
                                />
                            )}
                        </div>

                        <div className="border-t border-border pt-6">
                            <label className="flex cursor-pointer items-center gap-3">
                                <input
                                    type="checkbox"
                                    className="h-4 w-4 rounded border-input"
                                    {...register('isDefault')}
                                />
                                <div>
                                    <span className="font-medium text-foreground">Set as default printer</span>
                                    <p className="text-sm text-muted-foreground">Use this printer for all receipts</p>
                                </div>
                            </label>
                        </div>

                        <div className="flex justify-end gap-4 border-t border-border pt-6">
                            <Button type="button" variant="outline" onClick={() => navigate('/printers')}>
                                Cancel
                            </Button>
                            <Button type="submit" isLoading={isSubmitting}>
                                {isEditing ? 'Update Printer' : 'Add Printer'}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </form>
        </div>
    );
}
