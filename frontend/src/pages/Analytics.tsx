import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line, PieChart, Pie, Cell,
  RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar,
} from 'recharts';
import {
  Card, CardContent, CardHeader, CardTitle, CardDescription,
} from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { MotionCard } from '../components/ui/MotionCard';
import { PageHeader } from '../components/ui/PageHeader';
import { SkeletonStat, SkeletonChart, SkeletonCard } from '../components/ui/Skeleton';
import { Award, Target, TrendingUp, Clock, CheckCircle2, BookOpen } from 'lucide-react';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

// ─── Consistent chart colors ───────────────────────────────────────────────────
const C = {
  indigo:  '#6366f1',
  cyan:    '#06b6d4',
  emerald: '#10b981',
  amber:   '#f59e0b',
  rose:    '#f43f5e',
  violet:  '#8b5cf6',
};

const CHART_COLORS = [C.indigo, C.emerald, C.amber, C.rose, C.cyan];

const tooltipStyle = {
  backgroundColor: '#0b0f19',
  border: '1px solid rgba(255,255,255,0.07)',
  borderRadius: '12px',
  color: '#e2e8f0',
  boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
  fontSize: '12px',
  padding: '8px 12px',
};

// ─── Stagger container ────────────────────────────────────────────────────────
const stagger = {
  initial:  {},
  animate:  { transition: { staggerChildren: 0.08 } },
};
const fadeSlide = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.45, ease: [0.25, 0.1, 0.25, 1] } },
};

