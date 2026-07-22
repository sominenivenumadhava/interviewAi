import React, { forwardRef } from 'react';
import { cn } from '../../lib/utils';
import { Loader2 } from 'lucide-react';
export interface ButtonProps extends
  React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg' | 'icon';
  isLoading?: boolean;
}
export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
  {
    className,
    variant = 'primary',
    size = 'md',
    isLoading,
    children,
    disabled,
    ...props
  },
  ref) =>
  {
    const variants = {
      primary: 'bg-brand-600 text-white hover:bg-brand-700 shadow-sm',
      secondary:
      'bg-ink-900 text-white hover:bg-ink-800 dark:bg-ink-100 dark:text-ink-900 dark:hover:bg-white shadow-sm',
      outline:
      'border border-ink-200 bg-transparent hover:bg-ink-50 text-ink-900 dark:border-ink-700 dark:text-ink-100 dark:hover:bg-ink-800',
      ghost:
      'bg-transparent hover:bg-ink-100 text-ink-700 dark:text-ink-300 dark:hover:bg-ink-800 dark:hover:text-ink-100',
      danger: 'bg-danger text-white hover:bg-red-600 shadow-sm'
    };
    const sizes = {
      sm: 'h-8 px-3 text-xs rounded-lg',
      md: 'h-10 px-4 py-2 text-sm rounded-xl',
      lg: 'h-12 px-8 text-base rounded-2xl',
      icon: 'h-10 w-10 flex items-center justify-center rounded-xl'
    };
    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(
          'inline-flex items-center justify-center font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 disabled:opacity-50 disabled:pointer-events-none',
          variants[variant],
          sizes[size],
          className
        )}
        {...props}>
        
        {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
        {children}
      </button>);

  }
);
Button.displayName = 'Button';