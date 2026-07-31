import React from 'react';
import { PageHeader } from '@/components/page-header';
import { GhsCurrency } from '@/components/ghs-currency';
import { FinanceTabs } from '@/pages/finance/schedules';
import { Button } from '@/components/ui/button';
import { Link } from 'wouter';
import { Wallet, CheckCircle2, AlertCircle, Download, PieChart, ShieldCheck, ArrowRight, FileCheck } from 'lucide-react';

export default function FinanceReports() {
  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Financial Intelligence & Reports"
        description="Analysis of termly billing revenue, payment collections, and outstanding fee arrears."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Finance', href: '/finance/reports' },
          { label: 'Reports' },
        ]}
      >
        <Button variant="outline">
          <Download className="w-3.5 h-3.5 text-slate-500" />
          <span>Export Financial Summary</span>
        </Button>
      </PageHeader>

      <FinanceTabs />

      {/* Clean Financial Metric Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-2xs space-y-3">
          <div className="flex items-center justify-between">
            <div className="p-2.5 rounded-xl bg-slate-100 text-slate-700 border border-slate-200">
              <Wallet className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-bold text-slate-700 bg-slate-100 px-2.5 py-0.5 rounded-full border border-slate-200">
              Billed Revenue
            </span>
          </div>
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Total Expected</span>
            <div className="font-display text-2xl sm:text-3xl font-bold text-slate-900 mt-1">
              <GhsCurrency amount="150000.00" />
            </div>
            <p className="text-xs text-slate-500 font-medium mt-1">Total invoiced fees for Term 1</p>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-2xs space-y-3">
          <div className="flex items-center justify-between">
            <div className="p-2.5 rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-200">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
              70% Collected
            </span>
          </div>
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Total Collected</span>
            <div className="font-display text-2xl sm:text-3xl font-bold text-slate-900 mt-1">
              <GhsCurrency amount="105000.00" />
            </div>
            <p className="text-xs text-slate-500 font-medium mt-1">Bank deposits & cash receipts confirmed</p>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-2xs space-y-3">
          <div className="flex items-center justify-between">
            <div className="p-2.5 rounded-xl bg-rose-50 text-rose-700 border border-rose-200">
              <AlertCircle className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-bold text-rose-700 bg-rose-50 px-2.5 py-0.5 rounded-full border border-rose-200">
              30% Arrears
            </span>
          </div>
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Total Outstanding</span>
            <div className="font-display text-2xl sm:text-3xl font-bold text-slate-900 mt-1">
              <GhsCurrency amount="45000.00" />
            </div>
            <p className="text-xs text-slate-500 font-medium mt-1">Outstanding balances pending collection</p>
          </div>
        </div>
      </div>

      {/* Revenue Breakdown & Financial Governance */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Class Level Collection Table */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200/90 p-6 shadow-2xs space-y-6">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div>
              <h3 className="font-display font-bold text-slate-900 text-base">Class Level Collection Breakdown</h3>
              <p className="text-xs text-slate-500">Fee collection progress comparison across grade levels</p>
            </div>
            <div className="p-2 rounded-xl bg-slate-100 text-slate-600 border border-slate-200">
              <PieChart className="w-4 h-4" />
            </div>
          </div>

          <div className="space-y-4 divide-y divide-slate-100">
            <LevelCollectionRow level="Nursery & KG" expected="25000.00" collected="20000.00" rate={80} />
            <LevelCollectionRow level="Primary 1 – 3" expected="45000.00" collected="33750.00" rate={75} />
            <LevelCollectionRow level="Primary 4 – 6" expected="40000.00" collected="26000.00" rate={65} />
            <LevelCollectionRow level="JHS 1 – 3" expected="40000.00" collected="25250.00" rate={63} />
          </div>
        </div>

        {/* Clean Executive Governance Card */}
        <div className="bg-white rounded-2xl border border-slate-200/90 p-6 shadow-2xs space-y-5 flex flex-col justify-between">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Financial Governance</span>
              <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                <ShieldCheck className="w-3 h-3" />
                <span>Audited</span>
              </span>
            </div>

            <h3 className="font-display text-base font-bold text-slate-900 leading-snug">
              Immutable Financial Ledger & Audit Trail
            </h3>

            <p className="text-xs text-slate-500 font-medium leading-relaxed">
              All payment receipts, fee schedule revisions, and billing adjustments are strictly append-only and audited per GES financial governance rules.
            </p>

            <div className="bg-slate-50 border border-slate-200 rounded-xl p-3.5 space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-600">Ledger Compliance</span>
                <span className="font-mono font-bold text-emerald-700">100% Valid</span>
              </div>
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-600">Last Audit Check</span>
                <span className="font-mono text-slate-500">Today, 08:30 GMT</span>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-100 space-y-2">
            <Link href="/finance/schedules" className="w-full">
              <Button variant="secondary" className="w-full justify-between">
                <span>View Fee Schedules</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Button>
            </Link>

            <Link href="/finance/billing" className="w-full">
              <Button variant="outline" className="w-full justify-between">
                <span>Run Termly Billing</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

function LevelCollectionRow({ level, expected, collected, rate }: { level: string; expected: string; collected: string; rate: number }) {
  return (
    <div className="pt-3 space-y-2">
      <div className="flex items-center justify-between text-xs">
        <span className="font-bold text-slate-900">{level}</span>
        <div className="flex items-center gap-3 text-slate-600 font-semibold">
          <span>Collected: <GhsCurrency amount={collected} /></span>
          <span className="text-slate-300">|</span>
          <span className="text-slate-500">Expected: <GhsCurrency amount={expected} /></span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <div className="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
          <div
            className="h-full bg-slate-900 rounded-full transition-all duration-500"
            style={{ width: `${rate}%` }}
          />
        </div>
        <span className="text-xs font-mono font-bold text-slate-900 w-10 text-right">{rate}%</span>
      </div>
    </div>
  );
}
