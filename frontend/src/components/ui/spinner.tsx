import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

interface SpinnerProps {
    size?: 'sm' | 'default' | 'lg';
    className?: string;
}

export function Spinner({ size = 'default', className }: SpinnerProps) {
    const sizes = {
        sm: 'h-4 w-4',
        default: 'h-6 w-6',
        lg: 'h-8 w-8',
    };

    return (
        <Loader2 className={cn('animate-spin text-muted-foreground', sizes[size], className)} />
    );
}

interface LoadingPageProps {
    message?: string;
}

export function LoadingPage({ message = 'Loading...' }: LoadingPageProps) {
    return (
        <div className="flex h-screen items-center justify-center">
            <div className="text-center">
                <Spinner size="lg" className="mx-auto mb-4" />
                <p className="text-muted-foreground">{message}</p>
            </div>
        </div>
    );
}
