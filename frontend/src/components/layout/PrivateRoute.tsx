import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth.store';

interface PrivateRouteProps {
    children: React.ReactNode;
    roles?: string[];
}

export function PrivateRoute({ children, roles }: PrivateRouteProps) {
    const location = useLocation();
    const { isAuthenticated, user } = useAuthStore();

    if (!isAuthenticated) {
        // Redirect to login, preserving the intended destination
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // Check role-based access if roles are specified
    if (roles && user && !user.roles.some(r => roles.includes(r))) {
        // User doesn't have required role
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
}
