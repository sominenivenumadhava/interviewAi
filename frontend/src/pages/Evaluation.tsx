import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  CheckCircle2,
  AlertTriangle,
  Printer,
  RotateCcw,
  Sparkles,
  Target,
  User,
  Mail,
  Building2,
  Briefcase,
  TrendingUp,
  MessageSquare,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { useInterviewSession } from '../contexts/InterviewSessionContext';
import { useAuth } from '../contexts/AuthContext';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

const LAST_EVAL_KEY = 'interviai_last_eval_session';

function scoreColour(pct: number) {
  if (pct >= 80) return 'text-emerald-400';
  if (pct >= 60) return 'text-amber-400';
  return 'text-red-400';
}

function estimateHiringProbability(score: number): number {
  return Math.min(97, Math.max(10, Math.round(score * 0.85 + 10)));
}

function parseHiringProb(raw: unknown): number | null {
  if (typeof raw === 'number' && !Number.isNaN(raw)) return Math.round(raw);
  if (typeof raw === 'string') {
    const n = parseFloat(raw.replace('%', '').trim());
    return Number.isNaN(n) ? null : Math.round(n);
  }
  return null;
}

function clampPct(n: number): number {
  if (!Number.isFinite(n)) return 0;
  return Math.max(0, Math.min(100, Math.round(n)));
}

interface QuestionBreakdown {
  order: number;
  question: string;
  category?: string;
  userAnswer: string;
  feedback: string;
  score: number;
  strengths?: string[];
  weaknesses?: string[];
}

interface EvalView {
  overallScore: number;
  hiringProb: number | null;
  hiringProbIsEstimate: boolean;
  strengths: string[];
  improvements: string[];
  detailedFeedback: string;
  recommendedPractice: string[];
  commScore: number;
  techScore: number;
  problemScore: number;
  confScore: number;
  correctnessScore: number;
  optimizationScore: number;
  timeScore: number;
  weakSkills: string[];
  questions: QuestionBreakdown[];
  company: string;
  role: string;
  interviewType: string;
  answeredQuestions: number;
  totalQuestions: number;
}

interface LastEvalMeta {
  sessionId: string;
  company?: string;
  role?: string;
  interviewType?: string;
  difficulty?: string;
  currentScore?: number;
  scoreHistory?: number[];
  weakSkills?: string[];
}

function loadLastEvalMeta(): LastEvalMeta | null {
  try {
    const raw = sessionStorage.getItem(LAST_EVAL_KEY);
    return raw ? (JSON.parse(raw) as LastEvalMeta) : null;
  } catch {
    return null;
  }
}

