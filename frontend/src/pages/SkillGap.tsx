import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Target, BookOpen, Code2, ArrowRight, AlertTriangle, Loader2 } from 'lucide-react';
import {
  Card, CardContent, CardHeader, CardTitle, CardDescription,
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { PageHeader } from '../components/ui/PageHeader';
import { ProgressBar } from '../components/ui/ProgressBar';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';
import { useInterviewSession } from '../contexts/InterviewSessionContext';

const stagger = {
  initial: {},
  animate: { transition: { staggerChildren: 0.07 } },
};
const fadeSlide = {
  initial: { opacity: 0, y: 18 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.42, ease: [0.25, 0.1, 0.25, 1] } },
};

interface SkillGapItem {
  skillName: string;
  importance?: string;
  currentLevel?: string;
  requiredLevel?: string;
}

interface LearningResource {
  title: string;
  provider?: string;
  resourceType?: string;
  estimatedHours?: number;
  url?: string;
}

interface SkillGapData {
  skillGaps: SkillGapItem[];
  matchedSkills: string[];
  matchPercentage: number | null;
  categoryMatchPercentages: Record<string, number>;
  courses: LearningResource[];
  practiceItems: string[];
  overallReadiness: number | null;
}

type LoadState = 'loading' | 'success' | 'empty' | 'error';

function importanceVariant(importance?: string): 'danger' | 'warning' | 'secondary' {
  const v = (importance || '').toUpperCase();
  if (v === 'CRITICAL' || v === 'HIGH') return 'danger';
  if (v === 'MEDIUM') return 'warning';
  return 'secondary';
}

