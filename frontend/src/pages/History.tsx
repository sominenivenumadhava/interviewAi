import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, ChevronRight, Calendar, Clock, Loader2, AlertTriangle } from 'lucide-react';
import { Card, CardContent } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/PageHeader';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';
import { useAuth } from '../contexts/AuthContext';

interface InterviewHistoryItem {
  id: string;
  sessionId: string;
  company: string;
  role: string;
  startedAt: string | null;
  completedAt: string | null;
  createdAt: string;
  durationMinutes: number | null;
  actualDurationSeconds: number | null;
  overallScore: number | null;
  status: string;
  interviewType: string;
  difficultyLevel: string;
}

// ─── Score mini bar ───────────────────────────────────────────────────────────
function ScoreBar({ score }: { score: number }) {
  const color =
    score >= 85 ? '#10b981' :
    score >= 70 ? '#f59e0b' : '#f43f5e';
  return (
    <div className="flex items-center gap-2">
      <div className="w-16 h-1.5 rounded-full bg-ink-100 dark:bg-white/8 overflow-hidden">
        <motion.div
          initial={{ width: 0 }}
          animate={{ width: `${score}%` }}
          transition={{ duration: 0.8, ease: [0.34, 1.56, 0.64, 1], delay: 0.3 }}
          className="h-full rounded-full"
          style={{ background: color }}
        />
      </div>
      <span className="text-sm font-bold tabular-nums" style={{ color }}>
        {score}%
      </span>
    </div>
  );
}

/** Format a duration for display: prefer actualDurationSeconds, fall back to durationMinutes */
function formatDuration(item: InterviewHistoryItem): string {
  if (item.actualDurationSeconds && item.actualDurationSeconds > 0) {
    const mins = Math.round(item.actualDurationSeconds / 60);
    return `${mins}m`;
  }
  if (item.durationMinutes && item.durationMinutes > 0) {
    return `${item.durationMinutes}m`;
  }
  return '—';
}

