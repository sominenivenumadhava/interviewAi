import React, { useEffect, useState, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useTheme } from '../../contexts/ThemeContext';
import { useAuth } from '../../contexts/AuthContext';
import {
  BrainCircuit,
  Search,
  Flame,
  Bell,
  Moon,
  Sun,
  ChevronDown,
  LayoutDashboard,
  Video,
  LineChart,
  Target,
  Map,
  History,
  User,
  Settings,
  LogOut,
  ShieldAlert } from
'lucide-react';
import { cn } from '../../lib/utils';
import { Input } from '../ui/Input';
export function TopNav() {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const location = useLocation();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isInterviewsOpen, setIsInterviewsOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);
  const interviewsRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
      profileRef.current &&
      !profileRef.current.contains(event.target as Node))
      {
        setIsProfileOpen(false);
      }
      if (
      interviewsRef.current &&
      !interviewsRef.current.contains(event.target as Node))
      {
        setIsInterviewsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
  const navLinks = [
  {
    name: 'Dashboard',
    path: '/dashboard',
    icon: LayoutDashboard
  },
  {
    name: 'Find Jobs',
    path: '/jobs',
    icon: Search
  },
  {
    name: 'Analytics',
    path: '/analytics',
    icon: LineChart
  },
  {
    name: 'Skill Gap',
    path: '/skill-gap',
    icon: Target
  },
  {
    name: 'Roadmap',
    path: '/roadmap',
    icon: Map
  }];

  return (
    <header className="sticky top-0 z-50 w-full border-b border-ink-200 bg-white/80 backdrop-blur-md dark:border-ink-800 dark:bg-ink-950/80">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-8">
          <Link to="/dashboard" className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand-600 text-white">
              <BrainCircuit size={20} />
            </div>
            <span className="text-xl font-bold tracking-tight text-ink-900 dark:text-white">
              InterviAI
            </span>
          </Link>

          <nav className="hidden md:flex items-center gap-1">
            {navLinks.map((link) => {
              const isActive = location.pathname === link.path;
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className={cn(
                    'flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    isActive ?
                    'bg-ink-100 text-ink-900 dark:bg-ink-800 dark:text-white' :
                    'text-ink-600 hover:bg-ink-50 hover:text-ink-900 dark:text-ink-400 dark:hover:bg-ink-800/50 dark:hover:text-white'
                  )}>
                  
                  <link.icon size={16} />
                  {link.name}
                </Link>);

            })}

            {/* Interviews Dropdown */}
            <div className="relative" ref={interviewsRef}>
              <button
                onClick={() => setIsInterviewsOpen(!isInterviewsOpen)}
                className={cn(
                  'flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                  location.pathname.includes('/interview') ||
                  location.pathname === '/history' ?
                  'bg-ink-100 text-ink-900 dark:bg-ink-800 dark:text-white' :
                  'text-ink-600 hover:bg-ink-50 hover:text-ink-900 dark:text-ink-400 dark:hover:bg-ink-800/50 dark:hover:text-white'
                )}>
                
                <Video size={16} />
                Interviews
                <ChevronDown
                  size={14}
                  className={cn(
                    'transition-transform',
                    isInterviewsOpen && 'rotate-180'
                  )} />
                
              </button>

              {isInterviewsOpen &&
              <div className="absolute left-0 mt-2 w-48 origin-top-left rounded-xl border border-ink-200 bg-white p-1 shadow-lift dark:border-ink-800 dark:bg-ink-900">
                  <Link
                  to="/interview/company"
                  onClick={() => setIsInterviewsOpen(false)}
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-ink-700 hover:bg-ink-100 dark:text-ink-300 dark:hover:bg-ink-800">
                  
                    <Video size={16} />
                    New Interview
                  </Link>
                  <Link
                  to="/history"
                  onClick={() => setIsInterviewsOpen(false)}
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-ink-700 hover:bg-ink-100 dark:text-ink-300 dark:hover:bg-ink-800">
                  
                    <History size={16} />
                    History
                  </Link>
                </div>
              }
            </div>
          </nav>
        </div>

        <div className="flex items-center gap-4">
          <div className="hidden lg:block w-64">
            <Input
              placeholder="Search..."
              icon={<Search size={16} />}
              className="h-9 bg-ink-50 dark:bg-ink-900/50" />
            
          </div>

          <div className="flex items-center gap-1 rounded-full border border-orange-200 bg-orange-50 px-3 py-1 text-sm font-medium text-orange-600 dark:border-orange-900/50 dark:bg-orange-900/20 dark:text-orange-400">
            <Flame size={16} className="text-orange-500" />
            0
          </div>

          <button
            onClick={toggleTheme}
            className="flex h-9 w-9 items-center justify-center rounded-full text-ink-500 hover:bg-ink-100 dark:text-ink-400 dark:hover:bg-ink-800">
            
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>

          <button className="flex h-9 w-9 items-center justify-center rounded-full text-ink-500 hover:bg-ink-100 dark:text-ink-400 dark:hover:bg-ink-800">
            <Bell size={18} />
          </button>

          {/* Profile Dropdown */}
          <div className="relative" ref={profileRef}>
            <button
              onClick={() => setIsProfileOpen(!isProfileOpen)}
              className="flex h-9 w-9 items-center justify-center overflow-hidden rounded-full border border-ink-200 transition-transform hover:scale-105 dark:border-ink-700">
              
              <img
                src={user?.profilePictureUrl || `https://ui-avatars.com/api/?name=${user?.firstName}+${user?.lastName}&background=random`}
                alt={user?.firstName}
                className="h-full w-full object-cover" />
              
            </button>

            {isProfileOpen &&
            <div className="absolute right-0 mt-2 w-56 origin-top-right rounded-xl border border-ink-200 bg-white p-1 shadow-lift dark:border-ink-800 dark:bg-ink-900">
                <div className="border-b border-ink-100 px-3 py-2.5 dark:border-ink-800">
                  <p className="text-sm font-medium text-ink-900 dark:text-white">
                    {user ? `${user.firstName} ${user.lastName}` : 'Guest'}
                  </p>
                  <p className="text-xs text-ink-500 dark:text-ink-400">
                    {user?.email}
                  </p>
                </div>
                <div className="py-1">
                  <Link
                  to="/profile"
                  onClick={() => setIsProfileOpen(false)}
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-ink-700 hover:bg-ink-100 dark:text-ink-300 dark:hover:bg-ink-800">
                  
                    <User size={16} />
                    Profile
                  </Link>
                  <Link
                  to="/profile"
                  onClick={() => setIsProfileOpen(false)}
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-ink-700 hover:bg-ink-100 dark:text-ink-300 dark:hover:bg-ink-800">
                  
                    <Settings size={16} />
                    Settings
                  </Link>
                  <Link
                  to="/admin"
                  onClick={() => setIsProfileOpen(false)}
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-brand-600 hover:bg-brand-50 dark:text-brand-400 dark:hover:bg-brand-900/20">
                  
                    <ShieldAlert size={16} />
                    Admin Dashboard
                  </Link>
                </div>
                <div className="border-t border-ink-100 py-1 dark:border-ink-800">
                  <button
                  type="button"
                  onClick={() => {
                    setIsProfileOpen(false);
                    logout();
                    window.location.href = '/';
                  }}
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-danger hover:bg-red-50 dark:hover:bg-red-900/20">
                  
                    <LogOut size={16} />
                    Log out
                  </button>
                </div>
              </div>
            }
          </div>
        </div>
      </div>
    </header>);

}
