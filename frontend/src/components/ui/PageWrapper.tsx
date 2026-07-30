import React from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '../../lib/motion';
import { useReducedMotion } from '../../hooks/useReducedMotion';

interface PageWrapperProps {
  children: React.ReactNode;
  className?: string;
}

/**
 * PageWrapper — Wraps every page with Framer Motion enter/exit transitions.
 * • Enter: fade in + Y slide up + blur → sharp (0.5s ease out)
 * • Exit: fade out + slight scale down + blur
 */
export function PageWrapper({ children, className = '' }: PageWrapperProps) {
  const reduced = useReducedMotion();

  if (reduced) {
    return <div className={className}>{children}</div>;
  }

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      exit="exit"
      className={className}
    >
      {children}
    </motion.div>
  );
}
