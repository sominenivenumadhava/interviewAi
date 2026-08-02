import React, { useState } from 'react';
import { LayoutDashboard, Search, Bookmark, History, Settings, Menu, X } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

interface DashboardSidebarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

export function DashboardSidebar({ activeTab, setActiveTab }: DashboardSidebarProps) {
  const [isOpen, setIsOpen] = useState(false);
  const { user } = useAuth();

  const displayName = user
    ? `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username || 'User'
    : 'Guest';
  const email = user?.email || '';
  const initials = displayName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() || '')
    .join('') || 'U';

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'find-jobs', label: 'Find Jobs', icon: Search },
    { id: 'saved-jobs', label: 'Saved Jobs', icon: Bookmark },
    { id: 'search-history', label: 'Search History', icon: History },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <>
      {/* Mobile Toggle Button */}
      <div className="lg:hidden fixed bottom-6 right-6 z-50">
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className="p-3.5 bg-brand-600 hover:bg-brand-700 text-white rounded-full shadow-lg flex items-center justify-center focus:outline-none"
        >
          {isOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar Container */}
      <aside className={`
        fixed top-0 bottom-0 left-0 z-40 w-64 bg-white dark:bg-ink-900 border-r border-ink-200 dark:border-ink-800 pt-20 transition-transform duration-300 lg:translate-x-0 lg:static lg:h-auto lg:z-0
        ${isOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="h-full px-4 py-4 flex flex-col justify-between overflow-y-auto">
          <div className="space-y-1.5">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = item.id === activeTab;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => {
                    setActiveTab(item.id);
                    setIsOpen(false);
                  }}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-all focus:outline-none
                    ${isActive 
                      ? 'bg-brand-600 text-white shadow-md shadow-brand-500/10' 
                      : 'text-ink-600 dark:text-ink-400 hover:bg-ink-50 dark:hover:bg-ink-800'
                    }
                  `}
                >
                  <Icon className="w-4.5 h-4.5" />
                  {item.label}
                </button>
              );
            })}
          </div>

          <div className="border-t border-ink-150 dark:border-ink-850 pt-4 mt-6">
            <div className="flex items-center gap-3 px-4">
              <div className="w-8 h-8 rounded-full bg-ink-200 dark:bg-ink-800 flex items-center justify-center font-bold text-xs">
                {initials}
              </div>
              <div className="min-w-0">
                <div className="text-xs font-bold text-ink-900 dark:text-ink-100 truncate">{displayName}</div>
                {email && (
                  <div className="text-[10px] text-ink-400 truncate">{email}</div>
                )}
              </div>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
