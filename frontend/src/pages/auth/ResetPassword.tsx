import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { BrainCircuit, Lock, ArrowLeft, AlertCircle, CheckCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import apiClient, { API_ENDPOINTS } from '../../lib/apiClient';
import { AuthCanvas } from '../../components/landing/AuthCanvas';
import { TiltCard } from '../../components/landing/TiltCard';
import { PageWrapper } from '../../components/ui/PageWrapper';
import { staggerContainer, fadeUp } from '../../lib/motion';

export function ResetPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!token.trim()) {
      setError('Reset token is missing or invalid. Request a new link from Forgot Password.');
      return;
    }
    if (!newPassword || newPassword.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      await apiClient.post(
        API_ENDPOINTS.AUTH.RESET_PASSWORD,
        {
          token: token.trim(),
          newPassword,
          confirmPassword,
        },
        { skipAuth: true }
      );
      setSuccess(true);
      setTimeout(() => navigate('/login'), 1800);
    } catch (err: any) {
      setError(err?.message || 'Failed to reset password. The link may have expired.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageWrapper>
      <div className="relative min-h-screen flex items-center justify-center px-4 py-12 bg-[#05060A] text-white selection:bg-brand-500/30">
        <AuthCanvas />

        <motion.div
          variants={staggerContainer}
          initial="initial"
          animate="animate"
          className="relative z-10 w-full max-w-md"
        >
          <TiltCard className="p-8 sm:p-10 border border-white/15 bg-[#090D16]/80 backdrop-blur-2xl shadow-glow-indigo">
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
                {success ? 'Password updated' : 'Reset password'}
              </h1>
              <p className="mt-1.5 text-sm text-ink-400">
                {success
                  ? 'Redirecting you to login…'
                  : 'Choose a new password for your account.'}
              </p>
            </motion.div>

            {success ? (
              <motion.div variants={fadeUp} className="text-center space-y-6">
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-emerald-500/15 text-emerald-400">
                  <CheckCircle size={28} />
                </div>
                <Link
                  to="/login"
                  className="inline-flex items-center gap-2 text-sm font-semibold text-brand-400 hover:text-brand-300"
                >
                  <ArrowLeft size={16} />
                  Go to Login
                </Link>
              </motion.div>
            ) : (
              <form onSubmit={handleSubmit} noValidate className="space-y-5">
                <AnimatePresence>
                  {error && (
                    <motion.div
                      key="error"
                      initial={{ opacity: 0, y: -8, height: 0 }}
                      animate={{ opacity: 1, y: 0, height: 'auto' }}
                      exit={{ opacity: 0, y: -8, height: 0 }}
                      className="rounded-xl p-4 border bg-red-950/40 border-red-800/50 flex gap-3 text-red-300 text-sm"
                    >
                      <AlertCircle size={18} className="shrink-0 mt-0.5" />
                      <span>{error}</span>
                    </motion.div>
                  )}
                </AnimatePresence>

                {!token && (
                  <div className="rounded-xl p-4 border border-amber-800/50 bg-amber-950/30 text-amber-200 text-sm">
                    No reset token found in the URL. Open the link from your email, or{' '}
                    <Link to="/forgot-password" className="underline font-semibold text-brand-300">
                      request a new one
                    </Link>
                    .
                  </div>
                )}

                <motion.div variants={fadeUp}>
                  <label
                    htmlFor="reset-new-password"
                    className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-2"
                  >
                    New Password
                  </label>
                  <Input
                    id="reset-new-password"
                    type="password"
                    placeholder="Min 8 characters"
                    value={newPassword}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewPassword(e.target.value)}
                    autoComplete="new-password"
                    required
                    disabled={loading}
                    icon={<Lock size={18} className="text-brand-400" />}
                  />
                </motion.div>

                <motion.div variants={fadeUp}>
                  <label
                    htmlFor="reset-confirm-password"
                    className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-2"
                  >
                    Confirm Password
                  </label>
                  <Input
                    id="reset-confirm-password"
                    type="password"
                    placeholder="Repeat new password"
                    value={confirmPassword}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                    required
                    disabled={loading}
                    icon={<Lock size={18} className="text-brand-400" />}
                  />
                </motion.div>

                <motion.div variants={fadeUp}>
                  <Button
                    id="reset-submit-btn"
                    type="submit"
                    disabled={loading || !token}
                    isLoading={loading}
                    variant="gradient"
                    className="w-full h-12 text-sm font-bold gap-2 shadow-glow-indigo"
                  >
                    Reset Password
                  </Button>
                </motion.div>

                <motion.div variants={fadeUp} className="text-center">
                  <Link
                    to="/login"
                    className="inline-flex items-center gap-2 text-sm text-ink-500 hover:text-ink-300"
                  >
                    <ArrowLeft size={14} />
                    Back to Login
                  </Link>
                </motion.div>
              </form>
            )}
          </TiltCard>
        </motion.div>
      </div>
    </PageWrapper>
  );
}
