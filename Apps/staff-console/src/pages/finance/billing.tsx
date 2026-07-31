import React from 'react';
import { PageHeader } from '@/components/page-header';
import { useMutation } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { useJobPoller } from '@/hooks/use-job-poller';
import { Button } from '@/components/ui/button';
import { Play, RefreshCw } from 'lucide-react';
import { FinanceTabs } from '@/pages/finance/schedules';

export default function FinanceBilling() {
  const pollJob = useJobPoller();

  const billingMutation = useMutation({
    mutationFn: async () => {
      const idempotencyKey = crypto.randomUUID();
      const res = await apiClient('/finance/billing/run', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey },
      });
      return res.jobId;
    },
    onSuccess: (jobId) => {
      pollJob(jobId, () => {
        // Refresh data on complete
      });
    },
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Termly Billing Run"
        description="Trigger automated termly fee invoice generation for all active students based on level schedules."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Finance', href: '/finance/reports' },
          { label: 'Billing Run' },
        ]}
      />
      <FinanceTabs />

      <div className="bg-white rounded-2xl border border-slate-200/90 p-8 sm:p-12 shadow-2xs flex flex-col items-center text-center max-w-2xl mx-auto space-y-4">
        <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center border border-slate-200 shadow-2xs">
          <Play className="w-8 h-8 text-slate-800 ml-1" />
        </div>

        <h2 className="font-display text-xl font-bold text-slate-900">Run Term 1 2024-25 Billing</h2>

        <p className="text-xs sm:text-sm text-slate-500 font-medium max-w-md leading-relaxed">
          This will generate termly invoices for all active students based on their enrolled class's approved fee schedules. All generated billing items are immutable and logged.
        </p>

        <div className="pt-2">
          <Button
            onClick={() => billingMutation.mutate()}
            disabled={billingMutation.isPending}
            variant="default"
            size="lg"
          >
            {billingMutation.isPending ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Starting Billing Batch...</span>
              </>
            ) : (
              <>
                <Play className="w-4 h-4 text-emerald-400" />
                <span>Trigger Billing Run</span>
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}
