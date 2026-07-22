import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Settings, Clock, BrainCircuit, Code, Play, Cpu, UserCheck, Layers } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { cn } from '../../lib/utils';
import { useInterviewSession } from '../../contexts/InterviewSessionContext';

export function Config() {
  const navigate = useNavigate();
  const { selectedCompany, selectedRole, selectedConfig, setSelectedConfig } = useInterviewSession();

  const types = [
    {
      id: 'TECHNICAL',
      name: 'Technical',
      icon: Code,
      desc: 'Algorithms, Data Structures & CS Concepts'
    },
    {
      id: 'BEHAVIORAL',
      name: 'Behavioral',
      icon: BrainCircuit,
      desc: 'STAR Method, Leadership & Culture Fit'
    },
    {
      id: 'HR',
      name: 'HR / Screen',
      icon: UserCheck,
      desc: 'Career Goals, Salary & Background'
    },
    {
      id: 'SYSTEM_DESIGN',
      name: 'System Design',
      icon: Cpu,
      desc: 'High Level Architecture & Scalability'
    },
    {
      id: 'CODING',
      name: 'Coding Round',
      icon: Settings,
      desc: 'Live Problem Solving & Syntax'
    },
    {
      id: 'MIXED',
      name: 'Full Loop (Mixed)',
      icon: Layers,
      desc: 'Comprehensive Multi-round Interview'
    }
  ];

  const difficulties = ['EASY', 'MEDIUM', 'HARD'];
  const durations = ['15', '30', '45', '60'];
  const questionCounts = [5, 10, 15];
  const languages = ['TypeScript', 'Python', 'Java', 'Go', 'Rust', 'C++'];

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Configure {selectedCompany} Interview
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          Target Role: <span className="font-semibold text-brand-600 dark:text-brand-400">{selectedRole}</span>. AI will enforce question types based on your selection.
        </p>
      </div>

      <div className="space-y-6">
        {/* Type Selection */}
        <Card>
          <CardHeader>
            <CardTitle>Interview Round Type (Enforced)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              {types.map((t) => (
                <div
                  key={t.id}
                  onClick={() => setSelectedConfig((prev) => ({ ...prev, interviewType: t.id }))}
                  className={cn(
                    'cursor-pointer rounded-xl border p-4 transition-all',
                    selectedConfig.interviewType === t.id
                      ? 'border-brand-500 bg-brand-50 dark:bg-brand-900/20 shadow-sm'
                      : 'border-ink-200 hover:border-brand-300 dark:border-ink-700 dark:hover:border-brand-700'
                  )}
                >
                  <t.icon
                    size={22}
                    className={
                      selectedConfig.interviewType === t.id
                        ? 'text-brand-600 dark:text-brand-400'
                        : 'text-ink-500'
                    }
                  />
                  <h4 className="mt-3 font-semibold text-ink-900 dark:text-white text-sm">
                    {t.name}
                  </h4>
                  <p className="mt-1 text-xs text-ink-500 dark:text-ink-400 leading-normal">
                    {t.desc}
                  </p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
          {/* Difficulty */}
          <Card>
            <CardHeader>
              <CardTitle>Initial Difficulty</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col gap-2">
                {difficulties.map((d) => (
                  <button
                    key={d}
                    onClick={() => setSelectedConfig((prev) => ({ ...prev, difficulty: d }))}
                    className={cn(
                      'rounded-lg border px-3 py-2 text-xs font-semibold transition-colors text-left',
                      selectedConfig.difficulty === d
                        ? 'border-brand-500 bg-brand-600 text-white'
                        : 'border-ink-200 bg-white text-ink-700 hover:bg-ink-50 dark:border-ink-700 dark:bg-ink-900 dark:text-ink-300 dark:hover:bg-ink-800'
                    )}
                  >
                    {d}
                  </button>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Question Count */}
          <Card>
            <CardHeader>
              <CardTitle>Number of Questions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col gap-2">
                {questionCounts.map((count) => (
                  <button
                    key={count}
                    onClick={() => setSelectedConfig((prev) => ({ ...prev, numberOfQuestions: count }))}
                    className={cn(
                      'rounded-lg border px-3 py-2 text-xs font-semibold transition-colors text-left',
                      selectedConfig.numberOfQuestions === count
                        ? 'border-brand-500 bg-brand-600 text-white'
                        : 'border-ink-200 bg-white text-ink-700 hover:bg-ink-50 dark:border-ink-700 dark:bg-ink-900 dark:text-ink-300 dark:hover:bg-ink-800'
                    )}
                  >
                    {count} Questions
                  </button>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Duration */}
          <Card>
            <CardHeader>
              <CardTitle>Time Limit</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col gap-2">
                {durations.map((d) => (
                  <button
                    key={d}
                    onClick={() => setSelectedConfig((prev) => ({ ...prev, duration: d }))}
                    className={cn(
                      'flex items-center gap-2 rounded-lg border px-3 py-2 text-xs font-semibold transition-colors',
                      selectedConfig.duration === d
                        ? 'border-brand-500 bg-brand-600 text-white'
                        : 'border-ink-200 bg-white text-ink-700 hover:bg-ink-50 dark:border-ink-700 dark:bg-ink-900 dark:text-ink-300 dark:hover:bg-ink-800'
                    )}
                  >
                    <Clock size={14} />
                    {d} Minutes
                  </button>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Language Selection */}
        {(selectedConfig.interviewType === 'TECHNICAL' || selectedConfig.interviewType === 'CODING') && (
          <Card>
            <CardHeader>
              <CardTitle>Preferred Programming Language</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {languages.map((l) => (
                  <button
                    key={l}
                    onClick={() => setSelectedConfig((prev) => ({ ...prev, language: l }))}
                    className={cn(
                      'rounded-lg border px-4 py-2 text-xs font-medium transition-colors',
                      selectedConfig.language === l
                        ? 'border-brand-500 bg-brand-600 text-white'
                        : 'border-ink-200 bg-white text-ink-700 hover:bg-ink-50 dark:border-ink-700 dark:bg-ink-900 dark:text-ink-300 dark:hover:bg-ink-800'
                    )}
                  >
                    {l}
                  </button>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        <div className="flex justify-end pt-4">
          <Button
            size="lg"
            className="gap-2 shadow-lg"
            onClick={() => navigate('/interview/room')}
          >
            <Play size={18} />
            Start AI Interview Session
          </Button>
        </div>
      </div>
    </div>
  );
}