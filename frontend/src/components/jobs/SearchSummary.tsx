import React from 'react';
import { Briefcase, MapPin, Building2, Clock } from 'lucide-react';
import { JobResult } from '../../services/jobSearchApi';

interface SearchSummaryProps {
  jobs: JobResult[];
}

export function SearchSummary({ jobs }: SearchSummaryProps) {
  const totalJobs = jobs.length;
  
  const remoteJobs = jobs.filter(
    (j) => 
      (j.workType && j.workType.toLowerCase().includes('remote')) ||
      (j.location && j.location.toLowerCase().includes('remote'))
  ).length;

  const uniqueCompanies = new Set(jobs.map((j) => j.companyName).filter((c) => c && c !== 'Not provided')).size;

  const recentJobs = jobs.filter((j) => {
    if (!j.postedAt) return false;
    const time = j.postedAt.toLowerCase();
    return (
      time.includes('hour') || 
      time.includes('day') || 
      time.includes('minute') || 
      time.includes('yesterday') ||
      time.includes('24h') ||
      time.includes('1d')
    );
  }).length;

  const stats = [
    { label: 'Total Jobs', value: totalJobs, icon: Briefcase, color: 'text-blue-600 bg-blue-50 dark:text-blue-400 dark:bg-blue-950/20 border-blue-100 dark:border-blue-900/30' },
    { label: 'Remote Jobs', value: remoteJobs, icon: MapPin, color: 'text-purple-600 bg-purple-50 dark:text-purple-400 dark:bg-purple-950/20 border-purple-100 dark:border-purple-900/30' },
    { label: 'Companies Found', value: uniqueCompanies, icon: Building2, color: 'text-green-600 bg-green-50 dark:text-green-400 dark:bg-green-950/20 border-green-100 dark:border-green-900/30' },
    { label: 'Recently Posted', value: recentJobs, icon: Clock, color: 'text-amber-600 bg-amber-50 dark:text-amber-400 dark:bg-amber-950/20 border-amber-100 dark:border-amber-900/30' }
  ];

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, idx) => {
        const Icon = stat.icon;
        return (
          <div key={idx} className="bg-white dark:bg-ink-900 border border-ink-200 dark:border-ink-800 rounded-2xl p-4.5 shadow-sm flex items-center gap-4">
            <div className={`p-3 rounded-xl border ${stat.color}`}>
              <Icon className="w-5 h-5" />
            </div>
            <div>
              <div className="text-xs text-ink-500 dark:text-ink-400 font-medium">{stat.label}</div>
              <div className="text-xl font-black text-ink-900 dark:text-ink-100 mt-0.5">{stat.value}</div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
