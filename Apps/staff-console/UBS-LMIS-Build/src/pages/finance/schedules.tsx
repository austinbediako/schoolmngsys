import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { GhsCurrency } from '@/components/ghs-currency';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { PermissionGate } from '@/components/permission-gate';
import { toast } from 'sonner';
import { Plus, CheckCircle2, FileSpreadsheet, ShieldCheck, Clock } from 'lucide-react';

type FeeScheduleView = {
  id: string;
  levelId: string;
  termId: string;
  yearId: string;
  status: string;
  total: string;
  items: { id: string; name: string; amount: string }[];
};

const financeTabs = [
  { label: 'Fee Schedules', href: '/finance/schedules' },
  { label: 'Billing Run', href: '/finance/billing' },
  { label: 'Payments', href: '/finance/payments' },
  { label: 'Adjustments', href: '/finance/adjustments' },
  { label: 'Reports', href: '/finance/reports' },
];

export function FinanceTabs() {
  return <SectionTabs tabs={financeTabs} />;
}

export default function FeeSchedules() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ levelId: '', termId: 't2', yearId: 'y2' });

  const { data, isLoading } = useQuery<{ content: FeeScheduleView[] }>({
    queryKey: ['fee-schedules'],
    queryFn: () => apiClient('/finance/fee-schedules'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/finance/fee-schedules', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Fee schedule created as draft');
      queryClient.invalidateQueries({ queryKey: ['fee-schedules'] });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => apiClient(`/finance/fee-schedules/${id}/approve`, { method: 'POST' }),
    onSuccess: () => {
      toast.success('Fee schedule approved');
      queryClient.invalidateQueries({ queryKey: ['fee-schedules'] });
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Termly Fee Schedules"
        description="Define, manage, and approve level-specific fee breakdowns for student billing."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Finance', href: '/finance/reports' },
          { label: 'Schedules' },
        ]}
      />
      <FinanceTabs />

      <div className="flex justify-end">
        <Button onClick={() => setShowForm((s) => !s)} variant="default">
          <Plus className="w-4 h-4 text-emerald-400" />
          <span>New Fee Schedule</span>
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate(undefined);
          }}
          className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs grid grid-cols-1 md:grid-cols-3 gap-4 items-end"
        >
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Class Level Name</label>
            <input
              required
              value={form.levelId}
              onChange={(e) => setForm((f) => ({ ...f, levelId: e.target.value }))}
              placeholder="e.g. Primary 4"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <Button type="submit" disabled={createMutation.isPending} variant="default">
            {createMutation.isPending ? 'Saving...' : 'Save as Draft'}
          </Button>
        </form>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {isLoading ? (
          <div className="text-xs font-semibold text-slate-400 p-8 text-center col-span-2">
            Loading fee schedules...
          </div>
        ) : (
          data?.content.map((fs) => (
            <div
              key={fs.id}
              className="bg-white border border-slate-200/90 rounded-2xl shadow-2xs p-6 space-y-4 hover:border-slate-300 transition-colors"
            >
              <div className="flex justify-between items-start">
                <div>
                  <div className="font-display font-bold text-slate-900 text-base">{fs.levelId}</div>
                  <div className="text-xs font-mono font-medium text-slate-400 mt-0.5">
                    Term {fs.termId} • Year {fs.yearId}
                  </div>
                </div>

                {/* Clean Professional Status Tag */}
                {fs.status === 'APPROVED' ? (
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[11px] font-bold text-slate-700 bg-slate-100 border border-slate-200 uppercase tracking-wider">
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
                    <span>Approved</span>
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[11px] font-bold text-amber-800 bg-amber-50 border border-amber-200 uppercase tracking-wider">
                    <Clock className="w-3.5 h-3.5 text-amber-600" />
                    <span>Draft Schedule</span>
                  </span>
                )}
              </div>

              <div className="space-y-2 pt-2 border-t border-slate-100">
                {fs.items.map((item) => (
                  <div key={item.id} className="flex justify-between text-xs font-medium py-1">
                    <span className="text-slate-600">{item.name}</span>
                    <GhsCurrency amount={item.amount} className="font-mono text-slate-900 font-bold" />
                  </div>
                ))}
              </div>

              <div className="flex justify-between items-center pt-3 border-t border-slate-200 font-bold text-sm text-slate-900">
                <span>Total Level Fees</span>
                <GhsCurrency amount={fs.total} className="font-mono text-base text-slate-900" />
              </div>

              {fs.status === 'DRAFT' && (
                <PermissionGate perm="FINANCE_SCHEDULE_MANAGE">
                  <Button
                    onClick={() => approveMutation.mutate(fs.id)}
                    disabled={approveMutation.isPending}
                    variant="success"
                    className="w-full mt-2"
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Approve Fee Schedule</span>
                  </Button>
                </PermissionGate>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
