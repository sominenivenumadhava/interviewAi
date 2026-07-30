import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';
import { useReducedMotion } from '../../hooks/useReducedMotion';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  children?: React.ReactNode; // action buttons slot
  className?: string;
}

export function PageHeader({ title, subtitle, children, className }: PageHeaderProps) {
  const reduced = useReducedMotion();

  return (
    <motion.div
      initial={reduced ? { opacity: 1 } : { opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: [0.25, 0.1, 0.25, 1] }}
      className={cn('flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between', className)}
    >
      <div className="space-y-1">
        <h1 className="page-title">{title}</h1>
        {subtitle && <p className="page-subtitle">{subtitle}</p>}
      </div>

      {children && (
        <div className="flex items-center gap-3 shrink-0">{children}</div>
      )}
    </motion.div>
  );
}
