import React from 'react';
import { useParams } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { GhsCurrency } from '@/components/ghs-currency';
import { Icon } from '@/components/icon';

type PaymentReceipt = {
  id: string; receiptNumber: string; studentName: string; studentNumber: string;
  amount: string; method: string; reference?: string; createdAt: string; createdBy: string;
};

export default function PaymentReceiptPage() {
  const { id } = useParams<{ id: string }>();

  const { data: payment, isLoading } = useQuery<PaymentReceipt>({
    queryKey: ['payment', id],
    queryFn: () => apiClient(`/finance/payments/${id}`),
  });

  if (isLoading) return <div className="p-8">Loading...</div>;
  if (!payment) return <div className="p-8">Receipt not found.</div>;

  return (
    <div className="p-8 max-w-xl mx-auto">
      <div className="flex justify-end mb-4 print:hidden">
        <button
          onClick={() => window.print()}
          className="inline-flex items-center px-4 py-2 bg-primary text-white text-sm font-medium rounded-md hover:bg-primary/90"
        >
          <Icon name="printer" className="text-base mr-2" /> Print Receipt
        </button>
      </div>

      <div className="bg-white border border-slate-200 rounded-xl shadow-sm p-8 print:shadow-none print:border-0">
        <div className="text-center border-b border-dashed border-slate-300 pb-6 mb-6">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white font-bold mx-auto mb-2">U</div>
          <div className="font-semibold text-slate-900 text-lg">Unibridge Basic School</div>
          <div className="text-xs text-slate-500 mt-1">Official Payment Receipt — this record cannot be edited or deleted</div>
        </div>

        <div className="flex justify-between items-baseline mb-6">
          <span className="text-sm text-slate-500">Receipt No.</span>
          <span className="font-mono font-semibold text-slate-900">{payment.receiptNumber}</span>
        </div>

        <dl className="space-y-3 text-sm">
          <div className="flex justify-between">
            <dt className="text-slate-500">Student</dt>
            <dd className="text-slate-900 font-medium">{payment.studentName} ({payment.studentNumber})</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-slate-500">Date</dt>
            <dd className="text-slate-900">{new Date(payment.createdAt).toLocaleString('en-GB')}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-slate-500">Method</dt>
            <dd className="text-slate-900">{payment.method}{payment.reference ? ` (${payment.reference})` : ''}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-slate-500">Recorded By</dt>
            <dd className="text-slate-900">{payment.createdBy}</dd>
          </div>
        </dl>

        <div className="flex justify-between items-center pt-6 mt-6 border-t border-dashed border-slate-300">
          <span className="font-semibold text-slate-900">Amount Paid</span>
          <span className="text-2xl font-bold text-slate-900"><GhsCurrency amount={payment.amount} /></span>
        </div>
      </div>
    </div>
  );
}
