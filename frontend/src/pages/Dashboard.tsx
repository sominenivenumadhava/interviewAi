import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { MotionCard } from '../components/ui/MotionCard';
import { PageHeader } from '../components/ui/PageHeader';
import { DashboardSkeleton } from '../components/ui/Skeleton';
import {
  Play, FileText, TrendingUp, Target, Clock, Sparkles,
  Activity, RefreshCw, BarChart2,
} from 'lucide-react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { useInterviewSession } from '../contexts/InterviewSessionContext';

// ─── Chart tooltip ─────────────────────────────────────────────────────────────
const ChartTooltip = ({ active, payload, label }: any) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-xl border border-white/10 bg-obsidian-900 px-3 py-2 shadow-card text-xs">
      <p className="text-ink-400 mb-1">{label}</p>
      <p className="font-bold text-brand-400">{payload[0]?.value?.toFixed(1)}%</p>
    </div>
  );
};

// ─── Empty chart placeholder ────────────────────────────────────────────────────
function EmptyChart() {
  return (
    <div className="flex flex-col items-center justify-center h-[260px] gap-3 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-500/10 text-brand-500">
        <BarChart2 size={28} />
      </div>
      <div>
        <p className="text-sm font-semibold text-ink-300">No interview data yet</p>
        <p className="text-xs text-ink-500 mt-1">Complete your first mock interview to see your score trend here.</p>
      </div>
      <Link to="/interview/company">
        <Button variant="gradient" size="sm" className="mt-2">
          <Play className="h-3.5 w-3.5" /> Start First Interview
        </Button>
      </Link>
    </div>
  );
}