/** Format a real date to a readable string */
function formatDate(dateStr: string | null): string {
  if (!dateStr) return '—';
  try {
    return new Date(dateStr).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}

// ─── History ──────────────────────────────────────────────────────────────────
export function History() {
  const { isLoading: authLoading, isAuthenticated } = useAuth();
  const [search, setSearch] = useState('');
  const [interviews, setInterviews] = useState<InterviewHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const retryRef = React.useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => { return () => { if (retryRef.current) clearTimeout(retryRef.current); }; }, []);

  useEffect(() => {
    if (authLoading || !isAuthenticated) return;

    const fetchHistory = async (isRetry = false) => {
      setLoading(true);
      setError(null);
      try {
        // GET /api/v1/interviews — returns PageResponse<InterviewResponse>
        const res = await apiClient.get<any>(API_ENDPOINTS.INTERVIEW.LIST);
        const payload = res.data?.data ?? res.data;
        // Handle both paged { content: [...] } and direct array responses
        const items: any[] = Array.isArray(payload)
          ? payload
          : Array.isArray(payload?.content)
          ? payload.content
          : [];

        setInterviews(
          items.map((item: any) => ({
            id: item.id ?? item.sessionId ?? String(Math.random()),
            sessionId: item.sessionId ?? item.id ?? '',
            company: item.company ?? '—',
            role: item.role ?? '—',
            startedAt: item.startedAt ?? null,
            completedAt: item.completedAt ?? null,
            createdAt: item.createdAt ?? item.scheduledAt ?? '',
            durationMinutes: item.durationMinutes ?? null,
            actualDurationSeconds: item.actualDurationSeconds ?? null,
            overallScore: item.overallScore != null ? Math.round(item.overallScore) : null,
            status: item.status ?? 'UNKNOWN',
            interviewType: item.interviewType ?? '—',
            difficultyLevel: item.difficultyLevel ?? '—',
          }))
        );
      } catch (err: any) {
        console.error('Failed to fetch interview history:', err);
        if (!isRetry) {
          retryRef.current = setTimeout(() => fetchHistory(true), 2000);
        } else {
          setError('Could not load your interview history. Please try again.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [authLoading, isAuthenticated]);

  const filteredHistory = interviews.filter(
    (h) =>
      h.company.toLowerCase().includes(search.toLowerCase()) ||
      h.role.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-8">
      <PageHeader
        title="Interview History"
        subtitle="Review your past mock interviews and track your progress over time."
      />

      <Card>
        {/* Search bar */}
        <div className="border-b border-ink-100 dark:border-white/[0.06] px-5 py-4">
          <Input
            placeholder="Search by company or role…"
            icon={<Search size={15} />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <CardContent className="p-0">
          {/* Loading */}
          {loading && (
            <div className="flex flex-col items-center justify-center py-20 gap-3 text-center">
              <Loader2 size={28} className="animate-spin text-brand-400" />
              <p className="text-sm text-ink-400">Loading your interview history…</p>
            </div>
          )}

          {/* Error */}
          {!loading && error && (
            <div className="flex flex-col items-center justify-center py-20 gap-3 text-center">
              <AlertTriangle size={28} className="text-rose-400" />
              <p className="text-sm text-rose-400">{error}</p>
            </div>
          )}

          {/* Empty state — no interviews at all */}
          {!loading && !error && interviews.length === 0 && (
            <div className="flex flex-col items-center justify-center py-20 text-center">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-ink-100 dark:bg-white/5 mb-4">
                <Search size={28} className="text-ink-300 dark:text-ink-600" />
              </div>
              <p className="text-base font-semibold text-ink-600 dark:text-ink-300">
                No interviews yet
              </p>
              <p className="text-sm text-ink-400 mt-1">
                Complete your first mock interview to see your history here.
              </p>
              <Link to="/interview/company" className="mt-4">
                <Button variant="gradient" size="sm">Start Interview</Button>
              </Link>
            </div>
          )}

          {/* Empty state — search returned nothing */}
          {!loading && !error && interviews.length > 0 && filteredHistory.length === 0 && (
            <div className="flex flex-col items-center justify-center py-20 text-center">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-ink-100 dark:bg-white/5 mb-4">
                <Search size={28} className="text-ink-300 dark:text-ink-600" />
              </div>
              <p className="text-base font-semibold text-ink-600 dark:text-ink-300">No results found</p>
              <p className="text-sm text-ink-400 mt-1">Try searching with a different company or role name.</p>
            </div>
          )}

          {/* Table */}
          {!loading && !error && filteredHistory.length > 0 && (
            <div className="overflow-x-auto">
              <table className="premium-table">
                <thead>
                  <tr>
                    <th>Company &amp; Role</th>
                    <th>Date</th>
                    <th>Duration</th>
                    <th>Score</th>
                    <th>Type</th>
                    <th className="text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <AnimatePresence>
                    {filteredHistory.map((interview, i) => (
                      <motion.tr
                        key={interview.id}
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.3, delay: i * 0.05, ease: [0.25, 0.1, 0.25, 1] }}
                        className="group"
                      >
                        <td>
                          <div className="font-semibold text-ink-900 dark:text-white">{interview.company}</div>
                          <div className="text-xs text-ink-400 dark:text-ink-500 mt-0.5">{interview.role}</div>
                        </td>
                        <td>
                          <div className="flex items-center gap-1.5 text-sm text-ink-500 dark:text-ink-400">
                            <Calendar size={13} className="shrink-0" />
                            {/* Show completedAt first, then startedAt, then createdAt */}
                            {formatDate(interview.completedAt ?? interview.startedAt ?? interview.createdAt)}
                          </div>
                        </td>
                        <td>
                          <div className="flex items-center gap-1.5 text-sm text-ink-500 dark:text-ink-400">
                            <Clock size={13} className="shrink-0" />
                            {formatDuration(interview)}
                          </div>
                        </td>
                        <td>
                          {interview.overallScore != null ? (
                            <ScoreBar score={interview.overallScore} />
                          ) : (
                            <span className="text-xs text-ink-500 italic">Not scored</span>
                          )}
                        </td>
                        <td>
                          <Badge className="text-[10px] uppercase font-bold tracking-wider px-2.5 py-1 bg-brand-500/20 text-brand-300 border border-brand-500/30">
                            {interview.interviewType}
                          </Badge>
                        </td>
                        <td className="text-right">
                          <Link to="/evaluation">
                            <Button variant="outline" size="sm" className="gap-1 text-xs text-brand-400 border-brand-500/30 hover:bg-brand-500/10">
                              View Report <ChevronRight size={13} />
                            </Button>
                          </Link>
                        </td>
                      </motion.tr>
                    ))}
                  </AnimatePresence>
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}