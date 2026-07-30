import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Target, BookOpen, Code2, ArrowRight, AlertTriangle } from 'lucide-react';
import {
  Card, CardContent, CardHeader, CardTitle, CardDescription,
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { PageHeader } from '../components/ui/PageHeader';
import { ProgressBar } from '../components/ui/ProgressBar';
import { mockSkillGap } from '../data/mockData';

const stagger = {
  initial:  {},
  animate:  { transition: { staggerChildren: 0.07 } },
};
const fadeSlide = {
  initial: { opacity: 0, y: 18 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.42, ease: [0.25, 0.1, 0.25, 1] } },
};

// ─── Skill Gap Coverage data ──────────────────────────────────────────────────
const skillCoverage = [
  { name: 'System Design',   score: 58, color: 'rose'   as const },
  { name: 'Graph Algorithms', score: 65, color: 'amber'  as const },
  { name: 'Dynamic Programming', score: 72, color: 'amber'  as const },
  { name: 'Behavioral (STAR)',   score: 84, color: 'emerald' as const },
  { name: 'SQL & Databases',    score: 78, color: 'cyan'   as const },
];

export function SkillGap() {
  return (
    <div className="space-y-8">
      <PageHeader
        title="Skill Gap Analysis"
        subtitle="Based on your recent interviews — focus areas to reach the bar for your target roles."
      />

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">

        {/* ── Priority Focus Areas ── */}
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
              {mockSkillGap.missingSkills.map((skill, i) => (
                <motion.div
                  key={i}
                  variants={fadeSlide}
                  className="flex items-center justify-between p-3 rounded-xl border border-rose-500/15 dark:border-rose-500/10 bg-rose-50/50 dark:bg-rose-500/[0.04] hover:border-rose-500/30 transition-colors"
                >
                  <span className="text-sm font-medium text-ink-800 dark:text-ink-200">{skill}</span>
                  <Badge variant="danger">High Priority</Badge>
                </motion.div>
              ))}
            </CardContent>
          </Card>

          {/* ── Skill Coverage ── */}
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
        </motion.div>

        {/* ── Recommendations ── */}
        <motion.div
          variants={stagger}
          initial="initial"
          animate="animate"
          className="space-y-6 lg:col-span-2"
        >
          {/* Courses */}
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
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  {mockSkillGap.recommendedCourses.map((course, i) => (
                    <motion.div
                      key={i}
                      whileHover={{ y: -3, scale: 1.01 }}
                      transition={{ duration: 0.2 }}
                      className="group rounded-xl border border-ink-100 dark:border-white/[0.06] p-4 hover:border-brand-500/40 dark:hover:border-brand-500/30 hover:shadow-glow-sm transition-all cursor-pointer"
                    >
                      <div className="mb-2.5 flex items-center justify-between">
                        <Badge variant="secondary">{course.platform}</Badge>
                        <span className="text-xs text-ink-400">{course.duration}</span>
                      </div>
                      <h4 className="text-sm font-semibold text-ink-900 dark:text-white leading-snug">{course.title}</h4>
                      <div className="mt-3 flex items-center gap-1 text-xs font-semibold text-brand-500 opacity-0 group-hover:opacity-100 transition-opacity">
                        View Course <ArrowRight size={12} />
                      </div>
                    </motion.div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* LeetCode */}
          <motion.div variants={fadeSlide}>
            <Card hoverable>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-500">
                    <Code2 size={14} />
                  </div>
                  Recommended Practice Problems
                </CardTitle>
                <CardDescription>Specific problems to improve your technical execution</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2.5">
                  {mockSkillGap.recommendedLeetCode.map((problem, i) => (
                    <div
                      key={i}
                      className="flex items-center justify-between rounded-xl bg-ink-50 dark:bg-white/[0.03] border border-ink-100 dark:border-white/5 px-4 py-3 hover:border-brand-500/30 transition-colors"
                    >
                      <span className="text-sm font-medium text-ink-800 dark:text-ink-200">{problem}</span>
                      <Button variant="ghost" size="sm" className="text-xs">Practice</Button>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </motion.div>
      </div>

      <div className="flex justify-center pt-4">
        <Link to="/roadmap">
          <Button variant="gradient" size="lg" className="gap-2 px-8">
            Generate Learning Roadmap <ArrowRight size={18} />
          </Button>
        </Link>
      </div>
    </div>
  );
}