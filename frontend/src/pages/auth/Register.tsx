import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { BrainCircuit, Mail, Lock, User, CheckCircle, AlertCircle, WifiOff, Loader2 } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuth } from '../../contexts/AuthContext';
import { ApiError, ApiFieldError } from '../../lib/apiClient';

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
    // Clear field error when user starts typing
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

      // Show success state then redirect to login
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2500);

    } catch (err: unknown) {
      console.error('[Register] Caught error:', err);

      // Handle ApiError (network, 4xx, 5xx)
      if (err instanceof Error && err.name === 'ApiError') {
        const apiErr = err as ApiError & { fieldErrors?: ApiFieldError[]; details?: ApiFieldError[] };
        const isNetworkError = (apiErr as any).code === 'CONNECTION_REFUSED' ||
                               (apiErr as any).code === 'TIMEOUT' ||
                               (apiErr as any).code === 'NETWORK_ERROR';

        // Map server field errors to our field state
        // Spring Boot sends them in 'details' array; fallback to 'fieldErrors'
        const rawErrors: ApiFieldError[] = apiErr.fieldErrors || (apiErr as any).details || [];
        const serverFields: Record<string, string> = {};
        if (rawErrors.length > 0) {
          rawErrors.forEach((fe: ApiFieldError) => {
            // Map Spring field names to our form fields
            const fieldMap: Record<string, string> = {
              email: 'email',
              password: 'password',
              confirmPassword: 'confirmPassword',
              firstName: 'firstName',
              lastName: 'lastName',
              acceptTerms: 'acceptTerms',
              acceptPrivacy: 'acceptPrivacy',
            };
            const localField = fieldMap[fe.field] || fe.field;
            serverFields[localField] = fe.message;
          });
        }

        setError({
          general: apiErr.message,
          fields: serverFields,
          isNetworkError,
        });
      } else {
        // Fallback for unknown errors
        setError({
          general: err instanceof Error ? err.message : 'An unexpected error occurred. Please try again.',
          fields: {},
          isNetworkError: false,
        });
      }
    }
  };

  // ─── Success state ────────────────────────────────────────────────────────

  if (success) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-ink-50 dark:bg-ink-950 px-4">
        <div className="text-center max-w-md">
          <div className="flex justify-center mb-4">
            <CheckCircle size={64} className="text-green-500" />
          </div>
          <h2 className="text-2xl font-bold text-ink-900 dark:text-white mb-2">
            Account Created!
          </h2>
          <p className="text-ink-500 dark:text-ink-400 mb-4">
            Your account has been successfully created. Redirecting you to the login page…
          </p>
          <div className="flex justify-center">
            <Loader2 size={20} className="animate-spin text-brand-600" />
          </div>
        </div>
      </div>
    );
  }

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <div className="flex min-h-screen bg-ink-50 dark:bg-ink-950">
      {/* Form panel */}
      <div className="flex flex-1 flex-col justify-center px-4 py-12 sm:px-6 lg:flex-none lg:px-20 xl:px-24">
        <div className="mx-auto w-full max-w-sm lg:w-96">

          {/* Logo */}
          <div className="flex items-center gap-2 mb-8">
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand-600 text-white">
              <BrainCircuit size={20} />
            </div>
            <span className="text-xl font-bold tracking-tight text-ink-900 dark:text-white">
              InterviAI
            </span>
          </div>

          <h2 className="text-2xl font-bold leading-9 tracking-tight text-ink-900 dark:text-white">
            Create your account
          </h2>
          <p className="mt-2 text-sm leading-6 text-ink-500 dark:text-ink-400">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-brand-600 hover:text-brand-500">
              Sign in instead
            </Link>
          </p>

          <div className="mt-10">

            {/* General Error Banner */}
            {error.general && (
              <div
                className={`mb-6 rounded-lg p-4 border flex gap-3 ${
                  error.isNetworkError
                    ? 'bg-amber-50 border-amber-200 dark:bg-amber-900/10 dark:border-amber-800'
                    : 'bg-red-50 border-red-200 dark:bg-red-900/10 dark:border-red-800'
                }`}
              >
                <div className="flex-shrink-0 mt-0.5">
                  {error.isNetworkError
                    ? <WifiOff size={16} className="text-amber-500" />
                    : <AlertCircle size={16} className="text-red-500" />
                  }
                </div>
                <div className="flex-1">
                  <p className={`text-sm font-medium ${
                    error.isNetworkError
                      ? 'text-amber-800 dark:text-amber-400'
                      : 'text-red-800 dark:text-red-400'
                  }`}>
                    {error.isNetworkError ? 'Connection Error' : 'Registration Failed'}
                  </p>
                  <p className={`text-sm mt-0.5 ${
                    error.isNetworkError
                      ? 'text-amber-700 dark:text-amber-300'
                      : 'text-red-700 dark:text-red-300'
                  }`}>
                    {error.general}
                  </p>
                  {error.isNetworkError && (
                    <p className="text-xs text-amber-600 dark:text-amber-400 mt-1">
                      Make sure the Spring Boot backend is running on port 8082.
                    </p>
                  )}
                </div>
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleRegister} className="space-y-5" noValidate>

              {/* Name row */}
              <div className="grid grid-cols-2 gap-4">
                <FieldWrapper label="First Name" error={getFieldError('firstName')}>
                  <Input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                    icon={<User size={16} />}
                    placeholder="Alex"
                  />
                </FieldWrapper>

                <FieldWrapper label="Last Name" error={getFieldError('lastName')}>
                  <Input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                    placeholder="Chen"
                  />
                </FieldWrapper>
              </div>

              {/* Email */}
              <FieldWrapper label="Email address" error={getFieldError('email')}>
                <Input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                  icon={<Mail size={16} />}
                  placeholder="you@example.com"
                />
              </FieldWrapper>

              {/* Password */}
              <FieldWrapper label="Password" error={getFieldError('password')}>
                <Input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                  icon={<Lock size={16} />}
                  placeholder="••••••••"
                />
                <p className="mt-1 text-xs text-ink-400">
                  Min 8 chars with uppercase, lowercase, number, and special character
                </p>
              </FieldWrapper>

              {/* Confirm password */}
              <FieldWrapper label="Confirm Password" error={getFieldError('confirmPassword')}>
                <Input
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                  icon={<Lock size={16} />}
                  placeholder="••••••••"
                />
              </FieldWrapper>

              {/* Checkboxes */}
              <div className="space-y-3">
                <CheckboxField
                  id="accept-terms"
                  name="acceptTerms"
                  checked={formData.acceptTerms}
                  onChange={handleChange}
                  disabled={isLoading}
                  error={getFieldError('acceptTerms')}
                  label={
                    <>
                      I agree to the{' '}
                      <a href="#" className="font-semibold text-brand-600 hover:text-brand-500">
                        Terms of Service
                      </a>
                    </>
                  }
                />

                <CheckboxField
                  id="accept-privacy"
                  name="acceptPrivacy"
                  checked={formData.acceptPrivacy}
                  onChange={handleChange}
                  disabled={isLoading}
                  error={getFieldError('acceptPrivacy')}
                  label={
                    <>
                      I agree to the{' '}
                      <a href="#" className="font-semibold text-brand-600 hover:text-brand-500">
                        Privacy Policy
                      </a>
                    </>
                  }
                />
              </div>

              {/* Submit */}
              <div>
                <Button
                  type="submit"
                  className="w-full"
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <span className="flex items-center justify-center gap-2">
                      <Loader2 size={16} className="animate-spin" />
                      Creating account…
                    </span>
                  ) : (
                    'Create account'
                  )}
                </Button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Right panel */}
      <div className="relative hidden w-0 flex-1 lg:block bg-ink-900">
        <div className="absolute inset-0 flex items-center justify-center p-12">
          <div className="max-w-lg text-center">
            <BrainCircuit size={64} className="mx-auto text-brand-500 mb-8" />
            <h2 className="text-3xl font-bold text-white mb-4">
              Start Your Journey
            </h2>
            <p className="text-ink-300 text-lg">
              Get access to personalized AI interviews, detailed skill gap
              analysis, and a custom learning roadmap.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Sub-components ───────────────────────────────────────────────────────────

