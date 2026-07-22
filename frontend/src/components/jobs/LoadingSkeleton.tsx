import React from 'react';

interface LoadingSkeletonProps {
  count?: number;
}

export function LoadingSkeleton({ count = 3 }: LoadingSkeletonProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-pulse">
      {Array.from({ length: count }).map((_, idx) => (
        <div key={idx} className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-5 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-ink-200 dark:bg-ink-800 shrink-0" />
            <div className="space-y-2 flex-1">
              <div className="h-4 bg-ink-200 dark:bg-ink-800 rounded-md w-3/4" />
              <div className="h-3 bg-ink-200 dark:bg-ink-800 rounded-md w-1/2" />
            </div>
          </div>
          
          <div className="space-y-2">
            <div className="h-3 bg-ink-200 dark:bg-ink-800 rounded-md w-full" />
            <div className="h-3 bg-ink-200 dark:bg-ink-800 rounded-md w-5/6" />
          </div>

          <div className="flex gap-2">
            <div className="h-6 bg-ink-200 dark:bg-ink-800 rounded-md w-16" />
            <div className="h-6 bg-ink-200 dark:bg-ink-800 rounded-md w-16" />
            <div className="h-6 bg-ink-200 dark:bg-ink-800 rounded-md w-16" />
          </div>

          <div className="flex gap-4 pt-4 border-t border-ink-150 dark:border-ink-850">
            <div className="h-8 bg-ink-200 dark:bg-ink-800 rounded-xl flex-1" />
            <div className="h-8 bg-ink-200 dark:bg-ink-800 rounded-xl flex-1" />
          </div>
        </div>
      ))}
    </div>
  );
}
