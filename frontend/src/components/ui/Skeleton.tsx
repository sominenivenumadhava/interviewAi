import React from 'react';
import { cn } from '../../lib/utils';

// ─── Individual Skeletons ────────────────────────────────────────────────────

export function SkeletonLine({ className }: { className?: string }) {
  return <div className={cn('skeleton h-4 rounded-md', className)} />;
}

export function SkeletonBlock({ className }: { className?: string }) {
  return <div className={cn('skeleton rounded-2xl', className)} />;
}

export function SkeletonStat() {
  return (
    <div className="rounded-2xl border border-ink-100 dark:border-white/5 bg-white dark:bg-obsidian-900 p-5">
      <div className="flex items-start justify-between">
        <div className="space-y-2 flex-1">
          <SkeletonLine className="w-24 h-3" />
          <SkeletonLine className="w-16 h-8" />
          <SkeletonLine className="w-32 h-3" />
        </div>
        <SkeletonBlock className="h-11 w-11 rounded-xl shrink-0" />
      </div>
    </div>
  );
}

export function SkeletonCard({ lines = 3 }: { lines?: number }) {
  return (
    <div className="rounded-2xl border border-ink-100 dark:border-white/5 bg-white dark:bg-obsidian-900 p-6 space-y-4">
      <div className="space-y-2">
        <SkeletonLine className="w-40 h-5" />
        <SkeletonLine className="w-64 h-3.5" />
      </div>
      <div className="space-y-2.5">
        {Array.from({ length: lines }).map((_, i) => (
          <SkeletonLine
            key={i}
            className="h-3.5"
            style={{ width: `${100 - i * 12}%` } as React.CSSProperties}
          />
        ))}
      </div>
    </div>
  );
}

export function SkeletonChart() {
  return (
    <div className="rounded-2xl border border-ink-100 dark:border-white/5 bg-white dark:bg-obsidian-900 p-6">
      <div className="space-y-2 mb-6">
        <SkeletonLine className="w-40 h-5" />
        <SkeletonLine className="w-64 h-3.5" />
      </div>
      <SkeletonBlock className="w-full h-[260px] rounded-xl" />
    </div>
  );
}

export function SkeletonTableRow() {
  return (
    <tr>
      {[40, 20, 15, 15, 10].map((w, i) => (
        <td key={i} className="px-6 py-4">
          <SkeletonLine className={`h-4`} style={{ width: `${w}%` } as React.CSSProperties} />
        </td>
      ))}
    </tr>
  );
}

// ─── Dashboard Skeleton ───────────────────────────────────────────────────────

export function DashboardSkeleton() {
  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="space-y-2">
        <SkeletonLine className="w-72 h-8" />
        <SkeletonLine className="w-96 h-4" />
      </div>
      {/* Stat grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[0, 1, 2, 3].map((i) => <SkeletonStat key={i} />)}
      </div>
      {/* Charts */}
      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2"><SkeletonChart /></div>
        <SkeletonCard lines={5} />
      </div>
    </div>
  );
}
