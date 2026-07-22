import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { BrainCircuit, Mail, Lock, Github } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuth } from '../../contexts/AuthContext';

export function Login() {
  const navigate = useNavigate();
  const { login, isLoading } = useAuth();
  
  // Form state
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState<string>('');
  
  // Handle form changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };
  
  // Handle form submission
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    try {
      await login(formData);
      navigate('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    }
  };

  const handleOAuthLogin = (provider: 'google' | 'github') => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL?.replace('/api/v1', '') || 'http://localhost:8082';
    window.location.href = `${baseUrl}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="flex min-h-screen bg-ink-50 dark:bg-ink-950">
      <div className="flex flex-1 flex-col justify-center px-4 py-12 sm:px-6 lg:flex-none lg:px-20 xl:px-24">
        <div className="mx-auto w-full max-w-sm lg:w-96">
          <div className="flex items-center gap-2 mb-8">
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand-600 text-white">
              <BrainCircuit size={20} />
            </div>
            <span className="text-xl font-bold tracking-tight text-ink-900 dark:text-white">
              InterviAI
            </span>
          </div>

          <h2 className="text-2xl font-bold leading-9 tracking-tight text-ink-900 dark:text-white">
            Sign in to your account
          </h2>
          <p className="mt-2 text-sm leading-6 text-ink-500 dark:text-ink-400">
            Not a member?{' '}
            <Link
              to="/register"
              className="font-semibold text-brand-600 hover:text-brand-500">
              
              Start a 14-day free trial
            </Link>
          </p>

          <div className="mt-10">
            {error && (
              <div className="mb-4 rounded-md bg-red-50 p-4 border border-red-200 dark:bg-red-900/10 dark:border-red-800">
                <div className="text-sm text-red-800 dark:text-red-400">
                  {error}
                </div>
              </div>
            )}
            
            <form onSubmit={handleLogin} className="space-y-6">
              <div>
                <label className="block text-sm font-medium leading-6 text-ink-900 dark:text-ink-100">
                  Email address
                </label>
                <div className="mt-2">
                  <Input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                    icon={<Mail size={16} />}
                    placeholder="you@example.com" />
                  
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium leading-6 text-ink-900 dark:text-ink-100">
                  Password
                </label>
                <div className="mt-2">
                  <Input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                    icon={<Lock size={16} />}
                    placeholder="••••••••" />
                  
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <input
                    id="remember-me"
                    name="remember-me"
                    type="checkbox"
                    disabled={isLoading}
                    className="h-4 w-4 rounded border-ink-300 text-brand-600 focus:ring-brand-600 dark:border-ink-700 dark:bg-ink-900" />
                  
                  <label
                    htmlFor="remember-me"
                    className="ml-3 block text-sm leading-6 text-ink-700 dark:text-ink-300">
                    
                    Remember me
                  </label>
                </div>

                <div className="text-sm leading-6">
                  <a
                    href="#"
                    className="font-semibold text-brand-600 hover:text-brand-500">
                    
                    Forgot password?
                  </a>
                </div>
              </div>

              <div>
                <Button 
                  type="submit" 
                  className="w-full"
                  disabled={isLoading}
                >
                  {isLoading ? 'Signing in...' : 'Sign in'}
                </Button>
              </div>
            </form>

            <div className="mt-10">
              <div className="relative">
                <div
                  className="absolute inset-0 flex items-center"
                  aria-hidden="true">
                  
                  <div className="w-full border-t border-ink-200 dark:border-ink-800" />
                </div>
                <div className="relative flex justify-center text-sm font-medium leading-6">
                  <span className="bg-ink-50 px-6 text-ink-900 dark:bg-ink-950 dark:text-ink-100">
                    Or continue with
                  </span>
                </div>
              </div>

              <div className="mt-6 grid grid-cols-2 gap-4">
                <Button variant="outline" className="w-full gap-2" onClick={() => handleOAuthLogin('google')}>
                  <svg
                    className="h-5 w-5"
                    aria-hidden="true"
                    viewBox="0 0 24 24">
                    
                    <path
                      d="M12.0003 4.75C13.7703 4.75 15.3553 5.36002 16.6053 6.54998L20.0303 3.125C17.9502 1.19 15.2353 0 12.0003 0C7.31028 0 3.25527 2.69 1.28027 6.60998L5.27028 9.70498C6.21525 6.86002 8.87028 4.75 12.0003 4.75Z"
                      fill="#EA4335" />
                    
                    <path
                      d="M23.49 12.275C23.49 11.49 23.415 10.73 23.3 10H12V14.51H18.47C18.18 15.99 17.34 17.25 16.08 18.1L19.945 21.1C22.2 19.01 23.49 15.92 23.49 12.275Z"
                      fill="#4285F4" />
                    
                    <path
                      d="M5.26498 14.2949C5.02498 13.5699 4.88501 12.7999 4.88501 11.9999C4.88501 11.1999 5.01998 10.4299 5.26498 9.7049L1.275 6.60986C0.46 8.22986 0 10.0599 0 11.9999C0 13.9399 0.46 15.7699 1.28 17.3899L5.26498 14.2949Z"
                      fill="#FBBC05" />
                    
                    <path
                      d="M12.0004 24.0001C15.2404 24.0001 17.9654 22.935 19.9454 21.095L16.0804 18.095C15.0054 18.82 13.6204 19.245 12.0004 19.245C8.8704 19.245 6.21537 17.135 5.26538 14.29L1.27539 17.385C3.25539 21.31 7.3104 24.0001 12.0004 24.0001Z"
                      fill="#34A853" />
                    
                  </svg>
                  Google
                </Button>
                <Button variant="outline" className="w-full gap-2" onClick={() => handleOAuthLogin('github')}>
                  <Github size={18} />
                  GitHub
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="relative hidden w-0 flex-1 lg:block bg-ink-900">
        <div className="absolute inset-0 flex items-center justify-center p-12">
          <div className="max-w-lg text-center">
            <BrainCircuit size={64} className="mx-auto text-brand-500 mb-8" />
            <h2 className="text-3xl font-bold text-white mb-4">
              AI-Powered Interview Prep
            </h2>
            <p className="text-ink-300 text-lg">
              Join thousands of engineers who have landed their dream jobs at
              top tech companies using our personalized AI interview platform.
            </p>
          </div>
        </div>
      </div>
    </div>);

}