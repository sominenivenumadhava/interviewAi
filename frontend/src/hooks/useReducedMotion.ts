import { useReducedMotion as useFramerReducedMotion } from 'framer-motion';

/**
 * Returns true if the user prefers reduced motion.
 * All animation components should check this and skip or simplify animations.
 */
export function useReducedMotion(): boolean {
  const prefersReduced = useFramerReducedMotion();
  return prefersReduced ?? false;
}
