import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { User, Mail, Lock, Bell, Shield, Trash2, Camera } from 'lucide-react';
import {
  Card, CardContent, CardHeader, CardTitle, CardDescription,
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { PageHeader } from '../components/ui/PageHeader';
import { mockUser } from '../data/mockData';
import { cn } from '../lib/utils';

type NavSection = 'general' | 'security' | 'notifications' | 'danger';

const navSections: { id: NavSection; label: string; icon: React.ElementType; danger?: boolean }[] = [
  { id: 'general',       label: 'General',       icon: User    },
  { id: 'security',      label: 'Security',       icon: Shield  },
  { id: 'notifications', label: 'Notifications',  icon: Bell    },
  { id: 'danger',        label: 'Danger Zone',    icon: Trash2, danger: true },
];

export function Profile() {
  const [activeSection, setActiveSection] = useState<NavSection>('general');
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <PageHeader
        title="Profile Settings"
        subtitle="Manage your account details and preferences."
      />

      <div className="grid grid-cols-1 gap-8 md:grid-cols-4">
        {/* ── Sidebar Nav ── */}
        <div className="md:col-span-1">
          <nav className="flex flex-col gap-1">
            {navSections.map((section) => {
              const isActive = activeSection === section.id;
              const Icon = section.icon;
              return (
                <motion.button
                  key={section.id}
                  onClick={() => setActiveSection(section.id)}
                  whileHover={{ x: 2 }}
                  transition={{ duration: 0.15 }}
                  className={cn(
                    'relative flex items-center gap-2.5 rounded-xl px-3 py-2.5 text-sm font-medium text-left transition-colors w-full',
                    isActive
                      ? section.danger
                        ? 'bg-rose-50 dark:bg-rose-500/10 text-rose-600 dark:text-rose-400'
                        : 'bg-brand-50 dark:bg-brand-500/10 text-brand-700 dark:text-brand-400'
                      : section.danger
                        ? 'text-rose-600 dark:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10'
                        : 'text-ink-600 dark:text-ink-400 hover:bg-ink-50 dark:hover:bg-white/5 hover:text-ink-900 dark:hover:text-ink-100'
                  )}
                >
                  {isActive && !section.danger && (
                    <motion.div
                      layoutId="profile-nav-indicator"
                      className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-brand-500 rounded-r-full"
                      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
                    />
                  )}
                  <Icon size={15} />
                  {section.label}
                </motion.button>
              );
            })}
          </nav>
        </div>

        {/* ── Content ── */}
        <div className="space-y-6 md:col-span-3">
          {activeSection === 'general' && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="space-y-6"
            >
              <Card hoverable>
                <CardHeader>
                  <CardTitle>Profile Information</CardTitle>
                  <CardDescription>Update your personal details and avatar.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  {/* Avatar */}
                  <div className="flex items-center gap-5">
                    <div className="relative group">
                      <img
                        src={mockUser.avatar}
                        alt={mockUser.name}
                        className="h-20 w-20 rounded-2xl object-cover border-2 border-ink-100 dark:border-white/10"
                      />
                      <div className="absolute inset-0 rounded-2xl bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                        <Camera size={18} className="text-white" />
                      </div>
                    </div>
                    <div>
                      <Button variant="outline" size="sm">Change Avatar</Button>
                      <p className="text-xs text-ink-400 mt-1.5">JPG, PNG or GIF. Max 2MB.</p>
                    </div>
                  </div>

                  {/* Form */}
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div>
                      <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1.5">
                        Full Name
                      </label>
                      <Input defaultValue={mockUser.name} icon={<User size={15} />} />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1.5">
                        Email
                      </label>
                      <Input defaultValue={mockUser.email} icon={<Mail size={15} />} type="email" />
                    </div>
                  </div>

                  <div className="flex justify-end pt-2">
                    <Button onClick={handleSave} success={saved} variant="gradient">
                      {saved ? 'Saved!' : 'Save Changes'}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {activeSection === 'security' && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
            >
              <Card hoverable>
                <CardHeader>
                  <CardTitle>Change Password</CardTitle>
                  <CardDescription>Update your password to keep your account secure.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1.5">
                      Current Password
                    </label>
                    <Input type="password" icon={<Lock size={15} />} placeholder="••••••••" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1.5">
                      New Password
                    </label>
                    <Input type="password" icon={<Lock size={15} />} placeholder="Min 8 characters" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1.5">
                      Confirm New Password
                    </label>
                    <Input type="password" icon={<Lock size={15} />} placeholder="Repeat new password" />
                  </div>
                  <div className="flex justify-end pt-2">
                    <Button variant="secondary">Update Password</Button>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {activeSection === 'notifications' && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
            >
              <Card hoverable>
                <CardHeader>
                  <CardTitle>Notification Preferences</CardTitle>
                  <CardDescription>Choose how you want to be notified.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {[
                    'Interview reminders',
                    'Score updates',
                    'New feature announcements',
                    'Weekly progress reports',
                  ].map((label, i) => (
                    <div key={i} className="flex items-center justify-between py-2 border-b border-ink-100 dark:border-white/[0.06] last:border-0">
                      <span className="text-sm text-ink-700 dark:text-ink-300">{label}</span>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input type="checkbox" defaultChecked={i < 2} className="sr-only peer" />
                        <div className="w-10 h-5 bg-ink-200 dark:bg-white/10 rounded-full peer peer-checked:bg-brand-500 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:after:translate-x-5" />
                      </label>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </motion.div>
          )}

          {activeSection === 'danger' && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
            >
              <Card className="border-rose-200 dark:border-rose-500/20 bg-rose-50/30 dark:bg-rose-500/[0.03]">
                <CardHeader>
                  <CardTitle className="text-rose-700 dark:text-rose-400">Danger Zone</CardTitle>
                  <CardDescription className="text-rose-600/70 dark:text-rose-500/70">
                    Irreversible actions. Please proceed with caution.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between p-4 rounded-xl border border-rose-200 dark:border-rose-500/20 bg-white dark:bg-obsidian-900">
                    <div>
                      <p className="text-sm font-semibold text-ink-900 dark:text-white">Delete Account</p>
                      <p className="text-xs text-ink-400 mt-0.5">Permanently remove your account and all data.</p>
                    </div>
                    <Button variant="danger" size="sm">Delete Account</Button>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}
        </div>
      </div>
    </div>
  );
}