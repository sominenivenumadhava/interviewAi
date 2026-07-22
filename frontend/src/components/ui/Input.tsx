import React, { forwardRef } from 'react';
import { cn } from '../../lib/utils';
export interface InputProps extends
  React.InputHTMLAttributes<HTMLInputElement> {
  error?: string;
  icon?: React.ReactNode;
}
export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, icon, ...props }, ref) => {
    return (
      <div className="relative w-full">
        {icon &&
        <div className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-400">
            {icon}
          </div>
        }
        <input
          ref={ref}
          className={cn(
            'flex h-10 w-full rounded-xl border border-ink-200 bg-white px-3 py-2 text-sm text-ink-900 placeholder:text-ink-400 focus:outline-none focus:ring-2 focus:ring-brand-500 disabled:cursor-not-allowed disabled:opacity-50 dark:border-ink-700 dark:bg-ink-900 dark:text-ink-100 dark:placeholder:text-ink-500',
            icon && 'pl-10',
            error && 'border-danger focus:ring-danger',
            className
          )}
          {...props} />
        
        {error && <p className="mt-1 text-xs text-danger">{error}</p>}
      </div>);

  }
);
Input.displayName = 'Input';