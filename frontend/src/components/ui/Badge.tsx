import React from 'react';
import { cn } from '../../lib/utils';
export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?:
  'default' |
  'secondary' |
  'outline' |
  'success' |
  'warning' |
  'danger';
}
export function Badge({
  className,
  variant = 'default',
  ...props
}: BadgeProps) {
  const variants = {
    default:
    'border-transparent bg-brand-100 text-brand-700 dark:bg-brand-900/30 dark:text-brand-300',
    secondary:
    'border-transparent bg-ink-100 text-ink-900 dark:bg-ink-800 dark:text-ink-100',
    outline:
    'text-ink-900 dark:text-ink-100 border-ink-200 dark:border-ink-700',
    success:
    'border-transparent bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300',
    warning:
    'border-transparent bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300',
    danger:
    'border-transparent bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300'
  };
  return (
    <div
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2',
        variants[variant],
        className
      )}
      {...props} />);


}