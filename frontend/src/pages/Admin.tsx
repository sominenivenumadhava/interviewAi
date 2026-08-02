import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { Users, Activity, UserCheck, Lock, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { useAuth } from '../contexts/AuthContext';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  verifiedUsers: number;
  usersRegisteredToday: number;
  lockedUsers: number;
}

export function Admin() {
  const { hasRole, isLoading } = useAuth();
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isLoading || !hasRole('ADMIN')) return;

    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await apiClient.get<any>(API_ENDPOINTS.USER.ADMIN_STATISTICS);
        const data = res?.data ?? res;
        if (cancelled) return;
        setStats({
          totalUsers: Number(data.totalUsers ?? 0),
          activeUsers: Number(data.activeUsers ?? 0),
          verifiedUsers: Number(data.verifiedUsers ?? 0),
          usersRegisteredToday: Number(data.usersRegisteredToday ?? 0),
          lockedUsers: Number(data.lockedUsers ?? 0),
        });
      } catch (err: any) {
        if (!cancelled) {
          setStats(null);
          setError(err?.message || 'Failed to load admin statistics.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
  }, [hasRole, isLoading]);

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
        <Badge variant={error ? 'warning' : 'success'}>
          {error ? 'Metrics unavailable' : 'System Status: Healthy'}
        </Badge>
      </div>

      {loading && (
        <div className="flex items-center justify-center gap-3 py-20 text-ink-500">
          <Loader2 className="animate-spin" size={20} />
          Loading statistics…
        </div>
      )}

      {!loading && error && (
        <Card>
          <CardContent className="py-12 text-center space-y-2">
            <p className="text-sm font-medium text-ink-900 dark:text-white">
              Could not load admin metrics
            </p>
            <p className="text-sm text-ink-500">{error}</p>
          </CardContent>
        </Card>
      )}

      {!loading && stats && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-ink-400">Total Users</p>
                    <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                      {stats.totalUsers.toLocaleString()}
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
                    <p className="text-sm font-medium text-ink-400">Active Users</p>
                    <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                      {stats.activeUsers.toLocaleString()}
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
                    <p className="text-sm font-medium text-ink-400">Verified Users</p>
                    <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                      {stats.verifiedUsers.toLocaleString()}
                    </p>
                  </div>
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-cyan-500/15 text-cyan-400">
                    <UserCheck size={24} />
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-ink-400">Locked Accounts</p>
                    <p className="mt-2 text-3xl font-bold text-ink-900 dark:text-white">
                      {stats.lockedUsers.toLocaleString()}
                    </p>
                  </div>
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-orange-500/15 text-orange-400">
                    <Lock size={24} />
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Today</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-ink-500 dark:text-ink-400">
                  New registrations today
                </p>
                <p className="mt-2 text-4xl font-bold text-ink-900 dark:text-white">
                  {stats.usersRegisteredToday.toLocaleString()}
                </p>
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
