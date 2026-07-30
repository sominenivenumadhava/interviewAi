import React from 'react';
import { cn } from '../../lib/utils';

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'secondary' | 'outline' | 'success' | 'warning' | 'danger' | 'info' | 'premium';
  pulse?: boolean;
}

export function Badge({ className, variant = 'default', pulse = false, ...props }: BadgeProps) {
  const variants: Record<string, string> = {
    default:
      'border-transparent bg-brand-500/10 text-brand-600 dark:bg-brand-500/15 dark:text-brand-300',
    secondary:
      'border-transparent bg-ink-100 text-ink-700 dark:bg-white/8 dark:text-ink-200',
    outline:
      'text-ink-700 dark:text-ink-200 border-ink-200 dark:border-white/15 bg-transparent',
    success:
      'border-transparent bg-emerald-500/10 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400',
    warning:
      'border-transparent bg-amber-500/10 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400',
    danger:
      'border-transparent bg-rose-500/10 text-rose-700 dark:bg-rose-500/15 dark:text-rose-400',
    info:
      'border-transparent bg-cyan-500/10 text-cyan-700 dark:bg-cyan-500/15 dark:text-cyan-400',
    premium:
      'border-transparent bg-gradient-to-r from-brand-500/20 to-violet-500/20 text-brand-600 dark:text-brand-300 border border-brand-500/20',
  };

  return (
    <div
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold',
        'transition-colors duration-200',
        variants[variant],
        pulse && 'animate-pulse',
        className
      )}
      {...props}
    />
  );
}