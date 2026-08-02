import React, { forwardRef, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { cn } from '../../lib/utils';
import { Check, AlertCircle } from 'lucide-react';
import { useReducedMotion } from '../../hooks/useReducedMotion';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: string;
  icon?: React.ReactNode;
  label?: string;
  success?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, icon, label, success, id, ...props }, ref) => {
    const reduced = useReducedMotion();
    const [isFocused, setIsFocused] = useState(false);
    const hasValue = Boolean(props.value || props.defaultValue);

    return (
      <div className="relative w-full">
        {/* Floating label */}
        {label && (
          <motion.label
            htmlFor={id}
            className={cn(
              'absolute pointer-events-none text-ink-400 origin-left z-10',
              icon ? 'left-10' : 'left-3'
            )}
            animate={
              reduced
                ? {}
                : isFocused || hasValue
                ? { y: -22, scale: 0.78, color: error ? '#f43f5e' : '#14b8a6' }
                : { y: 0, scale: 1, color: '#94a3b8' }
            }
            style={{ top: '50%', translateY: '-50%', fontSize: '0.875rem' }}
            transition={{ duration: 0.18, ease: [0.4, 0, 0.2, 1] }}
          >
            {label}
          </motion.label>
        )}

        {/* Left icon */}
        {icon && (
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-400 dark:text-ink-500 z-10 pointer-events-none">
            {icon}
          </div>
        )}

        {/* Input field */}
        <motion.div
          animate={
            reduced ? {} : error ? { x: [0, -6, 6, -4, 4, 0] } : {}
          }
          transition={{ duration: 0.35, ease: 'easeInOut' }}
        >
          <input
            ref={ref}
            id={id}
            onFocus={(e) => {
              setIsFocused(true);
              props.onFocus?.(e);
            }}
            onBlur={(e) => {
              setIsFocused(false);
              props.onBlur?.(e);
            }}
            className={cn(
              // Layout
              'flex h-12 w-full rounded-xl px-3 py-2 text-sm',
              // Light mode
              'bg-white border border-ink-200 text-ink-900 placeholder:text-ink-400',
              // Dark mode
              'dark:bg-obsidian-900/80 dark:border-white/10 dark:text-white dark:placeholder:text-ink-500',
              // Transitions
              'transition-colors duration-200',
              // Focus — handled via style prop below
              'focus:outline-none focus:ring-0',
              // Disabled
              'disabled:cursor-not-allowed disabled:opacity-50',
              // Padding adjustments
              icon && 'pl-10',
              label && 'pt-4 pb-2',
              className
            )}
            style={{
              borderColor: isFocused
                ? error
                  ? '#f43f5e'
                  : '#14b8a6'
                : error
                ? 'rgba(244,63,94,0.5)'
                : undefined,
              boxShadow: isFocused
                ? error
                  ? '0 0 0 3px rgba(244,63,94,0.12)'
                  : '0 0 0 3px rgba(20,184,166,0.12)'
                : 'none',
            }}
            {...props}
          />
        </motion.div>

        {/* Right status icon */}
        <AnimatePresence>
          {success && !error && (
            <motion.div
              key="success"
              initial={{ opacity: 0, scale: 0.5 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.5 }}
              transition={{ duration: 0.2, type: 'spring', stiffness: 300 }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-emerald-500"
            >
              <Check size={16} />
            </motion.div>
          )}
          {error && (
            <motion.div
              key="error"
              initial={{ opacity: 0, scale: 0.5 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.5 }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-rose-400"
            >
              <AlertCircle size={16} />
            </motion.div>
          )}
        </AnimatePresence>

        {/* Error message */}
        <AnimatePresence>
          {error && (
            <motion.p
              key="error-msg"
              initial={{ opacity: 0, y: -4, height: 0 }}
              animate={{ opacity: 1, y: 0, height: 'auto' }}
              exit={{ opacity: 0, y: -4, height: 0 }}
              transition={{ duration: 0.2 }}
              className="mt-1.5 flex items-center gap-1 text-xs text-rose-500"
            >
              {error}
            </motion.p>
          )}
        </AnimatePresence>
      </div>
    );
  }
);

Input.displayName = 'Input';