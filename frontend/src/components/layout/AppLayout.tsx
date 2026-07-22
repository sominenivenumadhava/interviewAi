import React from 'react';
import { Outlet } from 'react-router-dom';
import { TopNav } from './TopNav';
export function AppLayout() {
  return (
    <div className="min-h-screen bg-ink-50 dark:bg-ink-950 transition-colors duration-200">
      <TopNav />
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>);

}