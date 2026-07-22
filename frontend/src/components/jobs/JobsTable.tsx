import React from 'react';
import { Bookmark, BookmarkCheck, ExternalLink } from 'lucide-react';
import { JobResult } from '../../services/jobSearchApi';

interface JobsTableProps {
  jobs: JobResult[];
  onViewDetails: (job: JobResult) => void;
  onToggleSave: (job: JobResult) => void;
  isSaving: boolean;
}

export function JobsTable({ jobs, onViewDetails, onToggleSave, isSaving }: JobsTableProps) {
  return (
    <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-ink-50 dark:bg-ink-950 border-b border-ink-200 dark:border-ink-800">
              <th className="px-6 py-4 text-xs font-semibold text-ink-500 dark:text-ink-400 uppercase tracking-wider">Job / Company</th>
              <th className="px-6 py-4 text-xs font-semibold text-ink-500 dark:text-ink-400 uppercase tracking-wider">Location</th>
              <th className="px-6 py-4 text-xs font-semibold text-ink-500 dark:text-ink-400 uppercase tracking-wider">Type / Level</th>
              <th className="px-6 py-4 text-xs font-semibold text-ink-500 dark:text-ink-400 uppercase tracking-wider">Date Posted</th>
              <th className="px-6 py-4 text-xs font-semibold text-ink-500 dark:text-ink-400 uppercase tracking-wider text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-150 dark:divide-ink-850">
            {jobs.map((job) => (
              <tr key={job.id} className="hover:bg-ink-50/50 dark:hover:bg-ink-850/30 transition-colors">
                <td className="px-6 py-4.5">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-brand-50 dark:bg-brand-950/20 flex items-center justify-center font-bold text-brand-600 dark:text-brand-400 shrink-0 text-sm">
                      {job.companyName ? job.companyName.substring(0, 2).toUpperCase() : 'CO'}
                    </div>
                    <div>
                      <div className="font-bold text-ink-900 dark:text-ink-100">{job.title}</div>
                      <div className="text-xs text-ink-500 dark:text-ink-400">{job.companyName}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4.5 text-sm text-ink-600 dark:text-ink-300">
                  {job.location}
                </td>
                <td className="px-6 py-4.5 text-sm text-ink-600 dark:text-ink-300">
                  <div className="flex flex-col">
                    <span>{job.workType} • {job.employmentType}</span>
                    <span className="text-xs text-ink-400">{job.experienceLevel}</span>
                  </div>
                </td>
                <td className="px-6 py-4.5 text-sm text-ink-500 dark:text-ink-400">
                  {job.postedAt}
                </td>
                <td className="px-6 py-4.5 text-right">
                  <div className="flex items-center justify-end gap-2.5">
                    <button
                      type="button"
                      disabled={isSaving}
                      onClick={() => onToggleSave(job)}
                      className="text-ink-400 hover:text-brand-600 dark:hover:text-brand-400 p-1.5 rounded-lg hover:bg-ink-100 dark:hover:bg-ink-800 transition-colors"
                    >
                      {job.saved ? (
                        <BookmarkCheck className="w-4.5 h-4.5 text-brand-600 dark:text-brand-400 fill-current" />
                      ) : (
                        <Bookmark className="w-4.5 h-4.5" />
                      )}
                    </button>

                    <button
                      type="button"
                      onClick={() => onViewDetails(job)}
                      className="text-xs font-semibold text-brand-600 dark:text-brand-400 hover:underline px-2.5 py-1.5 rounded-lg hover:bg-brand-50 dark:hover:bg-brand-950/20"
                    >
                      Details
                    </button>

                    {job.applyUrl && job.applyUrl !== 'Not provided' ? (
                      <a
                        href={job.applyUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 py-1.5 px-3 rounded-lg text-xs font-semibold bg-brand-600 hover:bg-brand-700 text-white shadow-sm transition-all"
                      >
                        Apply
                        <ExternalLink className="w-3 h-3" />
                      </a>
                    ) : (
                      <button
                        disabled
                        className="py-1.5 px-3 rounded-lg text-xs font-semibold bg-ink-100 dark:bg-ink-800 text-ink-400 dark:text-ink-600 cursor-not-allowed"
                      >
                        Apply
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
