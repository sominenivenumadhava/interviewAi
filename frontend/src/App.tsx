import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation, Link } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import { ThemeProvider } from './contexts/ThemeContext';
import { AuthProvider } from './contexts/AuthContext';
import { InterviewSessionProvider } from './contexts/InterviewSessionContext';
import { AppLayout } from './components/layout/AppLayout';
import { useLenis } from './hooks/useLenis';
// Pages
import { Landing } from './pages/Landing';
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { ForgotPassword } from './pages/auth/ForgotPassword';
import { ResetPassword } from './pages/auth/ResetPassword';
import { OAuthCallback } from './pages/auth/OAuthCallback';
import { Dashboard } from './pages/Dashboard';
import { JobSearchDashboard } from './pages/jobs/JobSearchDashboard';
import { ResumeUpload } from './pages/ResumeUpload';
import { CompanySelect } from './pages/interview/CompanySelect';
import { RoleSelect } from './pages/interview/RoleSelect';
import { Config } from './pages/interview/Config';
import { Room } from './pages/interview/Room';
import { Evaluation } from './pages/Evaluation';
import { SkillGap } from './pages/SkillGap';
import { Roadmap } from './pages/Roadmap';
import { History } from './pages/History';
import { Analytics } from './pages/Analytics';
import { Profile } from './pages/Profile';
import { Admin } from './pages/Admin';

function LegalPlaceholder({ title }: { title: string }) {
  return (
    <div className="min-h-screen bg-obsidian-950 text-white flex flex-col items-center justify-center px-6 text-center">
      <h1 className="font-display text-3xl font-bold mb-3">{title}</h1>
      <p className="text-ink-400 max-w-md mb-8">
        This page is a placeholder. Full legal copy will be published here soon.
      </p>
      <Link to="/" className="text-brand-400 hover:text-brand-300 text-sm font-semibold">
        ← Back to home
      </Link>
    </div>
  );
}

// ─── Animated Routes ──────────────────────────────────────────────────────────
function AnimatedRoutes() {
  const location = useLocation();

  // Key only the animated shell — keeps React Router stable and makes Link clicks reliable
  return (
    <AnimatePresence mode="wait" initial={false}>
      <Routes location={location}>
        {/* Public Routes */}
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/auth/callback" element={<OAuthCallback />} />
        <Route path="/terms" element={<LegalPlaceholder title="Terms of Service" />} />
        <Route path="/privacy" element={<LegalPlaceholder title="Privacy Policy" />} />

        {/* App Shell Routes */}
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/jobs" element={<JobSearchDashboard />} />
          <Route path="/resume" element={<ResumeUpload />} />
          <Route path="/interview/company" element={<CompanySelect />} />
          <Route path="/interview/role" element={<RoleSelect />} />
          <Route path="/interview/config" element={<Config />} />
          <Route path="/evaluation" element={<Evaluation />} />
          <Route path="/skill-gap" element={<SkillGap />} />
          <Route path="/roadmap" element={<Roadmap />} />
          <Route path="/history" element={<History />} />
          <Route path="/analytics" element={<Analytics />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/admin" element={<Admin />} />
        </Route>

        {/* Standalone Routes */}
        <Route path="/interview/room" element={<Room />} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AnimatePresence>
  );
}

// ─── Root with Lenis ──────────────────────────────────────────────────────────
function AppRoot() {
  useLenis();
  return <AnimatedRoutes />;
}

export function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <InterviewSessionProvider>
          <BrowserRouter>
            <AppRoot />
          </BrowserRouter>
        </InterviewSessionProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}
