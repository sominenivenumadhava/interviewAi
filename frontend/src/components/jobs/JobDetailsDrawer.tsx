import React from 'react';
import { X, ExternalLink, Bookmark, BookmarkCheck, MapPin, Briefcase, Award, Calendar, DollarSign, Users } from 'lucide-react';
import { JobResult } from '../../services/jobSearchApi';

interface JobDetailsDrawerProps {
  job: JobResult | null;
  onClose: () => void;
  onToggleSave: (job: JobResult) => void;
  isSaving: boolean;
}

export function JobDetailsDrawer({ job, onClose, onToggleSave, isSaving }: JobDetailsDrawerProps) {
  if (!job) return null;

  // Safe native HTML sanitizer using DOMParser
  const getSanitizedHtml = (htmlStr: string) => {
    if (!htmlStr) return '';
    try {
      const parser = new DOMParser();
      const doc = parser.parseFromString(htmlStr, 'text/html');
      
      // Remove unsafe tags
      const unsafeTags = ['script', 'style', 'iframe', 'frame', 'object', 'embed', 'form', 'input', 'button'];
      unsafeTags.forEach(tag => {
        doc.querySelectorAll(tag).forEach(el => el.remove());
      });

      // Strip on-event attributes
      const allElements = doc.querySelectorAll('*');
      allElements.forEach(el => {
        for (let i = el.attributes.length - 1; i >= 0; i--) {
          const attrName = el.attributes[i].name;
          if (attrName.toLowerCase().startsWith('on') || el.attributes[i].value.toLowerCase().startsWith('javascript:')) {
            el.removeAttribute(attrName);
          }
        }
      });

      return doc.body.innerHTML;
    } catch (e) {
      console.error('HTML Sanitization failed, returning plain text fallback', e);
      return htmlStr.replace(/<[^>]*>/g, ''); // Simple regex strip fallback
    }
  };

  const sanitizedDescription = getSanitizedHtml(job.description);

  return (
    <>
      {/* Backdrop */}
      <div 
        onClick={onClose}
        className="fixed inset-0 bg-black/40 dark:bg-black/60 z-40 transition-opacity animate-fadeIn" 
      />

      {/* Drawer */}
      <div className="fixed top-0 right-0 bottom-0 w-full max-w-2xl bg-white dark:bg-ink-900 shadow-2xl z-50 border-l border-ink-200 dark:border-ink-800 flex flex-col justify-between animate-slideLeft">
        
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-ink-200 dark:border-ink-800">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={onClose}
              className="p-1.5 rounded-lg hover:bg-ink-50 dark:hover:bg-ink-800 text-ink-500 dark:text-ink-400 focus:outline-none"
            >
              <X className="w-5 h-5" />
            </button>
            <h3 className="font-bold text-lg text-ink-900 dark:text-ink-100">Job Details</h3>
          </div>

          <button
            type="button"
            disabled={isSaving}
            onClick={() => onToggleSave(job)}
            className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold border border-ink-200 dark:border-ink-800 hover:bg-ink-50 dark:hover:bg-ink-800 text-ink-700 dark:text-ink-300 transition-colors focus:outline-none"
          >
            {job.saved ? (
              <>
                <BookmarkCheck className="w-4.5 h-4.5 text-brand-600 dark:text-brand-400 fill-current" />
                Saved
              </>
            ) : (
              <>
                <Bookmark className="w-4.5 h-4.5" />
                Save Job
              </>
            )}
          </button>
        </div>

        {/* Content Area */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          
          {/* Title & Company */}
          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-ink-900 dark:text-ink-100 leading-snug">{job.title}</h2>
            <div className="flex items-center gap-2">
              <span className="font-semibold text-brand-600 dark:text-brand-400">{job.companyName}</span>
              {job.companyLinkedInUrl && job.companyLinkedInUrl !== 'Not provided' && (
                <a 
                  href={job.companyLinkedInUrl} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="text-xs text-ink-400 hover:underline flex items-center gap-0.5"
                >
                  LinkedIn Profile <ExternalLink className="w-3 h-3" />
                </a>
              )}
            </div>
          </div>

          {/* Quick stats grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 bg-ink-50 dark:bg-ink-950 p-4 rounded-2xl border border-ink-150 dark:border-ink-850">
            <div className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300">
              <MapPin className="w-5 h-5 text-ink-400 shrink-0" />
              <div>
                <div className="text-[10px] uppercase font-bold text-ink-400">Location</div>
                <div className="font-semibold">{job.location}</div>
              </div>
            </div>

            <div className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300">
              <Briefcase className="w-5 h-5 text-ink-400 shrink-0" />
              <div>
                <div className="text-[10px] uppercase font-bold text-ink-400">Job Type</div>
                <div className="font-semibold">{job.workType} • {job.employmentType}</div>
              </div>
            </div>

            <div className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300">
              <Award className="w-5 h-5 text-ink-400 shrink-0" />
              <div>
                <div className="text-[10px] uppercase font-bold text-ink-400">Experience</div>
                <div className="font-semibold">{job.experienceLevel}</div>
              </div>
            </div>

            <div className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300">
              <Calendar className="w-5 h-5 text-ink-400 shrink-0" />
              <div>
                <div className="text-[10px] uppercase font-bold text-ink-400">Date Posted</div>
                <div className="font-semibold">{job.postedAt}</div>
              </div>
            </div>

            <div className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300">
              <DollarSign className="w-5 h-5 text-ink-400 shrink-0" />
              <div>
                <div className="text-[10px] uppercase font-bold text-ink-400">Salary</div>
                <div className="font-semibold">{job.salary}</div>
              </div>
            </div>

            <div className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300">
              <Users className="w-5 h-5 text-ink-400 shrink-0" />
              <div>
                <div className="text-[10px] uppercase font-bold text-ink-400">Applicants</div>
                <div className="font-semibold">{job.applicantsCount}</div>
              </div>
            </div>
          </div>

          {/* Description */}
          <div className="space-y-3">
            <h3 className="font-bold text-base text-ink-900 dark:text-ink-100">Job Description</h3>
            <div 
              className="text-sm text-ink-600 dark:text-ink-350 leading-relaxed space-y-4 font-normal"
              dangerouslySetInnerHTML={{ __html: sanitizedDescription }}
            />
          </div>

          {/* Skills */}
          {job.skills && job.skills.length > 0 && (
            <div className="space-y-3 pt-4 border-t border-ink-150 dark:border-ink-850">
              <h3 className="font-bold text-base text-ink-900 dark:text-ink-100">Key Skills Required</h3>
              <div className="flex flex-wrap gap-2">
                {job.skills.map((skill, index) => (
                  <span
                    key={index}
                    className="px-3 py-1 rounded-xl text-xs font-semibold bg-brand-50 dark:bg-brand-950/20 text-brand-700 dark:text-brand-300 border border-brand-100 dark:border-brand-900/30"
                  >
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Company details */}
          {(job.companyWebsite || job.companyIndustry || job.companySize) && (
            <div className="space-y-3 pt-4 border-t border-ink-150 dark:border-ink-850">
              <h3 className="font-bold text-base text-ink-900 dark:text-ink-100">About the Company</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm text-ink-600 dark:text-ink-400">
                {job.companyWebsite && job.companyWebsite !== 'Not provided' && (
                  <div>
                    <span className="font-bold text-ink-400 text-xs block uppercase">Website</span>
                    <a 
                      href={job.companyWebsite} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-brand-600 dark:text-brand-400 hover:underline inline-flex items-center gap-1 font-semibold"
                    >
                      {job.companyWebsite}
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  </div>
                )}
                {job.companyIndustry && job.companyIndustry !== 'Not provided' && (
                  <div>
                    <span className="font-bold text-ink-400 text-xs block uppercase">Industry</span>
                    <span className="font-semibold text-ink-800 dark:text-ink-200">{job.companyIndustry}</span>
                  </div>
                )}
                {job.companySize && job.companySize !== 'Not provided' && (
                  <div>
                    <span className="font-bold text-ink-400 text-xs block uppercase">Company Size</span>
                    <span className="font-semibold text-ink-800 dark:text-ink-200">{job.companySize}</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Footer actions */}
        <div className="p-6 border-t border-ink-200 dark:border-ink-800 bg-ink-50/50 dark:bg-ink-950/20 flex gap-4 mt-auto">
          {job.applyUrl && job.applyUrl !== 'Not provided' ? (
            <a
              href={job.applyUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl font-bold bg-brand-600 hover:bg-brand-700 text-white shadow-md shadow-brand-500/10 transition-all text-center focus:outline-none"
            >
              Apply to Job Opportunity
              <ExternalLink className="w-4 h-4" />
            </a>
          ) : (
            <button
              disabled
              className="flex-1 py-3 px-4 rounded-xl font-bold bg-ink-150 dark:bg-ink-800 text-ink-400 dark:text-ink-600 cursor-not-allowed text-center"
            >
              Apply URL Not Available
            </button>
          )}
        </div>
      </div>
    </>
  );
}
