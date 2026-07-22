import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Award,
  CheckCircle2,
  AlertTriangle,
  BookOpen,
  Code,
  Download,
  Share2,
  RotateCcw,
  Sparkles,
  TrendingUp,
  Target
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { useInterviewSession } from '../contexts/InterviewSessionContext';

export function Evaluation() {
  const navigate = useNavigate();
  const { activeSession, selectedCompany, selectedRole, selectedConfig } = useInterviewSession();

  const score = activeSession?.currentScore || 85;

  const handleDownloadPDF = () => {
    window.print();
  };

  return (
    <div className="mx-auto max-w-5xl space-y-8 pb-16">
      {/* Header Banner */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-ink-800 pb-6">
        <div>
          <Badge variant="success" className="mb-2">
            INTERVIEW COMPLETED
          </Badge>
          <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white sm:text-4xl">
            Candidate Evaluation Report
          </h1>
          <p className="mt-1 text-ink-500 dark:text-ink-400">
            Target: <span className="font-semibold text-white">{selectedCompany}</span> — {selectedRole} ({selectedConfig.interviewType})
          </p>
        </div>

        <div className="flex gap-3">
          <Button variant="outline" size="sm" onClick={handleDownloadPDF} className="gap-2">
            <Download size={16} />
            Download PDF Assessment
          </Button>
          <Button size="sm" onClick={() => navigate('/interview/company')} className="gap-2 shadow">
            <RotateCcw size={16} />
            Start New Session
          </Button>
        </div>
      </div>

      {/* Main Score Hero Card */}
      <Card className="bg-gradient-to-r from-brand-950 via-ink-900 to-purple-950 border-brand-500/40 p-6 shadow-lift">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
          <div className="flex flex-col items-center justify-center text-center border-b md:border-b-0 md:border-r border-ink-800 pb-6 md:pb-0 md:pr-6">
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              className="relative flex h-36 w-36 items-center justify-center rounded-full border-4 border-brand-500 bg-ink-950 text-5xl font-black text-white shadow-2xl"
            >
              {score}%
            </motion.div>
            <p className="mt-3 text-xs font-bold uppercase tracking-wider text-brand-400">
              Overall Candidate Score
            </p>
            <Badge variant="success" className="mt-1">
              Hiring Probability: 82%
            </Badge>
          </div>

          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-emerald-400">
              <Sparkles size={18} />
              AI Senior Bar Raiser Assessment
            </div>
            <p className="text-xs text-ink-200 leading-relaxed">
              The candidate demonstrated strong domain knowledge and structured problem breakdown relevant to a {selectedRole} role at {selectedCompany}. Technical accuracy remained solid throughout, with minor opportunities to deepen distributed system scaling trade-offs.
            </p>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
              <div className="rounded-lg bg-black/40 p-2.5 border border-white/5 text-center">
                <p className="text-[10px] text-ink-400 uppercase">Communication</p>
                <p className="text-sm font-bold text-white mt-0.5">90%</p>
              </div>
              <div className="rounded-lg bg-black/40 p-2.5 border border-white/5 text-center">
                <p className="text-[10px] text-ink-400 uppercase">Technical Depth</p>
                <p className="text-sm font-bold text-white mt-0.5">82%</p>
              </div>
              <div className="rounded-lg bg-black/40 p-2.5 border border-white/5 text-center">
                <p className="text-[10px] text-ink-400 uppercase">Problem Solving</p>
                <p className="text-sm font-bold text-white mt-0.5">85%</p>
              </div>
              <div className="rounded-lg bg-black/40 p-2.5 border border-white/5 text-center">
                <p className="text-[10px] text-ink-400 uppercase">Confidence</p>
                <p className="text-sm font-bold text-white mt-0.5">88%</p>
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Strengths & Weaknesses Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-emerald-400 text-base">
              <CheckCircle2 size={18} />
              Key Demonstrated Strengths
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5 text-xs text-ink-200">
            <div className="flex items-start gap-2 p-2 rounded bg-emerald-950/20 border border-emerald-500/20">
              <span className="text-emerald-400">•</span>
              <span>Exceptional clarity in explaining component lifecycles and state architecture.</span>
            </div>
            <div className="flex items-start gap-2 p-2 rounded bg-emerald-950/20 border border-emerald-500/20">
              <span className="text-emerald-400">•</span>
              <span>Proactive use of structured thinking before jumping into coding implementations.</span>
            </div>
            <div className="flex items-start gap-2 p-2 rounded bg-emerald-950/20 border border-emerald-500/20">
              <span className="text-emerald-400">•</span>
              <span>Strong alignment with team-oriented communication principles.</span>
            </div>
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
            <div className="flex items-start gap-2 p-2 rounded bg-amber-950/20 border border-amber-500/20">
              <span className="text-amber-400">•</span>
              <span>Elaborate more on caching strategies (Redis / Memcached) during high concurrency queries.</span>
            </div>
            <div className="flex items-start gap-2 p-2 rounded bg-amber-950/20 border border-amber-500/20">
              <span className="text-amber-400">•</span>
              <span>Include explicit metrics when describing past project achievements (STAR method).</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recommended LeetCode & Action Plan */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Target size={18} className="text-brand-400" />
            Recommended Practice Plan for {selectedCompany}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          <div className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1">
            <p className="font-semibold text-brand-400">1. LeetCode Target</p>
            <p className="text-ink-300">LRU Cache, Design Search Autocomplete, Topological Sort.</p>
          </div>
          <div className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1">
            <p className="font-semibold text-emerald-400">2. System Design Focus</p>
            <p className="text-ink-300">Grokking System Design — Read-heavy vs Write-heavy architectures.</p>
          </div>
          <div className="p-3 rounded-xl bg-ink-800/40 border border-ink-700/50 space-y-1">
            <p className="font-semibold text-purple-400">3. Recommended Project</p>
            <p className="text-ink-300">Build a distributed rate-limiter with Redis & Spring Boot.</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}