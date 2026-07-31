import React, { useEffect, useState } from 'react';
import { useWard } from '../contexts/WardContext';
import { fetchInvoices, fetchReceipts } from '../lib/api';
import { FeeInvoice, PaymentReceipt } from '../lib/types';
import { CreditCard, Receipt, Download } from 'lucide-react';
import { Button } from '../components/ui/button';

export const FinancePage: React.FC = () => {
  const { selectedWard } = useWard();
  const [invoices, setInvoices] = useState<FeeInvoice[]>([]);
  const [receipts, setReceipts] = useState<PaymentReceipt[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedWard) return;
    setLoading(true);
    Promise.all([
      fetchInvoices(selectedWard.id),
      fetchReceipts(selectedWard.id)
    ])
      .then(([invData, pmtData]) => {
        setInvoices(invData);
        setReceipts(pmtData);
      })
      .finally(() => setLoading(false));
  }, [selectedWard]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-slate-900"></div>
      </div>
    );
  }

  const activeInvoice = invoices[0];

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="bg-white border border-slate-200 rounded-3xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-xs">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <CreditCard className="w-5 h-5 text-amber-600" />
            School Fees & Billing Statements
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Official term billing and receipt history for {selectedWard?.firstName} {selectedWard?.lastName}
          </p>
        </div>
      </div>

      {/* Balance Summary Card */}
      {activeInvoice && (
        <div className="bg-white border border-slate-200 rounded-3xl p-6 shadow-xs relative overflow-hidden space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <span className="text-[10px] font-bold uppercase tracking-wider text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
                {activeInvoice.termLabel}
              </span>
              <h3 className="text-2xl font-extrabold text-slate-900 mt-2">
                Balance Due: <span className="text-amber-600">GHS {activeInvoice.balance.toFixed(2)}</span>
              </h3>
              <p className="text-xs text-slate-500 mt-1">
                Total Invoiced: GHS {activeInvoice.totalAmount.toFixed(2)} · Amount Paid: GHS {activeInvoice.paidAmount.toFixed(2)}
              </p>
            </div>

            <div className="flex items-center gap-3">
              <Button onClick={() => window.print()} variant="default" size="lg">
                <Download className="w-4 h-4 mr-1" /> Download Statement
              </Button>
            </div>
          </div>

          {/* Itemized Fee Lines */}
          <div className="pt-5 border-t border-slate-100 space-y-2">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">Itemized Term Schedule</h4>
            {activeInvoice.items.map((item, idx) => (
              <div key={idx} className="flex items-center justify-between text-xs py-2 border-b border-slate-100 last:border-0">
                <span className="text-slate-700 font-medium">{item.description}</span>
                <span className="font-bold text-slate-900 font-mono">GHS {item.amount.toFixed(2)}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Payment Receipts History */}
      <div className="bg-white border border-slate-200 rounded-3xl overflow-hidden shadow-xs">
        <div className="px-5 py-4 border-b border-slate-200 bg-slate-50/50 flex items-center justify-between">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-2">
            <Receipt className="w-4 h-4 text-emerald-600" />
            Official Payment Receipts
          </h3>
        </div>

        <div className="divide-y divide-slate-100">
          {receipts.map((rcp) => (
            <div key={rcp.id} className="p-4 flex items-center justify-between hover:bg-slate-50 transition-all text-xs">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-bold text-slate-900 font-mono text-sm">{rcp.receiptNumber}</span>
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                    {rcp.channel}
                  </span>
                </div>
                <p className="text-slate-500 text-[11px] mt-0.5">Date: {rcp.paymentDate} · Ref: {rcp.reference || 'N/A'}</p>
              </div>

              <div className="text-right">
                <p className="text-sm font-extrabold text-emerald-600 font-mono">GHS {rcp.amount.toFixed(2)}</p>
                <span className="text-[10px] text-slate-400 uppercase font-bold">Immutable Receipt</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
