import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Code2, ArrowRight } from 'lucide-react';
import { Card, CardContent } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { mockRoles } from '../../data/mockData';
import { useInterviewSession } from '../../contexts/InterviewSessionContext';

export function RoleSelect() {
  const navigate = useNavigate();
  const { setSelectedRole } = useInterviewSession();
  const [search, setSearch] = useState('');
  const normalizedSearch = search.trim();
  const filteredRoles = mockRoles.filter((r) =>
    r.toLowerCase().includes(normalizedSearch.toLowerCase())
  );

  const selectRole = (role: string) => {
    setSelectedRole(role);
    navigate('/interview/config');
  };

  const hasExactMatch = mockRoles.some(
    (role) => role.toLowerCase() === normalizedSearch.toLowerCase()
  );

  return (
    <div className="mx-auto max-w-5xl space-y-8">
      <div className="text-center">
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Select Your Role
        </h1>
        <p className="mt-2 text-ink-500 dark:text-ink-400">
          What position are you interviewing for?
        </p>
      </div>

      <div className="mx-auto max-w-xl">
        <Input
          placeholder="Search roles (e.g., Frontend, Backend)..."
          icon={<Search size={18} />}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && normalizedSearch) {
              selectRole(normalizedSearch);
            }
          }}
          className="h-12 text-base" />

        {normalizedSearch && !hasExactMatch && (
          <button
            type="button"
            onClick={() => selectRole(normalizedSearch)}
            className="mt-3 w-full rounded-lg border border-brand-300 bg-brand-50 px-4 py-3 text-left text-sm font-medium text-brand-700 transition-colors hover:bg-brand-100 dark:border-brand-800 dark:bg-brand-900/20 dark:text-brand-300 dark:hover:bg-brand-900/40"
          >
            Use “{normalizedSearch}” as the target role
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {filteredRoles.map((role, i) =>
        <motion.div
          key={role}
          initial={{
            opacity: 0,
            y: 20
          }}
          animate={{
            opacity: 1,
            y: 0
          }}
          transition={{
            delay: i * 0.05
          }}>
          
            <Card
            className="group cursor-pointer transition-all hover:border-brand-500 hover:shadow-lift dark:hover:border-brand-500"
            onClick={() => selectRole(role)}>
            
              <CardContent className="flex items-center justify-between p-6">
                <div className="flex items-center gap-4">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-900/20 dark:text-brand-400">
                    <Code2 size={20} />
                  </div>
                  <h3 className="font-medium text-ink-900 dark:text-white">
                    {role}
                  </h3>
                </div>
                <ArrowRight
                size={18}
                className="text-brand-600 opacity-0 transition-opacity group-hover:opacity-100 dark:text-brand-400" />
              
              </CardContent>
            </Card>
          </motion.div>
        )}
      </div>
    </div>);

}
