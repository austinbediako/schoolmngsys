import React from 'react';
import { useLocation, useParams } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Student } from '@/types';
import { toast } from 'sonner';
import { Icon } from '@/components/icon';

export default function ExitStudent() {
  const { id } = useParams<{ id: string }>();
  const [, setLocation] = useLocation();
  const [form, setForm] = React.useState({ type: 'WITHDRAWAL', reason: '', date: new Date().toISOString().slice(0, 10) });
  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [summary, setSummary] = React.useState<{ type: string; reason: string; date: string } | null>(null);

  const { data: student } = useQuery<Student>({
    queryKey: ['student', id],
    queryFn: () => apiClient(`/students/${id}`)
  });

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await apiClient(`/students/${id}/exit`, { method: 'POST', body: JSON.stringify(form) });
      toast.success('Student exit recorded');
      setSummary({ type: form.type, reason: form.reason, date: form.date });
    } catch (e) {
      handleApiError(e);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (summary && student) {
    return (
      <div className="p-8 max-w-xl mx-auto">
        <div className="flex justify-end mb-4 print:hidden">
          <button onClick={() => window.print()} className="inline-flex items-center px-4 py-2 bg-primary text-white text-sm font-medium rounded-md hover:bg-primary/90">
            <Icon name="printer" className="text-base mr-2" /> Print Transfer Summary
          </button>
        </div>
        <div className="bg-white border border-slate-200 rounded-xl shadow-sm p-8 print:shadow-none print:border-0">
          <div className="text-center border-b border-dashed border-slate-300 pb-6 mb-6">
            <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white font-bold mx-auto mb-2">U</div>
            <div className="font-semibold text-slate-900 text-lg">Unibridge Basic School</div>
            <div className="text-xs text-slate-500 mt-1">Transfer / Withdrawal Summary</div>
          </div>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between"><dt className="text-slate-500">Student</dt><dd className="text-slate-900 font-medium">{student.firstName} {student.lastName} ({student.studentNumber})</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Exit Type</dt><dd className="text-slate-900">{summary.type === 'WITHDRAWAL' ? 'Withdrawal' : 'Transfer-Out'}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Date</dt><dd className="text-slate-900">{summary.date}</dd></div>
            <div className="flex justify-between"><dt className="text-slate-500">Reason</dt><dd className="text-slate-900">{summary.reason}</dd></div>
          </dl>
        </div>
        <div className="mt-4 text-center print:hidden">
          <button onClick={() => setLocation(`/students/${id}`)} className="text-sm text-primary hover:underline">Back to student profile</button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <PageHeader
        title="Record Student Exit"
        breadcrumbs={[
          { label: 'Students', href: '/students' },
          { label: student ? `${student.firstName} ${student.lastName}` : '', href: `/students/${id}` },
          { label: 'Exit' }
        ]}
      />

      <form onSubmit={onSubmit} className="space-y-6 bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700">Exit Type <span className="text-red-500">*</span></label>
          <select required value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white">
            <option value="WITHDRAWAL">Withdrawal</option>
            <option value="TRANSFER">Transfer-Out</option>
          </select>
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700">Date <span className="text-red-500">*</span></label>
          <input required type="date" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700">Reason <span className="text-red-500">*</span></label>
          <textarea required rows={3} value={form.reason} onChange={e => setForm(f => ({ ...f, reason: e.target.value }))}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
        </div>
        <div className="pt-4 border-t border-slate-200 flex justify-end gap-3">
          <button type="button" onClick={() => setLocation(`/students/${id}`)} className="px-4 py-2 border border-slate-300 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-50">Cancel</button>
          <button type="submit" disabled={isSubmitting} className="px-4 py-2 bg-rose-600 text-white rounded-md text-sm font-medium hover:bg-rose-700 disabled:opacity-50">
            {isSubmitting ? 'Recording...' : 'Record Exit'}
          </button>
        </div>
      </form>
    </div>
  );
}
