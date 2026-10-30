import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';
import StaffHeader from '../components/StaffHeader';
import useLiveQueue from '../useLiveQueue';

function getQueueData(data) {
    return {
        current: data?.current ?? data?.currentVisit ?? data?.currentPatient ?? null,
        waiting: data?.waiting ?? data?.waitingVisits ?? data?.waitingTokens ?? [],
    };
}

function getPatientName(patient) {
    return patient?.patientName ?? patient?.name ?? 'Patient';
}

export default function DoctorPage() {
    const { auth } = useAuth();

    const [doctorId, setDoctorId] = useState(
        auth?.doctorId ?? auth?.doctor?.id ?? ''
    );
    const [queue, setQueue] = useState({ current: null, waiting: [] });
    const [loading, setLoading] = useState(true);
    const [busyAction, setBusyAction] = useState('');
    const [error, setError] = useState('');
    const [notice, setNotice] = useState('');

    const {
        snapshot,
        connected,
        error: liveError,
    } = useLiveQueue(doctorId);

    const loadQueue = useCallback(async () => {
        setLoading(true);
        setError('');

        try {
            const data = await api('/api/doctor/queue');

            if (!doctorId && data?.doctorId) {
                setDoctorId(data.doctorId);
            }

            setQueue(getQueueData(data));
        } catch (err) {
            setError(err.message || 'Could not load the queue.');
        } finally {
            setLoading(false);
        }
    }, [doctorId]);

    useEffect(() => {
        loadQueue();
    }, [loadQueue]);

    useEffect(() => {
        if (!snapshot) return;

        // Update immediately when a live queue snapshot arrives.
        setQueue(getQueueData(snapshot));
    }, [snapshot]);

    async function handleAction(action) {
        setBusyAction(action);
        setError('');
        setNotice('');

        try {
            await api(`/api/doctor/queue/${action.toLowerCase()}`, {
                method: 'POST',
            });

            setNotice(
                action === 'NEXT'
                    ? 'Moved to the next patient.'
                    : action === 'SKIP'
                        ? 'Patient skipped.'
                        : 'Consultation marked as done.'
            );

            await loadQueue();
        } catch (err) {
            setError(err.message || `Could not perform ${action.toLowerCase()}.`);
        } finally {
            setBusyAction('');
        }
    }

    const current = queue.current;
    const waiting = Array.isArray(queue.waiting) ? queue.waiting : [];

    return (
        <div className="min-h-screen bg-slate-50">
            <StaffHeader />

            <main className="mx-auto max-w-6xl space-y-6 p-4 sm:p-6">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">
                            Doctor Dashboard
                        </h1>
                        <p className="mt-1 text-sm text-slate-500">
                            Manage consultations and keep the queue moving.
                        </p>
                    </div>

                    <span
                        className={`inline-flex w-fit items-center gap-2 rounded-full px-3 py-2 text-sm font-medium ${
                            connected
                                ? 'bg-green-100 text-green-800'
                                : 'bg-amber-100 text-amber-800'
                        }`}
                    >
            <span
                className={`h-2.5 w-2.5 rounded-full ${
                    connected ? 'bg-green-500' : 'bg-amber-500'
                }`}
            />
                        {connected ? 'Live updates connected' : 'Connecting to live updates'}
          </span>
                </div>

                {error && (
                    <div
                        role="alert"
                        className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
                    >
                        {error}
                    </div>
                )}

                {liveError && (
                    <p className="text-sm text-amber-700">{liveError}</p>
                )}

                {notice && (
                    <div
                        role="status"
                        className="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800"
                    >
                        {notice}
                    </div>
                )}

                <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
                    <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                            <p className="text-sm font-medium text-teal-700">
                                CURRENT PATIENT
                            </p>

                            {loading ? (
                                <p className="mt-3 text-slate-500">Loading current patient...</p>
                            ) : current ? (
                                <>
                                    <h2 className="mt-2 text-2xl font-bold text-slate-900">
                                        {getPatientName(current)}
                                    </h2>
                                    <p className="mt-2 text-slate-600">
                                        Token number:{' '}
                                        <span className="font-bold text-teal-700">
                      {current.tokenNumber ?? current.token ?? '—'}
                    </span>
                                    </p>
                                    {current.phone && (
                                        <p className="mt-1 text-sm text-slate-500">
                                            Phone: {current.phone}
                                        </p>
                                    )}
                                </>
                            ) : (
                                <>
                                    <h2 className="mt-2 text-xl font-semibold text-slate-700">
                                        No active consultation
                                    </h2>
                                    <p className="mt-1 text-sm text-slate-500">
                                        Select Next when you're ready to call a patient.
                                    </p>
                                </>
                            )}
                        </div>

                        <div className="grid grid-cols-1 gap-3 sm:min-w-64">
                            <button
                                type="button"
                                onClick={() => handleAction('NEXT')}
                                disabled={loading || Boolean(busyAction)}
                                className="rounded-xl bg-teal-700 px-5 py-3 font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                {busyAction === 'NEXT' ? 'Calling next...' : 'Next Patient'}
                            </button>

                            <button
                                type="button"
                                onClick={() => handleAction('SKIP')}
                                disabled={loading || Boolean(busyAction) || !current}
                                className="rounded-xl border border-amber-300 bg-amber-50 px-5 py-3 font-semibold text-amber-800 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                {busyAction === 'SKIP' ? 'Skipping...' : 'Skip Patient'}
                            </button>

                            <button
                                type="button"
                                onClick={() => handleAction('DONE')}
                                disabled={loading || Boolean(busyAction) || !current}
                                className="rounded-xl border border-green-300 bg-green-50 px-5 py-3 font-semibold text-green-800 hover:bg-green-100 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                {busyAction === 'DONE' ? 'Saving...' : 'Mark as Done'}
                            </button>
                        </div>
                    </div>
                </section>

                <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <div className="flex items-center justify-between border-b border-slate-200 p-5 sm:px-6">
                        <div>
                            <h2 className="text-xl font-semibold text-slate-900">
                                Waiting List
                            </h2>
                            <p className="mt-1 text-sm text-slate-500">
                                Patients waiting for their consultation.
                            </p>
                        </div>

                        <span className="rounded-full bg-teal-50 px-3 py-1 text-sm font-bold text-teal-800">
              {waiting.length} waiting
            </span>
                    </div>

                    {loading ? (
                        <p className="p-8 text-center text-slate-500">
                            Loading waiting list...
                        </p>
                    ) : waiting.length === 0 ? (
                        <p className="p-8 text-center text-slate-500">
                            No patients are waiting right now.
                        </p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full min-w-[450px] text-left text-sm">
                                <thead className="bg-slate-50 text-slate-500">
                                <tr>
                                    <th className="px-5 py-3 font-medium">Token</th>
                                    <th className="px-5 py-3 font-medium">Patient</th>
                                    <th className="px-5 py-3 font-medium">Status</th>
                                </tr>
                                </thead>

                                <tbody>
                                {waiting.map((patient, index) => (
                                    <tr
                                        key={patient.id ?? patient.publicId ?? patient.tokenNumber ?? index}
                                        className="border-t border-slate-100"
                                    >
                                        <td className="px-5 py-4 font-semibold text-teal-700">
                                            {patient.tokenNumber ?? patient.token ?? '—'}
                                        </td>
                                        <td className="px-5 py-4 font-medium text-slate-800">
                                            {getPatientName(patient)}
                                        </td>
                                        <td className="px-5 py-4">
                        <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-800">
                          Waiting
                        </span>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    <div className="border-t border-slate-100 p-4">
                        <button
                            type="button"
                            onClick={loadQueue}
                            disabled={loading || Boolean(busyAction)}
                            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-slate-50 disabled:opacity-50"
                        >
                            Refresh Queue
                        </button>
                    </div>
                </section>
            </main>
        </div>
    );
}