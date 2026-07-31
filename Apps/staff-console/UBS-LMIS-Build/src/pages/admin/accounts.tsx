import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { StatusBadge } from '@/components/status-badge';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Account } from '@/types';
import { toast } from 'sonner';
import { Plus, UserCheck, ShieldCheck, Key, RefreshCw } from 'lucide-react';

const ROLES = ['SYSTEM_ADMIN', 'HEAD_OF_SCHOOL', 'SCHOOL_ADMIN', 'HOD', 'TEACHER', 'ACCOUNTANT', 'LIBRARIAN', 'NURSE'];

export default function AdminAccounts() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ firstName: '', lastName: '', email: '', phone: '', role: 'TEACHER' });
  const [lastCreated, setLastCreated] = React.useState<{ name: string; temporaryPassword: string } | null>(null);

  const { data, isLoading } = useQuery<{ content: Account[] }>({
    queryKey: ['accounts'],
    queryFn: () => apiClient('/admin/accounts'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/admin/accounts', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: (account: any) => {
      toast.success('Account provisioned — credentials generated');
      setLastCreated({ name: `${account.firstName} ${account.lastName}`, temporaryPassword: account.temporaryPassword });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      setForm({ firstName: '', lastName: '', email: '', phone: '', role: 'TEACHER' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => apiClient(`/admin/accounts/${id}/deactivate`, { method: 'POST' }),
    onSuccess: () => {
      toast.success('Account deactivated');
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Staff Accounts & System Access"
        description="Provision accounts, assign administrative roles, and manage staff access permissions."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Admin', href: '/admin/accounts' },
          { label: 'Accounts' },
        ]}
      />
      <SectionTabs
        tabs={[
          { label: 'Staff Accounts', href: '/admin/accounts' },
          { label: 'Audit Trail Logs', href: '/admin/audit-log' },
        ]}
      />

      {lastCreated && (
        <div className="bg-amber-50 border border-amber-200 text-amber-900 rounded-2xl p-4 text-xs font-semibold flex items-start gap-3 shadow-2xs">
          <Key className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          <div>
            One-time credentials generated for <strong>{lastCreated.name}</strong> — temporary password:{' '}
            <code className="bg-amber-100 px-1.5 py-0.5 rounded font-mono font-bold text-slate-900">{lastCreated.temporaryPassword}</code>.
            Deliver this securely to the staff member per security policy.
          </div>
        </div>
      )}

      <div className="flex justify-end">
        <Button onClick={() => setShowForm((s) => !s)} variant="default">
          <Plus className="w-4 h-4" />
          <span>Provision Staff Account</span>
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate(undefined);
          }}
          className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 items-end"
        >
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">First Name</label>
            <input
              required
              value={form.firstName}
              onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Last Name</label>
            <input
              required
              value={form.lastName}
              onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Phone</label>
            <input
              required
              value={form.phone}
              onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
              placeholder="+233201234567"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Assigned Role</label>
            <select
              value={form.role}
              onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold bg-white"
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>

          <Button type="submit" disabled={createMutation.isPending} variant="default">
            {createMutation.isPending ? 'Provisioning...' : 'Provision'}
          </Button>
        </form>
      )}

      {/* Staff Accounts Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Staff Member</th>
              <th className="px-5 py-3.5">System Roles</th>
              <th className="px-5 py-3.5">Status</th>
              <th className="px-5 py-3.5 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  Loading staff accounts...
                </td>
              </tr>
            ) : (
              data?.content.map((a) => (
                <tr key={a.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="font-bold text-slate-900 text-sm">
                      {a.firstName} {a.lastName}
                    </div>
                    <div className="text-slate-500 font-mono text-[11px] mt-0.5">
                      {a.staffNumber} • {a.phone}
                    </div>
                  </td>

                  <td className="px-5 py-3.5 font-semibold text-slate-700">
                    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg bg-slate-100 text-slate-700 border border-slate-200 font-mono text-[11px]">
                      {a.roles.join(', ').replace(/_/g, ' ')}
                    </span>
                  </td>

                  <td className="px-5 py-3.5">
                    <StatusBadge status={a.status} />
                  </td>

                  <td className="px-5 py-3.5 text-right">
                    {a.status === 'ACTIVE' && (
                      <button
                        onClick={() => deactivateMutation.mutate(a.id)}
                        disabled={deactivateMutation.isPending}
                        className="text-xs font-bold text-rose-600 hover:text-rose-700 hover:underline disabled:opacity-50"
                      >
                        Deactivate
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
