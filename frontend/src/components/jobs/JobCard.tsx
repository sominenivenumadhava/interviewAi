import React from 'react';
import { Bookmark, BookmarkCheck, ExternalLink, Calendar, MapPin, Briefcase, Award } from 'lucide-react';
import { JobResult } from '../../services/jobSearchApi';

interface JobCardProps {
  job: JobResult;
  onViewDetails: (job: JobResult) => void;
  onToggleSave: (job: JobResult) => void;
  isSaving: boolean;
}

export function JobCard({ job, onViewDetails, onToggleSave, isSaving }: JobCardProps) {
  // Generate initials for the logo if company logo is missing
  const getInitials = (name: string) => {
    if (!name || name === 'Not provided') return 'CO';
    return name
      .split(' ')
      .map((n) => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  };

  return (
    <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between h-full relative group">
      <div>
        {/* Header: Logo, Title, Save */}
        <div className="flex items-start justify-between gap-4 mb-4">
          <div className="flex items-center gap-3">
            {job.companyLogo && job.companyLogo !== 'Not provided' ? (
              <img
                src={job.companyLogo}
                alt={job.companyName}
                onError={(e) => {
                  (e.target as HTMLImageElement).src = ''; // Force initials fallback
                }}
                className="w-12 h-12 rounded-xl object-contain border border-ink-100 dark:border-ink-800 p-1 bg-ink-50 dark:bg-ink-950"
              />
            ) : (
              <div className="w-12 h-12 rounded-xl bg-brand-50 dark:bg-brand-950/30 flex items-center justify-center font-bold text-brand-600 dark:text-brand-400">
                {getInitials(job.companyName)}
              </div>
            )}

            <div>
              <h4 className="font-bold text-ink-900 dark:text-ink-100 group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors line-clamp-1">
                {job.title}
              </h4>
              <p className="text-sm font-semibold text-ink-600 dark:text-ink-400">
                {job.companyName}
              </p>
            </div>
          </div>

          <button
            type="button"
            disabled={isSaving}
            onClick={() => onToggleSave(job)}
            className="text-ink-400 hover:text-brand-600 dark:hover:text-brand-400 p-1.5 rounded-lg hover:bg-ink-50 dark:hover:bg-ink-800 transition-colors"
          >
            {job.saved ? (
              <BookmarkCheck className="w-5 h-5 text-brand-600 dark:text-brand-400 fill-current" />
            ) : (
              <Bookmark className="w-5 h-5" />
            )}
          </button>
        </div>

        {/* Badges / Metadata */}
        <div className="grid grid-cols-2 gap-2 text-xs font-medium text-ink-600 dark:text-ink-400 mb-4">
          <div className="flex items-center gap-1.5 line-clamp-1">
            <MapPin className="w-3.5 h-3.5 text-ink-400 shrink-0" />
            <span>{job.location}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Briefcase className="w-3.5 h-3.5 text-ink-400 shrink-0" />
            <span>{job.workType} • {job.employmentType}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Award className="w-3.5 h-3.5 text-ink-400 shrink-0" />
            <span>{job.experienceLevel}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5 text-ink-400 shrink-0" />
            <span>{job.postedAt}</span>
          </div>
        </div>

        {/* Short description */}
        <p className="text-xs text-ink-500 dark:text-ink-400 line-clamp-3 mb-4 leading-relaxed">
          {job.description ? job.description.replace(/<[^>]*>/g, '') : 'No description provided.'}
        </p>

        {/* Skills */}
        {job.skills && job.skills.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mb-5">
            {job.skills.slice(0, 3).map((skill, idx) => (
              <span
                key={idx}
                className="px-2 py-0.5 rounded-md text-[10px] font-semibold bg-ink-50 dark:bg-ink-800 text-ink-600 dark:text-ink-300 border border-ink-150 dark:border-ink-750"
              >
                {skill}
              </span>
            ))}
            {job.skills.length > 3 && (
              <span className="text-[10px] text-ink-400 dark:text-ink-500 font-medium self-center">
                +{job.skills.length - 3} more
              </span>
            )}
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex items-center gap-3 border-t border-ink-100 dark:border-ink-850 pt-4 mt-auto">
        <button
          type="button"
          onClick={() => onViewDetails(job)}
          className="flex-1 text-center py-2 px-3 rounded-xl text-xs font-semibold border border-ink-200 dark:border-ink-800 hover:bg-ink-50 dark:hover:bg-ink-800 text-ink-700 dark:text-ink-300 transition-colors"
        >
          View Details
        </button>

        {job.applyUrl && job.applyUrl !== 'Not provided' ? (
          <a
            href={job.applyUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex-1 flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl text-xs font-semibold bg-brand-600 hover:bg-brand-700 text-white shadow-sm transition-all text-center"
          >
            Apply
            <ExternalLink className="w-3 h-3" />
          </a>
        ) : (
          <button
            disabled
            className="flex-1 py-2 px-3 rounded-xl text-xs font-semibold bg-ink-100 dark:bg-ink-800 text-ink-400 dark:text-ink-600 cursor-not-allowed text-center"
          >
            Apply
          </button>
        )}
      </div>
    </div>
  );
}
