import React, { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { motion, useInView, animate } from 'framer-motion';
import gsap from 'gsap';
import Lenis from 'lenis';
import { useTheme } from '../contexts/ThemeContext';
import {
  BrainCircuit, ArrowRight, BarChart3, Target, Shield,
  Sparkles, Zap, TrendingUp, CheckCircle2, ChevronDown, Star,
  Award, Layers, Cpu, Compass, Sun, Moon
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

// ─── Landing Page ─────────────────────────────────────────────────────────────
export function Landing() {
  const { theme, toggleTheme } = useTheme();
  const heroRef = useRef<HTMLDivElement>(null);
  const navRef = useRef<HTMLElement>(null);

  // Initialize Lenis Smooth Scroll & GSAP Entrance Timeline
  useEffect(() => {
    // Lenis Smooth Scroll
    const lenis = new Lenis({
      duration: 1.2,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
    });

    function raf(time: number) {
      lenis.raf(time);
      requestAnimationFrame(raf);
    }
    requestAnimationFrame(raf);

    // GSAP Sequence Intro Timeline
    const ctx = gsap.context(() => {
      const tl = gsap.timeline({ defaults: { ease: 'power3.out', duration: 1.1 } });

      tl.from('.gsap-nav', { y: -30, opacity: 0, delay: 0.2 })
        .from('.gsap-badge', { y: 20, opacity: 0 }, '-=0.6')
        .from('.gsap-title', { y: 35, opacity: 0, duration: 1.3 }, '-=0.8')
        .from('.gsap-desc', { y: 25, opacity: 0 }, '-=0.9')
        .from('.gsap-cta', { y: 20, opacity: 0, stagger: 0.15 }, '-=0.8')
        .from('.gsap-stats', { y: 30, opacity: 0 }, '-=0.6')
        .from('.gsap-scroll', { opacity: 0 }, '-=0.4');
    }, heroRef);

    return () => {
      lenis.destroy();
      ctx.revert();
    };
  }, []);

  const features = [
    {
      icon: Target,
      title: 'AI-Personalized Questions',
      desc: 'Deep analytical model that tailors question sets dynamically based on your target role, resume, and specific company culture.',
    },
    {
      icon: BarChart3,
      title: 'Real-Time Evaluation Metrics',
      desc: 'Instant 360-degree performance feedback rating communication fluidity, technical depth, problem-solving, and confidence.',
    },
    {
      icon: Shield,
      title: 'Simulated High-Stakes Environment',
      desc: 'Authentic web-based video interview simulation with timed responses designed to mirror high-intensity technical rounds.',
    },
    {
      icon: Sparkles,
      title: 'Line-by-Line AI Coaching',
      desc: 'Receive optimal answer reformulations, STAR method optimizations, and key metrics to dramatically boost your pass rate.',
    },
    {
      icon: TrendingUp,
      title: 'Skill Gap Heatmap',
      desc: 'Pinpoint precise gaps in algorithmic execution, system architecture, or behavioral phrasing with personalized practice rounds.',
    },
    {
      icon: Zap,
      title: 'Sub-Second Turnaround',
      desc: 'Instantaneous evaluation report generation powered by custom high-throughput LLM pipelines for immediate iteration.',
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

        {/* ── Layer 6: Subtle Noise Grain Texture Overlay ── */}
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
              <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 via-indigo-600 to-violet-600 shadow-glow-indigo group-hover:scale-105 transition-transform duration-300">
                <BrainCircuit size={22} className="text-white" />
              </div>
              <span className="text-xl font-extrabold tracking-tight text-slate-900 dark:text-white group-hover:text-brand-500 transition-colors">
                InterviAI
              </span>
            </Link>

            <div className="flex items-center gap-4 sm:gap-6">
              {/* Theme toggle button */}
              <motion.button
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.9 }}
                onClick={toggleTheme}
                className="flex h-9 w-9 items-center justify-center rounded-full text-slate-600 hover:text-slate-900 hover:bg-slate-200/60 dark:text-ink-400 dark:hover:text-white dark:hover:bg-white/8 transition-colors cursor-pointer"
                aria-label="Toggle theme"
              >
                {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} className="text-slate-800" />}
              </motion.button>

              <Link to="/login">
                <span className="text-sm font-semibold text-slate-600 hover:text-slate-900 dark:text-ink-300 dark:hover:text-white transition-colors cursor-pointer">
                  Log in
                </span>
              </Link>
              <Link to="/register">
                <MagneticButton variant="primary" className="text-sm px-6 py-2.5">
                  Get Started <ArrowRight size={16} />
                </MagneticButton>
              </Link>
            </div>
          </div>
        </nav>

        {/* ── Hero Section ── */}
        <section className="relative z-20 pt-20 pb-32 px-6 text-center max-w-6xl mx-auto flex flex-col items-center">
          
          {/* Badge */}
          <div className="gsap-badge mb-8">
            <div className="inline-flex items-center gap-2.5 rounded-full border border-brand-500/30 bg-brand-500/10 px-5 py-2 text-xs font-semibold tracking-wider text-brand-600 dark:text-neon-cyan backdrop-blur-md shadow-glow-sm">
              <Sparkles size={14} className="text-brand-500 dark:text-brand-400 animate-pulse" />
              <span>NEXT-GEN AI INTERVIEW ENGINE</span>
            </div>
          </div>

          {/* Headline with Animated Shimmer AI */}
          <h1 className="gsap-title text-6xl sm:text-8xl lg:text-[110px] font-black tracking-tight leading-[0.98] text-slate-900 dark:text-white mb-8">
            Master Your Next<br />
            Interview with{' '}
            <span className="inline-block bg-gradient-to-r from-brand-500 via-indigo-500 to-violet-600 dark:from-brand-300 dark:via-neon-cyan dark:to-violet-400 bg-clip-text text-transparent drop-shadow-[0_0_35px_rgba(99,102,241,0.4)] animate-pulse">
              AI
            </span>
          </h1>

          {/* Subtitle */}
          <p className="gsap-desc max-w-2xl text-lg sm:text-xl font-normal leading-relaxed text-slate-600 dark:text-ink-400 mb-12">
            Hyper-personalized AI mock interviews tailored to your exact resume, target company, and technical seniority. Practice under real pressure with line-by-line feedback.
          </p>

          {/* CTA Buttons */}
          <div className="gsap-cta flex flex-wrap items-center justify-center gap-5 mb-16">
            <Link to="/register">
              <MagneticButton variant="primary" className="text-base px-9 py-4">
                Start Your First Mock Interview <ArrowRight size={18} />
              </MagneticButton>
            </Link>
            <Link to="/login">
              <MagneticButton variant="secondary" className="text-base px-8 py-4">
                Sign In
              </MagneticButton>
            </Link>
          </div>

          {/* Trust Highlights */}
          <div className="gsap-cta flex flex-wrap items-center justify-center gap-8 text-xs font-semibold text-slate-500 dark:text-ink-500 uppercase tracking-widest mb-16">
            <span className="flex items-center gap-2">
              <CheckCircle2 size={15} className="text-emerald-500 dark:text-emerald-400" /> Free to Start
            </span>
            <span className="flex items-center gap-2">
              <CheckCircle2 size={15} className="text-emerald-500 dark:text-emerald-400" /> No Credit Card Required
            </span>
            <span className="flex items-center gap-2">
              <CheckCircle2 size={15} className="text-emerald-500 dark:text-emerald-400" /> 650+ Companies Supported
            </span>
          </div>

          {/* Scroll Indicator */}
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

        {/* ── Feature Cards Grid with 3D Tilt ── */}
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
          </div>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f) => {
              const Icon = f.icon;
              return (
                <TiltCard key={f.title} className="flex flex-col justify-between h-full border-black/5 dark:border-white/10 bg-white/70 dark:bg-white/[0.03]">
                  <div>
                    <div className="mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500/10 to-violet-500/10 dark:from-brand-500/20 dark:to-violet-500/20 border border-brand-500/20 dark:border-white/15 text-brand-600 dark:text-neon-cyan shadow-glow-sm">
                      <Icon size={26} />
                    </div>
                    <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-3">{f.title}</h3>
                    <p className="text-sm leading-relaxed text-slate-600 dark:text-ink-400">{f.desc}</p>
                  </div>
                  <div className="mt-8 flex items-center gap-2 text-xs font-semibold text-brand-600 dark:text-brand-400 group-hover:text-brand-500 transition-colors">
                    Learn More <ArrowRight size={14} />
                  </div>
                </TiltCard>
              );
            })}
          </div>
        </section>

        {/* ── Final Call to Action ── */}
        <section className="relative z-20 py-32 px-6 text-center max-w-5xl mx-auto">
          <TiltCard className="p-16 border border-brand-500/30 bg-gradient-to-br from-brand-950/40 via-obsidian-950 to-violet-950/40 shadow-glow-lg">
            <div className="flex flex-col items-center space-y-6">
              <div className="flex h-16 w-16 items-center justify-center rounded-3xl bg-gradient-to-br from-brand-500 to-violet-600 text-white shadow-glow-indigo">
                <BrainCircuit size={32} />
              </div>
              <h2 className="text-4xl sm:text-5xl font-black text-white">
                Ready to land your dream offer?
              </h2>
              <p className="max-w-xl text-base text-ink-400">
                Join thousands of software engineers, product managers, and data scientists practicing smarter with InterviAI.
              </p>
              <Link to="/register">
                <MagneticButton variant="primary" className="text-base px-10 py-4 mt-4">
                  Start Your Free Trial Now <ArrowRight size={18} />
                </MagneticButton>
              </Link>
            </div>
          </TiltCard>
        </section>

        {/* ── Footer ── */}
        <footer className="relative z-20 border-t border-white/10 bg-transparent py-12 px-6">
          <div className="mx-auto max-w-7xl flex flex-col sm:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-violet-600 text-white">
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