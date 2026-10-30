const AUTH_KEY = 'clinicq_auth';

export function getAuth() {
    try {
        return JSON.parse(localStorage.getItem(AUTH_KEY));
    } catch {
        return null;
    }
}

export function saveAuth(auth) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
}

export function clearAuth() {
    localStorage.removeItem(AUTH_KEY);
}

export async function api(path, { method = 'GET', body, auth = true } = {}) {
    const headers = {};

    if (body) {
        headers['Content-Type'] = 'application/json';
    }

    const saved = auth ? getAuth() : null;

    if (saved?.token) {
        headers.Authorization = `Bearer ${saved.token}`;
    }

    const res = await fetch(path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });

    if (res.status === 401 && saved) {
        clearAuth();
        window.location.href = '/login';
        throw new Error('Session expired. Please log in again.');
    }

    let data = null;

    try {
        data = await res.json();
    } catch {}

    if (!res.ok) {
        const err = new Error(data?.error || `Request failed (${res.status})`);
        err.status = res.status;
        throw err;
    }

    return data;
}