import React from 'react';
import { PageHeader } from '@/components/page-header';
import { Button } from '@/components/ui/button';
import { Link } from 'wouter';
import { BookOpen, FileText, ArrowRight, PenSquare } from 'lucide-react';

export default function ResultsList() {
  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Results & Academic Assessment"
        description="Manage continuous assessments (SBA), terminal examinations, and report cards."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Results' },
        ]}
      >
        <Link href="/results/entry">
          <Button variant="default">
            <PenSquare className="w-4 h-4 text-emerald-400" />
            <span>Enter Assessment Scores</span>
          </Button>
        </Link>
      </PageHeader>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-2xs flex items-start gap-4">
          <div className="p-3 bg-slate-100 text-slate-800 rounded-xl border border-slate-200 shrink-0">
            <BookOpen className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="font-display font-bold text-slate-900 text-base">Score Entry Grid</h3>
            <p className="text-xs text-slate-500 font-medium leading-relaxed">
              Spreadsheet-like score entry interface for class tests, SBA, and term exams.
            </p>
            <Link
              href="/results/entry"
              className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-900 hover:text-indigo-600 mt-2 transition-colors"
            >
              <span>Go to Score Entry Grid</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-2xs flex items-start gap-4">
          <div className="p-3 bg-slate-100 text-slate-800 rounded-xl border border-slate-200 shrink-0">
            <FileText className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="font-display font-bold text-slate-900 text-base">Terminal Report Cards</h3>
            <p className="text-xs text-slate-500 font-medium leading-relaxed">
              Generate and compile official terminal report cards for student distribution.
            </p>
            <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-400 mt-2">
              <span>Term 1 Compilation Pending</span>
            </span>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50 font-display font-bold text-slate-900 text-sm">
          Recent Result Publications & Approvals
        </div>
        <div className="p-12 text-center text-xs font-medium text-slate-400">
          No published termly results yet for Term 1 (2024-25).
        </div>
      </div>
    </div>
  );
}
