import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { BrainCircuit, Mail, Lock, ArrowRight, AlertCircle, WifiOff } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuth } from '../../contexts/AuthContext';
import { AuthCanvas } from '../../components/landing/AuthCanvas';
import { MagneticButton } from '../../components/landing/MagneticButton';
import { TiltCard } from '../../components/landing/TiltCard';
import { PageWrapper } from '../../components/ui/PageWrapper';
import { staggerContainer, fadeUp } from '../../lib/motion';

export function Login() {
  const navigate = useNavigate();
  const { login, isLoading } = useAuth();

  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError('');
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await login(formData);
      navigate('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed. Please try again.');
    }
  };

  return (
    <PageWrapper>
      <div className="relative min-h-screen flex items-center justify-center px-4 py-12 bg-[#05060A] text-white selection:bg-brand-500/30">
        
        {/* ── Custom Auth Interactive 3D Cyber Background ── */}
        <AuthCanvas />

        {/* ── Awwwards 3D Tilt Glass Card ── */}
        <motion.div
          variants={staggerContainer}
          initial="initial"
          animate="animate"
          className="relative z-10 w-full max-w-md"
        >
          <TiltCard className="p-8 sm:p-10 border border-white/15 bg-[#090D16]/80 backdrop-blur-2xl shadow-glow-indigo">
            {/* Top Brand Logo */}
            <motion.div variants={fadeUp} className="flex flex-col items-center mb-8 text-center">
              <Link to="/">
                <motion.div
                  className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 via-teal-500 to-cyan-500 shadow-glow-indigo mb-4 cursor-pointer"
                  whileHover={{ scale: 1.08, rotate: 5 }}
                  transition={{ type: 'spring', stiffness: 300 }}
                >
                  <BrainCircuit size={28} className="text-white" />
                </motion.div>
              </Link>
              <h1 className="text-3xl font-black tracking-tight text-white">
                Welcome back
              </h1>
              <p className="mt-1.5 text-sm text-ink-400">Sign in to access your interview dashboard</p>
            </motion.div>

            {/* Error Banner */}
            <AnimatePresence>
              {error && (
                <motion.div
                  key="error"
                  initial={{ opacity: 0, y: -8, height: 0 }}
                  animate={{ opacity: 1, y: 0, height: 'auto' }}
                  exit={{ opacity: 0, y: -8, height: 0 }}
                  transition={{ duration: 0.25 }}
                  className="mb-6 rounded-xl p-4 border bg-red-950/40 border-red-800/50 flex gap-3 text-red-300"
                >
                  <AlertCircle size={18} className="shrink-0 mt-0.5 text-red-400" />
                  <p className="text-sm">{error}</p>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Form */}
            <motion.form
              variants={staggerContainer}
              onSubmit={handleLogin}
              className="space-y-5"
              noValidate
            >
              <motion.div variants={fadeUp}>
                <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-2">
                  Email Address
                </label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={isLoading}
                  autoComplete="email"
                  icon={<Mail size={16} className="text-brand-400" />}
                />
              </motion.div>

              <motion.div variants={fadeUp}>
                <div className="flex items-center justify-between mb-2">
                  <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest">
                    Password
                  </label>
                  <Link
                    to="/forgot-password"
                    className="text-xs font-medium text-brand-400 hover:text-white transition-colors"
                  >
                    Forgot password?
                  </Link>
                </div>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  disabled={isLoading}
                  autoComplete="current-password"
                  icon={<Lock size={16} className="text-brand-400" />}
                />
              </motion.div>

              <motion.div variants={fadeUp} className="pt-2">
                <MagneticButton
                  type="submit"
                  disabled={isLoading}
                  variant="primary"
                  className="w-full h-12 text-sm font-bold gap-2 shadow-glow-indigo"
                >
                  Sign In <ArrowRight size={16} />
                </MagneticButton>
              </motion.div>
            </motion.form>

            {/* Footer */}
            <motion.div
              variants={fadeUp}
              className="mt-8 text-center border-t border-white/10 pt-6"
            >
              <p className="text-sm text-ink-400">
                Don't have an account?{' '}
                <Link to="/register" className="font-bold text-brand-400 hover:text-white transition-colors">
                  Create one free
                </Link>
              </p>
            </motion.div>
          </TiltCard>
        </motion.div>
      </div>
    </PageWrapper>
  );
}