import React from 'react';
import { Link } from 'react-router-dom';
import { Users, Activity, Server, AlertTriangle, ArrowLeft } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription } from
'../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { mockAdminStats } from '../data/mockData';
export function Admin() {
  return (
    <div className="min-h-screen bg-ink-950 text-white">
      {/* Admin Top Nav */}
      <header className="border-b border-ink-800 bg-ink-900">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-4">
            <Link to="/dashboard" className="text-ink-400 hover:text-white">
              <ArrowLeft size={20} />
            </Link>
            <span className="text-lg font-bold text-brand-400">
              InterviAI Admin
            </span>
          </div>
          <Badge variant="danger">System Status: Healthy</Badge>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8 space-y-8">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card className="border-ink-800 bg-ink-900">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    Total Users
                  </p>
                  <p className="mt-2 text-3xl font-bold text-white">
                    {mockAdminStats.totalUsers.toLocaleString()}
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-900/30 text-brand-400">
                  <Users size={24} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-ink-800 bg-ink-900">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    Active Interviews
                  </p>
                  <p className="mt-2 text-3xl font-bold text-white">
                    {mockAdminStats.activeInterviews}
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-success/20 text-success">
                  <Activity size={24} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-ink-800 bg-ink-900">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    API Requests
                  </p>
                  <p className="mt-2 text-3xl font-bold text-white">
                    {(mockAdminStats.apiRequests / 1000000).toFixed(1)}M
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-purple-900/30 text-purple-400">
                  <Server size={24} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-ink-800 bg-ink-900">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-ink-400">
                    Avg Latency
                  </p>
                  <p className="mt-2 text-3xl font-bold text-white">
                    {mockAdminStats.avgLatency}
                  </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-orange-900/30 text-orange-400">
                  <AlertTriangle size={24} />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
          <Card className="border-ink-800 bg-ink-900">
            <CardHeader>
              <CardTitle className="text-white">Recent Signups</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {mockAdminStats.recentUsers.map((user) =>
                <div
                  key={user.id}
                  className="flex items-center justify-between border-b border-ink-800 pb-4 last:border-0 last:pb-0">
                  
                    <div>
                      <p className="font-medium text-white">{user.name}</p>
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

          <Card className="border-ink-800 bg-ink-900">
            <CardHeader>
              <CardTitle className="text-white">System Logs</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 font-mono text-sm">
                {mockAdminStats.systemLogs.map((log, i) =>
                <div
                  key={i}
                  className="flex items-start gap-3 rounded bg-ink-950 p-3">
                  
                    <span className="text-ink-500">{log.time}</span>
                    <span
                    className={`font-bold ${log.level === 'ERROR' ? 'text-danger' : log.level === 'WARN' ? 'text-warning' : 'text-brand-400'}`}>
                    
                      [{log.level}]
                    </span>
                    <span className="text-ink-300">{log.message}</span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>);

}