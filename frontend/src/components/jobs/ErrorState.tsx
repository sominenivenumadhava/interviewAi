import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface ErrorStateProps {
  title: string;
  message: string;
  searchId?: string;
  onRetry?: () => void;
}

export function ErrorState({ title, message, searchId, onRetry }: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center p-8 bg-red-50/50 dark:bg-red-950/10 border border-red-200 dark:border-red-900/30 rounded-2xl text-center space-y-4 shadow-sm animate-fadeIn">
      <div className="p-3 bg-red-100 dark:bg-red-900/30 rounded-full text-red-600 dark:text-red-400">
        <AlertTriangle className="w-6 h-6" />
      </div>
      <div>
        <h3 className="text-lg font-bold text-red-800 dark:text-red-400">{title}</h3>
        <p className="text-sm text-red-600/90 dark:text-red-400/80 mt-1 max-w-lg">{message}</p>
      </div>

      {searchId && (
        <div className="text-[11px] font-mono bg-white dark:bg-ink-950 px-3 py-1.5 rounded-lg border border-ink-150 dark:border-ink-850 text-ink-500 dark:text-ink-400">
          Search ID: {searchId}
        </div>
      )}

      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="flex items-center gap-2 px-5 py-2 rounded-xl text-xs font-semibold bg-red-600 hover:bg-red-700 text-white shadow-sm transition-all focus:outline-none"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          Retry Search
        </button>
      )}
    </div>
  );
}
