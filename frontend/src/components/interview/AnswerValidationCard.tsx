import React from 'react';
import { motion } from 'framer-motion';
import {
  CheckCircle2,
  XCircle,
  Sparkles,
  AlertTriangle,
  Star,
  Zap,
  Target,
  HelpCircle
} from 'lucide-react';
import { Badge } from '../ui/Badge';

export interface AnswerValidationData {
  isRelevant: boolean;
  score: number; // out of 10
  confidence: number; // percentage
  feedback: string;
  missingPoints: string[];
  strengths?: string[];
  weaknesses?: string[];
  idealAnswer?: string;
}

interface AnswerValidationCardProps {
  validation: AnswerValidationData | null;
  isValidating: boolean;
}

export function AnswerValidationCard({ validation, isValidating }: AnswerValidationCardProps) {
  if (isValidating) {
    return (
      <div className="rounded-xl border border-brand-500/30 bg-brand-950/20 p-4 animate-pulse space-y-3">
        <div className="flex items-center gap-2 text-xs font-semibold text-brand-300">
          <Sparkles size={16} className="animate-spin" />
          LLM Validating Answer Quality & Relevance...
        </div>
        <div className="h-2 w-3/4 bg-brand-800/40 rounded"></div>
        <div className="h-2 w-1/2 bg-brand-800/40 rounded"></div>
      </div>
    );
  }

  if (!validation) return null;

  // Render 10 stars corresponding to score (e.g. 8.5/10)
  const fullStars = Math.floor(validation.score);
  const hasHalf = validation.score % 1 >= 0.5;

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={`rounded-xl border p-4 space-y-4 ${
        validation.isRelevant
          ? 'border-emerald-500/40 bg-emerald-950/20'
          : 'border-red-500/40 bg-red-950/20'
      }`}
    >
      {/* Header Bar */}
      <div className="flex items-center justify-between border-b pb-3 border-white/10">
        <div className="flex items-center gap-2">
          <Sparkles size={16} className={validation.isRelevant ? 'text-emerald-400' : 'text-red-400'} />
          <span className="text-xs font-bold uppercase tracking-wider text-white">
            Real-Time Answer Evaluation
          </span>
        </div>

        <div className="flex items-center gap-2">
          {validation.isRelevant ? (
            <Badge variant="success" className="gap-1">
              <CheckCircle2 size={12} /> Relevant ✅
            </Badge>
          ) : (
            <Badge variant="danger" className="gap-1">
              <XCircle size={12} /> Not Relevant ❌
            </Badge>
          )}

          <Badge variant="outline" className="text-xs">
            Confidence: {validation.confidence}%
          </Badge>
        </div>
      </div>

      {/* Score Rating Stars */}
      <div className="flex items-center justify-between">
        <div>
          <p className="text-[11px] font-semibold text-ink-400 uppercase">Score Rating</p>
          <div className="flex items-center gap-1 mt-1">
            {Array.from({ length: 10 }).map((_, i) => (
              <Star
                key={i}
                size={14}
                className={
                  i < fullStars
                    ? 'fill-amber-400 text-amber-400'
                    : i === fullStars && hasHalf
                    ? 'fill-amber-400/50 text-amber-400'
                    : 'text-ink-700'
                }
              />
            ))}
            <span className="ml-2 text-sm font-bold text-white">
              {validation.score}/10
            </span>
          </div>
        </div>
      </div>

      {/* Feedback text */}
      <div className="space-y-1">
        <p className="text-[11px] font-semibold text-ink-300 uppercase">Feedback</p>
        <p className="text-xs text-ink-200 leading-relaxed bg-black/30 p-2.5 rounded-lg border border-white/5">
          {validation.feedback}
        </p>
      </div>

      {/* Missing Points section */}
      {validation.missingPoints && validation.missingPoints.length > 0 && (
        <div className="space-y-1.5 pt-1">
          <p className="text-[11px] font-semibold text-amber-400 flex items-center gap-1">
            <AlertTriangle size={12} />
            Missing Points To Cover:
          </p>
          <div className="flex flex-wrap gap-1.5">
            {validation.missingPoints.map((point, idx) => (
              <span
                key={idx}
                className="inline-flex items-center gap-1 rounded bg-amber-950/40 border border-amber-500/30 px-2 py-1 text-[11px] font-medium text-amber-200"
              >
                • {point}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Irrelevant Answer Warning Callout */}
      {!validation.isRelevant && (
        <div className="rounded-lg bg-red-950/40 p-3 border border-red-500/30 text-xs text-red-200 flex items-start gap-2">
          <XCircle size={16} className="shrink-0 text-red-400 mt-0.5" />
          <div>
            <p className="font-semibold text-red-300">Answer Irrelevant to Question</p>
            <p className="mt-0.5 text-ink-300">
              The transcript does not address the question asked. Please edit your text or re-record your response before submitting.
            </p>
          </div>
        </div>
      )}
    </motion.div>
  );
}