// ─── Dashboard ────────────────────────────────────────────────────────────────
export function Dashboard() {
  const { activeSession } = useInterviewSession();
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [loading, setLoading]      = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError]          = useState<string | null>(null);

  const fetchDashboardData = async () => {
    setError(null);
    try {
      const response = await apiClient.get<any>(API_ENDPOINTS.DASHBOARD.GET);
      if (response.data) {
        // Handle both wrapped { data: ... } and direct response shapes
        const payload = response.data?.data ?? response.data;
        setDashboardData(payload);
      }
    } catch (err: any) {
      console.warn('Dashboard fetch error:', err);
      setError('Could not load dashboard data. Please refresh.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  // Fetch on mount and whenever a session moves to 'completed'
  useEffect(() => {
    fetchDashboardData();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // If a session just finished (completed), refresh dashboard stats
  useEffect(() => {
    if (activeSession?.status === 'completed') {
      fetchDashboardData();
    }
  }, [activeSession?.status]); // eslint-disable-line react-hooks/exhaustive-deps

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  const isInterviewActive = activeSession?.status === 'in_progress';

  // ── Real chart data: from backend performanceOverview.last30DaysTrend ──────
  // Each entry has { date, score, label } from DashboardServiceImpl.buildScoreTrend()
  const rawTrend: any[] = dashboardData?.performanceOverview?.last30DaysTrend ?? [];
  const chartData = rawTrend.map((t: any) => ({
    label: t.label ?? t.date ?? '',
    score: typeof t.score === 'number' ? Math.round(t.score) : 0,
  }));
  const hasChartData = chartData.length > 0;

  // ── Stats: use real API values, fall back to 0 (not fake numbers) ──────────
  const totalSessions  = dashboardData?.interviewStats?.totalInterviews   ?? 0;
  const avgScore       = dashboardData?.performanceOverview?.averageScore  ?? 0;
  const practiceHours  = dashboardData?.userSummary?.totalPracticeHours   ?? 0;
  const bestScore      = dashboardData?.performanceOverview?.bestScore     ?? 0;
  const completedMonth = dashboardData?.interviewStats?.completedThisMonth ?? 0;
  const improvement    = dashboardData?.performanceOverview?.improvement   ?? 0;
  const userLevel      = dashboardData?.userSummary?.currentLevel         ?? 'Beginner';
  const bestRole       = dashboardData?.performanceOverview?.bestScoreRole ?? '';
  const trend          = dashboardData?.performanceOverview?.trend         ?? 'NO_DATA';

  const improvementText = trend === 'UP'
    ? `+${improvement?.toFixed(1)}% from last session`
    : trend === 'DOWN'
    ? `${improvement?.toFixed(1)}% from last session`
    : trend === 'NO_DATA' || totalSessions === 0
    ? 'Complete an interview to track'
    : 'No change from last session';

  if (loading) return <DashboardSkeleton />;

  return (
    <div className="space-y-8">

      {/* ── Active Interview Banner ── */}
      <AnimatePresence>
        {isInterviewActive && (
          <motion.div
            initial={{ opacity: 0, y: -16 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -16 }}
            transition={{ duration: 0.35 }}
            className="rounded-2xl border border-brand-500/40 bg-gradient-to-r from-brand-950/80 via-obsidian-950 to-cyan-950/80 p-6 shadow-glow-md backdrop-blur-md"
          >
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 pb-4 border-b border-brand-500/20">
              <div className="flex items-center gap-3">
                <span className="live-dot" />
                <div>
                  <div className="flex items-center gap-2">
                    <h2 className="text-lg font-bold text-white">Live Interview In Progress</h2>
                    <Badge variant="success" pulse>ACTIVE</Badge>
                  </div>
                  <p className="text-xs text-brand-300 mt-0.5">
                    Target: <span className="font-semibold text-white">{activeSession.company}</span>{' '}
                    — {activeSession.role} ({activeSession.interviewType})
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="text-xs text-ink-400">Remaining Time</p>
                  <p className="text-xl font-bold text-amber-400 font-mono tabular-nums">
                    {formatTime(activeSession.timeRemainingSeconds)}
                  </p>
                </div>
                <Link to="/interview/room">
                  <Button variant="gradient" className="gap-2">
                    <Activity size={16} />Resume Room
                  </Button>
                </Link>
              </div>
            </div>

            {/* Live Metrics */}
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 mt-5">
              {[
                { label: 'Current Question', value: `Q${activeSession.currentQuestionNumber} / ${activeSession.totalQuestions}` },
                { label: 'Live Score', value: activeSession.currentScore > 0 ? `${activeSession.currentScore}%` : 'Eval…', highlight: true },
                { label: 'Resume', value: 'Loaded & Active' },
              ].map((m, i) => (
                <div key={i} className="rounded-xl bg-black/30 border border-white/5 p-3">
                  <p className="text-[10px] text-ink-500 uppercase tracking-wider mb-1">{m.label}</p>
                  <p className={`text-base font-bold ${m.highlight ? 'text-emerald-400' : 'text-white'}`}>
                    {m.value}
                  </p>
                </div>
              ))}
              <div className="rounded-xl bg-black/30 border border-white/5 p-3">
                <p className="text-[10px] text-ink-500 uppercase tracking-wider mb-1">Difficulty</p>
                <Badge
                  variant={
                    activeSession.currentDifficulty === 'HARD' ? 'danger' :
                    activeSession.currentDifficulty === 'MEDIUM' ? 'warning' : 'secondary'
                  }
                >
                  {activeSession.currentDifficulty}
                </Badge>
              </div>
              <div className="rounded-xl bg-black/30 border border-white/5 p-3 col-span-2">
                <p className="text-[10px] text-ink-500 uppercase tracking-wider mb-1">Weak Skills</p>
                <p className="text-xs text-amber-300 font-medium truncate">
                  {activeSession.weakSkillsDetected.length > 0
                    ? activeSession.weakSkillsDetected.join(', ')
                    : 'None detected yet'}
                </p>
              </div>
            </div>

            {activeSession.lastEvaluation && (
              <div className="mt-4 rounded-xl bg-brand-950/50 border border-brand-800/30 p-3 flex items-start gap-2 text-xs">
                <Sparkles size={14} className="text-brand-400 shrink-0 mt-0.5" />
                <div>
                  <span className="font-semibold text-brand-300">Latest AI Feedback: </span>
                  <span className="text-ink-300">{activeSession.lastEvaluation.feedback}</span>
                </div>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Page Header ── */}
      <PageHeader
        title="Interview Dashboard"
        subtitle={
          totalSessions === 0
            ? "Welcome! Start your first mock interview to see your performance data."
            : "Real-time performance metrics generated from your interview history."
        }
      >
        <Button
          onClick={() => { setRefreshing(true); fetchDashboardData(); }}
          variant="outline"
          size="sm"
          disabled={refreshing}
        >
          <RefreshCw className={`h-3.5 w-3.5 ${refreshing ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
        <Link to="/interview/company">
          <Button variant="gradient" size="sm">
            <Play className="h-3.5 w-3.5" />
            New Session
          </Button>
        </Link>
      </PageHeader>

      {/* ── Error Banner ── */}
      {error && (
        <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-400 flex items-center gap-2">
          <span>{error}</span>
          <button
            onClick={() => { setRefreshing(true); fetchDashboardData(); }}
            className="ml-auto text-xs underline hover:no-underline"
          >
            Retry
          </button>
        </div>
      )}

      {/* ── Stat Cards ── */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MotionCard
          title="Total Practice Sessions"
          value={totalSessions}
          description={
            totalSessions === 0
              ? 'No sessions yet — start now!'
              : `${completedMonth} completed this month`
          }
          icon={<FileText size={20} />}
          iconColor="bg-brand-500/10"
          iconTextColor="text-brand-500"
          accentColor="border-l-brand-500"
          delay={0}
        />
        <MotionCard
          title="Average Score"
          value={avgScore > 0 ? Math.round(avgScore) : '—'}
          suffix={avgScore > 0 ? '%' : ''}
          description={improvementText}
          icon={<TrendingUp size={20} />}
          iconColor="bg-emerald-500/10"
          iconTextColor="text-emerald-500"
          accentColor="border-l-emerald-500"
          delay={0.08}
        />
        <MotionCard
          title="Practice Hours"
          value={practiceHours > 0 ? practiceHours : '—'}
          suffix={practiceHours > 0 ? ' hrs' : ''}
          description={practiceHours === 0 ? 'No practice recorded yet' : userLevel + ' Tier'}
          icon={<Clock size={20} />}
          iconColor="bg-amber-500/10"
          iconTextColor="text-amber-500"
          accentColor="border-l-amber-500"
          delay={0.16}
        />
        <MotionCard
          title="Best Score"
          value={bestScore > 0 ? Math.round(bestScore) : '—'}
          suffix={bestScore > 0 ? '%' : ''}
          description={bestScore > 0 && bestRole ? `Best in: ${bestRole}` : 'Complete an interview to track'}
          icon={<Target size={20} />}
          iconColor="bg-cyan-500/10"
          iconTextColor="text-cyan-500"
          accentColor="border-l-cyan-500"
          delay={0.24}
        />
      </div>

      {/* ── Score Progression Chart ── */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.3 }}
      >
        <Card hoverable>
          <CardHeader>
            <CardTitle>Score Progression Trend</CardTitle>
            <CardDescription>
              {hasChartData
                ? `Your interview performance across ${chartData.length} session${chartData.length !== 1 ? 's' : ''}`
                : 'Interview performance across historical sessions'}
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-2">
            {hasChartData ? (
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" vertical={false} />
                  <XAxis
                    dataKey="label"
                    tick={{ fill: '#64748b', fontSize: 11 }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <YAxis
                    domain={[0, 100]}
                    tick={{ fill: '#64748b', fontSize: 11 }}
                    axisLine={false}
                    tickLine={false}
                    tickFormatter={(v) => `${v}%`}
                  />
                  <Tooltip content={<ChartTooltip />} />
                  <Line
                    type="monotone"
                    dataKey="score"
                    stroke="#14b8a6"
                    strokeWidth={2.5}
                    dot={{ r: 4, fill: '#14b8a6', strokeWidth: 2, stroke: '#111827' }}
                    activeDot={{ r: 6, fill: '#818cf8' }}
                    animationDuration={1200}
                    animationEasing="ease-out"
                  />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <EmptyChart />
            )}
          </CardContent>
        </Card>
      </motion.div>

    </div>
  );
}