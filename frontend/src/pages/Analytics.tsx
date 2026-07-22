import React, { useState, useEffect } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar
} from 'recharts';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription
} from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Award, Target, TrendingUp, Clock, CheckCircle2, AlertTriangle, BookOpen, ExternalLink } from 'lucide-react';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

export function Analytics() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await apiClient.get(API_ENDPOINTS.ANALYTICS.USER);
        if (res.data) {
          setData(res.data);
        }
      } catch (err) {
        console.warn('Analytics API fallback loaded:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

  const weeklyPracticeData = [
    { name: 'Mon', hours: 1.5 },
    { name: 'Tue', hours: 2.0 },
    { name: 'Wed', hours: 1.0 },
    { name: 'Thu', hours: 3.2 },
    { name: 'Fri', hours: 2.5 },
    { name: 'Sat', hours: 4.0 },
    { name: 'Sun', hours: 3.0 }
  ];

  const scoreTrendData = [
    { date: 'Week 1', score: 68 },
    { date: 'Week 2', score: 74 },
    { date: 'Week 3', score: 78 },
    { date: 'Week 4', score: 82 },
    { date: 'Week 5', score: 86 }
  ];

  const skillRadarData = [
    { subject: 'Technical Accuracy', score: 85 },
    { subject: 'Communication', score: 90 },
    { subject: 'System Architecture', score: 76 },
    { subject: 'STAR Behavioral', score: 92 },
    { subject: 'Coding Speed', score: 80 },
    { subject: 'Problem Solving', score: 84 }
  ];

  const companyPerfData = [
    { company: 'Google', score: 84 },
    { company: 'Microsoft', score: 88 },
    { company: 'Amazon', score: 82 },
    { company: 'Meta', score: 78 },
    { company: 'Stripe', score: 86 }
  ];

  const difficultyDistribution = [
    { name: 'Easy', value: 20 },
    { name: 'Medium', value: 55 },
    { name: 'Hard', value: 25 }
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Comprehensive Interview Analytics
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          In-depth breakdown across skill heatmaps, company performance, difficulty distribution, and learning pathways.
        </p>
      </div>

      {/* Metric Summaries */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-ink-500 uppercase">Overall Score</p>
              <p className="text-2xl font-bold text-emerald-400 mt-1">84.5%</p>
            </div>
            <Award className="h-8 w-8 text-emerald-500" />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-ink-500 uppercase">Completion Rate</p>
              <p className="text-2xl font-bold text-brand-400 mt-1">96%</p>
            </div>
            <CheckCircle2 className="h-8 w-8 text-brand-500" />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-ink-500 uppercase">Avg Duration</p>
              <p className="text-2xl font-bold text-amber-400 mt-1">42 mins</p>
            </div>
            <Clock className="h-8 w-8 text-amber-500" />
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-ink-500 uppercase">Company Readiness</p>
              <p className="text-2xl font-bold text-purple-400 mt-1">Tier 1 Ready</p>
            </div>
            <Target className="h-8 w-8 text-purple-500" />
          </CardContent>
        </Card>
      </div>

      {/* Row 1 Charts — Radar Skill Heatmap & Score Progression */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Radar Skill Heatmap */}
        <Card>
          <CardHeader>
            <CardTitle>Skill Performance Radar</CardTitle>
            <CardDescription>Multi-dimensional interview competency</CardDescription>
          </CardHeader>
          <CardContent className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={skillRadarData}>
                <PolarGrid stroke="#334155" />
                <PolarAngleAxis dataKey="subject" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <PolarRadiusAxis domain={[0, 100]} />
                <Radar name="Candidate Score" dataKey="score" stroke="#6366f1" fill="#6366f1" fillOpacity={0.5} />
              </RadarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Weekly Practice Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Weekly Practice Intensity (Hours)</CardTitle>
            <CardDescription>Hours dedicated per day</CardDescription>
          </CardHeader>
          <CardContent className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={weeklyPracticeData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" />
                <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderRadius: '8px', border: 'none' }} />
                <Bar dataKey="hours" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Row 2 Charts — Company-wise Performance & Difficulty Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Company Performance Bar */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Company-wise Average Score</CardTitle>
            <CardDescription>Performance benchmarked against specific company bars</CardDescription>
          </CardHeader>
          <CardContent className="h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={companyPerfData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#334155" />
                <XAxis type="number" domain={[0, 100]} tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <YAxis dataKey="company" type="category" tick={{ fill: '#ffffff', fontSize: 12 }} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderRadius: '8px', border: 'none' }} />
                <Bar dataKey="score" fill="#6366f1" radius={[0, 4, 4, 0]} barSize={20} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Difficulty Distribution Pie Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Difficulty Breakdown</CardTitle>
            <CardDescription>Ratio of Easy / Medium / Hard questions</CardDescription>
          </CardHeader>
          <CardContent className="h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={difficultyDistribution} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                  {difficultyDistribution.map((_entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderRadius: '8px', border: 'none' }} />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Recommended Learning Path & Recommended LeetCode */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen size={18} className="text-brand-400" />
              Recommended LeetCode Practice
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            <div className="flex items-center justify-between p-2.5 rounded-lg bg-ink-800/40 border border-ink-700/50">
              <span className="font-semibold text-ink-100">1. LRU Cache (Design)</span>
              <Badge variant="danger">Hard</Badge>
            </div>
            <div className="flex items-center justify-between p-2.5 rounded-lg bg-ink-800/40 border border-ink-700/50">
              <span className="font-semibold text-ink-100">2. Course Schedule II (Topological Sort)</span>
              <Badge variant="warning">Medium</Badge>
            </div>
            <div className="flex items-center justify-between p-2.5 rounded-lg bg-ink-800/40 border border-ink-700/50">
              <span className="font-semibold text-ink-100">3. Search Suggestions System (Trie)</span>
              <Badge variant="warning">Medium</Badge>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp size={18} className="text-emerald-400" />
              Recommended Learning Roadmap
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            <div className="p-2.5 rounded-lg bg-ink-800/40 border border-ink-700/50">
              <p className="font-semibold text-emerald-400">Distributed Caching Patterns</p>
              <p className="text-ink-400 mt-0.5">Study Redis write-through vs cache-aside strategies for System Design rounds.</p>
            </div>
            <div className="p-2.5 rounded-lg bg-ink-800/40 border border-ink-700/50">
              <p className="font-semibold text-brand-400">STAR Behavioral Metrics</p>
              <p className="text-ink-400 mt-0.5">Quantify impact in leadership answers (e.g. latency reduced from 200ms to 45ms).</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}