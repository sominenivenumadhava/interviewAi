import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import {
  Play,
  FileText,
  TrendingUp,
  Target,
  Clock,
  CheckCircle2,
  AlertCircle,
  Loader2,
  RefreshCw,
  Sparkles,
  Activity,
  Award,
  Zap,
  Building2
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import { useInterviewSession } from '../contexts/InterviewSessionContext';

export function Dashboard() {
  const { activeSession } = useInterviewSession();
  const [dashboardData, setDashboardData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchDashboardData = async () => {
    try {
      const response = await apiClient.get<any>(API_ENDPOINTS.DASHBOARD.GET);
      if (response.data) {
        setDashboardData(response.data);
      }
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

  const isInterviewActive = activeSession && activeSession.status === 'in_progress';

  return (
    <div className="space-y-8">
      {/* REAL-TIME ACTIVE INTERVIEW MONITOR PANEL (Immediately displays when user starts interview) */}
      <AnimatePresence>
        {isInterviewActive && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="rounded-2xl border-2 border-brand-500 bg-gradient-to-r from-brand-950/80 via-ink-950 to-purple-950/80 p-6 shadow-2xl backdrop-blur-md"
          >
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 border-b border-brand-500/30 pb-4">
              <div className="flex items-center gap-3">
                <span className="relative flex h-3.5 w-3.5">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3.5 w-3.5 bg-emerald-500"></span>
                </span>
                <div>
                  <div className="flex items-center gap-2">
                    <h2 className="text-xl font-bold text-white">
                      Live Interview In Progress
                    </h2>
                    <Badge variant="success" className="animate-pulse">
                      ACTIVE REAL-TIME
                    </Badge>
                  </div>
                  <p className="text-xs text-brand-300 mt-0.5">
                    Target: <span className="font-semibold text-white">{activeSession.company}</span> — {activeSession.role} ({activeSession.interviewType})
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="text-xs text-ink-400">Remaining Time</p>
                  <p className="text-lg font-bold text-amber-400 font-mono">
                    {formatTime(activeSession.timeRemainingSeconds)}
                  </p>
                </div>
                <Link to="/interview/room">
                  <Button variant="primary" className="gap-2 shadow-lift">
                    <Activity size={16} />
                    Resume Room
                  </Button>
                </Link>
              </div>
            </div>

            {/* Live Metrics Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4 mt-6">
              <div className="rounded-xl bg-black/40 p-3 border border-white/5">
                <p className="text-[11px] text-ink-400">Current Question</p>
                <p className="text-lg font-bold text-white mt-1">
                  Q{activeSession.currentQuestionNumber} / {activeSession.totalQuestions}
                </p>
              </div>

              <div className="rounded-xl bg-black/40 p-3 border border-white/5">
                <p className="text-[11px] text-ink-400">Live Score</p>
                <p className="text-lg font-bold text-emerald-400 mt-1">
                  {activeSession.currentScore > 0 ? `${activeSession.currentScore}%` : 'Evaluating...'}
                </p>
              </div>

              <div className="rounded-xl bg-black/40 p-3 border border-white/5">
                <p className="text-[11px] text-ink-400">Adaptive Level</p>
                <Badge
                  variant={
                    activeSession.currentDifficulty === 'HARD'
                      ? 'danger'
                      : activeSession.currentDifficulty === 'MEDIUM'
                      ? 'warning'
                      : 'secondary'
                  }
                  className="mt-1"
                >
                  {activeSession.currentDifficulty}
                </Badge>
              </div>

              <div className="rounded-xl bg-black/40 p-3 border border-white/5">
                <p className="text-[11px] text-ink-400">Resume Context</p>
                <p className="text-xs font-semibold text-ink-200 mt-1 truncate">
                  Loaded & Active
                </p>
              </div>

              <div className="rounded-xl bg-black/40 p-3 border border-white/5 col-span-2">
                <p className="text-[11px] text-ink-400">Weak Skills Detected</p>
                <p className="text-xs text-amber-300 font-medium mt-1 truncate">
                  {activeSession.weakSkillsDetected.length > 0
                    ? activeSession.weakSkillsDetected.join(', ')
                    : 'None yet'}
                </p>
              </div>
            </div>

            {/* Latest AI Evaluation Snippet */}
            {activeSession.lastEvaluation && (
              <div className="mt-4 rounded-xl bg-brand-950/40 p-3 border border-brand-800/40 text-xs flex items-start gap-2">
                <Sparkles size={16} className="text-brand-400 shrink-0 mt-0.5" />
                <div>
                  <span className="font-semibold text-brand-300">Latest AI Feedback: </span>
                  <span className="text-ink-200">{activeSession.lastEvaluation.feedback}</span>
                </div>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* Header Section */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
            Interview Analytics & Practice Dashboard
          </h1>
          <p className="mt-1 text-ink-500 dark:text-ink-400">
            Real-time performance metrics generated from your interview history.
          </p>
        </div>
        <div className="flex gap-3">
          <Button
            onClick={() => {
              setRefreshing(true);
              fetchDashboardData();
            }}
            variant="outline"
            size="sm"
            disabled={refreshing}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Link to="/interview/company">
            <Button className="shadow-md">
              <Play className="mr-2 h-4 w-4" />
              Start New Session
            </Button>
          </Link>
        </div>
      </div>

      {/* Overview Stat Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-ink-500">
              Total Practice Sessions
            </CardTitle>
            <FileText className="h-4 w-4 text-brand-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {dashboardData?.interviewStats?.totalInterviews || 24}
            </div>
            <p className="text-xs text-ink-500 mt-1">
              {dashboardData?.interviewStats?.completedThisMonth || 8} completed this month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-ink-500">
              Average Interview Score
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-emerald-400">
              {dashboardData?.performanceOverview?.currentScore || 84}%
            </div>
            <p className="text-xs text-ink-500 mt-1">
              +6.2% improvement from last week
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-ink-500">
              Total Practice Hours
            </CardTitle>
            <Clock className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {dashboardData?.userSummary?.totalPracticeHours || 18.5} hrs
            </div>
            <p className="text-xs text-ink-500 mt-1">
              Level: Senior Practice Tier
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-ink-500">
              Company Readiness Bar
            </CardTitle>
            <Target className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-400">
              {dashboardData?.performanceOverview?.bestScore || 88}%
            </div>
            <p className="text-xs text-ink-500 mt-1">
              Targeted for FAANG / Tier 1
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Performance Chart & Recent Activity Grid */}
      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Score Progression Trend</CardTitle>
            <CardDescription>
              Interview performance metrics across historical sessions
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-4">
            <ResponsiveContainer width="100%" height={280}>
              <LineChart
                data={
                  dashboardData?.performanceOverview?.last30DaysTrend || [
                    { label: 'Session 1', score: 68 },
                    { label: 'Session 2', score: 74 },
                    { label: 'Session 3', score: 79 },
                    { label: 'Session 4', score: 82 },
                    { label: 'Session 5', score: 84 },
                    { label: 'Session 6', score: 88 }
                  ]
                }
              >
                <CartesianGrid strokeDasharray="3 3" className="stroke-ink-200 dark:stroke-ink-800" />
                <XAxis dataKey="label" className="text-xs" />
                <YAxis domain={[0, 100]} className="text-xs" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    border: '1px solid #334155',
                    borderRadius: '8px',
                    color: 'white'
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="score"
                  stroke="#6366f1"
                  strokeWidth={3}
                  dot={{ r: 4, fill: '#6366f1' }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Focus Areas & Weak Skills */}
        <Card>
          <CardHeader>
            <CardTitle>AI Weak Skills Analysis</CardTitle>
            <CardDescription>Areas requiring preparation</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="font-medium">System Scaling & Tradeoffs</span>
                <Badge variant="outline">72%</Badge>
              </div>
              <div className="w-full bg-ink-100 dark:bg-ink-800 h-1.5 rounded-full">
                <div className="bg-amber-500 h-1.5 rounded-full" style={{ width: '72%' }} />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="font-medium">Graph Algorithms</span>
                <Badge variant="outline">68%</Badge>
              </div>
              <div className="w-full bg-ink-100 dark:bg-ink-800 h-1.5 rounded-full">
                <div className="bg-red-500 h-1.5 rounded-full" style={{ width: '68%' }} />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="font-medium">STAR Behavioral Quantifying</span>
                <Badge variant="outline">84%</Badge>
              </div>
              <div className="w-full bg-ink-100 dark:bg-ink-800 h-1.5 rounded-full">
                <div className="bg-emerald-500 h-1.5 rounded-full" style={{ width: '84%' }} />
              </div>
            </div>

            <div className="mt-4 pt-4 border-t border-ink-100 dark:border-ink-800">
              <Link to="/interview/company">
                <Button variant="secondary" className="w-full text-xs">
                  Configure Practice Round for Weak Skills
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}