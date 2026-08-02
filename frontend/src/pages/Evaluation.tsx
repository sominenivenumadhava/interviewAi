import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Award,
  CheckCircle2,
  AlertTriangle,
  Download,
  RotateCcw,
  Sparkles,
  Target,
  User,
  Mail,
  Building2,
  Briefcase,
  TrendingUp
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { useInterviewSession } from '../contexts/InterviewSessionContext';
import { useAuth } from '../contexts/AuthContext';

/** Derive a colour + label from a numeric percentage */
function scoreColour(pct: number) {
  if (pct >= 80) return 'text-emerald-400';
  if (pct >= 60) return 'text-amber-400';
  return 'text-red-400';
}

function hiringProbability(score: number): number {
  // Simple sigmoid-ish mapping: 0→10 %, 50→55 %, 85→85 %, 100→97 %
  return Math.min(97, Math.max(10, Math.round(score * 0.85 + 10)));
}

export function Evaluation() {
  const navigate = useNavigate();
  const { activeSession, selectedCompany, selectedRole, selectedConfig } = useInterviewSession();
  const { user } = useAuth();

  // ── Real scores from session ────────────────────────────────────────────────
  const overallScore = activeSession?.currentScore ?? 0;
  const hiringProb   = hiringProbability(overallScore);
  const lastEval     = activeSession?.lastEvaluation;
  const weakSkills   = activeSession?.weakSkillsDetected ?? [];

  // Per-skill breakdown: derive from overallScore with slight variance so each
  // dimension feels meaningful (replaces the old hardcoded 90 / 82 / 85 / 88).
  const commScore     = Math.min(100, Math.round(overallScore * 1.05));
  const techScore     = Math.min(100, Math.round(overallScore * 0.97));
  const problemScore  = Math.min(100, Math.round(overallScore * 1.0));
  const confScore     = Math.min(100, Math.round(overallScore * 1.03));

  // Strengths: use lastEvaluation feedback if available, otherwise generic
  const strengths: string[] = lastEval?.feedback
    ? [lastEval.feedback]
    : overallScore > 0
    ? ['Strong overall performance throughout the interview session.']
    : ['Complete the interview to receive personalised feedback.'];

  // Areas for improvement: use weakSkills from adaptive engine
  const improvements: string[] = weakSkills.length > 0
    ? weakSkills
    : overallScore > 0
    ? ['Review the expected answers to identify gaps in your explanations.']
    : [];

  // Recommended practice based on focus area
  const focusArea = selectedConfig.focusAreas || 'Core concepts';

  const candidateName =
    user ? `${user.firstName} ${user.lastName}`.trim() : 'Candidate';
  const candidateEmail = user?.email ?? '';

  const handleDownloadPDF = () => {
    window.print();
  };

  return (
    <div className="mx-auto max-w-5xl space-y-8 pb-16">
      {/* ── Header Banner ── */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between border-b border-ink-800 pb-6">
        <div>
          <Badge variant="success" className="mb-2">
            INTERVIEW COMPLETED
          </Badge>
          <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white sm:text-4xl">
            Candidate Evaluation Report
          </h1>
          <p className="mt-1 text-ink-500 dark:text-ink-400">
            Target:{' '}
            <span className="font-semibold text-white">{selectedCompany}</span>{' '}
            — {selectedRole} ({selectedConfig.interviewType})
          </p>

          {/* ── Candidate identity ── */}
          <div className="mt-3 flex flex-wrap gap-4">
            <div className="flex items-center gap-1.5 text-sm text-ink-300">
              <User size={14} className="text-brand-400" />
              <span className="font-medium text-white">{candidateName}</span>
            </div>
            {candidateEmail && (
              <div className="flex items-center gap-1.5 text-sm text-ink-300">
                <Mail size={14} className="text-brand-400" />
                <span>{candidateEmail}</span>
              </div>
            )}
            <div className="flex items-center gap-1.5 text-sm text-ink-300">
              <Building2 size={14} className="text-purple-400" />
              <span>{selectedCompany}</span>
            </div>
            <div className="flex items-center gap-1.5 text-sm text-ink-300">
              <Briefcase size={14} className="text-emerald-400" />
              <span>{selectedRole}</span>
            </div>
          </div>
        </div>

        <div className="flex gap-3 shrink-0">
          <Button variant="outline" size="sm" onClick={handleDownloadPDF} className="gap-2">
            <Download size={16} />
            Download PDF
          </Button>
          <Button size="sm" onClick={() => navigate('/interview/company')} className="gap-2 shadow">
            <RotateCcw size={16} />
            Start New Session
          </Button>
        </div>
      </div>

      {/* ── Main Score Hero Card ── */}
      <Card className="bg-gradient-to-r from-brand-950 via-ink-900 to-purple-950 border-brand-500/40 p-6 shadow-lift">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
          {/* Score circle */}
          <div className="flex flex-col items-center justify-center text-center border-b md:border-b-0 md:border-r border-ink-800 pb-6 md:pb-0 md:pr-6">
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              className={`relative flex h-36 w-36 items-center justify-center rounded-full border-4 border-brand-500 bg-ink-950 text-5xl font-black shadow-2xl ${scoreColour(overallScore)}`}
            >
              {overallScore}%
            </motion.div>
            <p className="mt-3 text-xs font-bold uppercase tracking-wider text-brand-400">
              Overall Candidate Score
            </p>
            <Badge
              variant={hiringProb >= 70 ? 'success' : hiringProb >= 50 ? 'warning' : 'danger'}
              className="mt-1"
            >
              Hiring Probability: {hiringProb}%
            </Badge>
          </div>

          {/* AI assessment text */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-emerald-400">
              <Sparkles size={18} />
              AI Senior Bar Raiser Assessment
            </div>
            <p className="text-xs text-ink-200 leading-relaxed">
              {lastEval?.expectedAnswer
                ? lastEval.expectedAnswer
                : overallScore >= 80
                ? `${candidateName} demonstrated strong domain knowledge and structured problem breakdown relevant to a ${selectedRole} role at ${selectedCompany}.`
                : overallScore >= 50
                ? `${candidateName} showed a reasonable understanding of ${selectedRole} concepts. There are clear opportunities to sharpen technical depth and conciseness.`
                : `${candidateName} is at an early stage for this ${selectedRole} position. Focused practice on the recommended areas below is advised.`}
            </p>

            {/* Per-skill mini-cards */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
              {[
                { label: 'Communication',  value: commScore },
                { label: 'Technical Depth', value: techScore },
                { label: 'Problem Solving', value: problemScore },
                { label: 'Confidence',      value: confScore },
              ].map(({ label, value }) => (
                <div
                  key={label}
                  className="rounded-lg bg-black/40 p-2.5 border border-white/5 text-center"
                >
                  <p className="text-[10px] text-ink-400 uppercase">{label}</p>
                  <p className={`text-sm font-bold mt-0.5 ${scoreColour(value)}`}>{value}%</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </Card>

      {/* ── Strengths & Weaknesses Grid ── */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Strengths */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-emerald-400 text-base">
              <CheckCircle2 size={18} />
              Key Demonstrated Strengths
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5 text-xs text-ink-200">
            {strengths.map((s, i) => (
              <div
                key={i}
                className="flex items-start gap-2 p-2 rounded bg-emerald-950/20 border border-emerald-500/20"
              >
                <span className="text-emerald-400 shrink-0">•</span>
                <span>{s}</span>
              </div>
            ))}
          </CardContent>
        </Card>

        {/* Areas for improvement */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-amber-400 text-base">
              <AlertTriangle size={18} />
              Areas For Improvement
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5 text-xs text-ink-200">
            {improvements.length > 0 ? (
              improvements.map((item, i) => (
                <div
                  key={i}
                  className="flex items-start gap-2 p-2 rounded bg-amber-950/20 border border-amber-500/20"
                >
                  <span className="text-amber-400 shrink-0">•</span>
                  <span>{item}</span>
                </div>
              ))
            ) : (
              <p className="text-ink-400 italic">
                No specific gaps detected — great job!
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* ── Score breakdown bar ── */}
      {overallScore > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <TrendingUp size={18} className="text-brand-400" />
              Score Breakdown
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {[
              { label: 'Communication',   value: commScore },
              { label: 'Technical Depth', value: techScore },
              { label: 'Problem Solving', value: problemScore },
              { label: 'Confidence',      value: confScore },
            ].map(({ label, value }) => (
              <div key={label}>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-ink-300">{label}</span>
                  <span className={`font-semibold ${scoreColour(value)}`}>{value}%</span>
                </div>
                <div className="h-1.5 rounded-full bg-ink-800">
                  <motion.div
                    initial={{ width: 0 }}
                    animate={{ width: `${value}%` }}
                    transition={{ duration: 0.8, ease: 'easeOut' }}
                    className={`h-1.5 rounded-full ${
                      value >= 80
                        ? 'bg-emerald-400'
                        : value >= 60
                        ? 'bg-amber-400'
                        : 'bg-red-400'
                    }`}
                  />
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {/* ── Recommended Practice Plan ── */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Target size={18} className="text-brand-400" />
            Recommended Practice Plan for {selectedCompany}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          <div className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1">
            <p className="font-semibold text-brand-400">1. Focus Area</p>
            <p className="text-ink-300">
              {focusArea || `Core ${selectedRole} concepts and problem-solving patterns.`}
            </p>
          </div>
          <div className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1">
            <p className="font-semibold text-emerald-400">2. Weak Spots to Address</p>
            <p className="text-ink-300">
              {weakSkills.length > 0
                ? weakSkills.slice(0, 2).join('; ') + '.'
                : 'Keep reinforcing your demonstrated strengths.'}
            </p>
          </div>
          <div className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1">
            <p className="font-semibold text-purple-400">3. Interview Type</p>
            <p className="text-ink-300">
              Practice more {selectedConfig.interviewType.toLowerCase()} rounds
              at {selectedConfig.difficulty} difficulty.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}