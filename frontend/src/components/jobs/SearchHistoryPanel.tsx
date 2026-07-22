import React from 'react';
import { Clock, ArrowRight, CheckCircle2, XCircle, Loader2 } from 'lucide-react';
import { SearchHistoryItem } from '../../services/jobSearchApi';

interface SearchHistoryPanelProps {
  history: SearchHistoryItem[];
  onSelectSearch: (searchId: string) => void;
  activeSearchId?: string;
}

export function SearchHistoryPanel({ history, onSelectSearch, activeSearchId }: SearchHistoryPanelProps) {
  const getStatusIcon = (status: string) => {
    switch (status.toUpperCase()) {
      case 'SUCCEEDED':
        return <CheckCircle2 className="w-4 h-4 text-green-500 shrink-0" />;
      case 'FAILED':
      case 'ABORTED':
      case 'TIMED-OUT':
        return <XCircle className="w-4 h-4 text-red-500 shrink-0" />;
      case 'RUNNING':
      case 'PENDING':
        return <Loader2 className="w-4 h-4 text-blue-500 animate-spin shrink-0" />;
      default:
        return <Clock className="w-4 h-4 text-ink-400 shrink-0" />;
    }
  };

  if (history.length === 0) {
    return (
      <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-5 shadow-sm text-center">
        <p className="text-sm text-ink-500 dark:text-ink-400">No search history yet.</p>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-ink-900 rounded-2xl border border-ink-200 dark:border-ink-800 p-5 shadow-sm space-y-4">
      <h3 className="font-bold text-sm text-ink-900 dark:text-ink-100 flex items-center gap-2">
        <Clock className="w-4.5 h-4.5 text-ink-400" />
        Search History
      </h3>

      <div className="space-y-2.5 max-h-[380px] overflow-y-auto pr-1">
        {history.map((item) => {
          const isActive = item.id === activeSearchId;
          return (
            <div
              key={item.id}
              onClick={() => onSelectSearch(item.id)}
              className={`p-3 rounded-xl border text-left cursor-pointer transition-all flex items-center justify-between gap-3
                ${isActive 
                  ? 'border-brand-500 bg-brand-50/30 dark:bg-brand-950/15' 
                  : 'border-ink-150 dark:border-ink-850 hover:bg-ink-50 dark:hover:bg-ink-950'
                }
              `}
            >
              <div className="space-y-1 min-w-0">
                <div className="font-bold text-xs text-ink-900 dark:text-ink-100 truncate">
                  {item.designation}
                </div>
                <div className="text-[10px] text-ink-500 dark:text-ink-400 truncate">
                  {item.location}
                </div>
                <div className="flex items-center gap-1.5 text-[10px] font-semibold text-ink-500">
                  {getStatusIcon(item.status)}
                  <span className="capitalize">{item.status.toLowerCase()}</span>
                  {item.resultCount > 0 && (
                    <span>• {item.resultCount} jobs</span>
                  )}
                </div>
              </div>

              <ArrowRight className="w-4 h-4 text-ink-400 shrink-0" />
            </div>
          );
        })}
      </div>
    </div>
  );
}
