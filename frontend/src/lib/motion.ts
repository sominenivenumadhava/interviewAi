/**
 * motion.ts — Global Framer Motion variant library
 * All animations in InterviAI use these shared tokens.
 * Respects prefers-reduced-motion via useReducedMotion hook.
 */
import type { Variants, Transition } from 'framer-motion';

// ─── Easing Curves ────────────────────────────────────────────────────────────
export const ease = {
  smooth: [0.25, 0.1, 0.25, 1],
  snappy: [0.4, 0, 0.2, 1],
  bouncy: [0.34, 1.56, 0.64, 1],
  out: [0, 0, 0.2, 1],
  in: [0.4, 0, 1, 1],
} as const;

// ─── Spring Configs ───────────────────────────────────────────────────────────
export const spring = {
  gentle: { type: 'spring', stiffness: 100, damping: 20, mass: 1 } as Transition,
  snappy: { type: 'spring', stiffness: 300, damping: 30, mass: 0.8 } as Transition,
  bouncy: { type: 'spring', stiffness: 400, damping: 20, mass: 0.6 } as Transition,
  slow: { type: 'spring', stiffness: 60, damping: 18, mass: 1.2 } as Transition,
};

// ─── Page Transition Variants ─────────────────────────────────────────────────
export const pageVariants: Variants = {
  initial: {
    opacity: 0,
    y: 20,
    filter: 'blur(4px)',
  },
  animate: {
    opacity: 1,
    y: 0,
    filter: 'blur(0px)',
    transition: {
      duration: 0.5,
      ease: ease.out,
    },
  },
  exit: {
    opacity: 0,
    y: -10,
    scale: 0.98,
    filter: 'blur(4px)',
    transition: {
      duration: 0.3,
      ease: ease.in,
    },
  },
};

// ─── Common Variants ──────────────────────────────────────────────────────────
export const fadeUp: Variants = {
  initial: { opacity: 0, y: 24 },
  animate: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5, ease: ease.out },
  },
};

export const fadeIn: Variants = {
  initial: { opacity: 0 },
  animate: {
    opacity: 1,
    transition: { duration: 0.4, ease: ease.smooth },
  },
};

export const fadeDown: Variants = {
  initial: { opacity: 0, y: -20 },
  animate: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.45, ease: ease.out },
  },
};

export const scaleIn: Variants = {
  initial: { opacity: 0, scale: 0.92 },
  animate: {
    opacity: 1,
    scale: 1,
    transition: { duration: 0.4, ease: ease.bouncy },
  },
};

export const blurIn: Variants = {
  initial: { opacity: 0, filter: 'blur(12px)' },
  animate: {
    opacity: 1,
    filter: 'blur(0px)',
    transition: { duration: 0.6, ease: ease.out },
  },
};

export const slideInLeft: Variants = {
  initial: { opacity: 0, x: -30 },
  animate: {
    opacity: 1,
    x: 0,
    transition: { duration: 0.45, ease: ease.out },
  },
};

export const slideInRight: Variants = {
  initial: { opacity: 0, x: 30 },
  animate: {
    opacity: 1,
    x: 0,
    transition: { duration: 0.45, ease: ease.out },
  },
};

// ─── Stagger Container ────────────────────────────────────────────────────────
export const staggerContainer: Variants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.08,
      delayChildren: 0.1,
    },
  },
};

export const staggerContainerFast: Variants = {
  initial: {},
  animate: {
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.05,
    },
  },
};

// ─── Card Hover Variants ──────────────────────────────────────────────────────
export const cardHover = {
  rest: {
    y: 0,
    scale: 1,
    boxShadow: '0 4px 20px rgba(0,0,0,0.12)',
  },
  hover: {
    y: -4,
    scale: 1.01,
    boxShadow: '0 20px 40px rgba(99,102,241,0.2)',
    transition: spring.snappy,
  },
  tap: {
    scale: 0.98,
    transition: spring.snappy,
  },
};

// ─── Button Variants ──────────────────────────────────────────────────────────
export const buttonHover = {
  rest: { scale: 1, y: 0 },
  hover: { scale: 1.03, y: -1, transition: spring.snappy },
  tap: { scale: 0.96, y: 0, transition: spring.snappy },
};

// ─── Shake (error) variant ────────────────────────────────────────────────────
export const shakeVariant: Variants = {
  shake: {
    x: [0, -8, 8, -6, 6, -3, 3, 0],
    transition: { duration: 0.4, ease: 'easeInOut' },
  },
};

// ─── Drawer / Modal ───────────────────────────────────────────────────────────
export const modalVariants: Variants = {
  initial: { opacity: 0, scale: 0.94, y: 10 },
  animate: {
    opacity: 1,
    scale: 1,
    y: 0,
    transition: spring.snappy,
  },
  exit: {
    opacity: 0,
    scale: 0.96,
    y: 8,
    transition: { duration: 0.2, ease: ease.in },
  },
};

export const backdropVariants: Variants = {
  initial: { opacity: 0 },
  animate: { opacity: 1, transition: { duration: 0.2 } },
  exit: { opacity: 0, transition: { duration: 0.2 } },
};
