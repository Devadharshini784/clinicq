import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';

export default function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [busy, setBusy] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');
        setBusy(true);

        try {
            const data = await api('/api/auth/login', {
                method: 'POST',
                body: { username, password },
                auth: false,
            });

            login(data);

            const destination =
                location.state?.from?.pathname ||
                (data.role === 'DOCTOR'
                    ? '/doctor'
                    : data.role === 'RECEPTIONIST' || data.role === 'ADMIN'
                        ? '/reception'
                        : '/');

            navigate(destination, { replace: true });
        } catch (err) {
            setError(err.message || 'Login failed. Please try again.');
        } finally {
            setBusy(false);
        }
    }

    return (
        <main className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
            <form
                onSubmit={handleSubmit}
                className="w-full max-w-md space-y-5 rounded-2xl bg-white p-6 shadow-lg sm:p-8"
            >
                <div className="text-center">
                    <h1 className="text-3xl font-bold text-teal-700">ClinicQ</h1>
                    <p className="mt-2 text-sm text-slate-500">
                        Staff sign in
                    </p>
                </div>

                {error && (
                    <div className="rounded-lg bg-red-50 p-3 text-sm text-red-700">
                        {error}
                    </div>
                )}

                <div>
                    <label
                        htmlFor="username"
                        className="mb-1 block text-sm font-medium"
                    >
                        Username
                    </label>
                    <input
                        id="username"
                        value={username}
                        onChange={(event) => setUsername(event.target.value)}
                        autoComplete="username"
                        required
                        className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    />
                </div>

                <div>
                    <label
                        htmlFor="password"
                        className="mb-1 block text-sm font-medium"
                    >
                        Password
                    </label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        autoComplete="current-password"
                        required
                        className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
                    />
                </div>

                <button
                    type="submit"
                    disabled={busy}
                    className="w-full rounded-lg bg-teal-700 px-4 py-3 font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                    {busy ? 'Signing in...' : 'Sign in'}
                </button>
            </form>
        </main>
    );
}