// ─── Analytics ────────────────────────────────────────────────────────────────
export function Analytics() {
  const [data, setData]       = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await apiClient.get(API_ENDPOINTS.ANALYTICS.USER);
        if (res.data) setData(res.data);
      } catch (err) {
        console.warn('Analytics fallback:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const weeklyPracticeData = [
    { name: 'Mon', hours: 1.5 },
    { name: 'Tue', hours: 2.0 },
    { name: 'Wed', hours: 1.0 },
    { name: 'Thu', hours: 3.2 },
    { name: 'Fri', hours: 2.5 },
    { name: 'Sat', hours: 4.0 },
    { name: 'Sun', hours: 3.0 },
  ];

  const scoreTrendData = [
    { date: 'Week 1', score: 68 },
    { date: 'Week 2', score: 74 },
    { date: 'Week 3', score: 78 },
    { date: 'Week 4', score: 82 },
    { date: 'Week 5', score: 86 },
  ];

  const skillRadarData = [
    { subject: 'Technical',    score: 85 },
    { subject: 'Communication', score: 90 },
    { subject: 'Architecture', score: 76 },
    { subject: 'Behavioral',   score: 92 },
    { subject: 'Coding Speed', score: 80 },
    { subject: 'Problem Solve', score: 84 },
  ];

  const companyPerfData = [
    { company: 'Google',    score: 84 },
    { company: 'Microsoft', score: 88 },
    { company: 'Amazon',    score: 82 },
    { company: 'Meta',      score: 78 },
    { company: 'Stripe',    score: 86 },
  ];

  const difficultyData = [
    { name: 'Easy',   value: 20 },
    { name: 'Medium', value: 55 },
    { name: 'Hard',   value: 25 },
  ];

  const difficultyColors = [C.emerald, C.amber, C.rose];

  return (
    <div className="space-y-8">

      <PageHeader
        title="Analytics Overview"
        subtitle="In-depth breakdown across skill heatmaps, company performance, and learning pathways."
      />

      {/* ── Stat Cards ── */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[0,1,2,3].map(i => <SkeletonStat key={i} />)}
        </div>
      ) : (
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="grid grid-cols-2 lg:grid-cols-4 gap-4"
        >
          <motion.div variants={fadeSlide}>
            <MotionCard
              title="Overall Score"
              value={84.5}
              suffix="%"
              icon={<Award size={20} />}
              iconColor="bg-emerald-500/10"
              iconTextColor="text-emerald-500"
              accentColor="border-l-emerald-500"
            />
          </motion.div>
          <motion.div variants={fadeSlide}>
            <MotionCard
              title="Completion Rate"
              value={96}
              suffix="%"
              icon={<CheckCircle2 size={20} />}
              iconColor="bg-brand-500/10"
              iconTextColor="text-brand-500"
              accentColor="border-l-brand-500"
            />
          </motion.div>
          <motion.div variants={fadeSlide}>
            <MotionCard
              title="Avg Duration"
              value={42}
              suffix=" min"
              icon={<Clock size={20} />}
              iconColor="bg-amber-500/10"
              iconTextColor="text-amber-500"
              accentColor="border-l-amber-500"
            />
          </motion.div>
          <motion.div variants={fadeSlide}>
            <MotionCard
              title="Readiness"
              value="Tier 1"
              icon={<Target size={20} />}
              iconColor="bg-violet-500/10"
              iconTextColor="text-violet-500"
              accentColor="border-l-violet-500"
              animate={false}
            />
          </motion.div>
        </motion.div>
      )}

      {/* ── Row 1: Radar + Weekly Bar ── */}
      {loading ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <SkeletonChart />
          <SkeletonChart />
        </div>
      ) : (
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="grid grid-cols-1 lg:grid-cols-2 gap-6"
        >
          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle>Skill Performance Radar</CardTitle>
                <CardDescription>Multi-dimensional interview competency heatmap</CardDescription>
              </CardHeader>
              <CardContent className="h-[300px]">
                <ResponsiveContainer width="100%" height="100%">
                  <RadarChart data={skillRadarData}>
                    <PolarGrid stroke="rgba(255,255,255,0.07)" />
                    <PolarAngleAxis dataKey="subject" tick={{ fill: '#64748b', fontSize: 11 }} />
                    <PolarRadiusAxis domain={[0, 100]} tick={{ fill: '#475569', fontSize: 9 }} />
                    <Radar
                      name="Score"
                      dataKey="score"
                      stroke={C.indigo}
                      fill={C.indigo}
                      fillOpacity={0.25}
                      animationDuration={1200}
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle>Weekly Practice Intensity</CardTitle>
                <CardDescription>Hours dedicated per day this week</CardDescription>
              </CardHeader>
              <CardContent className="h-[300px]">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={weeklyPracticeData}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.06)" />
                    <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <Tooltip contentStyle={tooltipStyle} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
                    <Bar dataKey="hours" fill={C.emerald} radius={[5, 5, 0, 0]} animationDuration={1000} />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </motion.div>
        </motion.div>
      )}

      {/* ── Row 2: Company + Difficulty ── */}
      {loading ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2"><SkeletonChart /></div>
          <SkeletonChart />
        </div>
      ) : (
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="grid grid-cols-1 lg:grid-cols-3 gap-6"
        >
          <motion.div variants={fadeSlide} className="lg:col-span-2">
            <Card hoverable>
              <CardHeader>
                <CardTitle>Company-wise Average Score</CardTitle>
                <CardDescription>Performance benchmarked per company</CardDescription>
              </CardHeader>
              <CardContent className="h-[280px]">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={companyPerfData} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="rgba(255,255,255,0.06)" />
                    <XAxis type="number" domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <YAxis dataKey="company" type="category" tick={{ fill: '#94a3b8', fontSize: 12 }} axisLine={false} tickLine={false} width={70} />
                    <Tooltip contentStyle={tooltipStyle} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
                    <Bar dataKey="score" fill={C.indigo} radius={[0, 5, 5, 0]} barSize={22} animationDuration={1000} />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle>Difficulty Breakdown</CardTitle>
                <CardDescription>Easy / Medium / Hard distribution</CardDescription>
              </CardHeader>
              <CardContent className="h-[280px] flex flex-col items-center justify-center">
                <ResponsiveContainer width="100%" height={180}>
                  <PieChart>
                    <Pie
                      data={difficultyData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={45}
                      outerRadius={75}
                      paddingAngle={3}
                      animationDuration={1000}
                    >
                      {difficultyData.map((_, i) => (
                        <Cell key={i} fill={difficultyColors[i]} strokeWidth={0} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={tooltipStyle} />
                  </PieChart>
                </ResponsiveContainer>
                <div className="flex gap-4 mt-2">
                  {difficultyData.map((d, i) => (
                    <div key={d.name} className="flex items-center gap-1.5 text-xs text-ink-400">
                      <div className="h-2.5 w-2.5 rounded-full" style={{ background: difficultyColors[i] }} />
                      <span>{d.name}</span>
                      <span className="font-semibold text-ink-200">{d.value}%</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </motion.div>
      )}

      {/* ── Row 3: Learning Recommendations ── */}
      {loading ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <SkeletonCard />
          <SkeletonCard />
        </div>
      ) : (
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="grid grid-cols-1 lg:grid-cols-2 gap-6"
        >
          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <BookOpen size={16} className="text-brand-400" />
                  Recommended LeetCode Practice
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2.5">
                {[
                  { title: '1. LRU Cache (Design)',                 level: 'danger' as const,  label: 'Hard'   },
                  { title: '2. Course Schedule II (Topological Sort)', level: 'warning' as const, label: 'Medium' },
                  { title: '3. Search Suggestions System (Trie)',     level: 'warning' as const, label: 'Medium' },
                ].map((p, i) => (
                  <div
                    key={i}
                    className="flex items-center justify-between p-3 rounded-xl bg-ink-50 dark:bg-white/[0.03] border border-ink-100 dark:border-white/5 hover:border-brand-500/30 transition-colors"
                  >
                    <span className="text-sm font-medium text-ink-800 dark:text-ink-200">{p.title}</span>
                    <Badge variant={p.level}>{p.label}</Badge>
                  </div>
                ))}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp size={16} className="text-emerald-400" />
                  Recommended Learning Roadmap
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2.5">
                {[
                  {
                    title: 'Distributed Caching Patterns',
                    desc: 'Study Redis write-through vs cache-aside strategies for System Design rounds.',
                    color: 'text-emerald-400',
                  },
                  {
                    title: 'STAR Behavioral Metrics',
                    desc: 'Quantify impact in leadership answers (e.g. latency reduced from 200ms to 45ms).',
                    color: 'text-brand-400',
                  },
                ].map((r, i) => (
                  <div
                    key={i}
                    className="p-3.5 rounded-xl bg-ink-50 dark:bg-white/[0.03] border border-ink-100 dark:border-white/5 hover:border-brand-500/30 transition-colors"
                  >
                    <p className={`text-sm font-semibold ${r.color}`}>{r.title}</p>
                    <p className="text-xs text-ink-500 dark:text-ink-400 mt-1 leading-relaxed">{r.desc}</p>
                  </div>
                ))}
              </CardContent>
            </Card>
          </motion.div>
        </motion.div>
      )}
    </div>
  );
}