import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Printer as PrinterIcon, Wifi, Usb, Trash2, Settings, TestTube } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button, Badge, Spinner, Card, CardContent } from '@/components/ui';
import { printersApi } from '@/api/printers';
import type { Printer } from '@/types';

// Mock data
const mockPrinters: Printer[] = [
    { id: '1', name: 'Reception Printer', description: 'Main receipt printer', printerType: 'THERMAL_80MM', connectionType: 'NETWORK', ipAddress: '192.168.1.100', port: 9100, paperWidth: 80, isDefault: true, active: true },
    { id: '2', name: 'Kitchen Printer', description: 'Kitchen order printer', printerType: 'THERMAL_58MM', connectionType: 'USB', usbPath: '/dev/usb/lp0', paperWidth: 58, isDefault: false, active: true },
    { id: '3', name: 'Label Printer', description: 'Product label printer', printerType: 'LABEL', connectionType: 'USB', usbPath: '/dev/usb/lp1', paperWidth: 50, isDefault: false, active: false },
];

export function PrinterListPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data: printers = mockPrinters, isLoading } = useQuery({
        queryKey: ['printers'],
        queryFn: printersApi.getPrinters,
        placeholderData: mockPrinters,
    });

    const testMutation = useMutation({
        mutationFn: (printerId: string) => printersApi.testPrint(printerId),
        onSuccess: () => toast.success('Test page sent to printer'),
        onError: () => toast.error('Failed to send test page'),
    });

    const deleteMutation = useMutation({
        mutationFn: printersApi.deletePrinter,
        onSuccess: () => {
            toast.success('Printer deleted');
            queryClient.invalidateQueries({ queryKey: ['printers'] });
        },
    });

    const getConnectionIcon = (type: Printer['connectionType']) => {
        switch (type) {
            case 'NETWORK':
                return <Wifi className="h-4 w-4" />;
            case 'USB':
                return <Usb className="h-4 w-4" />;
            default:
                return <PrinterIcon className="h-4 w-4" />;
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-foreground">Printers</h1>
                    <p className="text-muted-foreground">Configure receipt and label printers</p>
                </div>
                <Button onClick={() => navigate('/printers/new')}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Printer
                </Button>
            </div>

            {isLoading ? (
                <div className="flex h-64 items-center justify-center">
                    <Spinner size="lg" />
                </div>
            ) : printers.length === 0 ? (
                <Card>
                    <CardContent className="flex h-64 flex-col items-center justify-center text-muted-foreground">
                        <PrinterIcon className="mb-2 h-8 w-8" />
                        <p>No printers configured</p>
                        <Button variant="link" onClick={() => navigate('/printers/new')}>
                            Add your first printer
                        </Button>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {printers.map((printer) => (
                        <Card key={printer.id} className="relative">
                            {printer.isDefault && (
                                <div className="absolute right-4 top-4">
                                    <Badge variant="success">Default</Badge>
                                </div>
                            )}
                            <CardContent className="p-6">
                                <div className="mb-4 flex items-center gap-3">
                                    <div className={`rounded-lg p-3 ${printer.active ? 'bg-primary/10' : 'bg-muted'}`}>
                                        <PrinterIcon className={`h-6 w-6 ${printer.active ? 'text-primary' : 'text-muted-foreground'}`} />
                                    </div>
                                    <div>
                                        <h3 className="font-semibold text-foreground">{printer.name}</h3>
                                        <p className="text-sm text-muted-foreground">{printer.description}</p>
                                    </div>
                                </div>

                                <div className="mb-4 space-y-2 text-sm">
                                    <div className="flex items-center justify-between">
                                        <span className="text-muted-foreground">Type</span>
                                        <span className="text-foreground">{printer.printerType.replace('_', ' ')}</span>
                                    </div>
                                    <div className="flex items-center justify-between">
                                        <span className="text-muted-foreground">Connection</span>
                                        <span className="flex items-center gap-1 text-foreground">
                                            {getConnectionIcon(printer.connectionType)}
                                            {printer.connectionType}
                                        </span>
                                    </div>
                                    {printer.ipAddress && (
                                        <div className="flex items-center justify-between">
                                            <span className="text-muted-foreground">IP Address</span>
                                            <span className="font-mono text-foreground">{printer.ipAddress}:{printer.port}</span>
                                        </div>
                                    )}
                                    <div className="flex items-center justify-between">
                                        <span className="text-muted-foreground">Paper Width</span>
                                        <span className="text-foreground">{printer.paperWidth}mm</span>
                                    </div>
                                    <div className="flex items-center justify-between">
                                        <span className="text-muted-foreground">Status</span>
                                        <Badge variant={printer.active ? 'success' : 'secondary'}>
                                            {printer.active ? 'Active' : 'Inactive'}
                                        </Badge>
                                    </div>
                                </div>

                                <div className="flex gap-2">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        className="flex-1"
                                        onClick={() => testMutation.mutate(printer.id)}
                                        disabled={!printer.active}
                                    >
                                        <TestTube className="mr-1 h-4 w-4" />
                                        Test
                                    </Button>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        className="flex-1"
                                        onClick={() => navigate(`/printers/${printer.id}/edit`)}
                                    >
                                        <Settings className="mr-1 h-4 w-4" />
                                        Edit
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        onClick={() => {
                                            if (confirm(`Delete ${printer.name}?`)) deleteMutation.mutate(printer.id);
                                        }}
                                    >
                                        <Trash2 className="h-4 w-4 text-destructive" />
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}

export { PrinterListPage as default };
