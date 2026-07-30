import React, { forwardRef, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { cn } from '../../lib/utils';
import { Loader2, Check } from 'lucide-react';
import { useReducedMotion } from '../../hooks/useReducedMotion';
import { spring } from '../../lib/motion';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'gradient';
  size?: 'sm' | 'md' | 'lg' | 'icon';
  isLoading?: boolean;
  success?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = 'primary',
      size = 'md',
      isLoading,
      success,
      children,
      disabled,
      onClick,
      ...props
    },
    ref
  ) => {
    const reduced = useReducedMotion();
    const [ripples, setRipples] = useState<{ id: number; x: number; y: number }[]>([]);

    const variants: Record<string, string> = {
      // Primary — Indigo solid
      primary:
        'bg-brand-500 text-white hover:bg-brand-600 shadow-sm hover:shadow-glow-sm',
      // Secondary — Neutral surface
      secondary:
        'bg-ink-100 text-ink-800 hover:bg-ink-200 dark:bg-white/8 dark:text-ink-100 dark:hover:bg-white/12',
      // Outline — theme-aware bordered
      outline:
        'border border-ink-200 dark:border-white/15 bg-transparent text-ink-700 dark:text-ink-200 hover:bg-ink-50 dark:hover:bg-white/5',
      // Ghost — transparent
      ghost:
        'bg-transparent text-ink-500 dark:text-ink-400 hover:bg-ink-100 dark:hover:bg-white/8 hover:text-ink-900 dark:hover:text-white',
      // Danger — Rose
      danger:
        'bg-rose-500 text-white hover:bg-rose-600 shadow-sm hover:shadow-glow-rose',
      // Gradient — Premium animated
      gradient:
        'gradient-button text-white border-0 shadow-glow-md',
    };

    const sizes: Record<string, string> = {
      sm:   'h-8 px-3 text-xs rounded-lg gap-1.5',
      md:   'h-10 px-4 py-2 text-sm rounded-xl gap-2',
      lg:   'h-12 px-6 text-base rounded-2xl gap-2',
      icon: 'h-10 w-10 flex items-center justify-center rounded-xl p-0',
    };

    const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
      if (!reduced && !disabled && !isLoading) {
        const rect = e.currentTarget.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const id = Date.now();
        setRipples((prev) => [...prev, { id, x, y }]);
        setTimeout(() => setRipples((prev) => prev.filter((r) => r.id !== id)), 600);
      }
      onClick?.(e);
    };

    const motionProps = reduced
      ? {}
      : {
          whileHover: disabled || isLoading ? {} : { scale: 1.025, y: -1 },
          whileTap:   disabled || isLoading ? {} : { scale: 0.97 },
          transition: spring.snappy,
        };

    return (
      <motion.button
        ref={ref}
        disabled={disabled || isLoading}
        onClick={handleClick}
        className={cn(
          'relative inline-flex items-center justify-center font-semibold overflow-hidden',
          'transition-all duration-200',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:focus-visible:ring-offset-obsidian-950',
          'disabled:opacity-40 disabled:pointer-events-none',
          variants[variant],
          sizes[size],
          className
        )}
        {...motionProps}
        {...(props as any)}
      >
        {/* Ripple effects */}
        <AnimatePresence>
          {ripples.map((ripple) => (
            <motion.span
              key={ripple.id}
              className="absolute rounded-full bg-white/25 pointer-events-none"
              style={{ left: ripple.x, top: ripple.y, width: 8, height: 8, translateX: '-50%', translateY: '-50%' }}
              initial={{ scale: 0, opacity: 0.6 }}
              animate={{ scale: 18, opacity: 0 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.55, ease: 'easeOut' }}
            />
          ))}
        </AnimatePresence>

        {/* Content */}
        <AnimatePresence mode="wait" initial={false}>
          {isLoading ? (
            <motion.span
              key="loading"
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.8 }}
              className="flex items-center gap-2"
            >
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Loading…</span>
            </motion.span>
          ) : success ? (
            <motion.span
              key="success"
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.8 }}
              className="flex items-center gap-2"
            >
              <Check className="h-4 w-4" />
              <span>Done!</span>
            </motion.span>
          ) : (
            <motion.span
              key="content"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex items-center gap-2"
            >
              {children}
            </motion.span>
          )}
        </AnimatePresence>
      </motion.button>
    );
  }
);

Button.displayName = 'Button';