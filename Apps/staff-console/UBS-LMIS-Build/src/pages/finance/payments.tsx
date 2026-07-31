import React from 'react';
import { Link } from 'wouter';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { GhsCurrency } from '@/components/ghs-currency';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { FinanceTabs } from '@/pages/finance/schedules';
import { Receipt, DollarSign, X } from 'lucide-react';

type InvoiceView = {
  id: string;
  studentId: string;
  studentName: string;
  studentNumber: string;
  totalAmount: string;
  paidAmount: string;
  balance: string;
  status: string;
};

type PaymentView = {
  id: string;
  studentName: string;
  studentNumber: string;
  amount: string;
  method: string;
  receiptNumber: string;
  createdAt: string;
};

export default function Payments() {
  const queryClient = useQueryClient();
  const [recordingFor, setRecordingFor] = React.useState<InvoiceView | null>(null);
  const [form, setForm] = React.useState({ amount: '', method: 'CASH', reference: '' });

  const { data: invoices } = useQuery<{ content: InvoiceView[] }>({
    queryKey: ['invoices-outstanding'],
    queryFn: () => apiClient('/finance/invoices?size=100'),
  });

  const { data: payments, isLoading } = useQuery<{ content: PaymentView[] }>({
    queryKey: ['payments'],
    queryFn: () => apiClient('/finance/payments?size=50'),
  });

  const outstanding = invoices?.content.filter((i) => i.status !== 'PAID') || [];

  const recordMutation = useMutation({
    mutationFn: () =>
      apiClient('/finance/payments', {
        method: 'POST',
        headers: { 'Idempotency-Key': crypto.randomUUID() },
        body: JSON.stringify({
          invoiceId: recordingFor!.id,
          amount: form.amount,
          method: form.method,
          reference: form.reference || undefined,
        }),
      }),
    onSuccess: (payment: any) => {
      toast.success(`Payment recorded — receipt ${payment.receiptNumber}`);
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['invoices-outstanding'] });
      setRecordingFor(null);
      setForm({ amount: '', method: 'CASH', reference: '' });
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Fee Payments & Cashier Receipts"
        description="Record fee collection, process payment receipts, and review transaction history."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Finance', href: '/finance/reports' },
          { label: 'Payments' },
        ]}
      />
      <FinanceTabs />

      {/* Outstanding Invoices Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50 font-display font-bold text-slate-900 text-sm">
          Outstanding Student Invoices Pending Payment
        </div>
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Student</th>
              <th className="px-5 py-3.5 text-right">Balance Due</th>
              <th className="px-5 py-3.5 text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {outstanding.slice(0, 10).map((inv) => (
              <tr key={inv.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-5 py-3.5">
                  <div className="font-bold text-slate-900">{inv.studentName}</div>
                  <div className="text-[11px] text-slate-400 font-mono mt-0.5">{inv.studentNumber}</div>
                </td>
                <td className="px-5 py-3.5 text-right">
                  <GhsCurrency amount={inv.balance} className="font-mono font-bold text-slate-900 text-sm" />
                </td>
                <td className="px-5 py-3.5 text-right">
                  <Button
                    onClick={() => {
                      setRecordingFor(inv);
                      setForm({ amount: inv.balance, method: 'CASH', reference: '' });
                    }}
                    variant="default"
                    size="sm"
                  >
                    <span>Record Payment</span>
                  </Button>
                </td>
              </tr>
            ))}
            {outstanding.length === 0 && (
              <tr>
                <td colSpan={3} className="px-5 py-8 text-center text-slate-400">
                  No outstanding invoices found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Record Payment Modal */}
      {recordingFor && (
        <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              recordMutation.mutate(undefined);
            }}
            className="bg-white rounded-2xl border border-slate-200 shadow-2xl p-6 w-full max-w-md space-y-4"
          >
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="font-display font-bold text-slate-900 text-base">
                Record Payment — {recordingFor.studentName}
              </h3>
              <button onClick={() => setRecordingFor(null)} className="p-1 text-slate-400 hover:text-slate-600">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-700">Amount (GHS)</label>
              <input
                required
                type="number"
                step="0.01"
                value={form.amount}
                onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-bold text-slate-900"
              />
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-700">Payment Method</label>
              <select
                value={form.method}
                onChange={(e) => setForm((f) => ({ ...f, method: e.target.value }))}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold bg-white"
              >
                <option value="CASH">Cash</option>
                <option value="BANK">Bank Transfer</option>
                <option value="CHEQUE">Cheque</option>
                <option value="MOMO">Mobile Money</option>
              </select>
            </div>

            {form.method !== 'CASH' && (
              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-700">Transaction Reference</label>
                <input
                  value={form.reference}
                  onChange={(e) => setForm((f) => ({ ...f, reference: e.target.value }))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
                />
              </div>
            )}

            <div className="flex justify-end gap-2 pt-3">
              <Button type="button" onClick={() => setRecordingFor(null)} variant="outline">
                Cancel
              </Button>
              <Button type="submit" disabled={recordMutation.isPending} variant="default">
                {recordMutation.isPending ? 'Recording...' : 'Issue Receipt'}
              </Button>
            </div>
          </form>
        </div>
      )}

      {/* Payment History Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50 font-display font-bold text-slate-900 text-sm">
          Recent Payment Receipt Log
        </div>
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Receipt #</th>
              <th className="px-5 py-3.5">Student</th>
              <th className="px-5 py-3.5">Method</th>
              <th className="px-5 py-3.5 text-right">Amount Paid</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  Loading payment history...
                </td>
              </tr>
            ) : payments?.content.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  No payments recorded yet.
                </td>
              </tr>
            ) : (
              payments?.content.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5">
                    <Link
                      href={`/finance/payments/${p.id}`}
                      className="text-slate-900 font-mono font-bold hover:underline"
                    >
                      {p.receiptNumber}
                    </Link>
                  </td>
                  <td className="px-5 py-3.5 text-slate-900">
                    <span className="font-bold">{p.studentName}</span>{' '}
                    <span className="text-slate-400 font-mono">{p.studentNumber}</span>
                  </td>
                  <td className="px-5 py-3.5 text-slate-600 font-semibold">{p.method}</td>
                  <td className="px-5 py-3.5 text-right">
                    <GhsCurrency amount={p.amount} className="font-mono font-bold text-slate-900" />
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