export function Evaluation() {
  const navigate = useNavigate();
  const { activeSession, selectedCompany, selectedRole, selectedConfig } = useInterviewSession();
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);
  const [apiFallback, setApiFallback] = useState(false);
  const [view, setView] = useState<EvalView | null>(null);

  const meta = loadLastEvalMeta();
  const displayCompany = meta?.company || selectedCompany;
  const displayRole = meta?.role || selectedRole;
  const displayType = meta?.interviewType || selectedConfig.interviewType;

  const buildSessionFallback = (sessionMeta: LastEvalMeta | null): EvalView => {
    const overallScore = clampPct(
      activeSession?.currentScore ?? sessionMeta?.currentScore ?? 0
    );
    const lastEval = activeSession?.lastEvaluation;
    const weakSkills =
      activeSession?.weakSkillsDetected ?? sessionMeta?.weakSkills ?? [];
    const history = sessionMeta?.scoreHistory ?? [];

    return {
      overallScore,
      hiringProb: overallScore > 0 ? estimateHiringProbability(overallScore) : null,
      hiringProbIsEstimate: true,
      strengths: lastEval?.feedback
        ? [lastEval.feedback]
        : overallScore > 0
          ? ['Strong overall performance throughout the interview session.']
          : ['Complete the interview and answer questions to receive a scored report.'],
      improvements:
        weakSkills.length > 0
          ? weakSkills
          : overallScore > 0
            ? ['Review expected answers to deepen explanations.']
            : ['Answer at least one question before finishing.'],
      detailedFeedback: lastEval?.expectedAnswer || '',
      recommendedPractice: [
        `Practice more ${displayType} rounds for ${displayRole}`,
        weakSkills[0] ? `Focus on: ${weakSkills[0]}` : 'Schedule another mock interview',
      ],
      commScore: clampPct(overallScore * 1.05),
      techScore: clampPct(overallScore * 0.97),
      problemScore: clampPct(overallScore),
      confScore: clampPct(overallScore * 1.03),
      correctnessScore: clampPct(overallScore),
      optimizationScore: clampPct(overallScore * 0.95),
      timeScore: clampPct(overallScore * 0.98),
      weakSkills,
      questions: history.map((score, i) => ({
        order: i + 1,
        question: `Question ${i + 1}`,
        userAnswer: '(Answer submitted during session)',
        feedback: lastEval?.feedback || 'See overall feedback.',
        score: clampPct(score),
      })),
      company: displayCompany,
      role: displayRole,
      interviewType: displayType,
      answeredQuestions: history.length,
      totalQuestions: selectedConfig.numberOfQuestions || history.length,
    };
  };

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      const sessionMeta = loadLastEvalMeta();
      const sessionId = activeSession?.sessionId || sessionMeta?.sessionId;

      if (!sessionId) {
        setView(buildSessionFallback(sessionMeta));
        setApiFallback(false);
        setLoading(false);
        return;
      }

      setLoading(true);
      try {
        const res = await apiClient.get<any>(API_ENDPOINTS.EVALUATION.INTERVIEW(sessionId), {
          timeout: 120000,
          retry: false,
        });
        const data = res?.data ?? res;
        if (cancelled || !data) throw new Error('Empty evaluation');

        const cats = data.categoryScores || {};
        const overall = clampPct(Number(data.overallScore ?? 0));
        const apiHiring =
          parseHiringProb(data.hiringProbability) ??
          parseHiringProb(data.hiringProb) ??
          parseHiringProb(data.benchmarkComparison?.percentileRank);

        const strengths: string[] =
          (Array.isArray(data.topStrengths) && data.topStrengths.length > 0
            ? data.topStrengths
            : null) ||
          (data.skillsAssessment?.demonstratedSkills?.length
            ? data.skillsAssessment.demonstratedSkills
            : null) ||
          ['Interview evaluation completed.'];

        const improvements: string[] =
          (Array.isArray(data.keyImprovements) && data.keyImprovements.length > 0
            ? data.keyImprovements
            : null) ||
          (data.skillsAssessment?.skillGaps?.length
            ? data.skillsAssessment.skillGaps
            : null) ||
          [];

        const practice: string[] = Array.isArray(data.recommendedPractice)
          ? data.recommendedPractice
          : [];

        const questions: QuestionBreakdown[] = Array.isArray(data.questionPerformances)
          ? data.questionPerformances.map((q: any) => ({
              order: Number(q.questionOrder ?? 0),
              question: q.questionText || '',
              category: q.category,
              userAnswer: q.userAnswer || '(No answer text stored)',
              feedback: q.feedback || 'No per-question feedback.',
              score: clampPct(Number(q.score ?? 0)),
              strengths: Array.isArray(q.strengths) ? q.strengths : [],
              weaknesses: Array.isArray(q.weaknesses) ? q.weaknesses : [],
            }))
          : [];

        // Safety: never show 0% when answered questions exist and scores are present
        let safeOverall = overall;
        if (
          (data.answeredQuestions > 0 || questions.length > 0) &&
          safeOverall <= 0
        ) {
          const fromQs = questions.filter((q) => q.score > 0);
          if (fromQs.length) {
            safeOverall = clampPct(
              fromQs.reduce((a, b) => a + b.score, 0) / fromQs.length
            );
          } else if (sessionMeta?.currentScore) {
            safeOverall = clampPct(sessionMeta.currentScore);
          }
        }

        setView({
          overallScore: safeOverall,
          hiringProb: apiHiring ?? (safeOverall > 0 ? estimateHiringProbability(safeOverall) : null),
          hiringProbIsEstimate: apiHiring == null,
          strengths,
          improvements,
          detailedFeedback: data.detailedFeedback || data.recommendations || '',
          recommendedPractice: practice,
          commScore: clampPct(Number(cats.communicationScore ?? safeOverall)),
          techScore: clampPct(Number(cats.technicalScore ?? safeOverall)),
          problemScore: clampPct(Number(cats.problemSolvingScore ?? safeOverall)),
          confScore: clampPct(
            Number(cats.confidenceScore ?? cats.behavioralScore ?? safeOverall)
          ),
          correctnessScore: clampPct(Number(cats.correctnessScore ?? safeOverall)),
          optimizationScore: clampPct(Number(cats.optimizationScore ?? safeOverall)),
          timeScore: clampPct(Number(cats.timeManagementScore ?? safeOverall)),
          weakSkills: data.skillsAssessment?.skillGaps || [],
          questions,
          company: data.company || displayCompany,
          role: data.role || displayRole,
          interviewType: data.interviewType || displayType,
          answeredQuestions: Number(data.answeredQuestions ?? questions.length),
          totalQuestions: Number(data.totalQuestions ?? questions.length),
        });
        setApiFallback(false);
      } catch {
        if (!cancelled) {
          setView(buildSessionFallback(sessionMeta));
          setApiFallback(true);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeSession?.sessionId]);

  const candidateName = user ? `${user.firstName} ${user.lastName}`.trim() : 'Candidate';
  const candidateEmail = user?.email ?? '';

  if (loading || !view) {
    return (
      <div className="mx-auto max-w-5xl py-20 text-center text-ink-500 dark:text-ink-400">
        Generating AI evaluation report…
      </div>
    );
  }

  const {
    overallScore,
    hiringProb,
    hiringProbIsEstimate,
    strengths,
    improvements,
    detailedFeedback,
    recommendedPractice,
    commScore,
    techScore,
    problemScore,
    confScore,
    correctnessScore,
    optimizationScore,
    timeScore,
    weakSkills,
    questions,
  } = view;

  const dimensionBars = [
    { label: 'Communication', value: commScore },
    { label: 'Technical Depth', value: techScore },
    { label: 'Problem Solving', value: problemScore },
    { label: 'Confidence', value: confScore },
    { label: 'Correctness', value: correctnessScore },
    { label: 'Optimization', value: optimizationScore },
    { label: 'Time Management', value: timeScore },
  ];

  return (
    <div className="mx-auto max-w-5xl space-y-8 pb-16">
      {apiFallback && (
        <div className="rounded-xl border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-200">
          Showing session summary — full AI evaluation unavailable. Scores below use recorded
          answer scores when present.
        </div>
      )}

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
            <span className="font-semibold text-white">{view.company}</span> — {view.role} (
            {view.interviewType})
          </p>
          <p className="mt-1 text-xs text-ink-500">
            Answered {view.answeredQuestions} of {view.totalQuestions} questions
          </p>

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
              <Building2 size={14} className="text-teal-400" />
              <span>{view.company}</span>
            </div>
            <div className="flex items-center gap-1.5 text-sm text-ink-300">
              <Briefcase size={14} className="text-emerald-400" />
              <span>{view.role}</span>
            </div>
          </div>
        </div>

        <div className="flex gap-3 shrink-0">
          <Button variant="outline" size="sm" onClick={() => window.print()} className="gap-2">
            <Printer size={16} />
            Print report
          </Button>
          <Button size="sm" onClick={() => navigate('/interview/company')} className="gap-2 shadow">
            <RotateCcw size={16} />
            Start New Session
          </Button>
        </div>
      </div>

      <Card className="bg-gradient-to-r from-brand-950 via-ink-900 to-teal-950 border-brand-500/40 p-6 shadow-lift">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
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
            {hiringProb != null && (
              <Badge
                variant={hiringProb >= 70 ? 'success' : hiringProb >= 50 ? 'warning' : 'danger'}
                className="mt-1"
              >
                {hiringProbIsEstimate
                  ? `Estimated hiring chance: ${hiringProb}%`
                  : `Hiring Probability: ${hiringProb}%`}
              </Badge>
            )}
          </div>

          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-emerald-400">
              <Sparkles size={18} />
              {apiFallback ? 'Session Summary' : 'AI Bar Raiser Assessment'}
            </div>
            <p className="text-xs text-ink-200 leading-relaxed whitespace-pre-wrap">
              {detailedFeedback ||
                (overallScore >= 80
                  ? `${candidateName} demonstrated strong domain knowledge for a ${view.role} role at ${view.company}.`
                  : overallScore >= 50
                    ? `${candidateName} showed a reasonable understanding. There are clear opportunities to sharpen depth and structure.`
                    : view.answeredQuestions === 0
                      ? 'No answers were submitted, so the score remains at 0%.'
                      : `${candidateName} is at an early stage for this role. Focused practice on the recommendations below is advised.`)}
            </p>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
              {[
                { label: 'Communication', value: commScore },
                { label: 'Technical Depth', value: techScore },
                { label: 'Problem Solving', value: problemScore },
                { label: 'Confidence', value: confScore },
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

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
              <p className="text-ink-400 italic">No specific gaps detected — great job!</p>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <TrendingUp size={18} className="text-brand-400" />
            Score Breakdown
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {dimensionBars.map(({ label, value }) => (
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
                    value >= 80 ? 'bg-emerald-400' : value >= 60 ? 'bg-amber-400' : 'bg-red-400'
                  }`}
                />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {questions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <MessageSquare size={18} className="text-brand-400" />
              Question-wise Breakdown
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {questions.map((q) => (
              <div
                key={q.order}
                className="rounded-xl border border-ink-700/60 bg-ink-950/40 p-4 space-y-3"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-brand-400">
                      Question {q.order}
                      {q.category ? ` · ${q.category}` : ''}
                    </p>
                    <p className="mt-1 text-sm text-ink-100 whitespace-pre-wrap">{q.question}</p>
                  </div>
                  <span className={`text-lg font-black shrink-0 ${scoreColour(q.score)}`}>
                    {q.score}%
                  </span>
                </div>
                <div>
                  <p className="text-[10px] uppercase text-ink-500 font-semibold mb-1">
                    Your Answer
                  </p>
                  <p className="text-xs text-ink-300 whitespace-pre-wrap rounded-lg bg-ink-900/80 p-3 border border-ink-800">
                    {q.userAnswer || '(empty)'}
                  </p>
                </div>
                <div>
                  <p className="text-[10px] uppercase text-ink-500 font-semibold mb-1">
                    AI Feedback
                  </p>
                  <p className="text-xs text-ink-200">{q.feedback}</p>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Target size={18} className="text-brand-400" />
            Recommended Practice Plan for {view.company}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          {(recommendedPractice.length > 0
            ? recommendedPractice.slice(0, 3)
            : [
                `Core ${view.role} concepts and problem-solving patterns.`,
                weakSkills.length > 0
                  ? weakSkills.slice(0, 2).join('; ')
                  : 'Keep reinforcing demonstrated strengths.',
                `Practice more ${String(view.interviewType).toLowerCase()} rounds.`,
              ]
          ).map((item, i) => (
            <div
              key={i}
              className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1"
            >
              <p className="font-semibold text-brand-400">{i + 1}. Action</p>
              <p className="text-ink-300">{item}</p>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
