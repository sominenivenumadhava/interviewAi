import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, Filter, ChevronRight, Calendar, Clock } from 'lucide-react';
import { Card, CardContent } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/PageHeader';
import { mockHistory } from '../data/mockData';

// ─── Score mini bar ───────────────────────────────────────────────────────────
function ScoreBar({ score }: { score: number }) {
  const color =
    score >= 85 ? '#10b981' :
    score >= 70 ? '#f59e0b' : '#f43f5e';
  return (
    <div className="flex items-center gap-2">
      <div className="w-16 h-1.5 rounded-full bg-ink-100 dark:bg-white/8 overflow-hidden">
        <motion.div
          initial={{ width: 0 }}
          animate={{ width: `${score}%` }}
          transition={{ duration: 0.8, ease: [0.34, 1.56, 0.64, 1], delay: 0.3 }}
          className="h-full rounded-full"
          style={{ background: color }}
        />
      </div>
      <span className="text-sm font-bold tabular-nums" style={{ color }}>
        {score}%
      </span>
    </div>
  );
}

// ─── History ──────────────────────────────────────────────────────────────────
export function History() {
  const [search, setSearch] = useState('');

  const filteredHistory = mockHistory.filter(
    (h) =>
      h.company.toLowerCase().includes(search.toLowerCase()) ||
      h.role.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-8">
      <PageHeader
        title="Interview History"
        subtitle="Review your past mock interviews and track your progress over time."
      />

      <Card>
        {/* Search + Filter bar */}
        <div className="border-b border-ink-100 dark:border-white/[0.06] px-5 py-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            <div className="flex-1">
              <Input
                placeholder="Search by company or role…"
                icon={<Search size={15} />}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <Button variant="outline" size="sm" className="gap-2 shrink-0">
              <Filter size={14} />
              Filter
            </Button>
          </div>
        </div>

        <CardContent className="p-0">
          {filteredHistory.length === 0 ? (
            /* ── Empty State ── */
            <div className="flex flex-col items-center justify-center py-20 text-center">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-ink-100 dark:bg-white/5 mb-4">
                <Search size={28} className="text-ink-300 dark:text-ink-600" />
              </div>
              <p className="text-base font-semibold text-ink-600 dark:text-ink-300">No results found</p>
              <p className="text-sm text-ink-400 mt-1">Try searching with a different company or role name.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="premium-table">
                <thead>
                  <tr>
                    <th>Company & Role</th>
                    <th>Date</th>
                    <th>Duration</th>
                    <th>Score</th>
                    <th className="text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <AnimatePresence>
                    {filteredHistory.map((interview, i) => (
                      <motion.tr
                        key={interview.id}
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.3, delay: i * 0.05, ease: [0.25, 0.1, 0.25, 1] }}
                        className="group"
                      >
                        <td>
                          <div className="font-semibold text-ink-900 dark:text-white">{interview.company}</div>
                          <div className="text-xs text-ink-400 dark:text-ink-500 mt-0.5">{interview.role}</div>
                        </td>
                        <td>
                          <div className="flex items-center gap-1.5 text-sm text-ink-500 dark:text-ink-400">
                            <Calendar size={13} className="shrink-0" />
                            {interview.date}
                          </div>
                        </td>
                        <td>
                          <div className="flex items-center gap-1.5 text-sm text-ink-500 dark:text-ink-400">
                            <Clock size={13} className="shrink-0" />
                            {interview.duration}
                          </div>
                        </td>
                        <td>
                          <ScoreBar score={interview.score} />
                        </td>
                        <td className="text-right">
                          <Link to="/evaluation">
                            <Button variant="ghost" size="sm" className="gap-1 text-xs opacity-0 group-hover:opacity-100 transition-opacity">
                              View Report <ChevronRight size={13} />
                            </Button>
                          </Link>
                        </td>
                      </motion.tr>
                    ))}
                  </AnimatePresence>
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}