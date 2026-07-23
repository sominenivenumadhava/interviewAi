import React from 'react';
import { Outlet } from 'react-router-dom';
import { TopNav } from './TopNav';
import { useRequireAuth } from '../../contexts/AuthContext';

export function AppLayout() {
  const auth = useRequireAuth();

  if (auth.isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-ink-50 text-ink-600 dark:bg-ink-950 dark:text-ink-300">
        Checking your session…
      </div>
    );
  }

  if (!auth.isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-ink-50 dark:bg-ink-950 transition-colors duration-200">
      <TopNav />
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>);

}