function FieldWrapper({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <label className="block text-sm font-medium leading-6 text-ink-900 dark:text-ink-100">
        {label}
      </label>
      <div className="mt-2">{children}</div>
      {error && (
        <p className="mt-1 flex items-center gap-1 text-xs text-red-600 dark:text-red-400">
          <AlertCircle size={12} />
          {error}
        </p>
      )}
    </div>
  );
}

function CheckboxField({
  id,
  name,
  checked,
  onChange,
  disabled,
  error,
  label,
}: {
  id: string;
  name: string;
  checked: boolean;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  disabled: boolean;
  error?: string;
  label: React.ReactNode;
}) {
  return (
    <div>
      <div className="flex items-start">
        <input
          id={id}
          name={name}
          type="checkbox"
          checked={checked}
          onChange={onChange}
          disabled={disabled}
          className={`h-4 w-4 rounded border-ink-300 text-brand-600 focus:ring-brand-600 dark:border-ink-700 dark:bg-ink-900 mt-0.5 ${
            error ? 'border-red-400' : ''
          }`}
        />
        <label
          htmlFor={id}
          className="ml-3 block text-sm leading-6 text-ink-700 dark:text-ink-300"
        >
          {label}
        </label>
      </div>
      {error && (
        <p className="mt-0.5 ml-7 flex items-center gap-1 text-xs text-red-600 dark:text-red-400">
          <AlertCircle size={12} />
          {error}
        </p>
      )}
    </div>
  );
}