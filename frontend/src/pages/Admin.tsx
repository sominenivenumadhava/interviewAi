import React from 'react';
import { Navigate } from 'react-router-dom';
import { Users, Activity, Server, AlertTriangle } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle } from
'../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { mockAdminStats } from '../data/mockData';
import { useAuth } from '../contexts/AuthContext';

export function Admin() {
  const { hasRole, isLoading } = useAuth();

  if (!isLoading && !hasRole('ADMIN')) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Admin Dashboard</h1>
          <p className="page-subtitle">System health and platform metrics</p>
        </div>
        <Badge variant="danger">System Status: Healthy</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    Total Users
                  </p>
                  <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                    {mockAdminStats.totalUsers.toLocaleString()}
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-500/15 text-brand-400">
                  <Users size={24} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    Active Interviews
                  </p>
                  <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                    {mockAdminStats.activeInterviews}
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-success/20 text-success">
                  <Activity size={24} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    API Requests
                  </p>
                  <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                    {(mockAdminStats.apiRequests / 1000000).toFixed(1)}M
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-cyan-500/15 text-cyan-400">
                  <Server size={24} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    Avg Latency
                  </p>
                  <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                    {mockAdminStats.avgLatency}
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-orange-500/15 text-orange-400">
                  <AlertTriangle size={24} />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Recent Signups</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {mockAdminStats.recentUsers.map((user) =>
                <div
                  key={user.id}
                  className="flex items-center justify-between border-b border-ink-100 dark:border-ink-800 pb-4 last:border-0 last:pb-0">
                  
                    <div>
                      <p className="font-medium text-ink-900 dark:text-white">{user.name}</p>
                      <p className="text-sm text-ink-400">{user.email}</p>
                    </div>
                    <div className="text-right">
                      <Badge
                      variant={user.plan === 'Pro' ? 'success' : 'secondary'}>
                      
                        {user.plan}
                      </Badge>
                      <p className="mt-1 text-xs text-ink-500">{user.joined}</p>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>System Logs</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 font-mono text-sm">
                {mockAdminStats.systemLogs.map((log, i) =>
                <div
                  key={i}
                  className="flex items-start gap-3 rounded bg-ink-50 dark:bg-ink-950 p-3">
                  
                    <span className="text-ink-500">{log.time}</span>
                    <span
                    className={`font-bold ${log.level === 'ERROR' ? 'text-danger' : log.level === 'WARN' ? 'text-warning' : 'text-brand-400'}`}>
                    
                      [{log.level}]
                    </span>
                    <span className="text-ink-600 dark:text-ink-300">{log.message}</span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
    </div>);

}
