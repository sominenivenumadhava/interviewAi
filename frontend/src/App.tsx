import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
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

// ─── Animated Routes ──────────────────────────────────────────────────────────
function AnimatedRoutes() {
  const location = useLocation();

  return (
    <AnimatePresence mode="wait" initial={false}>
      <Routes location={location} key={location.pathname}>
        {/* Public Routes */}
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/auth/callback" element={<OAuthCallback />} />

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
        </Route>

        {/* Standalone Routes */}
        <Route path="/interview/room" element={<Room />} />
        <Route path="/admin" element={<Admin />} />

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