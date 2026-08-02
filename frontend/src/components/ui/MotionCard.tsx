import React from 'react';
import { motion, useInView, useMotionValue, animate } from 'framer-motion';
import { useEffect, useRef } from 'react';
import { cn } from '../../lib/utils';
import { useReducedMotion } from '../../hooks/useReducedMotion';

// ─── Animated counter hook ────────────────────────────────────────────────────
function useCountUp(to: number, enabled: boolean) {
  const ref = useRef<HTMLSpanElement>(null);
  useEffect(() => {
    if (!enabled || !ref.current) return;
    const controls = animate(0, to, {
      duration: 1.6,
      ease: [0.16, 1, 0.3, 1],
      onUpdate(v) {
        if (ref.current) ref.current.textContent = Math.floor(v).toLocaleString();
      },
    });
    return () => controls.stop();
  }, [to, enabled]);
  return ref;
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
export interface MotionCardProps {
  title: string;
  value: number | string;
  suffix?: string;
  prefix?: string;
  description?: string;
  icon: React.ReactNode;
  iconColor?: string;   // Tailwind bg class e.g. 'bg-brand-500/15'
  iconTextColor?: string; // Tailwind text class e.g. 'text-brand-400'
  accentColor?: string; // Border left color class e.g. 'border-brand-500'
  delay?: number;
  animate?: boolean;
}

export function MotionCard({
  title,
  value,
  suffix = '',
  prefix = '',
  description,
  icon,
  iconColor = 'bg-brand-500/10',
  iconTextColor = 'text-brand-400',
  accentColor = 'border-brand-500',
  delay = 0,
  animate: animateCounter = true,
}: MotionCardProps) {
  const reduced = useReducedMotion();
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: '-40px' });
  const numericValue = typeof value === 'number' ? value : parseFloat(String(value)) || 0;
  const isNumeric = typeof value === 'number';
  const countRef = useCountUp(numericValue, inView && animateCounter && isNumeric && !reduced);

  return (
    <motion.div
      ref={ref}
      initial={reduced ? { opacity: 1 } : { opacity: 0, y: 20 }}
      animate={inView ? { opacity: 1, y: 0 } : {}}
      transition={{ duration: 0.45, delay, ease: [0.25, 0.1, 0.25, 1] }}
      whileHover={reduced ? {} : { y: -3, scale: 1.005 }}
      className={cn(
        'relative overflow-hidden rounded-2xl p-5',
        'border-l-2',
        accentColor,
        'bg-white dark:bg-obsidian-900',
        'border border-ink-100 dark:border-white/5',
        'shadow-soft dark:shadow-card',
        'transition-shadow duration-300 hover:shadow-lift dark:hover:shadow-card-hover',
        'group cursor-default'
      )}
    >
      {/* Subtle background glow */}
      <div
        className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"
        style={{
          background: 'radial-gradient(circle at top left, rgba(20,184,166,0.04), transparent 60%)',
        }}
      />

      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <p className="text-[11px] font-semibold uppercase tracking-widest text-ink-500 dark:text-ink-400 mb-2">
            {title}
          </p>
          <div className="flex items-baseline gap-1">
            {prefix && (
              <span className="text-lg font-bold text-ink-600 dark:text-ink-300">{prefix}</span>
            )}
            {isNumeric && animateCounter && !reduced ? (
              <span
                ref={countRef}
                className="text-3xl font-extrabold tracking-tight text-ink-900 dark:text-white tabular-nums"
              >
                0
              </span>
            ) : (
              <span className="text-3xl font-extrabold tracking-tight text-ink-900 dark:text-white tabular-nums">
                {isNumeric ? numericValue.toLocaleString() : value}
              </span>
            )}
            {suffix && (
              <span className="text-lg font-bold text-ink-500 dark:text-ink-400">{suffix}</span>
            )}
          </div>
          {description && (
            <p className="mt-1.5 text-xs text-ink-400 dark:text-ink-500 truncate">{description}</p>
          )}
        </div>

        <div
          className={cn(
            'flex h-11 w-11 items-center justify-center rounded-xl shrink-0 ml-3',
            iconColor,
            iconTextColor
          )}
        >
          {icon}
        </div>
      </div>
    </motion.div>
  );
}
