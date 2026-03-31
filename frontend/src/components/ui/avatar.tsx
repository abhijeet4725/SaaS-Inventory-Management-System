import { cn } from '@/lib/utils';

interface AvatarProps {
    src?: string;
    alt?: string;
    fallback: string;
    size?: 'sm' | 'default' | 'lg';
    className?: string;
}

export function Avatar({ src, alt, fallback, size = 'default', className }: AvatarProps) {
    const sizes = {
        sm: 'h-8 w-8 text-xs',
        default: 'h-10 w-10 text-sm',
        lg: 'h-12 w-12 text-base',
    };

    if (src) {
        return (
            <img
                src={src}
                alt={alt || fallback}
                className={cn('rounded-full object-cover', sizes[size], className)}
            />
        );
    }

    return (
        <div
            className={cn(
                'flex items-center justify-center rounded-full bg-primary font-medium text-primary-foreground',
                sizes[size],
                className
            )}
        >
            {fallback.toUpperCase().slice(0, 2)}
        </div>
    );
}
