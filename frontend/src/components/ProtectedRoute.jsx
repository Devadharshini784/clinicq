import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth';

export default function ProtectedRoute({ roles }) {
  const { auth } = useAuth();
  const location = useLocation();

  if (!auth?.token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles && !roles.includes(auth.role)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}