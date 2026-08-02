import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Building2, ArrowRight, Star, Tag } from 'lucide-react';
import { Card, CardContent } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { mockCompanies, Company } from '../../data/mockData';
import { useInterviewSession } from '../../contexts/InterviewSessionContext';

export function CompanySelect() {
  const navigate = useNavigate();
  const { setSelectedCompany } = useInterviewSession();

  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [favorites, setFavorites] = useState<string[]>(() => {
    const saved = localStorage.getItem('fav_companies');
    return saved ? JSON.parse(saved) : ['google', 'microsoft', 'phonepe'];
  });

  const categories = ['All', 'FAANG', 'Big Tech', 'Indian Product', 'AI', 'FinTech', 'Cloud', 'Service'];

  const toggleFavorite = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    const updated = favorites.includes(id)
      ? favorites.filter((f) => f !== id)
      : [...favorites, id];
    setFavorites(updated);
    localStorage.setItem('fav_companies', JSON.stringify(updated));
  };

  const filteredCompanies = mockCompanies.filter((c) => {
    const matchesSearch = c.name.toLowerCase().includes(search.toLowerCase()) ||
                          c.industry.toLowerCase().includes(search.toLowerCase()) ||
                          c.techStack.some((t) => t.toLowerCase().includes(search.toLowerCase()));
    const matchesCategory = selectedCategory === 'All' || c.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const handleSelect = (company: Company) => {
    setSelectedCompany(company.name);
    navigate('/interview/role');
  };

  return (
    <div className="mx-auto max-w-6xl space-y-8">
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white sm:text-4xl">
          Choose Your Target Company
        </h1>
        <p className="mx-auto max-w-2xl text-ink-500 dark:text-ink-400">
          Curated company catalog. We tailor AI interviewer personality, coding expectations, and system design difficulty to match their hiring bar.
        </p>
      </div>

      {/* Search & Category Filter */}
      <div className="space-y-4">
        <div className="mx-auto max-w-xl">
          <Input
            placeholder="Search company, tech stack (e.g. React, Java), or category..."
            icon={<Search size={18} />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="h-12 text-base shadow-sm"
          />
        </div>

        {/* Category Pills */}
        <div className="flex flex-wrap items-center justify-center gap-2 pt-2">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`rounded-full px-4 py-1.5 text-xs font-medium transition-all ${
                selectedCategory === cat
                  ? 'bg-brand-600 text-white shadow'
                  : 'bg-ink-100 text-ink-600 hover:bg-ink-200 dark:bg-ink-800 dark:text-ink-300 dark:hover:bg-ink-700'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Company Grid */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {filteredCompanies.map((company, i) => {
          const isFav = favorites.includes(company.id);
          return (
            <motion.div
              key={company.id}
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.04 }}
            >
              <Card
                className="group relative cursor-pointer transition-all hover:-translate-y-1 hover:border-brand-500 hover:shadow-lg dark:hover:border-brand-500"
                onClick={() => handleSelect(company)}
              >
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-ink-100 dark:bg-ink-800 border border-ink-200 dark:border-ink-700">
                      <Building2 size={24} className="text-brand-600 dark:text-brand-400" />
                    </div>
                    <button
                      onClick={(e) => toggleFavorite(e, company.id)}
                      className="p-1 text-ink-400 hover:text-amber-400 transition-colors"
                      title={isFav ? 'Remove Favorite' : 'Save Favorite'}
                    >
                      <Star size={18} className={isFav ? 'fill-amber-400 text-amber-400' : ''} />
                    </button>
                  </div>

                  <div className="mt-4">
                    <div className="flex items-center gap-2">
                      <h3 className="text-xl font-bold text-ink-900 dark:text-white">
                        {company.name}
                      </h3>
                      <Badge variant="outline" className="text-[10px]">
                        {company.category}
                      </Badge>
                    </div>
                    <p className="text-xs text-ink-500 dark:text-ink-400 mt-0.5">
                      {company.industry}
                    </p>
                  </div>

                  <p className="mt-3 text-xs text-ink-600 dark:text-ink-300 line-clamp-2 leading-relaxed">
                    {company.description}
                  </p>

                  <div className="mt-4 flex flex-wrap gap-1">
                    {company.techStack.slice(0, 4).map((tech) => (
                      <span
                        key={tech}
                        className="inline-flex items-center gap-1 rounded bg-ink-100 dark:bg-ink-800/60 px-2 py-0.5 text-[10px] text-ink-600 dark:text-ink-400"
                      >
                        <Tag size={10} />
                        {tech}
                      </span>
                    ))}
                  </div>

                  <div className="mt-5 flex items-center justify-between border-t border-ink-100 dark:border-ink-800 pt-3">
                    <Badge
                      variant={
                        company.difficulty === 'Hard'
                          ? 'danger'
                          : company.difficulty === 'Medium'
                          ? 'warning'
                          : 'secondary'
                      }
                    >
                      {company.difficulty} Bar
                    </Badge>
                    <div className="flex items-center text-xs font-semibold text-brand-600 opacity-0 transition-opacity group-hover:opacity-100 dark:text-brand-400">
                      Configure Session <ArrowRight size={14} className="ml-1" />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          );
        })}
      </div>
    </div>
  );
}