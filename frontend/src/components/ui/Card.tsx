import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';
import { useReducedMotion } from '../../hooks/useReducedMotion';

// ─── Types ────────────────────────────────────────────────────────────────────
type CardVariant = 'default' | 'elevated' | 'glass' | 'stat';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: CardVariant;
  hoverable?: boolean;
  noPadding?: boolean;
}

// ─── Base styles per variant ─────────────────────────────────────────────────
const variantStyles: Record<CardVariant, string> = {
  default:
    'bg-white dark:bg-obsidian-900 border border-ink-100 dark:border-white/[0.06] shadow-soft dark:shadow-card',
  elevated:
    'bg-white dark:bg-obsidian-850 border border-ink-200 dark:border-white/[0.08] shadow-lift dark:shadow-card',
  glass:
    'glass-panel',
  stat:
    'bg-white dark:bg-obsidian-900 border-l-2 border-l-brand-500 border border-ink-100 dark:border-white/[0.06] shadow-soft dark:shadow-card',
};

// ─── Card ─────────────────────────────────────────────────────────────────────
export function Card({
  className,
  children,
  variant = 'default',
  hoverable = false,
  ...props
}: CardProps) {
  const reduced = useReducedMotion();

  const hoverProps =
    !reduced && hoverable
      ? {
          whileHover: { y: -3, scale: 1.005 },
          transition: { duration: 0.22, ease: [0.25, 0.1, 0.25, 1] },
        }
      : {};

  return (
    <motion.div
      className={cn(
        'rounded-2xl text-ink-900 dark:text-ink-100',
        variantStyles[variant],
        hoverable && 'cursor-default transition-shadow duration-300 hover:shadow-lift dark:hover:shadow-card-hover',
        className
      )}
      {...hoverProps}
      {...(props as any)}
    >
      {children}
    </motion.div>
  );
}

// ─── CardHeader ───────────────────────────────────────────────────────────────
export function CardHeader({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn('flex flex-col space-y-1 p-6', className)}
      {...props}
    />
  );
}

// ─── CardTitle ────────────────────────────────────────────────────────────────
export function CardTitle({
  className,
  ...props
}: React.HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h3
      className={cn(
        'text-base font-semibold leading-snug tracking-tight text-ink-900 dark:text-white',
        className
      )}
      {...props}
    />
  );
}

// ─── CardDescription ─────────────────────────────────────────────────────────
export function CardDescription({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) {
  return (
    <p
      className={cn('text-sm text-ink-500 dark:text-ink-400 leading-relaxed', className)}
      {...props}
    />
  );
}

// ─── CardContent ──────────────────────────────────────────────────────────────
export function CardContent({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('p-6 pt-0', className)} {...props} />;
}

// ─── CardFooter ───────────────────────────────────────────────────────────────
export function CardFooter({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        'flex items-center p-6 pt-0 border-t border-ink-100 dark:border-white/[0.06] mt-2',
        className
      )}
      {...props}
    />
  );
}