import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { BrainCircuit, Mail, Lock, User, CheckCircle, AlertCircle, WifiOff, Loader2, ArrowRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuth } from '../../contexts/AuthContext';
import { ApiError, ApiFieldError } from '../../lib/apiClient';
import { AuthCanvas } from '../../components/landing/AuthCanvas';
import { TiltCard } from '../../components/landing/TiltCard';
import { PageWrapper } from '../../components/ui/PageWrapper';
import { staggerContainer, fadeUp } from '../../lib/motion';

// ─── Types ───────────────────────────────────────────────────────────────────

interface FormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  acceptTerms: boolean;
  acceptPrivacy: boolean;
}

interface ErrorState {
  general: string;
  fields: Record<string, string>;
  isNetworkError: boolean;
}

// ─── Component ───────────────────────────────────────────────────────────────

export function Register() {
  const navigate = useNavigate();
  const { register, isLoading } = useAuth();

  const [formData, setFormData] = useState<FormData>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false,
    acceptPrivacy: false,
  });

  const [error, setError] = useState<ErrorState>({
    general: '',
    fields: {},
    isNetworkError: false,
  });

  const [success, setSuccess] = useState(false);

  // ─── Helpers ─────────────────────────────────────────────────────────────

  const clearErrors = () => setError({ general: '', fields: {}, isNetworkError: false });

  const getFieldError = (field: string): string => error.fields[field] || '';

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    if (error.fields[name]) {
      setError(prev => ({
        ...prev,
        fields: { ...prev.fields, [name]: '' },
      }));
    }
  };

  // ─── Validation ──────────────────────────────────────────────────────────

  const validateForm = (): boolean => {
    const newFields: Record<string, string> = {};

    if (!formData.firstName.trim()) newFields.firstName = 'First name is required';
    if (!formData.lastName.trim()) newFields.lastName = 'Last name is required';
    if (!formData.email.trim()) {
      newFields.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newFields.email = 'Please enter a valid email address';
    }
    if (!formData.password) {
      newFields.password = 'Password is required';
    } else if (formData.password.length < 8) {
      newFields.password = 'Password must be at least 8 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(formData.password)) {
      newFields.password =
        'Password must include uppercase, lowercase, number, and special character (@$!%*?&)';
    }
    if (!formData.confirmPassword) {
      newFields.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      newFields.confirmPassword = 'Passwords do not match';
    }
    if (!formData.acceptTerms) newFields.acceptTerms = 'You must accept the Terms of Service';
    if (!formData.acceptPrivacy) newFields.acceptPrivacy = 'You must accept the Privacy Policy';

    if (Object.keys(newFields).length > 0) {
      setError({ general: 'Please fix the errors below', fields: newFields, isNetworkError: false });
      return false;
    }
    return true;
  };

  // ─── Submit ──────────────────────────────────────────────────────────────

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    clearErrors();

    if (!validateForm()) return;

    try {
      await register({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim().toLowerCase(),
        password: formData.password,
        confirmPassword: formData.confirmPassword,
        acceptTerms: formData.acceptTerms,
        acceptPrivacy: formData.acceptPrivacy,
      });

      setSuccess(true);
      setTimeout(() => navigate('/login'), 2500);

    } catch (err: unknown) {
      console.error('[Register] Caught error:', err);

      if (err instanceof Error && err.name === 'ApiError') {
        const apiErr = err as ApiError & { fieldErrors?: ApiFieldError[]; details?: ApiFieldError[] };
        const isNetworkError = (apiErr as any).code === 'CONNECTION_REFUSED' ||
                               (apiErr as any).code === 'TIMEOUT' ||
                               (apiErr as any).code === 'NETWORK_ERROR';

        const rawErrors: ApiFieldError[] = apiErr.fieldErrors || (apiErr as any).details || [];
        const serverFields: Record<string, string> = {};

        if (rawErrors.length > 0) {
          rawErrors.forEach((fe: ApiFieldError) => {
            const fieldMap: Record<string, string> = {
              email: 'email', password: 'password', confirmPassword: 'confirmPassword',
              firstName: 'firstName', lastName: 'lastName',
              acceptTerms: 'acceptTerms', acceptPrivacy: 'acceptPrivacy',
            };
            const localField = fieldMap[fe.field] || fe.field;
            serverFields[localField] = fe.message;
          });
        }

        setError({ general: apiErr.message, fields: serverFields, isNetworkError });
      } else {
        setError({
          general: err instanceof Error ? err.message : 'An unexpected error occurred.',
          fields: {},
          isNetworkError: false,
        });
      }
    }
  };

  // ─── Success Screen ───────────────────────────────────────────────────────

  if (success) {
    return (
      <div className="relative min-h-screen flex items-center justify-center bg-[#05060A]">
        <AuthCanvas />
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: 'spring', stiffness: 200, damping: 20 }}
          className="relative z-10 text-center max-w-sm mx-4"
        >
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.2, type: 'spring', stiffness: 300 }}
            className="flex justify-center mb-6"
          >
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-green-400 to-emerald-500 shadow-[0_0_40px_rgba(34,197,94,0.4)]">
              <CheckCircle size={44} className="text-white" />
            </div>
          </motion.div>
          <motion.h2
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.35 }}
            className="text-3xl font-extrabold text-white mb-3"
          >
            Account Created!
          </motion.h2>
          <motion.p
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.45 }}
            className="text-ink-400 mb-6"
          >
            Redirecting you to the login page…
          </motion.p>
          <Loader2 size={20} className="animate-spin text-neon-indigo mx-auto" />
        </motion.div>
      </div>
    );
  }

  // ─── Render ───────────────────────────────────────────────────────────────

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
          className="relative z-10 w-full max-w-xl"
        >
          <TiltCard className="p-8 sm:p-10 border border-white/15 bg-[#090D16]/80 backdrop-blur-2xl shadow-glow-indigo">

            {/* Brand Logo Header */}
            <motion.div variants={fadeUp} className="flex flex-col items-center mb-8 text-center">
              <Link to="/">
                <motion.div
                  className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 via-indigo-600 to-violet-600 shadow-glow-indigo mb-4 cursor-pointer"
                  whileHover={{ scale: 1.08, rotate: 5 }}
                  transition={{ type: 'spring', stiffness: 300 }}
                >
                  <BrainCircuit size={28} className="text-white" />
                </motion.div>
              </Link>
              <h1 className="text-3xl font-black tracking-tight text-white">
                Join <span className="gradient-text">InterviAI</span>
              </h1>
              <p className="mt-1.5 text-sm text-ink-400">
                Elevate your career with personalized AI mock interviews
              </p>
            </motion.div>

            {/* General Error Banner */}
            <AnimatePresence>
              {error.general && (
                <motion.div
                  key="error"
                  initial={{ opacity: 0, y: -8, height: 0 }}
                  animate={{ opacity: 1, y: 0, height: 'auto' }}
                  exit={{ opacity: 0, y: -8, height: 0 }}
                  transition={{ duration: 0.25 }}
                  className={`mb-6 rounded-xl p-4 border flex gap-3 ${
                    error.isNetworkError
                      ? 'bg-amber-950/40 border-amber-800/50 text-amber-300'
                      : 'bg-red-950/40 border-red-800/50 text-red-300'
                  }`}
                >
                  <div className="shrink-0 mt-0.5">
                    {error.isNetworkError
                      ? <WifiOff size={18} className="text-amber-400" />
                      : <AlertCircle size={18} className="text-red-400" />
                    }
                  </div>
                  <div>
                    <p className="text-sm font-semibold">
                      {error.isNetworkError ? 'Connection Error' : 'Registration Failed'}
                    </p>
                    <p className="text-xs mt-0.5 opacity-90">{error.general}</p>
                    {error.isNetworkError && (
                      <p className="text-xs text-amber-400/80 mt-1">
                        Ensure the backend is running on port 8082.
                      </p>
                    )}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Form */}
            <motion.form
              variants={staggerContainer}
              onSubmit={handleRegister}
              className="space-y-5"
              noValidate
            >
              {/* Name row */}
              <motion.div variants={fadeUp} className="grid grid-cols-1 gap-5 sm:grid-cols-2">
                <div>
                  <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-1.5">First Name</label>
                  <Input
                    id="firstName"
                    name="firstName"
                    type="text"
                    placeholder="Jane"
                    value={formData.firstName}
                    onChange={handleChange}
                    disabled={isLoading}
                    autoComplete="given-name"
                    icon={<User size={16} className="text-ink-400" />}
                    error={getFieldError('firstName')}
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-1.5">Last Name</label>
                  <Input
                    id="lastName"
                    name="lastName"
                    type="text"
                    placeholder="Doe"
                    value={formData.lastName}
                    onChange={handleChange}
                    disabled={isLoading}
                    autoComplete="family-name"
                    icon={<User size={16} className="text-ink-400" />}
                    error={getFieldError('lastName')}
                  />
                </div>
              </motion.div>

              <motion.div variants={fadeUp}>
                <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-1.5">Email Address</label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="jane.doe@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={isLoading}
                  autoComplete="email"
                  icon={<Mail size={16} className="text-ink-400" />}
                  error={getFieldError('email')}
                />
              </motion.div>

              <motion.div variants={fadeUp}>
                <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-1.5">Password</label>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  disabled={isLoading}
                  autoComplete="new-password"
                  icon={<Lock size={16} className="text-ink-400" />}
                  error={getFieldError('password')}
                />
                {!getFieldError('password') && (
                  <p className="mt-1 text-xs text-ink-500">
                    Min 8 chars · uppercase · lowercase · number · special character
                  </p>
                )}
              </motion.div>

              <motion.div variants={fadeUp}>
                <label className="block text-xs font-semibold text-ink-400 uppercase tracking-widest mb-1.5">Confirm Password</label>
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  disabled={isLoading}
                  autoComplete="new-password"
                  icon={<Lock size={16} className="text-ink-400" />}
                  error={getFieldError('confirmPassword')}
                />
              </motion.div>

              {/* Checkboxes */}
              <motion.div variants={fadeUp} className="space-y-3 pt-1">
                {[
                  { name: 'acceptTerms', to: '/terms', label: 'Terms of Service' },
                  { name: 'acceptPrivacy', to: '/privacy', label: 'Privacy Policy' },
                ].map(({ name, to, label }) => (
                  <div key={name}>
                    <div className="flex items-start gap-3">
                      <input
                        id={name}
                        name={name}
                        type="checkbox"
                        checked={formData[name as keyof FormData] as boolean}
                        onChange={handleChange}
                        disabled={isLoading}
                        className="h-4 w-4 mt-0.5 rounded border-white/20 bg-obsidian-900 text-neon-indigo focus:ring-neon-indigo accent-indigo-500"
                      />
                      <label htmlFor={name} className="text-sm text-ink-300 cursor-pointer">
                        I agree to the{' '}
                        <Link to={to} className="font-semibold text-neon-cyan hover:underline">
                          {label}
                        </Link>
                      </label>
                    </div>
                    {getFieldError(name) && (
                      <p className="mt-1 ml-7 text-xs text-red-400 flex items-center gap-1">
                        <AlertCircle size={11} /> {getFieldError(name)}
                      </p>
                    )}
                  </div>
                ))}
              </motion.div>

              {/* Submit */}
              <motion.div variants={fadeUp}>
                <Button
                  type="submit"
                  isLoading={isLoading}
                  variant="gradient"
                  className="w-full h-12 text-sm font-bold gap-2 shadow-glow-indigo mt-2"
                >
                  Create Account <ArrowRight size={16} />
                </Button>
              </motion.div>
            </motion.form>

            {/* Sign in link */}
            <motion.div
              variants={fadeUp}
              className="mt-8 text-center border-t border-white/10 pt-6"
            >
              <p className="text-sm text-ink-400">
                Already have an account?{' '}
                <Link to="/login" className="font-bold text-brand-400 hover:text-white transition-colors">
                  Sign in instead
                </Link>
              </p>
            </motion.div>
          </TiltCard>
        </motion.div>
      </div>
    </PageWrapper>
  );
}