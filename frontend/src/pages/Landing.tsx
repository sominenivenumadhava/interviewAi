import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion, useInView, animate } from 'framer-motion';
import gsap from 'gsap';
import { useTheme } from '../contexts/ThemeContext';
import {
  BrainCircuit, ArrowRight, BarChart3, Target, Shield,
  Sparkles, Zap, TrendingUp, CheckCircle2, ChevronDown,
  Sun, Moon, RotateCcw
} from 'lucide-react';

import { HeroCanvas } from '../components/landing/HeroCanvas';
import { MagneticButton } from '../components/landing/MagneticButton';
import { TiltCard } from '../components/landing/TiltCard';
import { PageWrapper } from '../components/ui/PageWrapper';

// ─── Animated Counter Component ───────────────────────────────────────────────
function AnimatedCounter({ to, suffix = '' }: { to: number; suffix?: string }) {
  const ref = useRef<HTMLSpanElement>(null);
  const inView = useInView(ref, { once: true, margin: '-40px' });

  useEffect(() => {
    if (!inView || !ref.current) return;
    const controls = animate(0, to, {
      duration: 2.2,
      ease: [0.16, 1, 0.3, 1],
      onUpdate(value) {
        if (ref.current) ref.current.textContent = Math.floor(value).toLocaleString() + suffix;
      },
    });
    return () => controls.stop();
  }, [inView, to, suffix]);

  return <span ref={ref}>0{suffix}</span>;
}

// ─── Flip Card Component ──────────────────────────────────────────────────────
interface FlipCardProps {
  icon: React.ElementType;
  title: string;
  desc: string;
  details: {
    headline: string;
    points: string[];
    badge: string;
  };
}

