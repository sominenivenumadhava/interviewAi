import React from 'react';
import { CheckCircle2, Circle, Clock } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';

export function Roadmap() {
  const timeline = [
    {
      week: 'Week 1',
      title: 'System Design Fundamentals',
      status: 'completed',
      tasks: [
        'Review CAP Theorem',
        'Study Load Balancing Strategies',
        'Practice: Design a URL Shortener',
      ],
    },
    {
      week: 'Week 2',
      title: 'Advanced Data Structures',
      status: 'current',
      tasks: [
        'Graph Traversal (BFS/DFS)',
        'Topological Sort',
        'Practice: Course Schedule (LeetCode)',
      ],
    },
    {
      week: 'Week 3',
      title: 'Behavioral Deep Dive',
      status: 'upcoming',
      tasks: [
        'Refine STAR method stories',
        'Mock Interview: Leadership Principles',
        'Review past project metrics',
      ],
    },
    {
      week: 'Week 4',
      title: 'Mock Interview Gauntlet',
      status: 'upcoming',
      tasks: [
        'Full 60-min Technical Mock',
        'Full 45-min System Design Mock',
        'Final Resume Review',
      ],
    },
  ];

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Suggested practice plan
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          A 4-week curriculum template to prepare for your target roles.
        </p>
        <p className="mt-2 text-sm text-ink-400 dark:text-ink-500">
          Template plan — refine after completing interviews
        </p>
      </div>

      <div className="space-y-6">
        {timeline.map((item, index) => (
          <Card
            key={index}
            className={
              item.status === 'current'
                ? 'border-brand-500 shadow-soft dark:border-brand-500'
                : ''
            }
          >
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  {item.status === 'completed' ? (
                    <CheckCircle2 className="text-success" size={24} />
                  ) : item.status === 'current' ? (
                    <Clock className="text-brand-500" size={24} />
                  ) : (
                    <Circle className="text-ink-300 dark:text-ink-700" size={24} />
                  )}
                  <div>
                    <CardDescription className="font-medium text-brand-600 dark:text-brand-400">
                      {item.week}
                    </CardDescription>
                    <CardTitle className="text-xl">{item.title}</CardTitle>
                  </div>
                </div>
                <Badge
                  variant={
                    item.status === 'completed'
                      ? 'success'
                      : item.status === 'current'
                        ? 'default'
                        : 'secondary'
                  }
                >
                  {item.status.charAt(0).toUpperCase() + item.status.slice(1)}
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <ul className="ml-9 space-y-3">
                {item.tasks.map((task, i) => (
                  <li
                    key={i}
                    className="flex items-center gap-3 text-sm text-ink-700 dark:text-ink-300"
                  >
                    <div
                      className={`h-1.5 w-1.5 rounded-full ${
                        item.status === 'completed'
                          ? 'bg-success'
                          : 'bg-ink-300 dark:bg-ink-600'
                      }`}
                    />
                    {task}
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
