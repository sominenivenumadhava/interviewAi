import React from 'react';
import { Bookmark, MapPin, Briefcase } from 'lucide-react';
import { JobResult } from '../../services/jobSearchApi';

interface SavedJobsPanelProps {
  savedJobs: JobResult[];
  onSelectJob: (job: JobResult) => void;
}

export function SavedJobsPanel({ savedJobs, onSelectJob }: SavedJobsPanelProps) {
  if (savedJobs.length === 0) {
    return (
      <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-5 shadow-sm text-center">
        <p className="text-sm text-ink-500 dark:text-ink-400">No saved jobs yet.</p>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-5 shadow-sm space-y-4">
      <h3 className="font-bold text-sm text-ink-900 dark:text-ink-100 flex items-center gap-2">
        <Bookmark className="w-4.5 h-4.5 text-brand-500 fill-current" />
        Saved Opportunities ({savedJobs.length})
      </h3>

      <div className="space-y-2.5 max-h-[380px] overflow-y-auto pr-1">
        {savedJobs.map((job) => (
          <div
            key={job.id}
            onClick={() => onSelectJob(job)}
            className="p-3 rounded-xl border border-ink-150 dark:border-ink-850 hover:bg-ink-50 dark:hover:bg-ink-950 text-left cursor-pointer transition-all space-y-1 min-w-0"
          >
            <div className="font-bold text-xs text-ink-900 dark:text-ink-100 truncate">
              {job.title}
            </div>
            <div className="text-[10px] font-semibold text-brand-600 dark:text-brand-400 truncate">
              {job.companyName}
            </div>
            <div className="flex items-center gap-1.5 text-[9px] text-ink-500 dark:text-ink-450">
              <span className="truncate">{job.location}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
