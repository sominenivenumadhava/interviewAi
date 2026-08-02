import React, { useState, useEffect, useRef } from 'react';
import { DashboardSidebar } from '../../components/jobs/DashboardSidebar';
import { JobSearchForm } from '../../components/jobs/JobSearchForm';
import { SearchProgress } from '../../components/jobs/SearchProgress';
import { SearchSummary } from '../../components/jobs/SearchSummary';
import { JobFilters } from '../../components/jobs/JobFilters';
import { JobCard } from '../../components/jobs/JobCard';
import { JobsTable } from '../../components/jobs/JobsTable';
import { JobDetailsDrawer } from '../../components/jobs/JobDetailsDrawer';
import { EmptyState } from '../../components/jobs/EmptyState';
import { ErrorState } from '../../components/jobs/ErrorState';
import { SearchHistoryPanel } from '../../components/jobs/SearchHistoryPanel';
import { SavedJobsPanel } from '../../components/jobs/SavedJobsPanel';
import { LoadingSkeleton } from '../../components/jobs/LoadingSkeleton';

import {
  jobSearchApi,
  JobSearchRequest,
  JobResult,
  SearchHistoryItem
} from '../../services/jobSearchApi';

export function JobSearchDashboard() {
  // Navigation / Tabs state
  const [activeTab, setActiveTab] = useState('find-jobs');

  // Search execution & Polling states
  const [isLoading, setIsLoading] = useState(false);
  const [activeSearchId, setActiveSearchId] = useState<string | undefined>(undefined);
  const [searchStatus, setSearchStatus] = useState<string>('IDLE'); // IDLE, PENDING, RUNNING, SUCCEEDED, FAILED
  const [progressMessage, setProgressMessage] = useState<string>('');
  const [jobsCollected, setJobsCollected] = useState(0);
  const [requestedJobs, setRequestedJobs] = useState(25);

  // Job results & static panels state
  const [jobs, setJobs] = useState<JobResult[]>([]);
  const [history, setHistory] = useState<SearchHistoryItem[]>([]);
  const [savedJobs, setSavedJobs] = useState<JobResult[]>([]);
  
  // Filtering & Sorting states
  const [searchTerm, setSearchTerm] = useState('');
  const [locationFilter, setLocationFilter] = useState('');
  const [companyFilter, setCompanyFilter] = useState('');
  const [workTypeFilter, setWorkTypeFilter] = useState('');
  const [employmentTypeFilter, setEmploymentTypeFilter] = useState('');
  const [experienceLevelFilter, setExperienceLevelFilter] = useState('');
  const [showSavedOnly, setShowSavedOnly] = useState(false);
  const [sortBy, setSortBy] = useState('recent');
  const [viewMode, setViewMode] = useState<'card' | 'table'>('card');

  // Selection states (Drawer)
  const [selectedJob, setSelectedJob] = useState<JobResult | null>(null);
  const [isSavingAction, setIsSavingAction] = useState(false);

  // Error state
  const [errorTitle, setErrorTitle] = useState<string | undefined>(undefined);
  const [errorMessage, setErrorMessage] = useState<string | undefined>(undefined);

  // Ref for polling interval reference
  const pollingRef = useRef<NodeJS.Timeout | null>(null);

  // Initial load: Fetch search history and saved jobs
  useEffect(() => {
    loadInitialData();
    return () => {
      clearPolling();
    };
  }, []);

  const loadInitialData = async () => {
    try {
      const histData = await jobSearchApi.getSearchHistory();
      setHistory(histData);
      const savedData = await jobSearchApi.getSavedJobs();
      setSavedJobs(savedData);
    } catch (e) {
      console.error('Failed to load initial search data', e);
    }
  };

  const clearPolling = () => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
  };

  const startPolling = (searchId: string) => {
    clearPolling();
    pollingRef.current = setInterval(async () => {
      try {
        const statusResponse = await jobSearchApi.getSearchStatus(searchId);
        setSearchStatus(statusResponse.status);
        setProgressMessage(statusResponse.progressMessage);
        setJobsCollected(statusResponse.jobsCollected);
        setRequestedJobs(statusResponse.requestedJobs);

        const isFinal = ['SUCCEEDED', 'FAILED', 'ABORTED', 'TIMED-OUT'].includes(statusResponse.status);
        if (isFinal) {
          clearPolling();
          setIsLoading(false);

          if (statusResponse.status === 'SUCCEEDED') {
            // Fetch final dataset results
            const results = await jobSearchApi.getSearchResults(searchId);
            setJobs(results.jobs);
            // Refresh history
            loadInitialData();
          } else {
            setErrorTitle('The job search could not be completed');
            setErrorMessage(`Apify actor finished with status: ${statusResponse.status}. You may try searching again.`);
            setSearchStatus('FAILED');
          }
        }
      } catch (err: any) {
        logError(err);
      }
    }, 3000);
  };

  const logError = (err: any) => {
    clearPolling();
    setIsLoading(false);
    setSearchStatus('FAILED');
    setErrorTitle('Search Execution Error');
    
    // User-friendly mapping of typical status codes
    const status = err.status || (err.details && err.details.status);
    const message = err.message || '';

    if (status === 401 || message.includes('authentication failed')) {
      setErrorMessage('Apify authentication failed. Check the backend API token.');
    } else if (status === 402 || message.includes('credits')) {
      setErrorMessage('The Apify account does not have enough available usage credits.');
    } else if (status === 429 || message.includes('rate limit')) {
      setErrorMessage('Too many searches were started. Wait briefly and try again.');
    } else if (status === 408 || message.includes('timeout')) {
      setErrorMessage('The search took longer than expected. You can retry with fewer jobs.');
    } else if (message.includes('connect') || message.includes('network')) {
      setErrorMessage('Backend is unavailable. Make sure your server is running on port 8082.');
    } else {
      setErrorMessage(err.message || 'An unexpected networking error occurred. Please try again.');
    }
  };

  const handleStartSearch = async (formData: JobSearchRequest) => {
    setIsLoading(true);
    setSearchStatus('PENDING');
    setProgressMessage('Preparing LinkedIn search URL');
    setJobsCollected(0);
    setRequestedJobs(formData.numberOfJobs);
    setErrorTitle(undefined);
    setErrorMessage(undefined);
    setJobs([]);

    try {
      const response = await jobSearchApi.startSearch(formData);
      setActiveSearchId(response.searchId);
      setSearchStatus(response.status);
      startPolling(response.searchId);
    } catch (err: any) {
      logError(err);
    }
  };

  // Re-trigger search from history list
  const handleSelectHistorySearch = async (searchId: string) => {
    clearPolling();
    setIsLoading(false);
    setErrorTitle(undefined);
    setErrorMessage(undefined);
    setActiveSearchId(searchId);

    try {
      const statusRes = await jobSearchApi.getSearchStatus(searchId);
      setSearchStatus(statusRes.status);
      setProgressMessage(statusRes.progressMessage);
      setJobsCollected(statusRes.jobsCollected);
      setRequestedJobs(statusRes.requestedJobs);

      if (statusRes.status === 'SUCCEEDED') {
        const results = await jobSearchApi.getSearchResults(searchId);
        setJobs(results.jobs);
      } else if (['PENDING', 'RUNNING'].includes(statusRes.status)) {
        setIsLoading(true);
        startPolling(searchId);
      } else {
        setErrorTitle('Past Job Search Incomplete');
        setErrorMessage(`This search is in ${statusRes.status} state.`);
      }
    } catch (err) {
      logError(err);
    }
  };

  const handleToggleSaveJob = async (job: JobResult) => {
    setIsSavingAction(true);
    try {
      if (job.saved) {
        await jobSearchApi.removeSavedJob(job.id);
        
        // Update state in loaded jobs list
        setJobs(prev => prev.map(j => j.id === job.id ? { ...j, saved: false } : j));
        // Update in selected drawer if open
        if (selectedJob && selectedJob.id === job.id) {
          setSelectedJob(prev => prev ? { ...prev, saved: false } : null);
        }
      } else {
        const updatedJob = { ...job, saved: true };
        await jobSearchApi.saveJob(job.id, updatedJob);
        
        // Update state in loaded jobs list
        setJobs(prev => prev.map(j => j.id === job.id ? { ...j, saved: true } : j));
        if (selectedJob && selectedJob.id === job.id) {
          setSelectedJob(prev => prev ? { ...prev, saved: true } : null);
        }
      }
      // Refresh saved jobs list
      const savedData = await jobSearchApi.getSavedJobs();
      setSavedJobs(savedData);
    } catch (e) {
      console.error('Failed to toggle save job state', e);
    } finally {
      setIsSavingAction(false);
    }
  };

  // Client-side filtering & sorting calculations
  const filteredJobs = jobs.filter((job) => {
    // 1. Search within text fields
    const searchLower = searchTerm.toLowerCase();
    const matchesSearch = 
      !searchTerm ||
      job.title.toLowerCase().includes(searchLower) ||
      job.companyName.toLowerCase().includes(searchLower) ||
      job.location.toLowerCase().includes(searchLower) ||
      (job.description && job.description.toLowerCase().includes(searchLower)) ||
      (job.skills && job.skills.some(s => s.toLowerCase().includes(searchLower)));

    // 2. Exact match filters
    const matchesLocation = !locationFilter || job.location === locationFilter;
    const matchesCompany = !companyFilter || job.companyName === companyFilter;
    const matchesWorkType = !workTypeFilter || job.workType === workTypeFilter;
    const matchesEmployment = !employmentTypeFilter || job.employmentType === employmentTypeFilter;
    const matchesExperience = !experienceLevelFilter || job.experienceLevel === experienceLevelFilter;
    const matchesSavedOnly = !showSavedOnly || job.saved;

    return (
      matchesSearch && 
      matchesLocation && 
      matchesCompany && 
      matchesWorkType && 
      matchesEmployment && 
      matchesExperience && 
      matchesSavedOnly
    );
  });

  // Client-side sorting
  const sortedJobs = [...filteredJobs].sort((a, b) => {
    if (sortBy === 'recent') {
      return b.postedAt.localeCompare(a.postedAt);
    }
    if (sortBy === 'oldest') {
      return a.postedAt.localeCompare(b.postedAt);
    }
    if (sortBy === 'company-az') {
      return a.companyName.localeCompare(b.companyName);
    }
    if (sortBy === 'title-az') {
      return a.title.localeCompare(b.title);
    }
    return 0;
  });

  return (
    <div className="flex flex-col lg:flex-row min-h-[calc(100vh-80px)] -mx-4 -my-8 sm:-mx-6 lg:-mx-8">
      {/* Sidebar Navigation */}
      <DashboardSidebar activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Main Panel Content */}
      <main className="flex-1 bg-ink-50 dark:bg-ink-950 px-4 py-8 sm:px-6 lg:px-8 space-y-6 overflow-hidden">
        
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-5 border-b border-ink-200 dark:border-ink-800">
          <div>
            <h1 className="text-3xl font-black text-ink-900 dark:text-ink-100">Find Your Next Opportunity</h1>
            <p className="text-sm text-ink-500 dark:text-ink-400 mt-1.5">
              Search LinkedIn job listings by designation, location, experience and work preference.
            </p>
          </div>
        </div>

        {/* Dynamic content rendering based on active sidebar tab */}
        {activeTab === 'find-jobs' && (
          <div className="space-y-6">
            
            {/* Search form component */}
            <JobSearchForm onSubmit={handleStartSearch} isLoading={isLoading} />

            {/* Active search progress card */}
            {isLoading && (
              <SearchProgress 
                status={searchStatus}
                progressMessage={progressMessage}
                jobsCollected={jobsCollected}
                requestedJobs={requestedJobs}
              />
            )}

            {/* Error banner */}
            {errorMessage && (
              <ErrorState
                title={errorTitle || 'An error occurred'}
                message={errorMessage}
                searchId={activeSearchId}
                onRetry={activeSearchId ? () => handleSelectHistorySearch(activeSearchId) : undefined}
              />
            )}

            {/* Loaded Result Summary, Filters, and Jobs Grid */}
            {jobs.length > 0 && !isLoading && (
              <div className="space-y-6 animate-fadeIn">
                <SearchSummary jobs={jobs} />

                <JobFilters
                  jobs={jobs}
                  searchTerm={searchTerm}
                  onSearchChange={setSearchTerm}
                  locationFilter={locationFilter}
                  onLocationChange={setLocationFilter}
                  companyFilter={companyFilter}
                  onCompanyChange={setCompanyFilter}
                  workTypeFilter={workTypeFilter}
                  onWorkTypeChange={setWorkTypeFilter}
                  employmentTypeFilter={employmentTypeFilter}
                  onEmploymentTypeChange={setEmploymentTypeFilter}
                  experienceLevelFilter={experienceLevelFilter}
                  onExperienceLevelChange={setExperienceLevelFilter}
                  savedOnly={showSavedOnly}
                  onSavedOnlyChange={setShowSavedOnly}
                  sortBy={sortBy}
                  onSortByChange={setSortBy}
                  viewMode={viewMode}
                  onViewModeChange={setViewMode}
                />

                {sortedJobs.length === 0 ? (
                  <EmptyState 
                    title="No matching jobs found" 
                    message="Try changing your search terms or filters." 
                  />
                ) : viewMode === 'card' ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {sortedJobs.map((job) => (
                      <JobCard
                        key={job.id}
                        job={job}
                        onViewDetails={setSelectedJob}
                        onToggleSave={handleToggleSaveJob}
                        isSaving={isSavingAction}
                      />
                    ))}
                  </div>
                ) : (
                  <JobsTable
                    jobs={sortedJobs}
                    onViewDetails={setSelectedJob}
                    onToggleSave={handleToggleSaveJob}
                    isSaving={isSavingAction}
                  />
                )}
              </div>
            )}

            {/* Initial landing state */}
            {jobs.length === 0 && !isLoading && !errorMessage && (
              <EmptyState 
                title="Start your job search" 
                message="Enter a designation and location to discover matching opportunities." 
              />
            )}
          </div>
        )}

        {/* Tab: Saved Jobs */}
        {activeTab === 'saved-jobs' && (
          <div className="space-y-6">
            <h2 className="text-xl font-black text-ink-900 dark:text-ink-100">Saved Job Openings</h2>
            
            {savedJobs.length === 0 ? (
              <EmptyState 
                title="No saved jobs yet" 
                message="Opportunities you bookmark during job searches will appear here." 
              />
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {savedJobs.map((job) => (
                  <JobCard
                    key={job.id}
                    job={job}
                    onViewDetails={setSelectedJob}
                    onToggleSave={handleToggleSaveJob}
                    isSaving={isSavingAction}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab: Search History */}
        {activeTab === 'search-history' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-1">
              <SearchHistoryPanel 
                history={history} 
                onSelectSearch={handleSelectHistorySearch}
                activeSearchId={activeSearchId}
              />
            </div>
            
            <div className="lg:col-span-2">
              {jobs.length > 0 ? (
                <div className="space-y-6">
                  <div className="flex items-center justify-between">
                    <h3 className="font-bold text-lg text-ink-900 dark:text-ink-100">Search Results</h3>
                    <button
                      onClick={() => setJobs([])}
                      className="text-xs text-ink-500 hover:text-brand-500 flex items-center gap-1.5 focus:outline-none"
                    >
                      Close Results
                    </button>
                  </div>
                  
                  {viewMode === 'card' ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {sortedJobs.map((job) => (
                        <JobCard
                          key={job.id}
                          job={job}
                          onViewDetails={setSelectedJob}
                          onToggleSave={handleToggleSaveJob}
                          isSaving={isSavingAction}
                        />
                      ))}
                    </div>
                  ) : (
                    <JobsTable
                      jobs={sortedJobs}
                      onViewDetails={setSelectedJob}
                      onToggleSave={handleToggleSaveJob}
                      isSaving={isSavingAction}
                    />
                  )}
                </div>
              ) : (
                <div className="bg-white dark:bg-ink-900 border border-ink-200 dark:border-ink-800 rounded-2xl p-6 text-center text-ink-500 dark:text-ink-400">
                  Select a past search from the history panel to view its results.
                </div>
              )}
            </div>
          </div>
        )}

        {/* Tab: Dashboard (Summary widgets & mini history) */}
        {activeTab === 'dashboard' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <SearchSummary jobs={savedJobs} />
              
              <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-6 shadow-sm">
                <h3 className="font-bold text-base text-ink-900 dark:text-ink-100 mb-4">Job Hunting Activity</h3>
                <p className="text-sm text-ink-600 dark:text-ink-400">
                  Welcome to your Job Finder Dashboard. You can trigger searches to collect fresh job listings, bookmark opportunities, and view your detailed hunt history.
                </p>
              </div>
            </div>

            <div className="lg:col-span-1 space-y-6">
              <SearchHistoryPanel history={history.slice(0, 5)} onSelectSearch={handleSelectHistorySearch} />
              <SavedJobsPanel savedJobs={savedJobs.slice(0, 5)} onSelectJob={setSelectedJob} />
            </div>
          </div>
        )}

        {/* Tab: Settings */}
        {activeTab === 'settings' && (
          <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-6 shadow-sm max-w-xl space-y-6">
            <h2 className="text-xl font-bold text-ink-900 dark:text-ink-100">Job Finder Settings</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Apify Actor ID</label>
                <input
                  type="text"
                  disabled
                  value="hKByXkMQaC5Qt9UMN"
                  className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-ink-50 dark:bg-ink-950 text-ink-500 dark:text-ink-400 focus:outline-none"
                />
                <p className="text-xs text-ink-400 mt-1">Default LinkedIn Jobs Scraper actor.</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">LinkedIn Base URL</label>
                <input
                  type="text"
                  disabled
                  value="https://www.linkedin.com/jobs/search/"
                  className="w-full px-4 py-2.5 rounded-xl border border-ink-200 dark:border-ink-800 bg-ink-50 dark:bg-ink-950 text-ink-500 dark:text-ink-400 focus:outline-none"
                />
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Slide-out details drawer component */}
      <JobDetailsDrawer
        job={selectedJob}
        onClose={() => setSelectedJob(null)}
        onToggleSave={handleToggleSaveJob}
        isSaving={isSavingAction}
      />
    </div>
  );
}
