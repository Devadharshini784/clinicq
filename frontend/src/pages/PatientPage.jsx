import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';
import useLiveQueue from '../useLiveQueue';

export default function PatientPage() {
  const { publicId } = useParams();

  const [visit, setVisit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const doctorId = visit?.doctorId;
  const { snapshot, connected, error: liveError } = useLiveQueue(doctorId);

  const loadVisit = useCallback(async () => {
    try {
      setError('');

      // This endpoint must be public because patients do not log in.
      const data = await api(
        `/api/public/visits/${encodeURIComponent(publicId)}`,
        { auth: false },
      );

      setVisit(data);
    } catch (err) {
      setError(err.message || 'Unable to load your queue details.');
    } finally {
      setLoading(false);
    }
  }, [publicId]);

  useEffect(() => {
    loadVisit();
  }, [loadVisit]);

  const waitingTokens = snapshot?.waitingTokens ?? [];

  const waitingIndex = waitingTokens.findIndex(
    (item) => item.publicId === publicId,
  );

  const isCurrentPatient = snapshot?.current?.publicId === publicId;

  const ahead = isCurrentPatient
    ? 0
    : waitingIndex >= 0
      ? waitingIndex + (snapshot?.current ? 1 : 0)
      : null;

  const tokenNumber = visit?.tokenNumber;
  const isFinished = ['DONE', 'SKIPPED'].includes(visit?.status);

  const positionText = isCurrentPatient
    ? 'It is your turn!'
    : ahead === 0
      ? "You're next!"
      : ahead !== null
        ? `${ahead} patient${ahead === 1 ? '' : 's'} ahead`
        : null;

  const waitMinutes = isCurrentPatient
    ? 0
    : ahead !== null
      ? ahead * (snapshot?.averageServiceMinutes ?? 10)
      : visit?.estimatedWaitMinutes;

  useEffect(() => {
    if (!snapshot || !visit) return;

    const current = snapshot.current?.publicId === publicId;
    if (current && visit.status !== 'IN_PROGRESS') {
      setVisit((previous) =>
        previous ? { ...previous, status: 'IN_PROGRESS' } : previous,
      );
    }
  }, [snapshot, publicId, visit]);

  return (
    <main className="mx-auto min-h-screen max-w-lg p-4 sm:flex sm:items-center">
      <section className="w-full overflow-hidden rounded-2xl bg-white shadow-lg">
        <header className="bg-teal-700 p-6 text-white">
          <p className="text-sm font-medium text-teal-100">
            Live clinic queue
          </p>
          <h1 className="mt-1 text-3xl font-bold">ClinicQ</h1>
          <p className="mt-2 text-sm text-teal-100">
            Track your turn without waiting at the reception desk.
          </p>
        </header>

        <div className="space-y-5 p-6">
          {loading ? (
            <p className="text-slate-500">Loading your queue details...</p>
          ) : error ? (
            <div className="rounded-lg bg-red-50 p-4 text-red-700">
              {error}
            </div>
          ) : (
            <>
              <div>
                <p className="text-sm text-slate-500">Patient</p>
                <h2 className="text-xl font-semibold">
                  {visit?.patientName ?? visit?.name ?? 'Patient'}
                </h2>
                <p className="mt-1 text-sm text-slate-500">
                  {visit?.doctorName ? `Doctor: ${visit.doctorName}` : ''}
                </p>
              </div>

              <div className="rounded-xl bg-teal-50 p-5 text-center">
                <p className="text-sm font-medium text-teal-800">
                  Your token number
                </p>
                <p className="mt-2 text-6xl font-bold text-teal-700">
                  {tokenNumber ?? '—'}
                </p>
              </div>

              {isFinished ? (
                <div className="rounded-lg bg-slate-100 p-4 text-center">
                  <p className="font-semibold">
                    {visit.status === 'DONE'
                      ? 'Your visit is complete.'
                      : 'Your token was skipped.'}
                  </p>
                </div>
              ) : (
                <div className="rounded-xl border border-slate-200 p-5">
                  <p className="text-sm text-slate-500">Queue status</p>
                  <p className="mt-1 text-xl font-semibold text-slate-900">
                    {isCurrentPatient
                      ? 'Please come in now!'
                      : positionText ?? visit?.status ?? 'Waiting'}
                  </p>

                  {waitMinutes !== null && waitMinutes !== undefined && (
                    <p className="mt-2 text-sm text-slate-600">
                      Estimated wait: {waitMinutes} minute
                      {waitMinutes === 1 ? '' : 's'}
                    </p>
                  )}
                </div>
              )}

              <div className="flex items-center gap-2 text-sm">
                <span
                  className={`h-2.5 w-2.5 rounded-full ${
    connected ? 'bg-green-500' : 'bg-amber-500'
}`}
                />
                <span className="text-slate-600">
                  {connected ? 'Live updates connected' : 'Connecting to live updates...'}
                </span>
              </div>

              {liveError && (
                <p className="text-sm text-amber-700">{liveError}</p>
              )}

              <button
                onClick={loadVisit}
                className="w-full rounded-lg border border-slate-300 px-4 py-3 font-medium hover:bg-slate-50"
              >
                Refresh queue details
              </button>

              <p className="text-center text-xs text-slate-400">
                Queue estimates are approximate and may change.
              </p>
            </>
          )}
        </div>
      </section>
    </main>
  );
}