function FlipCard({ icon: Icon, title, desc, details }: FlipCardProps) {
  const [flipped, setFlipped] = useState(false);

  return (
    <div
      className="group relative h-80 cursor-pointer"
      style={{ perspective: '1200px' }}
      role="button"
      tabIndex={0}
      aria-pressed={flipped}
      aria-label={`${title}. ${flipped ? 'Showing details' : 'Activate to flip'}`}
      onMouseEnter={() => setFlipped(true)}
      onMouseLeave={() => setFlipped(false)}
      onClick={() => setFlipped((v) => !v)}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          setFlipped((v) => !v);
        }
      }}
    >
      <motion.div
        className="relative w-full h-full"
        animate={{ rotateY: flipped ? 180 : 0 }}
        transition={{ duration: 0.65, ease: [0.23, 1, 0.32, 1] }}
        style={{ transformStyle: 'preserve-3d' }}
      >
        {/* ── Front Face ── */}
        <div
          className="absolute inset-0 rounded-3xl border border-black/5 dark:border-white/10 bg-white/70 dark:bg-white/[0.03] backdrop-blur-xl p-8 flex flex-col justify-between shadow-soft dark:shadow-card"
          style={{ backfaceVisibility: 'hidden' }}
        >
          {/* Hover shimmer */}
          <div
            className="absolute inset-0 rounded-3xl pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-300"
            style={{ background: 'radial-gradient(300px circle at 60% 40%, rgba(20,184,166,0.12), transparent 70%)' }}
          />
          <div>
            <div className="mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500/10 to-cyan-500/10 dark:from-brand-500/20 dark:to-cyan-500/20 border border-brand-500/20 dark:border-white/15 text-brand-600 dark:text-neon-cyan shadow-glow-sm">
              <Icon size={26} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-3">{title}</h3>
            <p className="text-sm leading-relaxed text-slate-600 dark:text-ink-400">{desc}</p>
          </div>
          <div className="flex items-center gap-1.5 text-xs font-semibold text-brand-500 dark:text-brand-400 mt-4 opacity-60">
            <RotateCcw size={12} />
            <span>Hover or tap to explore</span>
          </div>
        </div>

        {/* ── Back Face ── */}
        <div
          className="absolute inset-0 rounded-3xl border border-brand-500/30 dark:border-brand-400/30 bg-gradient-to-br from-brand-600 via-teal-700 to-cyan-700 dark:from-brand-800/90 dark:via-teal-900/90 dark:to-cyan-900/90 backdrop-blur-xl p-8 flex flex-col justify-between shadow-glow-indigo"
          style={{ backfaceVisibility: 'hidden', transform: 'rotateY(180deg)' }}
        >
          {/* Grain overlay */}
          <div
            className="absolute inset-0 rounded-3xl pointer-events-none opacity-[0.04]"
            style={{
              backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='1'/%3E%3C/svg%3E")`,
              backgroundSize: '160px 160px',
            }}
          />
          <div>
            <span className="inline-block rounded-full border border-white/30 bg-white/15 px-3 py-1 text-[10px] font-bold uppercase tracking-widest text-white mb-4">
              {details.badge}
            </span>
            <h4 className="text-lg font-extrabold text-white mb-4 leading-snug">{details.headline}</h4>
            <ul className="space-y-2">
              {details.points.map((point, i) => (
                <li key={i} className="flex items-start gap-2 text-xs text-white/85 leading-relaxed">
                  <CheckCircle2 size={13} className="text-emerald-300 mt-0.5 shrink-0" />
                  {point}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </motion.div>
    </div>
  );
}

// ─── Landing Page ─────────────────────────────────────────────────────────────
export function Landing() {
  const { theme, toggleTheme } = useTheme();
  const heroRef = useRef<HTMLDivElement>(null);
  const navRef = useRef<HTMLElement>(null);

  // GSAP entrance timeline (Lenis is handled globally in App)
  useEffect(() => {
    const ctx = gsap.context(() => {
      const tl = gsap.timeline({ defaults: { ease: 'power3.out', duration: 1.1 } });
      tl.from('.gsap-nav', { y: -30, opacity: 0, delay: 0.2 })
        .from('.gsap-title', { y: 35, opacity: 0, duration: 1.3 }, '-=0.6')
        .from('.gsap-desc', { y: 25, opacity: 0 }, '-=0.9')
        .from('.gsap-cta', { y: 20, opacity: 0, stagger: 0.15 }, '-=0.8')
        .from('.gsap-scroll', { opacity: 0 }, '-=0.4');
    }, heroRef);

    return () => {
      ctx.revert();
    };
  }, []);

  const features: FlipCardProps[] = [
    {
      icon: Target,
      title: 'AI-Personalized Questions',
      desc: 'Deep analytical model that tailors question sets dynamically based on your target role, resume, and specific company culture.',
      details: {
        badge: 'Adaptive Engine',
        headline: 'Questions built around YOU — not a generic question bank.',
        points: [
          'Parses your resume for skills, years of experience, and project domains',
          'Maps questions to role-level expectations (SDE-1 vs Staff Engineer)',
          'Adapts difficulty in real-time based on prior answer quality',
          'Covers DSA, system design, behavioral, and domain-specific areas',
        ],
      },
    },
    {
      icon: BarChart3,
      title: 'Real-Time Evaluation Metrics',
      desc: 'Instant 360-degree performance feedback rating communication fluidity, technical depth, problem-solving, and confidence.',
      details: {
        badge: 'Live Scoring',
        headline: 'Know exactly where you stand after every single answer.',
        points: [
          'Scores on 5 axes: accuracy, clarity, depth, structure, confidence',
          'Keyword density and STAR-method adherence analysis',
          'Benchmarked against real-world pass/fail thresholds at top firms',
          'Trend graph shows improvement curve across sessions',
        ],
      },
    },
    {
      icon: Shield,
      title: 'Simulated High-Stakes Environment',
      desc: 'Authentic web-based video interview simulation with timed responses designed to mirror high-intensity technical rounds.',
      details: {
        badge: 'Pressure Mode',
        headline: 'Real interview pressure. Safe practice environment.',
        points: [
          'Countdown timers that match actual FAANG interview windows',
          'Follow-up clarification rounds simulate real interviewer behavior',
          'Cognitive-load profiling to detect hesitation and response gaps',
          'Stress-test mode with back-to-back questions, no breaks',
        ],
      },
    },
    {
      icon: Sparkles,
      title: 'Line-by-Line AI Coaching',
      desc: 'Receive optimal answer reformulations, STAR method optimizations, and key metrics to dramatically boost your pass rate.',
      details: {
        badge: 'Coaching Engine',
        headline: 'Every word in your answer is analyzed and improved.',
        points: [
          'Sentence-level rewrite suggestions with explanations',
          'STAR (Situation-Task-Action-Result) structure enforcement',
          'Detects filler words, passive voice, and vague quantifiers',
          'Provides model answers from top-performer response patterns',
        ],
      },
    },
    {
      icon: TrendingUp,
      title: 'Skill Gap Heatmap',
      desc: 'Pinpoint precise gaps in algorithmic execution, system architecture, or behavioral phrasing with personalized practice rounds.',
      details: {
        badge: 'Gap Analysis',
        headline: 'Visual roadmap of your weakest areas — with a fix plan.',
        points: [
          'Heat-mapped skill radar across 12+ engineering domains',
          'Highlights repeated failure patterns across sessions',
          'Auto-generates targeted drills for bottom-quartile skills',
          'Tracks improvement weekly with delta scoring',
        ],
      },
    },
    {
      icon: Zap,
      title: 'Sub-Second Turnaround',
      desc: 'Instantaneous evaluation report generation powered by custom high-throughput LLM pipelines for immediate iteration.',
      details: {
        badge: 'Speed',
        headline: 'Evaluate, iterate, and improve — in under a second.',
        points: [
          'Custom LLM pipeline with <800ms avg. evaluation latency',
          'Parallel processing of transcript, tone, and logic simultaneously',
          'Instant score display without any page reload or wait state',
          'Supports high-concurrency — no queuing during peak hours',
        ],
      },
    },
  ];

  const stats = [
    { label: 'Mock Interviews Conducted', value: 50000, suffix: '+' },
    { label: 'Candidate Pass Rate Boost', value: 78, suffix: '%' },
    { label: 'Enterprise & SaaS Roles Covered', value: 650, suffix: '+' },
    { label: 'AI Evaluation Prompts Run', value: 300000, suffix: '+' },
  ];

  return (
    <PageWrapper>
      <div
        ref={heroRef}
        className="relative min-h-screen bg-white dark:bg-[#04060A] text-slate-900 dark:text-white overflow-hidden selection:bg-brand-500/30 transition-colors duration-500"
      >

        {/* ── 3D Interactive Multi-Layer Canvas Background ── */}
        <HeroCanvas theme={theme} />

        {/* ── Noise Grain Texture Overlay ── */}
        <div
          className="fixed inset-0 pointer-events-none z-10 opacity-[0.025]"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='1'/%3E%3C/svg%3E")`,
            backgroundSize: '180px 180px',
          }}
        />

        {/* ── Glass Navbar ── */}
        <nav
          ref={navRef}
          className="gsap-nav sticky top-0 z-50 border-b border-black/5 dark:border-white/10 bg-white/70 dark:bg-[#04060A]/75 backdrop-blur-2xl transition-colors duration-300"
        >
          <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-6 lg:px-8">
            <Link to="/" className="flex items-center gap-3 group">
              <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 via-teal-500 to-cyan-500 shadow-glow-indigo group-hover:scale-105 transition-transform duration-300">
                <BrainCircuit size={22} className="text-white" />
              </div>
              <span className="text-xl font-extrabold tracking-tight text-slate-900 dark:text-white group-hover:text-brand-500 transition-colors">
                InterviAI
              </span>
            </Link>

            <div className="flex items-center gap-4 sm:gap-6">
              <motion.button
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.9 }}
                onClick={toggleTheme}
                className="flex h-9 w-9 items-center justify-center rounded-full text-slate-600 hover:text-slate-900 hover:bg-slate-200/60 dark:text-ink-400 dark:hover:text-white dark:hover:bg-white/8 transition-colors cursor-pointer"
                aria-label="Toggle theme"
              >
                {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} className="text-slate-800" />}
              </motion.button>
              <Link
                to="/login"
                className="relative z-50 text-sm font-semibold text-slate-600 hover:text-slate-900 dark:text-ink-300 dark:hover:text-white transition-colors"
              >
                Log in
              </Link>
              <MagneticButton to="/register" variant="primary" className="text-sm px-6 py-2.5">
                Get Started <ArrowRight size={16} />
              </MagneticButton>
            </div>
          </div>
        </nav>

        {/* ── Hero Section ── */}
        <section className="relative z-20 min-h-[calc(100vh-5rem)] pt-24 pb-20 px-6 text-center max-w-6xl mx-auto flex flex-col items-center justify-center">

          <h1 className="gsap-title font-display text-6xl sm:text-8xl lg:text-[120px] font-extrabold tracking-tight leading-[0.95] mb-6">
            <span className="inline-block bg-gradient-to-r from-brand-500 via-cyan-400 to-teal-300 dark:from-brand-300 dark:via-neon-cyan dark:to-cyan-200 bg-clip-text text-transparent drop-shadow-[0_0_40px_rgba(20,184,166,0.35)]">
              InterviAI
            </span>
          </h1>

          <p className="gsap-desc max-w-xl text-base sm:text-lg font-normal leading-relaxed text-slate-600 dark:text-ink-400 mb-10">
            Personalized AI mock interviews with real-time feedback for your target role.
          </p>

          <div className="gsap-cta relative z-30 flex flex-wrap items-center justify-center gap-5 mb-14">
            <MagneticButton to="/register" variant="primary" className="text-base px-9 py-4">
              Start Mock Interview <ArrowRight size={18} />
            </MagneticButton>
            <MagneticButton to="/login" variant="secondary" className="text-base px-8 py-4">
              Sign In
            </MagneticButton>
          </div>

          <div className="gsap-scroll flex flex-col items-center gap-2 text-slate-400 dark:text-ink-500 text-xs tracking-widest uppercase">
            <span>Scroll to Explore</span>
            <ChevronDown size={16} className="animate-bounce text-brand-500 dark:text-brand-400" />
          </div>
        </section>

        {/* ── Stats Bar ── */}
        <section className="relative z-20 py-20">
          <div className="mx-auto max-w-7xl px-6 lg:px-8">
            <div className="grid grid-cols-2 gap-8 lg:grid-cols-4 rounded-3xl border border-black/5 dark:border-white/10 bg-white/60 dark:bg-white/[0.03] backdrop-blur-xl p-10 shadow-soft dark:shadow-card">
              {stats.map((s) => (
                <div key={s.label} className="text-center space-y-2">
                  <p className="text-4xl sm:text-5xl font-black tracking-tight text-slate-900 dark:text-white tabular-nums">
                    <AnimatedCounter to={s.value} suffix={s.suffix} />
                  </p>
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-ink-400">
                    {s.label}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ── Feature Flip Cards Grid ── */}
        <section className="relative z-20 py-32 px-6 max-w-7xl mx-auto">
          <div className="text-center max-w-3xl mx-auto mb-20 space-y-4">
            <span className="inline-block rounded-full border border-brand-500/20 bg-brand-500/10 dark:border-white/10 dark:bg-white/5 px-4 py-1.5 text-xs font-bold uppercase tracking-widest text-brand-600 dark:text-brand-300">
              ARCHITECTED FOR TOP 1% PERFORMANCE
            </span>
            <h2 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-slate-900 dark:text-white">
              Built like a real candidate lab.
            </h2>
            <p className="text-base sm:text-lg text-slate-600 dark:text-ink-400">
              Every element simulates the exact pressure, structure, and algorithmic evaluation criteria used by tier-1 tech firms.
            </p>
            <p className="text-xs text-slate-400 dark:text-ink-500 flex items-center justify-center gap-1.5 pt-2">
              <RotateCcw size={12} />
              Hover any card to reveal deep feature details
            </p>
          </div>

          <div className="grid grid-cols-1 gap-x-10 gap-y-8 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f) => (
              <FlipCard key={f.title} {...f} />
            ))}
          </div>
        </section>

        {/* ── Final Call to Action ── */}
        <section className="relative z-20 py-32 px-6 text-center max-w-5xl mx-auto">
          <TiltCard className="p-16 border border-brand-500/30 bg-gradient-to-br from-brand-950/40 via-obsidian-950 to-cyan-950/40 shadow-glow-lg">
            <div className="flex flex-col items-center space-y-6">
              <div className="flex h-16 w-16 items-center justify-center rounded-3xl bg-gradient-to-br from-brand-500 to-cyan-500 text-white shadow-glow-indigo">
                <BrainCircuit size={32} />
              </div>
              <h2 className="text-4xl sm:text-5xl font-black text-white">
                Ready to land your dream offer?
              </h2>
              <p className="max-w-xl text-base text-ink-400">
                Join thousands of software engineers, product managers, and data scientists practicing smarter with InterviAI.
              </p>
              <MagneticButton to="/register" variant="primary" className="text-base px-10 py-4 mt-4">
                Start Your Free Trial Now <ArrowRight size={18} />
              </MagneticButton>
            </div>
          </TiltCard>
        </section>

        {/* ── Footer ── */}
        <footer className="relative z-20 border-t border-white/10 bg-transparent py-12 px-6">
          <div className="mx-auto max-w-7xl flex flex-col sm:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-cyan-500 text-white">
                <BrainCircuit size={16} />
              </div>
              <span className="font-extrabold text-white">InterviAI</span>
            </div>
            <p className="text-xs text-ink-500">
              © {new Date().getFullYear()} InterviAI Inc. All rights reserved. Awwwards-inspired frontend design.
            </p>
          </div>
        </footer>

      </div>
    </PageWrapper>
  );
}