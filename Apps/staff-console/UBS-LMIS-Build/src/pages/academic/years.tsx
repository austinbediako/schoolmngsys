import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'wouter';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { StatusBadge } from '@/components/status-badge';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { AcademicYear } from '@/types';
import { toast } from 'sonner';
import { Plus, Calendar, ArrowRight } from 'lucide-react';

export default function AcademicYears() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ name: '', startDate: '', endDate: '' });

  const { data, isLoading } = useQuery<{ content: AcademicYear[] }>({
    queryKey: ['academic-years'],
    queryFn: () => apiClient('/academic/years'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/academic/years', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Academic year created');
      queryClient.invalidateQueries({ queryKey: ['academic-years'] });
      setForm({ name: '', startDate: '', endDate: '' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Academic Calendar & Years"
        description="Configure school calendar years, active terms, and academic session boundaries."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Academic', href: '/academic/years' },
          { label: 'Years & Terms' },
        ]}
      />
      <SectionTabs
        tabs={[
          { label: 'Years & Terms', href: '/academic/years' },
          { label: 'Class Levels', href: '/academic/classes' },
        ]}
      />

      <div className="flex justify-end">
        <Button onClick={() => setShowForm((s) => !s)} variant="default">
          <Plus className="w-4 h-4 text-emerald-400" />
          <span>Add Academic Year</span>
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
            <label className="text-xs font-semibold text-slate-700">Academic Year Name</label>
            <input
              required
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              placeholder="e.g. 2025-26"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Start Date</label>
            <input
              required
              type="date"
              value={form.startDate}
              onChange={(e) => setForm((f) => ({ ...f, startDate: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">End Date</label>
            <input
              required
              type="date"
              value={form.endDate}
              onChange={(e) => setForm((f) => ({ ...f, endDate: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <Button type="submit" disabled={createMutation.isPending} variant="default">
            {createMutation.isPending ? 'Saving...' : 'Save Year'}
          </Button>
        </form>
      )}

      {/* Academic Years Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Academic Year</th>
              <th className="px-5 py-3.5">Start Date</th>
              <th className="px-5 py-3.5">End Date</th>
              <th className="px-5 py-3.5">Status</th>
              <th className="px-5 py-3.5 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  Loading academic years...
                </td>
              </tr>
            ) : data?.content.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  No academic years configured yet.
                </td>
              </tr>
            ) : (
              data?.content.map((y) => (
                <tr key={y.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">{y.name}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{y.startDate}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{y.endDate}</td>
                  <td className="px-5 py-3.5">
                    <StatusBadge status={y.status} />
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <Link
                      href={`/academic/years/${y.id}/terms`}
                      className="inline-flex items-center gap-1 text-xs font-bold text-slate-900 hover:text-indigo-600 transition-colors"
                    >
                      <span>Manage Terms</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
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
