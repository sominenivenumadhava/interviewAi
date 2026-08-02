import React, { useRef } from 'react';
import { motion, useInView } from 'framer-motion';
import { cn } from '../../lib/utils';
import { useReducedMotion } from '../../hooks/useReducedMotion';

interface ProgressBarProps {
  value: number;       // 0–100
  label?: string;
  showValue?: boolean;
  color?: 'indigo' | 'emerald' | 'amber' | 'rose' | 'cyan' | 'violet';
  size?: 'sm' | 'md' | 'lg';
  delay?: number;
  className?: string;
}

const colorMap: Record<string, string> = {
  indigo:  'bg-brand-500',
  emerald: 'bg-emerald-500',
  amber:   'bg-amber-500',
  rose:    'bg-rose-500',
  cyan:    'bg-cyan-500',
  violet:  'bg-cyan-500',
};

const sizeMap: Record<string, string> = {
  sm: 'h-1.5',
  md: 'h-2',
  lg: 'h-3',
};

export function ProgressBar({
  value,
  label,
  showValue = true,
  color = 'indigo',
  size = 'md',
  delay = 0,
  className,
}: ProgressBarProps) {
  const reduced = useReducedMotion();
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: '-20px' });
  const clamped = Math.min(100, Math.max(0, value));

  return (
    <div ref={ref} className={cn('w-full', className)}>
      {(label || showValue) && (
        <div className="flex items-center justify-between mb-1.5">
          {label && (
            <span className="text-xs font-medium text-ink-700 dark:text-ink-300">{label}</span>
          )}
          {showValue && (
            <span className="text-xs font-semibold text-ink-500 dark:text-ink-400 tabular-nums">
              {clamped}%
            </span>
          )}
        </div>
      )}

      <div className={cn('w-full rounded-full overflow-hidden progress-bar-track', sizeMap[size])}>
        <motion.div
          initial={reduced ? { width: `${clamped}%` } : { width: 0 }}
          animate={inView ? { width: `${clamped}%` } : { width: 0 }}
          transition={{ duration: 1, delay, ease: [0.34, 1.56, 0.64, 1] }}
          className={cn('h-full rounded-full progress-bar-fill', colorMap[color])}
        />
      </div>
    </div>
  );
}
