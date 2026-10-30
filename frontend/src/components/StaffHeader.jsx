import { Link } from 'react-router-dom';
import { useAuth } from '../auth';

export default function StaffHeader() {
    const { auth, logout } = useAuth();

    return (
        <header className="flex items-center justify-between border-b bg-white px-4 py-3 shadow-sm sm:px-6">
            <Link to="/" className="text-xl font-bold text-teal-700">
                ClinicQ
            </Link>

            <div className="flex items-center gap-3">
        <span className="hidden text-sm text-slate-600 sm:inline">
          {auth?.name} · {auth?.role}
        </span>

                <button
                    onClick={logout}
                    className="rounded-lg bg-slate-100 px-3 py-2 text-sm font-medium hover:bg-slate-200"
                >
                    Logout
                </button>
            </div>
        </header>
    );
}