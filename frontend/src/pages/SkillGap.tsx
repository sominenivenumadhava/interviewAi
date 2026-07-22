import React from 'react';
import { Link } from 'react-router-dom';
import { Target, BookOpen, Code2, ArrowRight } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription } from
'../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { mockSkillGap } from '../data/mockData';
export function SkillGap() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Skill Gap Analysis
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          Based on your recent interviews, here are the areas you need to focus
          on to hit the bar for your target roles.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
        {/* Missing Skills */}
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Target size={20} className="text-brand-500" />
              Priority Focus Areas
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {mockSkillGap.missingSkills.map((skill, i) =>
              <div
                key={i}
                className="flex items-center justify-between rounded-lg border border-ink-200 p-3 dark:border-ink-800">
                
                  <span className="font-medium text-ink-900 dark:text-white">
                    {skill}
                  </span>
                  <Badge variant="danger">High Priority</Badge>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Recommendations */}
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <BookOpen size={20} className="text-brand-500" />
                Recommended Courses
              </CardTitle>
              <CardDescription>
                Curated learning materials to bridge your gaps
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {mockSkillGap.recommendedCourses.map((course, i) =>
                <div
                  key={i}
                  className="group rounded-xl border border-ink-200 p-4 transition-all hover:border-brand-500 hover:shadow-soft dark:border-ink-800 dark:hover:border-brand-500">
                  
                    <div className="mb-2 flex items-center justify-between">
                      <Badge variant="secondary">{course.platform}</Badge>
                      <span className="text-xs text-ink-500">
                        {course.duration}
                      </span>
                    </div>
                    <h4 className="font-medium text-ink-900 dark:text-white">
                      {course.title}
                    </h4>
                    <a
                    href="#"
                    className="mt-3 inline-flex items-center text-sm font-medium text-brand-600 opacity-0 transition-opacity group-hover:opacity-100 dark:text-brand-400">
                    
                      View Course <ArrowRight size={16} className="ml-1" />
                    </a>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Code2 size={20} className="text-brand-500" />
                Recommended Practice
              </CardTitle>
              <CardDescription>
                Specific problems to improve your technical execution
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {mockSkillGap.recommendedLeetCode.map((problem, i) =>
                <div
                  key={i}
                  className="flex items-center justify-between rounded-lg bg-ink-50 p-3 dark:bg-ink-900/50">
                  
                    <span className="text-sm font-medium text-ink-900 dark:text-white">
                      {problem}
                    </span>
                    <Button variant="ghost" size="sm">
                      Practice
                    </Button>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <div className="flex justify-center pt-4">
        <Link to="/roadmap">
          <Button size="lg" className="gap-2">
            Generate Learning Roadmap <ArrowRight size={18} />
          </Button>
        </Link>
      </div>
    </div>);

}