export function SkillGap() {
  const { selectedRole, activeSession } = useInterviewSession();
  const [state, setState] = useState<LoadState>('loading');
  const [errorMsg, setErrorMsg] = useState('');
  const [data, setData] = useState<SkillGapData | null>(null);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      setState('loading');
      setErrorMsg('');
      const targetRole =
        selectedRole ||
        activeSession?.role ||
        new URLSearchParams(window.location.search).get('role') ||
        '';

      if (!targetRole) {
        setState('empty');
        return;
      }

      try {
        const res = await apiClient.get<any>(
          `${API_ENDPOINTS.EVALUATION.SKILL_GAP}?targetRole=${encodeURIComponent(targetRole)}`
        );
        const raw = res?.data ?? res;
        if (cancelled) return;

        const skillsMatching = raw?.skillsMatching || {};
        const skillGaps: SkillGapItem[] = Array.isArray(skillsMatching.skillGaps)
          ? skillsMatching.skillGaps
          : [];
        const recommendations = raw?.learningRecommendations || {};
        const courses: LearningResource[] = [
          ...(recommendations.immediateActions || []),
          ...(recommendations.shortTermGoals || []),
        ].filter((r: LearningResource) => r?.title);

        const practiceItems: string[] = [];
        for (const phase of raw?.learningRoadmap || []) {
          for (const m of phase?.milestones || []) {
            if (typeof m === 'string') practiceItems.push(m);
          }
        }

        const categoryMatchPercentages: Record<string, number> =
          skillsMatching.categoryMatchPercentages || {};

        const hasContent =
          skillGaps.length > 0 ||
          courses.length > 0 ||
          practiceItems.length > 0 ||
          Object.keys(categoryMatchPercentages).length > 0;

        if (!hasContent) {
          setData(null);
          setState('empty');
          return;
        }

        setData({
          skillGaps,
          matchedSkills: skillsMatching.matchedSkills || [],
          matchPercentage:
            typeof skillsMatching.matchPercentage === 'number'
              ? skillsMatching.matchPercentage
              : null,
          categoryMatchPercentages,
          courses,
          practiceItems,
          overallReadiness:
            typeof raw?.overallReadiness === 'number' ? raw.overallReadiness : null,
        });
        setState('success');
      } catch (err: any) {
        if (!cancelled) {
          setErrorMsg(err?.message || 'Failed to load skill gap analysis.');
          setData(null);
          setState('error');
        }
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
  }, [selectedRole, activeSession?.role]);

  if (state === 'loading') {
    return (
      <div className="space-y-8">
        <PageHeader
          title="Skill Gap Analysis"
          subtitle="Based on your recent interviews — focus areas to reach the bar for your target roles."
        />
        <div className="flex items-center justify-center gap-3 py-24 text-ink-500 dark:text-ink-400">
          <Loader2 className="animate-spin" size={20} />
          Analyzing skill gaps…
        </div>
      </div>
    );
  }

  if (state === 'error' || state === 'empty') {
    return (
      <div className="space-y-8">
        <PageHeader
          title="Skill Gap Analysis"
          subtitle="Based on your recent interviews — focus areas to reach the bar for your target roles."
        />
        <Card>
          <CardContent className="py-16 text-center space-y-4">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-500/10 text-brand-500">
              <Target size={22} />
            </div>
            <h2 className="text-lg font-semibold text-ink-900 dark:text-white">
              {state === 'error' ? 'Skill gap analysis unavailable' : 'No skill gap data yet'}
            </h2>
            <p className="mx-auto max-w-md text-sm text-ink-500 dark:text-ink-400">
              {state === 'error'
                ? errorMsg || 'We could not load your analysis. Complete an interview and try again.'
                : 'Complete at least one interview for your target role to generate a real skill gap report.'}
            </p>
            <Link to="/interview/company">
              <Button variant="gradient" className="gap-2 mt-2">
                Complete an interview <ArrowRight size={16} />
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    );
  }

  const coverageEntries = Object.entries(data!.categoryMatchPercentages);
  const skillCoverage =
    coverageEntries.length > 0
      ? coverageEntries.map(([name, score]) => ({
          name,
          score: Math.round(Number(score)),
          color:
            Number(score) >= 80
              ? ('emerald' as const)
              : Number(score) >= 60
                ? ('amber' as const)
                : ('rose' as const),
        }))
      : data!.skillGaps.slice(0, 5).map((g) => ({
          name: g.skillName,
          score: g.currentLevel === 'EXPERT' ? 90 : g.currentLevel === 'PROFICIENT' ? 75 : 45,
          color: 'amber' as const,
        }));

  return (
    <div className="space-y-8">
      <PageHeader
        title="Skill Gap Analysis"
        subtitle={
          data!.overallReadiness != null
            ? `Target: ${selectedRole || activeSession?.role || 'role'} — readiness ${Math.round(data!.overallReadiness)}%`
            : 'Based on your recent interviews — focus areas to reach the bar for your target roles.'
        }
      />

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="lg:col-span-1 space-y-3"
        >
          <Card hoverable>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-rose-500/10 text-rose-500">
                  <AlertTriangle size={14} />
                </div>
                Priority Focus Areas
              </CardTitle>
              <CardDescription>Skills to improve before your next interview</CardDescription>
            </CardHeader>
            <CardContent className="space-y-2.5">
              {data!.skillGaps.length === 0 ? (
                <p className="text-sm text-ink-500">No critical gaps identified.</p>
              ) : (
                data!.skillGaps.map((skill, i) => (
                  <motion.div
                    key={`${skill.skillName}-${i}`}
                    variants={fadeSlide}
                    className="flex items-center justify-between p-3 rounded-xl border border-rose-500/15 dark:border-rose-500/10 bg-rose-50/50 dark:bg-rose-500/[0.04] hover:border-rose-500/30 transition-colors"
                  >
                    <span className="text-sm font-medium text-ink-800 dark:text-ink-200">
                      {skill.skillName}
                    </span>
                    <Badge variant={importanceVariant(skill.importance)}>
                      {skill.importance || 'Focus'}
                    </Badge>
                  </motion.div>
                ))
              )}
            </CardContent>
          </Card>

          {skillCoverage.length > 0 && (
            <Card hoverable>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand-500/10 text-brand-500">
                    <Target size={14} />
                  </div>
                  Skill Coverage
                </CardTitle>
                <CardDescription>Your current proficiency per area</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {skillCoverage.map((s, i) => (
                  <ProgressBar
                    key={s.name}
                    label={s.name}
                    value={s.score}
                    color={s.color}
                    delay={0.3 + i * 0.1}
                  />
                ))}
              </CardContent>
            </Card>
          )}
        </motion.div>

        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="space-y-6 lg:col-span-2"
        >
          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand-500/10 text-brand-500">
                    <BookOpen size={14} />
                  </div>
                  Recommended Courses
                </CardTitle>
                <CardDescription>Curated learning materials to bridge your gaps</CardDescription>
              </CardHeader>
              <CardContent>
                {data!.courses.length === 0 ? (
                  <p className="text-sm text-ink-500">No course recommendations yet.</p>
                ) : (
                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                    {data!.courses.map((course, i) => (
                      <motion.div
                        key={`${course.title}-${i}`}
                        whileHover={{ y: -3, scale: 1.01 }}
                        transition={{ duration: 0.2 }}
                        className="group rounded-xl border border-ink-100 dark:border-white/[0.06] p-4 hover:border-brand-500/40 dark:hover:border-brand-500/30 hover:shadow-glow-sm transition-all"
                      >
                        <div className="mb-2.5 flex items-center justify-between">
                          <Badge variant="secondary">
                            {course.provider || course.resourceType || 'Resource'}
                          </Badge>
                          {course.estimatedHours != null && (
                            <span className="text-xs text-ink-400">{course.estimatedHours}h</span>
                          )}
                        </div>
                        <h4 className="text-sm font-semibold text-ink-900 dark:text-white leading-snug">
                          {course.title}
                        </h4>
                        {course.url && (
                          <a
                            href={course.url}
                            target="_blank"
                            rel="noreferrer"
                            className="mt-3 inline-flex items-center gap-1 text-xs font-semibold text-brand-500"
                          >
                            View Course <ArrowRight size={12} />
                          </a>
                        )}
                      </motion.div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-500">
                    <Code2 size={14} />
                  </div>
                  Recommended Practice
                </CardTitle>
                <CardDescription>Milestones and practice items from your analysis</CardDescription>
              </CardHeader>
              <CardContent>
                {data!.practiceItems.length === 0 ? (
                  <p className="text-sm text-ink-500">No practice items returned yet.</p>
                ) : (
                  <div className="space-y-2.5">
                    {data!.practiceItems.map((problem, i) => (
                      <div
                        key={i}
                        className="flex items-center justify-between rounded-xl bg-ink-50 dark:bg-white/[0.03] border border-ink-100 dark:border-white/5 px-4 py-3"
                      >
                        <span className="text-sm font-medium text-ink-800 dark:text-ink-200">
                          {problem}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>
        </motion.div>
      </div>

      <div className="flex justify-center pt-4">
        <Link to="/roadmap">
          <Button variant="gradient" size="lg" className="gap-2 px-8">
            View practice plan <ArrowRight size={18} />
          </Button>
        </Link>
      </div>
    </div>
  );
}
