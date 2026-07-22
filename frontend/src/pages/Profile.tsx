import React from 'react';
import { User, Mail, Lock, Bell, Trash2 } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription } from
'../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { mockUser } from '../data/mockData';
export function Profile() {
  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-ink-900 dark:text-white">
          Profile Settings
        </h1>
        <p className="mt-1 text-ink-500 dark:text-ink-400">
          Manage your account details and preferences.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
        <div className="md:col-span-1">
          <nav className="flex flex-col gap-2">
            <a
              href="#"
              className="rounded-lg bg-ink-100 px-3 py-2 text-sm font-medium text-ink-900 dark:bg-ink-800 dark:text-white">
              
              General
            </a>
            <a
              href="#"
              className="rounded-lg px-3 py-2 text-sm font-medium text-ink-600 hover:bg-ink-50 dark:text-ink-400 dark:hover:bg-ink-800/50">
              
              Security
            </a>
            <a
              href="#"
              className="rounded-lg px-3 py-2 text-sm font-medium text-ink-600 hover:bg-ink-50 dark:text-ink-400 dark:hover:bg-ink-800/50">
              
              Notifications
            </a>
            <a
              href="#"
              className="rounded-lg px-3 py-2 text-sm font-medium text-danger hover:bg-red-50 dark:hover:bg-red-900/20">
              
              Danger Zone
            </a>
          </nav>
        </div>

        <div className="space-y-6 md:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Profile Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center gap-6">
                <img
                  src={mockUser.avatar}
                  alt={mockUser.name}
                  className="h-20 w-20 rounded-full object-cover" />
                
                <Button variant="outline">Change Avatar</Button>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-medium text-ink-900 dark:text-white">
                    Full Name
                  </label>
                  <Input
                    defaultValue={mockUser.name}
                    icon={<User size={16} />} />
                  
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-ink-900 dark:text-white">
                    Email
                  </label>
                  <Input
                    defaultValue={mockUser.email}
                    icon={<Mail size={16} />} />
                  
                </div>
              </div>

              <div className="flex justify-end">
                <Button>Save Changes</Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Password</CardTitle>
              <CardDescription>
                Update your password to keep your account secure.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <label className="mb-2 block text-sm font-medium text-ink-900 dark:text-white">
                  Current Password
                </label>
                <Input type="password" icon={<Lock size={16} />} />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-ink-900 dark:text-white">
                  New Password
                </label>
                <Input type="password" icon={<Lock size={16} />} />
              </div>
              <div className="flex justify-end pt-2">
                <Button variant="secondary">Update Password</Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>);

}