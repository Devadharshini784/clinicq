import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth';
import ProtectedRoute from './components/ProtectedRoute';
import StaffHeader from './components/StaffHeader';
import LoginPage from './pages/LoginPage';
import PatientPage from './pages/PatientPage';
import ReceptionPage from './pages/ReceptionPage';
import DoctorPage from './pages/DoctorPage';

function HomePage() {
  return (
    <main className="mx-auto max-w-3xl p-6">
      <h1 className="text-3xl font-bold text-teal-700">ClinicQ</h1>
      <p className="mt-2 text-slate-600">
        Live token and queue system for small clinics.
      </p>

      <nav className="mt-6 flex flex-wrap gap-4">
        <Link className="text-teal-700 underline" to="/login">
          Staff Login
        </Link>
      </nav>
    </main>
  );
}

function StaffLayout() {
  return (
    <>
      <StaffHeader />
      <main className="p-6">
        <h1 className="text-2xl font-bold">Staff Dashboard</h1>
        <p className="mt-2 text-slate-600">
          Your staff page will be added in the next commits.
        </p>
      </main>
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute roles={['RECEPTIONIST', 'ADMIN']} />}>
          <Route path="/reception" element={<ReceptionPage />} />
        </Route>

        <Route element={<ProtectedRoute roles={['DOCTOR']} />}>
          <Route path="/doctor" element={<DoctorPage />} />
        </Route>

        <Route path="/q/:publicId" element={<PatientPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}
