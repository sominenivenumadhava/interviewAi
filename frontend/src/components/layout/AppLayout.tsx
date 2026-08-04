import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { BrainCircuit } from 'lucide-react';
import { TopNav } from './TopNav';
import { useRequireAuth } from '../../contexts/AuthContext';
import { AnimatedBackground } from '../ui/AnimatedBackground';
import { PageWrapper } from '../ui/PageWrapper';

// ─── Page transition variant ──────────────────────────────────────────────────
const pageVariants = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  exit:    { opacity: 0, y: -6 },
};
const pageTransition = { duration: 0.3, ease: [0.25, 0.1, 0.25, 1] };

// ─── Premium Loading Screen ───────────────────────────────────────────────────
function LoadingScreen() {
  return (
    <AnimatedBackground variant="dashboard" className="flex min-h-screen items-center justify-center">
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.4, type: 'spring' }}
        className="flex flex-col items-center gap-8"
      >
        {/* Logo */}
        <motion.div
          className="flex h-20 w-20 items-center justify-center rounded-3xl bg-gradient-to-br from-brand-500 to-cyan-500 shadow-glow-lg"
          animate={{ scale: [1, 1.06, 1] }}
          transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
        >
          <BrainCircuit size={36} className="text-white" />
        </motion.div>

        {/* Dots */}
        <div className="flex gap-2">
          {[0, 1, 2].map((i) => (
            <motion.div
              key={i}
              className="h-2 w-2 rounded-full bg-brand-500"
              animate={{ opacity: [0.3, 1, 0.3], scale: [0.8, 1.2, 0.8] }}
              transition={{ duration: 1.2, repeat: Infinity, delay: i * 0.2, ease: 'easeInOut' }}
            />
          ))}
        </div>

        <p className="text-sm text-ink-400 font-medium">Loading InterviewAI…</p>
      </motion.div>
    </AnimatedBackground>
  );
}

// ─── AppLayout ────────────────────────────────────────────────────────────────
export function AppLayout() {
  const auth = useRequireAuth();

  if (auth.isLoading) return <LoadingScreen />;
  if (!auth.isAuthenticated) return <Navigate to="/login" replace />;

  return (
    <AnimatedBackground variant="dashboard" className="min-h-screen">
      <TopNav />
      <PageWrapper>
        <motion.main
          variants={pageVariants}
          initial="initial"
          animate="animate"
          exit="exit"
          transition={pageTransition}
          className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8"
        >
          <Outlet />
        </motion.main>
      </PageWrapper>
    </AnimatedBackground>
  );
}
