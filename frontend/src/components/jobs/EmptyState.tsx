import React from 'react';
import { Search } from 'lucide-react';

interface EmptyStateProps {
  title: string;
  message: string;
}

export function EmptyState({ title, message }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center p-12 bg-white dark:bg-ink-900 border border-ink-200 dark:border-ink-800 rounded-2xl text-center space-y-4 shadow-sm animate-fadeIn">
      <div className="p-4 bg-brand-50 dark:bg-brand-950/20 rounded-full text-brand-600 dark:text-brand-400">
        <Search className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-bold text-ink-900 dark:text-ink-100">{title}</h3>
      <p className="text-sm text-ink-500 dark:text-ink-400 max-w-sm">{message}</p>
    </div>
  );
}
