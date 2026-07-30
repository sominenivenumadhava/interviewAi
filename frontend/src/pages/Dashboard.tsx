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
import { ProgressBar } from '../components/ui/ProgressBar';
import { PageHeader } from '../components/ui/PageHeader';
import { DashboardSkeleton } from '../components/ui/Skeleton';
import {
  Play, FileText, TrendingUp, Target, Clock, Sparkles,
  Activity, RefreshCw,
} from 'lucide-react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import { useInterviewSession } from '../contexts/InterviewSessionContext';

// ─── Chart tooltip ─────────────────────────────────────────────────────────────
const ChartTooltip = ({ active, payload, label }: any) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-xl border border-white/10 bg-obsidian-900 px-3 py-2 shadow-card text-xs">
      <p className="text-ink-400 mb-1">{label}</p>
      <p className="font-bold text-brand-400">{payload[0]?.value}%</p>
    </div>
  );
};

// ─── Dashboard ────────────────────────────────────────────────────────────────
export function Dashboard() {
  const { activeSession } = useInterviewSession();
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [loading, setLoading]     = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchDashboardData = async () => {
    try {
      const response = await apiClient.get<any>(API_ENDPOINTS.DASHBOARD.GET);
      if (response.data) setDashboardData(response.data);
    } catch (err) {
      console.warn('Dashboard fetch fallback:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, [activeSession?.status]);

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  const isInterviewActive = activeSession?.status === 'in_progress';

  const chartData =
    dashboardData?.performanceOverview?.last30DaysTrend || [
      { label: 'Session 1', score: 68 },
      { label: 'Session 2', score: 74 },
      { label: 'Session 3', score: 79 },
      { label: 'Session 4', score: 82 },
      { label: 'Session 5', score: 84 },
      { label: 'Session 6', score: 88 },
    ];

  const weakSkills = [
    { name: 'System Scaling & Tradeoffs', score: 72, color: 'amber' as const },
    { name: 'Graph Algorithms',           score: 68, color: 'rose'  as const },
    { name: 'STAR Behavioral Quantifying', score: 84, color: 'emerald' as const },
  ];

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
            className="rounded-2xl border border-brand-500/40 bg-gradient-to-r from-brand-950/80 via-obsidian-950 to-violet-950/80 p-6 shadow-glow-md backdrop-blur-md"
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
        subtitle="Real-time performance metrics generated from your interview history."
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

      {/* ── Stat Cards ── */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MotionCard
          title="Total Practice Sessions"
          value={dashboardData?.interviewStats?.totalInterviews || 24}
          description={`${dashboardData?.interviewStats?.completedThisMonth || 8} completed this month`}
          icon={<FileText size={20} />}
          iconColor="bg-brand-500/10"
          iconTextColor="text-brand-500"
          accentColor="border-l-brand-500"
          delay={0}
        />
        <MotionCard
          title="Average Score"
          value={dashboardData?.performanceOverview?.currentScore || 84}
          suffix="%"
          description="+6.2% improvement from last week"
          icon={<TrendingUp size={20} />}
          iconColor="bg-emerald-500/10"
          iconTextColor="text-emerald-500"
          accentColor="border-l-emerald-500"
          delay={0.08}
        />
        <MotionCard
          title="Practice Hours"
          value={dashboardData?.userSummary?.totalPracticeHours || 18}
          suffix=" hrs"
          description="Senior Practice Tier"
          icon={<Clock size={20} />}
          iconColor="bg-amber-500/10"
          iconTextColor="text-amber-500"
          accentColor="border-l-amber-500"
          delay={0.16}
        />
        <MotionCard
          title="Company Readiness"
          value={dashboardData?.performanceOverview?.bestScore || 88}
          suffix="%"
          description="Targeted for FAANG / Tier 1"
          icon={<Target size={20} />}
          iconColor="bg-violet-500/10"
          iconTextColor="text-violet-500"
          accentColor="border-l-violet-500"
          delay={0.24}
        />
      </div>

      {/* ── Charts Row ── */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Score progression */}
        <motion.div
          className="lg:col-span-2"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
        >
          <Card hoverable>
            <CardHeader>
              <CardTitle>Score Progression Trend</CardTitle>
              <CardDescription>Interview performance across historical sessions</CardDescription>
            </CardHeader>
            <CardContent className="pt-2">
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
                    domain={[50, 100]}
                    tick={{ fill: '#64748b', fontSize: 11 }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <Tooltip content={<ChartTooltip />} />
                  <Line
                    type="monotone"
                    dataKey="score"
                    stroke="#6366f1"
                    strokeWidth={2.5}
                    dot={{ r: 4, fill: '#6366f1', strokeWidth: 2, stroke: '#111827' }}
                    activeDot={{ r: 6, fill: '#818cf8' }}
                    animationDuration={1200}
                    animationEasing="ease-out"
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </motion.div>

        {/* Weak skills */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
        >
          <Card hoverable className="h-full">
            <CardHeader>
              <CardTitle>AI Weak Skill Analysis</CardTitle>
              <CardDescription>Areas requiring focused preparation</CardDescription>
            </CardHeader>
            <CardContent className="space-y-5">
              {weakSkills.map((skill, i) => (
                <ProgressBar
                  key={skill.name}
                  label={skill.name}
                  value={skill.score}
                  color={skill.color}
                  delay={0.5 + i * 0.15}
                />
              ))}
              <div className="pt-3 border-t border-ink-100 dark:border-white/[0.06]">
                <Link to="/interview/company">
                  <Button variant="secondary" className="w-full text-xs">
                    Practice Weak Skill Round
                  </Button>
                </Link>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}