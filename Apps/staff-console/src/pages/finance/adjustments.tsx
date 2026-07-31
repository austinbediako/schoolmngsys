import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { StatusBadge } from '@/components/status-badge';
import { GhsCurrency } from '@/components/ghs-currency';
import { PermissionGate } from '@/components/permission-gate';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { FinanceTabs } from '@/pages/finance/schedules';
import { Plus, CheckCircle2 } from 'lucide-react';

type AdjustmentView = {
  id: string;
  studentId: string;
  studentName: string;
  studentNumber: string;
  amount: string;
  reason: string;
  status: string;
};

export default function Adjustments() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ studentId: '', amount: '', reason: '' });

  const { data, isLoading } = useQuery<{ content: AdjustmentView[] }>({
    queryKey: ['adjustments'],
    queryFn: () => apiClient('/finance/adjustments'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/finance/adjustments', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Adjustment submitted for approval');
      queryClient.invalidateQueries({ queryKey: ['adjustments'] });
      setForm({ studentId: '', amount: '', reason: '' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => apiClient(`/finance/adjustments/${id}/approve`, { method: 'POST' }),
    onSuccess: () => {
      toast.success('Adjustment approved');
      queryClient.invalidateQueries({ queryKey: ['adjustments'] });
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Fee Adjustments & Waivers"
        description="Process student-specific fee discounts, approved waivers, and billing balance corrections."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Finance', href: '/finance/reports' },
          { label: 'Adjustments' },
        ]}
      />
      <FinanceTabs />

      <div className="flex justify-end">
        <Button onClick={() => setShowForm((s) => !s)} variant="default">
          <Plus className="w-4 h-4 text-emerald-400" />
          <span>Request Adjustment</span>
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate(undefined);
          }}
          className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs grid grid-cols-1 md:grid-cols-4 gap-4 items-end"
        >
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Student ID Number</label>
            <input
              required
              value={form.studentId}
              onChange={(e) => setForm((f) => ({ ...f, studentId: e.target.value }))}
              placeholder="e.g. s3"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Amount (Negative for discount)</label>
            <input
              required
              type="number"
              step="0.01"
              value={form.amount}
              onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))}
              placeholder="-50.00"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Reason / Justification</label>
            <input
              required
              value={form.reason}
              onChange={(e) => setForm((f) => ({ ...f, reason: e.target.value }))}
              placeholder="Staff scholarship waiver"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <Button type="submit" disabled={createMutation.isPending} variant="default">
            {createMutation.isPending ? 'Submitting...' : 'Submit Adjustment'}
          </Button>
        </form>
      )}

      {/* Adjustments Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Student</th>
              <th className="px-5 py-3.5">Reason</th>
              <th className="px-5 py-3.5 text-right">Adjustment Amount</th>
              <th className="px-5 py-3.5">Status</th>
              <th className="px-5 py-3.5 text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  Loading adjustments...
                </td>
              </tr>
            ) : data?.content.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  No fee adjustments recorded yet.
                </td>
              </tr>
            ) : (
              data?.content.map((a) => (
                <tr key={a.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5">
                    <div className="font-bold text-slate-900">{a.studentName}</div>
                    <div className="text-[11px] text-slate-400 font-mono mt-0.5">{a.studentNumber}</div>
                  </td>
                  <td className="px-5 py-3.5 text-slate-700 font-semibold">{a.reason}</td>
                  <td className="px-5 py-3.5 text-right">
                    <GhsCurrency amount={a.amount} className="font-mono font-bold text-slate-900" />
                  </td>
                  <td className="px-5 py-3.5">
                    <StatusBadge status={a.status} />
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    {a.status === 'PENDING' && (
                      <PermissionGate perm="FINANCE_ADJUSTMENT_APPROVE">
                        <Button
                          onClick={() => approveMutation.mutate(a.id)}
                          disabled={approveMutation.isPending}
                          variant="success"
                          size="sm"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          <span>Approve</span>
                        </Button>
                      </PermissionGate>
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
