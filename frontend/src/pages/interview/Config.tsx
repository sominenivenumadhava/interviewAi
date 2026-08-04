import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Settings,
  Clock,
  BrainCircuit,
  Code,
  Play,
  Cpu,
  UserCheck,
  Calculator,
  Briefcase
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { cn } from '../../lib/utils';
import { useInterviewSession } from '../../contexts/InterviewSessionContext';

export function Config() {
  const navigate = useNavigate();
  const { selectedCompany, selectedRole, selectedConfig, setSelectedConfig } = useInterviewSession();

  const types = [
    {
      id: 'HR',
      name: 'HR Round',
      icon: UserCheck,
      desc: 'Background, motivation, strengths & culture fit',
      focusAreas: 'Motivation, Culture Fit & Soft Skills'
    },
    {
      id: 'TECHNICAL',
      name: 'Technical',
      icon: Code,
      desc: 'CS concepts, frameworks, APIs & theory',
      focusAreas: 'OOP, DBMS, REST, Microservices & Core Concepts'
    },
    {
      id: 'CODING',
      name: 'Coding Round',
      icon: Settings,
      desc: 'LeetCode-style DSA problems only',
      focusAreas: 'Arrays, Strings, Trees, Graphs, DP & Binary Search'
    },
    {
      id: 'SYSTEM_DESIGN',
      name: 'System Design',
      icon: Cpu,
      desc: 'HLD, scalability, APIs & trade-offs',
      focusAreas: 'Architecture, Scalability, Caching & DB Design'
    },
    {
      id: 'MANAGERIAL',
      name: 'Managerial / Bar Raiser',
      icon: Briefcase,
      desc: 'Ownership, leadership & decision making',
      focusAreas: 'Leadership, Ownership, Stakeholders & Prioritization'
    },
    {
      id: 'APTITUDE',
      name: 'Aptitude / Assessment',
      icon: Calculator,
      desc: 'Quant, logical, verbal & puzzles',
      focusAreas: 'Quantitative, Logical Reasoning & Puzzles'
    }
  ];

  const difficulties = ['EASY', 'MEDIUM', 'HARD'];
  const durations = ['15', '30', '45', '60'];
  const questionCounts = [5, 10, 15];
  const languages = ['TypeScript', 'Python', 'Java', 'Go', 'Rust', 'C++'];
  const showLanguage = ['CODING', 'TECHNICAL'].includes(selectedConfig.interviewType);

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Configure {selectedCompany} Interview
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          Target Role: <span className="font-semibold text-brand-600 dark:text-brand-400">{selectedRole}</span>.
          Questions are generated strictly for the selected round only.
        </p>
      </div>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Interview Round Type (Strictly Enforced)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              {types.map((t) => (
                <div
                  key={t.id}
                  onClick={() =>
                    setSelectedConfig((prev) => ({
                      ...prev,
                      interviewType: t.id,
                      focusAreas: t.focusAreas
                    }))
                  }
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

        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <BrainCircuit size={18} /> Initial Difficulty
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {difficulties.map((d) => (
                <button
                  key={d}
                  onClick={() => setSelectedConfig((prev) => ({ ...prev, difficulty: d }))}
                  className={cn(
                    'w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors',
                    selectedConfig.difficulty === d
                      ? 'bg-brand-600 text-white'
                      : 'bg-ink-100 text-ink-700 hover:bg-ink-200 dark:bg-ink-800 dark:text-ink-200 dark:hover:bg-ink-700'
                  )}
                >
                  {d}
                </button>
              ))}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <Settings size={18} /> Number of Questions
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {questionCounts.map((n) => (
                <button
                  key={n}
                  onClick={() => setSelectedConfig((prev) => ({ ...prev, numberOfQuestions: n }))}
                  className={cn(
                    'w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors',
                    selectedConfig.numberOfQuestions === n
                      ? 'bg-brand-600 text-white'
                      : 'bg-ink-100 text-ink-700 hover:bg-ink-200 dark:bg-ink-800 dark:text-ink-200 dark:hover:bg-ink-700'
                  )}
                >
                  {n} Questions
                </button>
              ))}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <Clock size={18} /> Time Limit
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {durations.map((d) => (
                <button
                  key={d}
                  onClick={() => setSelectedConfig((prev) => ({ ...prev, duration: d }))}
                  className={cn(
                    'w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors',
                    selectedConfig.duration === d
                      ? 'bg-brand-600 text-white'
                      : 'bg-ink-100 text-ink-700 hover:bg-ink-200 dark:bg-ink-800 dark:text-ink-200 dark:hover:bg-ink-700'
                  )}
                >
                  {d} Minutes
                </button>
              ))}
            </CardContent>
          </Card>
        </div>

        {showLanguage && (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Preferred Language</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-2">
              {languages.map((lang) => (
                <button
                  key={lang}
                  onClick={() => setSelectedConfig((prev) => ({ ...prev, language: lang }))}
                  className={cn(
                    'rounded-lg px-3 py-1.5 text-sm font-medium transition-colors',
                    selectedConfig.language === lang
                      ? 'bg-brand-600 text-white'
                      : 'bg-ink-100 text-ink-700 hover:bg-ink-200 dark:bg-ink-800 dark:text-ink-200'
                  )}
                >
                  {lang}
                </button>
              ))}
            </CardContent>
          </Card>
        )}

        <div className="flex justify-end pt-2">
          <Button
            size="lg"
            className="gap-2"
            onClick={() => {
              try {
                sessionStorage.removeItem('interviai_room_state');
              } catch (_) {}
              navigate('/interview/room');
            }}
          >
            <Play size={18} />
            Start Interview
          </Button>
        </div>
      </div>
    </div>
  );
}
