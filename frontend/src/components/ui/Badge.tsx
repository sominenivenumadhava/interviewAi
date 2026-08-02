import React from 'react';
import { cn } from '../../lib/utils';

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'secondary' | 'outline' | 'success' | 'warning' | 'danger' | 'info' | 'premium';
  pulse?: boolean;
}

export function Badge({ className, variant = 'default', pulse = false, ...props }: BadgeProps) {
  const variants: Record<string, string> = {
    default:
      'border-brand-500/30 bg-brand-500/15 text-brand-300',
    secondary:
      'border-white/10 bg-white/10 text-white font-semibold',
    outline:
      'border-white/20 bg-transparent text-ink-200 font-medium',
    success:
      'border-emerald-500/30 bg-emerald-500/15 text-emerald-400',
    warning:
      'border-amber-500/30 bg-amber-500/15 text-amber-300',
    danger:
      'border-rose-500/30 bg-rose-500/15 text-rose-300',
    info:
      'border-cyan-500/30 bg-cyan-500/15 text-cyan-300',
    premium:
      'border-brand-500/30 bg-gradient-to-r from-brand-500/20 to-cyan-500/20 text-brand-300',
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