import React from 'react';
import { Loader2, CheckCircle2, AlertCircle } from 'lucide-react';

interface SearchProgressProps {
  status: string;
  progressMessage: string;
  jobsCollected: number;
  requestedJobs: number;
}

export function SearchProgress({ status, progressMessage, jobsCollected, requestedJobs }: SearchProgressProps) {
  const steps = [
    { label: 'Preparing LinkedIn search URL', completedStates: ['RUNNING', 'SUCCEEDED', 'FAILED', 'ABORTED', 'TIMED-OUT'] },
    { label: 'Starting Apify Actor', completedStates: ['RUNNING', 'SUCCEEDED', 'FAILED', 'ABORTED', 'TIMED-OUT'] },
    { label: 'Scraping job listings', completedStates: ['SUCCEEDED'] },
    { label: 'Scraping company details', completedStates: ['SUCCEEDED'] },
    { label: 'Processing results', completedStates: ['SUCCEEDED'] },
    { label: 'Search completed', completedStates: ['SUCCEEDED'] }
  ];

  const getStepStatus = (stepIndex: number) => {
    // Basic heuristics to determine the active step
    const isSucceeded = status === 'SUCCEEDED';
    const isFailed = ['FAILED', 'ABORTED', 'TIMED-OUT'].includes(status);
    
    if (isSucceeded) return 'completed';
    if (isFailed) return 'failed';

    if (status === 'PENDING') {
      if (stepIndex === 0) return 'completed';
      if (stepIndex === 1) return 'active';
      return 'pending';
    }

    if (status === 'RUNNING') {
      if (stepIndex < 2) return 'completed';
      if (stepIndex === 2 && jobsCollected === 0) return 'active';
      if (stepIndex === 3 && jobsCollected > 0) return 'active';
      if (stepIndex === 4 && jobsCollected >= requestedJobs) return 'active';
      return 'pending';
    }

    return 'pending';
  };

  return (
    <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-6 shadow-sm space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-ink-900 dark:text-ink-100">Scraping LinkedIn Jobs</h3>
          <p className="text-sm text-ink-500 dark:text-ink-400 mt-1">{progressMessage}</p>
        </div>
        {status === 'RUNNING' || status === 'PENDING' ? (
          <Loader2 className="w-6 h-6 text-brand-500 animate-spin" />
        ) : status === 'SUCCEEDED' ? (
          <CheckCircle2 className="w-6 h-6 text-green-500" />
        ) : (
          <AlertCircle className="w-6 h-6 text-red-500" />
        )}
      </div>

      {/* Indeterminate / Determinate progress bar */}
      <div className="w-full bg-ink-100 dark:bg-ink-950 rounded-full h-2 overflow-hidden relative">
        {status === 'RUNNING' || status === 'PENDING' ? (
          <div className="bg-brand-500 h-2 rounded-full absolute top-0 left-0 w-1/3 animate-indeterminateBar" />
        ) : status === 'SUCCEEDED' ? (
          <div className="bg-green-500 h-2 rounded-full w-full" />
        ) : (
          <div className="bg-red-500 h-2 rounded-full w-full" />
        )}
      </div>

      {/* Real-time jobs count if available */}
      {jobsCollected > 0 && (
        <div className="text-sm font-semibold text-ink-700 dark:text-ink-300 flex items-center justify-between bg-ink-50 dark:bg-ink-950 p-3 rounded-xl">
          <span>Jobs found in LinkedIn dataset:</span>
          <span className="text-brand-600 dark:text-brand-400">{jobsCollected} / {requestedJobs}</span>
        </div>
      )}

      {/* Steps checklist */}
      <div className="space-y-3.5 pt-2">
        {steps.map((step, idx) => {
          const stepStatus = getStepStatus(idx);
          return (
            <div key={idx} className="flex items-center gap-3 text-sm">
              {stepStatus === 'completed' ? (
                <div className="w-5 h-5 rounded-full bg-green-50 dark:bg-green-950 flex items-center justify-center text-green-600 dark:text-green-400">
                  ✓
                </div>
              ) : stepStatus === 'active' ? (
                <div className="w-5 h-5 rounded-full border-2 border-brand-500 border-t-transparent animate-spin flex items-center justify-center" />
              ) : stepStatus === 'failed' ? (
                <div className="w-5 h-5 rounded-full bg-red-50 dark:bg-red-950 flex items-center justify-center text-red-600 dark:text-red-400 font-bold text-xs">
                  ✗
                </div>
              ) : (
                <div className="w-5 h-5 rounded-full border border-ink-300 dark:border-ink-700 flex items-center justify-center text-ink-400 text-xs">
                  {idx + 1}
                </div>
              )}
              <span className={`
                ${stepStatus === 'completed' ? 'text-ink-500 dark:text-ink-400 line-through' : ''}
                ${stepStatus === 'active' ? 'text-ink-900 dark:text-ink-100 font-semibold' : 'text-ink-600 dark:text-ink-400'}
              `}>
                {step.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
