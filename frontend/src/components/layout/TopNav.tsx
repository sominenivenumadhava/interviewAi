import React, { useEffect, useState, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '../../contexts/ThemeContext';
import { useAuth } from '../../contexts/AuthContext';
import {
  BrainCircuit, Bell, Moon, Sun, ChevronDown,
  LayoutDashboard, Video, LineChart, Target, Map, History,
  User, Settings, LogOut, ShieldAlert, Search, FileText,
} from 'lucide-react';
import { cn } from '../../lib/utils';

// ─── Animation variants ───────────────────────────────────────────────────────
const dropdownVariants = {
  hidden:  { opacity: 0, scale: 0.95, y: -8 },
  visible: { opacity: 1, scale: 1,    y: 0  },
  exit:    { opacity: 0, scale: 0.95, y: -8 },
};
const dropdownTransition = { duration: 0.15, ease: [0.25, 0.1, 0.25, 1] };

// ─── Nav link ─────────────────────────────────────────────────────────────────
function NavLink({
  to,
  icon: Icon,
  label,
  isActive,
  onClick,
}: {
  to: string;
  icon: React.ElementType;
  label: string;
  isActive: boolean;
  onClick?: () => void;
}) {
  return (
    <Link
      to={to}
      onClick={onClick}
      className={cn(
        'relative flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-all duration-200 whitespace-nowrap',
        isActive
          ? 'bg-brand-500/10 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400'
          : 'text-ink-600 hover:bg-ink-50 hover:text-ink-900 dark:text-ink-400 dark:hover:bg-white/5 dark:hover:text-ink-100'
      )}
    >
      {isActive && (
        <motion.div
          layoutId="nav-indicator"
          className="absolute inset-0 rounded-lg bg-brand-500/10 dark:bg-brand-500/15"
          transition={{ type: 'spring', stiffness: 300, damping: 30 }}
        />
      )}
      <Icon size={15} className="relative z-10" />
      <span className="relative z-10">{label}</span>
    </Link>
  );
}

// ─── TopNav ───────────────────────────────────────────────────────────────────
export function TopNav() {
  const { theme, toggleTheme } = useTheme();
  const { user, logout, hasRole } = useAuth();
  const location = useLocation();
  const [isProfileOpen, setIsProfileOpen]     = useState(false);
  const [isInterviewsOpen, setIsInterviewsOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const profileRef   = useRef<HTMLDivElement>(null);
  const interviewsRef = useRef<HTMLDivElement>(null);
  const notificationsRef = useRef<HTMLDivElement>(null);

  // Close dropdowns on outside click
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
        setIsProfileOpen(false);
      }
      if (interviewsRef.current && !interviewsRef.current.contains(event.target as Node)) {
        setIsInterviewsOpen(false);
      }
      if (notificationsRef.current && !notificationsRef.current.contains(event.target as Node)) {
        setIsNotificationsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const navLinks = [
    { name: 'Dashboard', path: '/dashboard',  icon: LayoutDashboard },
    { name: 'Find Jobs',  path: '/jobs',        icon: Search },
    { name: 'Analytics', path: '/analytics',   icon: LineChart },
  ];

  const isInterviewsActive =
    location.pathname.startsWith('/interview') ||
    location.pathname === '/history' ||
    location.pathname === '/resume';

  return (
    <header className="sticky top-0 z-50 w-full border-b border-ink-100 dark:border-white/[0.06] bg-white/85 dark:bg-obsidian-950/85 backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">

        {/* ── Left: Logo + Nav ── */}
        <div className="flex items-center gap-6">
          {/* Logo */}
          <Link to="/dashboard" className="flex items-center gap-2.5 group">
            <motion.div
              whileHover={{ scale: 1.08, rotate: 5 }}
              transition={{ type: 'spring', stiffness: 400, damping: 20 }}
              className="flex h-8 w-8 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-cyan-500 shadow-glow-sm text-white"
            >
              <BrainCircuit size={18} />
            </motion.div>
            <span className="text-[17px] font-extrabold tracking-tight text-ink-900 dark:text-white">
              InterviewAI
            </span>
          </Link>

          {/* Nav Links */}
          <nav className="hidden md:flex items-center gap-0.5">
            {navLinks.map((link) => (
              <NavLink
                key={link.path}
                to={link.path}
                icon={link.icon}
                label={link.name}
                isActive={location.pathname === link.path}
              />
            ))}

            {/* Interviews Dropdown */}
            <div className="relative" ref={interviewsRef}>
              <button
                onClick={() => setIsInterviewsOpen(!isInterviewsOpen)}
                className={cn(
                  'flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-all duration-200 whitespace-nowrap',
                  isInterviewsActive
                    ? 'bg-brand-500/10 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400'
                    : 'text-ink-600 hover:bg-ink-50 hover:text-ink-900 dark:text-ink-400 dark:hover:bg-white/5 dark:hover:text-ink-100'
                )}
              >
                <Video size={15} />
                Interviews
                <motion.div
                  animate={{ rotate: isInterviewsOpen ? 180 : 0 }}
                  transition={{ duration: 0.2 }}
                >
                  <ChevronDown size={13} />
                </motion.div>
              </button>

              <AnimatePresence>
                {isInterviewsOpen && (
                  <motion.div
                    variants={dropdownVariants}
                    initial="hidden"
                    animate="visible"
                    exit="exit"
                    transition={dropdownTransition}
                    className="absolute left-0 mt-2 w-52 origin-top-left rounded-2xl border border-ink-100 dark:border-white/[0.08] bg-white dark:bg-obsidian-900 p-1.5 shadow-lift dark:shadow-card"
                  >
                    <Link
                      to="/interview/company"
                      onClick={() => setIsInterviewsOpen(false)}
                      className="flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm text-ink-700 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-white/5 transition-colors"
                    >
                      <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand-500/10 text-brand-500">
                        <Video size={14} />
                      </div>
                      New Interview
                    </Link>
                    <Link
                      to="/resume"
                      onClick={() => setIsInterviewsOpen(false)}
                      className="flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm text-ink-700 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-white/5 transition-colors"
                    >
                      <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-cyan-500/10 text-cyan-500">
                        <FileText size={14} />
                      </div>
                      Resume
                    </Link>
                    <Link
                      to="/history"
                      onClick={() => setIsInterviewsOpen(false)}
                      className="flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm text-ink-700 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-white/5 transition-colors"
                    >
                      <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-ink-100 dark:bg-white/8 text-ink-500 dark:text-ink-400">
                        <History size={14} />
                      </div>
                      History
                    </Link>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </nav>
        </div>

        {/* ── Right: Actions ── */}
        <div className="flex items-center gap-2">
          {/* Theme toggle */}
          <motion.button
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            onClick={toggleTheme}
            className="flex h-9 w-9 items-center justify-center rounded-full text-ink-500 dark:text-ink-400 hover:bg-ink-100 dark:hover:bg-white/8 transition-colors"
            aria-label="Toggle theme"
          >
            <AnimatePresence mode="wait" initial={false}>
              {theme === 'dark' ? (
                <motion.div key="sun" initial={{ rotate: -90, opacity: 0 }} animate={{ rotate: 0, opacity: 1 }} exit={{ rotate: 90, opacity: 0 }} transition={{ duration: 0.2 }}>
                  <Sun size={17} />
                </motion.div>
              ) : (
                <motion.div key="moon" initial={{ rotate: 90, opacity: 0 }} animate={{ rotate: 0, opacity: 1 }} exit={{ rotate: -90, opacity: 0 }} transition={{ duration: 0.2 }}>
                  <Moon size={17} />
                </motion.div>
              )}
            </AnimatePresence>
          </motion.button>

          {/* Notifications */}
          <div className="relative" ref={notificationsRef}>
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={() => setIsNotificationsOpen(!isNotificationsOpen)}
              className="flex h-9 w-9 items-center justify-center rounded-full text-ink-500 dark:text-ink-400 hover:bg-ink-100 dark:hover:bg-white/8 transition-colors"
              aria-label="Notifications"
              aria-expanded={isNotificationsOpen}
            >
              <Bell size={17} />
            </motion.button>

            <AnimatePresence>
              {isNotificationsOpen && (
                <motion.div
                  variants={dropdownVariants}
                  initial="hidden"
                  animate="visible"
                  exit="exit"
                  transition={dropdownTransition}
                  className="absolute right-0 mt-2 w-64 origin-top-right rounded-2xl border border-ink-100 dark:border-white/[0.08] bg-white dark:bg-obsidian-900 p-3 shadow-lift dark:shadow-card"
                >
                  <p className="text-xs font-semibold uppercase tracking-wider text-ink-400 mb-2 px-1">
                    Notifications
                  </p>
                  <p className="px-2 py-6 text-center text-sm text-ink-500 dark:text-ink-400">
                    No notifications
                  </p>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Profile Dropdown */}
          <div className="relative" ref={profileRef}>
            <motion.button
              whileHover={{ scale: 1.06 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setIsProfileOpen(!isProfileOpen)}
              className="flex h-9 w-9 items-center justify-center overflow-hidden rounded-full border-2 border-ink-200 dark:border-white/15 hover:border-brand-400 dark:hover:border-brand-500 transition-colors"
            >
              <img
                src={
                  user?.profilePictureUrl ||
                  `https://ui-avatars.com/api/?name=${user?.firstName}+${user?.lastName}&background=14b8a6&color=fff&bold=true`
                }
                alt={user?.firstName}
                className="h-full w-full object-cover"
              />
            </motion.button>

            <AnimatePresence>
              {isProfileOpen && (
                <motion.div
                  variants={dropdownVariants}
                  initial="hidden"
                  animate="visible"
                  exit="exit"
                  transition={dropdownTransition}
                  className="absolute right-0 mt-2 w-60 origin-top-right rounded-2xl border border-ink-100 dark:border-white/[0.08] bg-white dark:bg-obsidian-900 p-1.5 shadow-lift dark:shadow-card"
                >
                  {/* User info */}
                  <div className="px-3 py-3 mb-1 border-b border-ink-100 dark:border-white/[0.06]">
                    <p className="text-sm font-semibold text-ink-900 dark:text-white">
                      {user ? `${user.firstName} ${user.lastName}` : 'Guest'}
                    </p>
                    <p className="text-xs text-ink-400 dark:text-ink-500 mt-0.5 truncate">{user?.email}</p>
                  </div>

                  <div className="py-1 space-y-0.5">
                    <Link
                      to="/profile"
                      onClick={() => setIsProfileOpen(false)}
                      className="flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-ink-700 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-white/5 transition-colors"
                    >
                      <div className="flex h-6 w-6 items-center justify-center rounded-lg bg-ink-100 dark:bg-white/8 text-ink-500 dark:text-ink-400">
                        <User size={13} />
                      </div>
                      Profile
                    </Link>
                    <Link
                      to="/profile"
                      onClick={() => setIsProfileOpen(false)}
                      className="flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-ink-700 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-white/5 transition-colors"
                    >
                      <div className="flex h-6 w-6 items-center justify-center rounded-lg bg-ink-100 dark:bg-white/8 text-ink-500 dark:text-ink-400">
                        <Settings size={13} />
                      </div>
                      Settings
                    </Link>
                    {hasRole('ADMIN') && (
                      <Link
                        to="/admin"
                        onClick={() => setIsProfileOpen(false)}
                        className="flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-brand-600 dark:text-brand-400 hover:bg-brand-50 dark:hover:bg-brand-500/10 transition-colors"
                      >
                        <div className="flex h-6 w-6 items-center justify-center rounded-lg bg-brand-100 dark:bg-brand-500/15 text-brand-500">
                          <ShieldAlert size={13} />
                        </div>
                        Admin Dashboard
                      </Link>
                    )}
                  </div>

                  <div className="pt-1 mt-1 border-t border-ink-100 dark:border-white/[0.06]">
                    <button
                      type="button"
                      onClick={() => {
                        setIsProfileOpen(false);
                        logout();
                        window.location.href = '/';
                      }}
                      className="flex w-full items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 transition-colors"
                    >
                      <div className="flex h-6 w-6 items-center justify-center rounded-lg bg-rose-100 dark:bg-rose-500/15 text-rose-500">
                        <LogOut size={13} />
                      </div>
                      Log out
                    </button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </header>
  );
}
