import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter, ChevronRight } from 'lucide-react';
import { Card, CardContent } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { mockHistory } from '../data/mockData';
export function History() {
  const [search, setSearch] = useState('');
  const filteredHistory = mockHistory.filter(
    (h) =>
    h.company.toLowerCase().includes(search.toLowerCase()) ||
    h.role.toLowerCase().includes(search.toLowerCase())
  );
  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
            Interview History
          </h1>
          <p className="mt-1 text-ink-500 dark:text-ink-400">
            Review your past mock interviews and track your progress.
          </p>
        </div>
      </div>

      <Card>
        <div className="border-b border-ink-200 p-4 dark:border-ink-800">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
            <div className="flex-1">
              <Input
                placeholder="Search by company or role..."
                icon={<Search size={16} />}
                value={search}
                onChange={(e) => setSearch(e.target.value)} />
              
            </div>
            <Button variant="outline" className="gap-2">
              <Filter size={16} />
              Filter
            </Button>
          </div>
        </div>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-ink-50 dark:bg-ink-900/50">
                <tr className="text-ink-500 dark:text-ink-400">
                  <th className="px-6 py-4 font-medium">Company & Role</th>
                  <th className="px-6 py-4 font-medium">Date</th>
                  <th className="px-6 py-4 font-medium">Duration</th>
                  <th className="px-6 py-4 font-medium">Score</th>
                  <th className="px-6 py-4 font-medium text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100 dark:divide-ink-800">
                {filteredHistory.map((interview) =>
                <tr
                  key={interview.id}
                  className="group transition-colors hover:bg-ink-50 dark:hover:bg-ink-800/50">
                  
                    <td className="px-6 py-4">
                      <div className="font-medium text-ink-900 dark:text-white">
                        {interview.company}
                      </div>
                      <div className="text-ink-500 dark:text-ink-400">
                        {interview.role}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-ink-600 dark:text-ink-300">
                      {interview.date}
                    </td>
                    <td className="px-6 py-4 text-ink-600 dark:text-ink-300">
                      {interview.duration}
                    </td>
                    <td className="px-6 py-4">
                      <Badge
                      variant={
                      interview.score >= 85 ?
                      'success' :
                      interview.score >= 70 ?
                      'warning' :
                      'danger'
                      }>
                      
                        {interview.score}%
                      </Badge>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link to="/evaluation">
                        <Button variant="ghost" size="sm" className="gap-1">
                          Report <ChevronRight size={14} />
                        </Button>
                      </Link>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>);

}