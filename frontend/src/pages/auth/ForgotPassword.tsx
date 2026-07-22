import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { BrainCircuit, Mail, ArrowLeft, CheckCircle } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import apiClient, { API_ENDPOINTS } from '../../lib/apiClient';

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
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #0f0f1a 0%, #1a1a2e 50%, #16213e 100%)',
      padding: '2rem',
    }}>
      <div style={{
        width: '100%',
        maxWidth: '440px',
        background: 'rgba(255,255,255,0.05)',
        backdropFilter: 'blur(20px)',
        border: '1px solid rgba(255,255,255,0.1)',
        borderRadius: '1.5rem',
        padding: '2.5rem',
        boxShadow: '0 25px 50px rgba(0,0,0,0.4)',
      }}>
        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '2rem' }}>
          <div style={{
            width: 44, height: 44,
            borderRadius: '0.75rem',
            background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <BrainCircuit size={24} color="white" />
          </div>
          <span style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fff', letterSpacing: '-0.02em' }}>
            InterviAI
          </span>
        </div>

        {success ? (
          /* Success state */
          <div style={{ textAlign: 'center' }}>
            <div style={{
              width: 64, height: 64,
              borderRadius: '50%',
              background: 'rgba(34,197,94,0.1)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 1.5rem',
            }}>
              <CheckCircle size={32} color="#22c55e" />
            </div>
            <h1 style={{ color: '#fff', fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.75rem' }}>
              Check Your Inbox
            </h1>
            <p style={{ color: 'rgba(255,255,255,0.6)', lineHeight: 1.6, marginBottom: '2rem' }}>
              If an account with <strong style={{ color: '#a5b4fc' }}>{email}</strong> exists,
              we've sent a password reset link. It may take a few minutes to arrive.
            </p>
            <Link to="/login" style={{
              display: 'inline-flex', alignItems: 'center', gap: '0.5rem',
              color: '#818cf8', textDecoration: 'none', fontWeight: 500,
              transition: 'color 0.2s',
            }}>
              <ArrowLeft size={16} />
              Back to Login
            </Link>
          </div>
        ) : (
          /* Form state */
          <>
            <h1 style={{ color: '#fff', fontSize: '1.75rem', fontWeight: 700, marginBottom: '0.5rem' }}>
              Forgot Password?
            </h1>
            <p style={{ color: 'rgba(255,255,255,0.5)', marginBottom: '2rem', lineHeight: 1.6 }}>
              No worries! Enter your email and we'll send you a reset link.
            </p>

            {error && (
              <div style={{
                background: 'rgba(239,68,68,0.12)',
                border: '1px solid rgba(239,68,68,0.3)',
                borderRadius: '0.75rem',
                padding: '0.875rem 1rem',
                color: '#fca5a5',
                fontSize: '0.875rem',
                marginBottom: '1.5rem',
              }}>
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <div style={{ marginBottom: '1.5rem' }}>
                <label htmlFor="forgot-email" style={{
                  display: 'block', color: 'rgba(255,255,255,0.8)',
                  fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.5rem',
                }}>
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
                  icon={<Mail size={18} />}
                />
              </div>

              <Button
                id="forgot-submit-btn"
                type="submit"
                disabled={loading}
                style={{ width: '100%', marginBottom: '1.5rem' }}
              >
                {loading ? 'Sending…' : 'Send Reset Link'}
              </Button>

              <div style={{ textAlign: 'center' }}>
                <Link to="/login" style={{
                  display: 'inline-flex', alignItems: 'center', gap: '0.5rem',
                  color: 'rgba(255,255,255,0.5)', textDecoration: 'none',
                  fontSize: '0.875rem', transition: 'color 0.2s',
                }}>
                  <ArrowLeft size={14} />
                  Back to Login
                </Link>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
