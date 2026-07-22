import apiClient from '../lib/apiClient';

export interface JobSearchRequest {
  designation: string;
  location: string;
  numberOfJobs: number;
  experienceLevel?: string;
  workType?: string;
  employmentType?: string;
  datePosted?: string;
  scrapeCompanyDetails: boolean;
  splitByCity: boolean;
  country?: string;
}

export interface JobSearchResponse {
  searchId: string;
  apifyRunId: string;
  status: string;
  message: string;
}

export interface JobSearchStatusResponse {
  searchId: string;
  status: string;
  progressMessage: string;
  jobsCollected: number;
  requestedJobs: number;
}

export interface JobResult {
  id: string;
  title: string;
  companyName: string;
  companyLogo: string;
  companyLinkedInUrl: string;
  location: string;
  workType: string;
  employmentType: string;
  experienceLevel: string;
  salary: string;
  postedAt: string;
  applicantsCount: string;
  description: string;
  skills: string[];
  jobUrl: string;
  applyUrl: string;
  companyWebsite: string;
  companyIndustry: string;
  companySize: string;
  scrapedAt: string;
  saved: boolean;
}

export interface JobSearchResultsResponse {
  searchId: string;
  status: string;
  totalResults: number;
  jobs: JobResult[];
}

export interface SearchHistoryItem {
  id: string;
  designation: string;
  location: string;
  status: string;
  resultCount: number;
  requestedJobCount: number;
  createdAt: string;
  completedAt?: string;
  linkedinSearchUrl?: string;
}

export const jobSearchApi = {
  async startSearch(request: JobSearchRequest): Promise<JobSearchResponse> {
    const response = await apiClient.post<JobSearchResponse>('/api/jobs/search', request);
    return response.data!;
  },

  async getSearchStatus(searchId: string): Promise<JobSearchStatusResponse> {
    const response = await apiClient.get<JobSearchStatusResponse>(`/api/jobs/search/${searchId}/status`);
    return response.data!;
  },

  async getSearchResults(searchId: string): Promise<JobSearchResultsResponse> {
    const response = await apiClient.get<JobSearchResultsResponse>(`/api/jobs/search/${searchId}/results`);
    return response.data!;
  },

  async getSearchHistory(): Promise<SearchHistoryItem[]> {
    const response = await apiClient.get<SearchHistoryItem[]>('/api/jobs/search/history');
    return response.data || [];
  },

  async saveJob(jobId: string, job: JobResult): Promise<void> {
    await apiClient.post(`/api/jobs/${jobId}/save`, job);
  },

  async removeSavedJob(jobId: string): Promise<void> {
    await apiClient.delete(`/api/jobs/${jobId}/save`);
  },

  async getSavedJobs(): Promise<JobResult[]> {
    const response = await apiClient.get<JobResult[]>('/api/jobs/saved');
    return response.data || [];
  },
};
