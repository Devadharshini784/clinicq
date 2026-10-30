import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import useLiveQueue from '../useLiveQueue';

export default function TodayList({ doctors }) {
  const [doctorId, setDoctorId] = useState('');
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const {
    snapshot,
    connected,
    error: liveError,
  } = useLiveQueue(doctorId);

  const loadVisits = useCallback(async () => {
    if (!doctorId) {
      setVisits([]);
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await api(
        `/api/visits/today?doctorId=${encodeURIComponent(doctorId)}`,
      );

      setVisits(Array.isArray(data) ? data : data.content ?? []);
    } catch (err) {
      setError(err.message || "Couldn't load today's queue.");
    } finally {
      setLoading(false);
    }
  }, [doctorId]);

  useEffect(() => {
    loadVisits();
  }, [loadVisits]);

  // Refresh the list whenever a new live queue snapshot arrives.
  useEffect(() => {
    if (snapshot) {
      loadVisits();
    }
  }, [snapshot, loadVisits]);

  const statusStyles = {
    WAITING: 'bg-amber-100 text-amber-800',
    IN_PROGRESS: 'bg-blue-100 text-blue-800',
    DONE: 'bg-green-100 text-green-800',
    SKIPPED: 'bg-slate-100 text-slate-600',
  };

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-semibold">Today's Queue</h2>
          <p className="mt-1 text-sm text-slate-500">
            View patients registered with the selected doctor.
          </p>
        </div>

        <span
          className={`inline-flex items-center gap-2 text-sm ${
    connected ? 'text-green-700' : 'text-amber-700'
}`}
        >
          <span
            className={`h-2.5 w-2.5 rounded-full ${
    connected ? 'bg-green-500' : 'bg-amber-500'
}`}
          />
          {connected ? 'Live connected' : 'Connecting...'}
        </span>
      </div>

      <div className="mt-5">
        <label
          htmlFor="queueDoctor"
          className="mb-1 block text-sm font-medium text-slate-700"
        >
          Select doctor
        </label>

        <select
          id="queueDoctor"
          value={doctorId}
          onChange={(event) => setDoctorId(event.target.value)}
          className="w-full rounded-lg border border-slate-300 bg-white px-3 py-3 sm:max-w-sm"
        >
          <option value="">Choose a doctor</option>

          {doctors.map((doctor) => (
            <option key={doctor.id} value={String(doctor.id)}>
              {doctor.name}
            </option>
          ))}
        </select>
      </div>

      {error && (
        <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">
          {error}
        </p>
      )}

      {liveError && (
        <p className="mt-3 text-sm text-amber-700">{liveError}</p>
      )}

      <div className="mt-5 overflow-x-auto">
        {loading ? (
          <p className="py-6 text-center text-slate-500">
            Loading today's queue...
          </p>
        ) : visits.length === 0 ? (
          <p className="py-6 text-center text-slate-500">
            {doctorId
              ? "No visits found for this doctor today."
              : 'Select a doctor to view the queue.'}
          </p>
        ) : (
          <table className="w-full min-w-[500px] text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500">
                <th className="px-3 py-3 font-medium">Token</th>
                <th className="px-3 py-3 font-medium">Patient</th>
                <th className="px-3 py-3 font-medium">Status</th>
              </tr>
            </thead>

            <tbody>
              {visits.map((visit) => (
                <tr
                  key={visit.id ?? visit.publicId}
                  className="border-b border-slate-100 last:border-0"
                >
                  <td className="px-3 py-4 font-semibold text-teal-700">
                    {visit.tokenNumber}
                  </td>

                  <td className="px-3 py-4">
                    {visit.patientName ?? visit.name ?? 'Patient'}
                  </td>

                  <td className="px-3 py-4">
                    <span
                      className={`inline-block rounded-full px-3 py-1 text-xs font-semibold ${
    statusStyles[visit.status] ?? 'bg-slate-100 text-slate-700'
}`}
                    >
                      {visit.status ?? 'UNKNOWN'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <button
        type="button"
        onClick={loadVisits}
        disabled={loading || !doctorId}
        className="mt-4 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-slate-50 disabled:opacity-50"
      >
        Refresh list
      </button>
    </section>
  );
}