import React from 'react';
import { LayoutGrid, Table, SlidersHorizontal } from 'lucide-react';
import { JobResult } from '../../services/jobSearchApi';

interface JobFiltersProps {
  jobs: JobResult[];
  searchTerm: string;
  onSearchChange: (val: string) => void;
  locationFilter: string;
  onLocationChange: (val: string) => void;
  companyFilter: string;
  onCompanyChange: (val: string) => void;
  workTypeFilter: string;
  onWorkTypeChange: (val: string) => void;
  employmentTypeFilter: string;
  onEmploymentTypeChange: (val: string) => void;
  experienceLevelFilter: string;
  onExperienceLevelChange: (val: string) => void;
  savedOnly: boolean;
  onSavedOnlyChange: (val: boolean) => void;
  sortBy: string;
  onSortByChange: (val: string) => void;
  viewMode: 'card' | 'table';
  onViewModeChange: (val: 'card' | 'table') => void;
}

export function JobFilters({
  jobs,
  searchTerm,
  onSearchChange,
  locationFilter,
  onLocationChange,
  companyFilter,
  onCompanyChange,
  workTypeFilter,
  onWorkTypeChange,
  employmentTypeFilter,
  onEmploymentTypeChange,
  experienceLevelFilter,
  onExperienceLevelChange,
  savedOnly,
  onSavedOnlyChange,
  sortBy,
  onSortByChange,
  viewMode,
  onViewModeChange
}: JobFiltersProps) {
  
  // Extract unique filter options from currently loaded jobs list
  const uniqueLocations = Array.from(new Set(jobs.map(j => j.location).filter(Boolean)));
  const uniqueCompanies = Array.from(new Set(jobs.map(j => j.companyName).filter(c => c && c !== 'Not provided')));
  const uniqueWorkTypes = Array.from(new Set(jobs.map(j => j.workType).filter(Boolean)));
  const uniqueEmploymentTypes = Array.from(new Set(jobs.map(j => j.employmentType).filter(Boolean)));
  const uniqueExperienceLevels = Array.from(new Set(jobs.map(j => j.experienceLevel).filter(Boolean)));

  return (
    <div className="bg-white dark:bg-ink-900 border border-ink-200 dark:border-ink-800 rounded-2xl p-5 shadow-sm space-y-4">
      {/* Top row: Search, Sort, View Toggle */}
      <div className="flex flex-col md:flex-row items-center gap-4 justify-between">
        <div className="w-full md:max-w-md">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search within results (title, company, description...)"
            className="w-full px-4 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-transparent text-ink-900 dark:text-ink-100 placeholder-ink-400 focus:outline-none focus:ring-2 focus:ring-brand-500 text-sm"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto justify-end">
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value)}
            className="px-3 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-100 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="recent">Most Recent</option>
            <option value="oldest">Oldest</option>
            <option value="company-az">Company A–Z</option>
            <option value="title-az">Job Title A–Z</option>
          </select>

          {/* View Toggles */}
          <div className="flex border border-ink-200 dark:border-ink-800 rounded-xl overflow-hidden shrink-0">
            <button
              type="button"
              onClick={() => onViewModeChange('card')}
              className={`p-2 focus:outline-none ${viewMode === 'card' ? 'bg-brand-50 dark:bg-brand-950/20 text-brand-600 dark:text-brand-400' : 'text-ink-400 hover:bg-ink-50 dark:hover:bg-ink-800'}`}
            >
              <LayoutGrid className="w-4.5 h-4.5" />
            </button>
            <button
              type="button"
              onClick={() => onViewModeChange('table')}
              className={`p-2 focus:outline-none ${viewMode === 'table' ? 'bg-brand-50 dark:bg-brand-950/20 text-brand-600 dark:text-brand-400' : 'text-ink-400 hover:bg-ink-50 dark:hover:bg-ink-800'}`}
            >
              <Table className="w-4.5 h-4.5" />
            </button>
          </div>
        </div>
      </div>

      {/* Filters row: Multi dropdowns */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 pt-2 border-t border-ink-100 dark:border-ink-800">
        <select
          value={locationFilter}
          onChange={(e) => onLocationChange(e.target.value)}
          className="px-3 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-700 dark:text-ink-300 text-xs focus:outline-none"
        >
          <option value="">All Locations</option>
          {uniqueLocations.map((loc, i) => (
            <option key={i} value={loc}>{loc}</option>
          ))}
        </select>

        <select
          value={companyFilter}
          onChange={(e) => onCompanyChange(e.target.value)}
          className="px-3 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-700 dark:text-ink-300 text-xs focus:outline-none"
        >
          <option value="">All Companies</option>
          {uniqueCompanies.map((comp, i) => (
            <option key={i} value={comp}>{comp}</option>
          ))}
        </select>

        <select
          value={workTypeFilter}
          onChange={(e) => onWorkTypeChange(e.target.value)}
          className="px-3 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-700 dark:text-ink-300 text-xs focus:outline-none"
        >
          <option value="">All Work Types</option>
          {uniqueWorkTypes.map((wt, i) => (
            <option key={i} value={wt}>{wt}</option>
          ))}
        </select>

        <select
          value={employmentTypeFilter}
          onChange={(e) => onEmploymentTypeChange(e.target.value)}
          className="px-3 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-700 dark:text-ink-300 text-xs focus:outline-none"
        >
          <option value="">All Employments</option>
          {uniqueEmploymentTypes.map((et, i) => (
            <option key={i} value={et}>{et}</option>
          ))}
        </select>

        <select
          value={experienceLevelFilter}
          onChange={(e) => onExperienceLevelChange(e.target.value)}
          className="px-3 py-2 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-700 dark:text-ink-300 text-xs focus:outline-none col-span-2 lg:col-span-1"
        >
          <option value="">All Experiences</option>
          {uniqueExperienceLevels.map((el, i) => (
            <option key={i} value={el}>{el}</option>
          ))}
        </select>
      </div>

      {/* Saved jobs filter toggle */}
      <div className="flex items-center gap-2 pt-1.5 select-none">
        <input
          type="checkbox"
          id="savedOnly"
          checked={savedOnly}
          onChange={(e) => onSavedOnlyChange(e.target.checked)}
          className="w-4 h-4 text-brand-600 border-ink-300 rounded focus:ring-brand-500"
        />
        <label htmlFor="savedOnly" className="text-xs font-semibold text-ink-700 dark:text-ink-300 cursor-pointer">
          Show Saved Jobs Only
        </label>
      </div>
    </div>
  );
}
