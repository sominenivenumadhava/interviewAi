import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import apiClient, { API_ENDPOINTS } from '../../lib/apiClient';

export function OAuthCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const hashParams = new URLSearchParams(
      window.location.hash.startsWith('#')
        ? window.location.hash.slice(1)
        : window.location.hash
    );

    // Prefer hash fragment (secure) — fall back to query for older redirects
    const accessToken =
      hashParams.get('accessToken') || searchParams.get('accessToken');
    const refreshToken =
      hashParams.get('refreshToken') || searchParams.get('refreshToken');

    if (accessToken && refreshToken) {
      localStorage.setItem('interviai_access_token', accessToken);
      localStorage.setItem('interviai_refresh_token', refreshToken);

      // Clear tokens from the address bar
      if (window.location.hash) {
        window.history.replaceState(null, '', window.location.pathname);
      }

      apiClient.get(API_ENDPOINTS.USER.PROFILE)
        .then(response => {
          if (response.data) {
            localStorage.setItem('interviai_user', JSON.stringify(response.data));
            window.location.href = '/dashboard';
          } else {
            console.error('Failed to fetch user profile');
            navigate('/login');
          }
        })
        .catch(err => {
          console.error('Error fetching profile during OAuth callback', err);
          navigate('/login');
        });
    } else {
      navigate('/login');
    }
  }, [searchParams, navigate]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-ink-50 dark:bg-ink-950">
      <div className="text-center">
        <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-brand-500 border-r-transparent align-[-0.125em]" role="status"></div>
        <p className="mt-4 text-ink-600 dark:text-ink-400">Authenticating...</p>
      </div>
    </div>
  );
}
