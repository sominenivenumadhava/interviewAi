import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  BrainCircuit,
  ArrowRight,
  CheckCircle2,
  BarChart3,
  Target,
  Shield } from
'lucide-react';
import { Button } from '../components/ui/Button';
export function Landing() {
  return (
    <div className="min-h-screen bg-ink-50 dark:bg-ink-950">
      {/* Navigation */}
      <nav className="border-b border-ink-200 bg-white/80 backdrop-blur-md dark:border-ink-800 dark:bg-ink-950/80 sticky top-0 z-50">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand-600 text-white">
              <BrainCircuit size={20} />
            </div>
            <span className="text-xl font-bold tracking-tight text-ink-900 dark:text-white">
              InterviAI
            </span>
          </div>
          <div className="flex items-center gap-4">
            <Link
              to="/login"
              className="text-sm font-medium text-ink-600 hover:text-ink-900 dark:text-ink-300 dark:hover:text-white">
              
              Log in
            </Link>
            <Link to="/register">
              <Button size="sm">Get Started</Button>
            </Link>
          </div>
        </div>
      </nav>

      <main>
        {/* Hero Section */}
        <section className="relative overflow-hidden pt-24 pb-32 lg:pt-36">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 text-center">
            <motion.div
              initial={{
                opacity: 0,
                y: 20
              }}
              animate={{
                opacity: 1,
                y: 0
              }}
              transition={{
                duration: 0.5
              }}>
              
              <h1 className="mx-auto max-w-4xl text-5xl font-extrabold tracking-tight text-ink-900 dark:text-white sm:text-7xl">
                Master Your Next Interview with{' '}
                <span className="text-brand-600">AI</span>
              </h1>
              <p className="mx-auto mt-6 max-w-2xl text-lg leading-8 text-ink-600 dark:text-ink-300">
                Personalized virtual interviews powered by AI that adapt to your
                resume, skills, and dream company. Practice, get feedback, and
                land the job.
              </p>
              <div className="mt-10 flex items-center justify-center gap-4">
                <Link to="/register">
                  <Button size="lg" className="gap-2">
                    Start Interview <ArrowRight size={18} />
                  </Button>
                </Link>
                <Link to="/dashboard">
                  <Button size="lg" variant="outline">
                    View Demo
                  </Button>
                </Link>
              </div>
            </motion.div>
          </div>
        </section>

        {/* Features */}
        <section className="py-24 bg-white dark:bg-ink-900">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-16">
              <h2 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white sm:text-4xl">
                Everything you need to succeed
              </h2>
            </div>
            <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
              {[
              {
                icon: Target,
                title: 'Personalized Questions',
                desc: "AI generates questions based on your specific resume and the role you're targeting."
              },
              {
                icon: BarChart3,
                title: 'Detailed Analytics',
                desc: 'Get comprehensive feedback on your communication, technical skills, and confidence.'
              },
              {
                icon: Shield,
                title: 'Realistic Environment',
                desc: 'Practice in a stress-free environment that mimics real video interviews.'
              }].
              map((feature, i) =>
              <div
                key={i}
                className="rounded-2xl border border-ink-200 p-8 dark:border-ink-800">
                
                  <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-100 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400">
                    <feature.icon size={24} />
                  </div>
                  <h3 className="mb-2 text-xl font-semibold text-ink-900 dark:text-white">
                    {feature.title}
                  </h3>
                  <p className="text-ink-600 dark:text-ink-400">
                    {feature.desc}
                  </p>
                </div>
              )}
            </div>
          </div>
        </section>
      </main>
    </div>);

}