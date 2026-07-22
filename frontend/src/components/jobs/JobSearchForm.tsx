import React, { useState, useEffect } from 'react';
import { Search, RotateCcw, ChevronDown, ChevronUp, Link as LinkIcon, Info } from 'lucide-react';
import { JobSearchRequest } from '../../services/jobSearchApi';

interface JobSearchFormProps {
  onSubmit: (data: JobSearchRequest) => void;
  isLoading: boolean;
}

export function JobSearchForm({ onSubmit, isLoading }: JobSearchFormProps) {
  const [designation, setDesignation] = useState('');
  const [location, setLocation] = useState('');
  const [numberOfJobs, setNumberOfJobs] = useState(25);
  const [experienceLevel, setExperienceLevel] = useState('');
  const [workType, setWorkType] = useState('');
  const [employmentType, setEmploymentType] = useState('');
  const [datePosted, setDatePosted] = useState('');
  const [scrapeCompanyDetails, setScrapeCompanyDetails] = useState(true);
  const [splitByCity, setSplitByCity] = useState(false);
  const [country, setCountry] = useState('');
  
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [showLiveUrl, setShowLiveUrl] = useState(false);
  const [liveUrl, setLiveUrl] = useState('');

  // Live URL builder on input change
  useEffect(() => {
    if (!designation.trim() || !location.trim()) {
      setLiveUrl('');
      return;
    }
    let url = `https://www.linkedin.com/jobs/search/?keywords=${encodeURIComponent(designation.trim())}&location=${encodeURIComponent(location.trim())}&position=1&pageNum=0`;
    
    if (datePosted) {
      if (datePosted === 'PAST_24_HOURS') url += '&f_TPR=r86400';
      else if (datePosted === 'PAST_WEEK') url += '&f_TPR=r604800';
      else if (datePosted === 'PAST_MONTH') url += '&f_TPR=r2592000';
    }

    if (employmentType) {
      if (employmentType === 'FULL_TIME') url += '&f_JT=F';
      else if (employmentType === 'PART_TIME') url += '&f_JT=P';
      else if (employmentType === 'CONTRACT') url += '&f_JT=C';
      else if (employmentType === 'TEMPORARY') url += '&f_JT=T';
      else if (employmentType === 'INTERNSHIP') url += '&f_JT=I';
      else if (employmentType === 'VOLUNTEER') url += '&f_JT=V';
    }

    if (experienceLevel) {
      if (experienceLevel === 'INTERNSHIP') url += '&f_E=1';
      else if (experienceLevel === 'ENTRY_LEVEL') url += '&f_E=2';
      else if (experienceLevel === 'ASSOCIATE') url += '&f_E=3';
      else if (experienceLevel === 'MID_SENIOR_LEVEL') url += '&f_E=4';
      else if (experienceLevel === 'DIRECTOR') url += '&f_E=5';
      else if (experienceLevel === 'EXECUTIVE') url += '&f_E=6';
    }

    setLiveUrl(url);
  }, [designation, location, datePosted, employmentType, experienceLevel]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (designation.trim().length < 2) return;
    if (!location.trim()) return;

    onSubmit({
      designation: designation.trim(),
      location: location.trim(),
      numberOfJobs,
      experienceLevel: experienceLevel || undefined,
      workType: workType || undefined,
      employmentType: employmentType || undefined,
      datePosted: datePosted || undefined,
      scrapeCompanyDetails,
      splitByCity,
      country: splitByCity && country.trim() ? country.trim() : undefined,
    });
  };

  const handleReset = () => {
    setDesignation('');
    setLocation('');
    setNumberOfJobs(25);
    setExperienceLevel('');
    setWorkType('');
    setEmploymentType('');
    setDatePosted('');
    setScrapeCompanyDetails(true);
    setSplitByCity(false);
    setCountry('');
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-6 shadow-sm space-y-6">
      {/* Required fields */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
            Designation / Job Title <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            required
            min={2}
            value={designation}
            onChange={(e) => setDesignation(e.target.value)}
            placeholder="e.g. Java Developer"
            className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-transparent text-ink-900 dark:text-ink-100 placeholder-ink-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
            Location <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            required
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="e.g. Bengaluru, Karnataka, India"
            className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-transparent text-ink-900 dark:text-ink-100 placeholder-ink-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
            Number of Jobs <span className="text-ink-400 font-normal">(Max 500)</span>
          </label>
          <input
            type="number"
            min={1}
            max={500}
            value={numberOfJobs}
            onChange={(e) => setNumberOfJobs(Math.min(500, Math.max(1, parseInt(e.target.value) || 25)))}
            className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-transparent text-ink-900 dark:text-ink-100 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
      </div>

      {/* Advanced Collapsible Section Trigger */}
      <button
        type="button"
        onClick={() => setShowAdvanced(!showAdvanced)}
        className="flex items-center gap-2 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 focus:outline-none"
      >
        {showAdvanced ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
        Advanced Search Parameters
      </button>

      {/* Advanced Fields */}
      {showAdvanced && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 pt-2 border-t border-ink-100 dark:border-ink-800 animate-fadeIn">
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
              Experience Level
            </label>
            <select
              value={experienceLevel}
              onChange={(e) => setExperienceLevel(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-100 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">Any Level</option>
              <option value="INTERNSHIP">Internship</option>
              <option value="ENTRY_LEVEL">Entry Level</option>
              <option value="ASSOCIATE">Associate</option>
              <option value="MID_SENIOR_LEVEL">Mid-Senior Level</option>
              <option value="DIRECTOR">Director</option>
              <option value="EXECUTIVE">Executive</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
              Work Type
            </label>
            <select
              value={workType}
              onChange={(e) => setWorkType(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-100 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">Any Type</option>
              <option value="ON_SITE">On-site</option>
              <option value="REMOTE">Remote</option>
              <option value="HYBRID">Hybrid</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
              Employment Type
            </label>
            <select
              value={employmentType}
              onChange={(e) => setEmploymentType(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-100 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">Any Type</option>
              <option value="FULL_TIME">Full-time</option>
              <option value="PART_TIME">Part-time</option>
              <option value="CONTRACT">Contract</option>
              <option value="TEMPORARY">Temporary</option>
              <option value="INTERNSHIP">Internship</option>
              <option value="VOLUNTEER">Volunteer</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
              Date Posted
            </label>
            <select
              value={datePosted}
              onChange={(e) => setDatePosted(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-100 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">Any time</option>
              <option value="PAST_24_HOURS">Past 24 hours</option>
              <option value="PAST_WEEK">Past week</option>
              <option value="PAST_MONTH">Past month</option>
            </select>
          </div>

          <div className="flex items-center gap-3 py-2 col-span-1 md:col-span-2">
            <input
              type="checkbox"
              id="scrapeCompany"
              checked={scrapeCompanyDetails}
              onChange={(e) => setScrapeCompanyDetails(e.target.checked)}
              className="w-4.5 h-4.5 text-brand-600 border-ink-300 rounded focus:ring-brand-500"
            />
            <label htmlFor="scrapeCompany" className="text-sm font-medium text-ink-700 dark:text-ink-300 select-none">
              Scrape Company Details (extracts website, size, etc.)
            </label>
          </div>

          <div className="flex items-center gap-3 py-2 col-span-1 md:col-span-2">
            <input
              type="checkbox"
              id="splitByCity"
              checked={splitByCity}
              onChange={(e) => setSplitByCity(e.target.checked)}
              className="w-4.5 h-4.5 text-brand-600 border-ink-300 rounded focus:ring-brand-500"
            />
            <label htmlFor="splitByCity" className="text-sm font-medium text-ink-700 dark:text-ink-300 select-none">
              Split Search by City (bypasses LinkedIn's 1000-job limit)
            </label>
          </div>

          {splitByCity && (
            <div className="col-span-1 md:col-span-2 animate-slideDown">
              <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">
                Country <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                required
                value={country}
                onChange={(e) => setCountry(e.target.value)}
                placeholder="e.g. US, IN, GB"
                className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-transparent text-ink-900 dark:text-ink-100 placeholder-ink-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
          )}
        </div>
      )}

      {/* Collapsible live LinkedIn jobs search URL section */}
      {liveUrl && (
        <div className="border-t border-ink-100 dark:border-ink-800 pt-4">
          <button
            type="button"
            onClick={() => setShowLiveUrl(!showLiveUrl)}
            className="flex items-center gap-2 text-xs font-semibold text-ink-500 dark:text-ink-400 hover:text-ink-700 focus:outline-none"
          >
            <LinkIcon className="w-3.5 h-3.5" />
            {showLiveUrl ? 'Hide' : 'Show'} Advanced Search URL (Debug)
          </button>
          {showLiveUrl && (
            <div className="mt-2 p-3 bg-ink-50 dark:bg-ink-950 rounded-xl text-xs font-mono text-ink-600 dark:text-ink-300 break-all border border-ink-150 dark:border-ink-850">
              {liveUrl}
            </div>
          )}
        </div>
      )}

      {/* Buttons */}
      <div className="flex items-center justify-between border-t border-ink-100 dark:border-ink-800 pt-6">
        <button
          type="button"
          onClick={handleReset}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium text-ink-700 dark:text-ink-300 hover:bg-ink-100 dark:hover:bg-ink-800 transition-colors focus:outline-none"
        >
          <RotateCcw className="w-4 h-4" />
          Reset Filters
        </button>

        <button
          type="submit"
          disabled={isLoading || designation.trim().length < 2 || !location.trim()}
          className="flex items-center gap-2 px-6 py-2.5 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed shadow-md shadow-brand-500/10 transition-all focus:outline-none"
        >
          <Search className="w-4 h-4" />
          Search Jobs
        </button>
      </div>
    </form>
  );
}
