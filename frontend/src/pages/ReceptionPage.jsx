import { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { api } from '../api';
import StaffHeader from '../components/StaffHeader';

export default function ReceptionPage() {
  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');

  const [loadingDoctors, setLoadingDoctors] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    async function loadDoctors() {
      try {
        const data = await api('/api/doctors');
        const list = Array.isArray(data) ? data : data.content ?? [];

        setDoctors(list);

        if (list.length > 0) {
          setDoctorId(String(list[0].id));
        }
      } catch (err) {
        setError(err.message || 'Unable to load doctors.');
      } finally {
        setLoadingDoctors(false);
      }
    }

    loadDoctors();
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess(null);
    setBusy(true);

    try {
      const visit = await api('/api/visits', {
        method: 'POST',
        body: {
          doctorId: Number(doctorId),
          name: name.trim(),
          phone: phone.trim(),
        },
      });

      setSuccess(visit);
      setName('');
      setPhone('');
    } catch (err) {
      setError(err.message || 'Unable to register the patient.');
    } finally {
      setBusy(false);
    }
  }

  const trackingUrl = success?.publicId
    ? `${window.location.origin}/q/${success.publicId}`
    : '';

  return (
    <div className="min-h-screen">
      <StaffHeader />

      <main className="mx-auto max-w-5xl space-y-6 p-4 sm:p-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">
            Reception Dashboard
          </h1>
          <p className="mt-2 text-slate-600">
            Register patients and give them a link to follow their queue.
          </p>
        </div>

        {error && (
          <div
            role="alert"
            className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700"
          >
            {error}
          </div>
        )}

        <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
          <h2 className="text-xl font-semibold">Register a patient</h2>

          <form onSubmit={handleSubmit} className="mt-5 space-y-5">
            <div>
              <label
                htmlFor="patientName"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Patient name
              </label>

              <input
                id="patientName"
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
                maxLength={100}
                placeholder="Enter patient's name"
                className="w-full rounded-lg border border-slate-300 px-3 py-3 outline-none focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
              />
            </div>

            <div>
              <label
                htmlFor="patientPhone"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Phone number
              </label>

              <input
                id="patientPhone"
                type="tel"
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
                required
                maxLength={20}
                placeholder="Enter phone number"
                className="w-full rounded-lg border border-slate-300 px-3 py-3 outline-none focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
              />
            </div>

            <div>
              <label
                htmlFor="doctor"
                className="mb-1 block text-sm font-medium text-slate-700"
              >
                Select doctor
              </label>

              <select
                id="doctor"
                value={doctorId}
                onChange={(event) => setDoctorId(event.target.value)}
                required
                disabled={loadingDoctors || doctors.length === 0}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-3 outline-none focus:border-teal-600 focus:ring-2 focus:ring-teal-100"
              >
                <option value="">
                  {loadingDoctors ? 'Loading doctors...' : 'Select a doctor'}
                </option>

                {doctors.map((doctor) => (
                  <option key={doctor.id} value={String(doctor.id)}>
                    {doctor.name}
                    {doctor.specialization
                      ? ` — ${doctor.specialization}`
                      : ''}
                  </option>
                ))}
              </select>

              {!loadingDoctors && doctors.length === 0 && (
                <p className="mt-2 text-sm text-amber-700">
                  No doctors are available. Please check the doctor records.
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={busy || loadingDoctors || !doctorId}
              className="w-full rounded-lg bg-teal-700 px-4 py-3 font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
            >
              {busy ? 'Registering...' : 'Register patient'}
            </button>
          </form>
        </section>

        {success && (
          <section className="rounded-2xl border border-green-200 bg-white p-5 shadow-sm sm:p-6">
            <div className="rounded-lg bg-green-50 p-4 text-green-800">
              <h2 className="font-semibold">Patient registered successfully!</h2>
              <p className="mt-1 text-sm">
                Give the patient this QR code to track their queue.
              </p>
            </div>

            <div className="mt-6 grid gap-6 sm:grid-cols-2">
              <div className="space-y-3">
                <p className="text-sm text-slate-500">Patient</p>
                <p className="text-lg font-semibold">
                  {success.patientName ?? success.name ?? name ?? 'Registered patient'}
                </p>

                <p className="text-sm text-slate-500">Doctor</p>
                <p className="font-medium">
                  {success.doctorName ?? 'Assigned doctor'}
                </p>

                <p className="text-sm text-slate-500">Token number</p>
                <p className="text-4xl font-bold text-teal-700">
                  {success.tokenNumber ?? '—'}
                </p>

                <p className="text-sm text-slate-500">Status</p>
                <p className="font-medium">{success.status ?? 'WAITING'}</p>
              </div>

              <div className="flex flex-col items-center justify-center rounded-xl bg-slate-50 p-5">
                <QRCodeSVG value={trackingUrl} size={190} level="M" />

                <p className="mt-4 text-center text-sm font-medium">
                  Scan to track your queue
                </p>

                <a
                  href={trackingUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="mt-3 break-all text-center text-sm text-teal-700 underline"
                >
                  Open tracking page
                </a>

                <button
                  type="button"
                  onClick={() => navigator.clipboard.writeText(trackingUrl)}
                  className="mt-4 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-white"
                >
                  Copy tracking link
                </button>
              </div>
            </div>

            <p className="mt-5 text-sm text-slate-500">
              The tracking link uses this browser's current origin. For a
              patient using another phone, the frontend must be accessible
              from that phone on the network or through a deployed URL.
            </p>
          </section>
        )}
      </main>
    </div>
  );
}
