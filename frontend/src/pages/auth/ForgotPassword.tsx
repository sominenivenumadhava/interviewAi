import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { BrainCircuit, Mail, ArrowLeft, CheckCircle, AlertCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import apiClient, { API_ENDPOINTS } from '../../lib/apiClient';
import { AuthCanvas } from '../../components/landing/AuthCanvas';
import { TiltCard } from '../../components/landing/TiltCard';
import { PageWrapper } from '../../components/ui/PageWrapper';
import { staggerContainer, fadeUp } from '../../lib/motion';

export function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email.trim()) {
      setError('Please enter your email address.');
      return;
    }

    setLoading(true);
    try {
      await apiClient.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, {
        email: email.trim().toLowerCase(),
      });
      setSuccess(true);
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        'Something went wrong. Please try again.';
      setError(msg);
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
                {success ? 'Check your inbox' : 'Forgot password?'}
              </h1>
              <p className="mt-1.5 text-sm text-ink-400">
                {success
                  ? 'We sent a reset link if that email exists.'
                  : "Enter your email and we'll send a reset link."}
              </p>
            </motion.div>

            {success ? (
              <motion.div variants={fadeUp} className="text-center space-y-6">
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-emerald-500/15 text-emerald-400">
                  <CheckCircle size={28} />
                </div>
                <p className="text-sm text-ink-400 leading-relaxed">
                  If an account with <span className="text-brand-300 font-medium">{email}</span> exists,
                  we've sent a password reset link. It may take a few minutes to arrive.
                </p>
                <Link
                  to="/login"
                  className="inline-flex items-center gap-2 text-sm font-semibold text-brand-400 hover:text-brand-300"
                >
                  <ArrowLeft size={16} />
                  Back to Login
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

                <motion.div variants={fadeUp}>
                  <label
                    htmlFor="forgot-email"
                    className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-2"
                  >
                    Email Address
                  </label>
                  <Input
                    id="forgot-email"
                    type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                    autoComplete="email"
                    required
                    disabled={loading}
                    icon={<Mail size={18} className="text-brand-400" />}
                  />
                </motion.div>

                <motion.div variants={fadeUp}>
                  <Button
                    id="forgot-submit-btn"
                    type="submit"
                    disabled={loading}
                    isLoading={loading}
                    variant="gradient"
                    className="w-full h-12 text-sm font-bold gap-2 shadow-glow-indigo"
                  >
                    Send Reset Link
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
