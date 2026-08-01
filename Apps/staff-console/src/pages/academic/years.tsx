import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { StatusBadge } from '@/components/status-badge';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { Plus, Calendar, CheckCircle, RefreshCw } from 'lucide-react';

interface AcademicYearItem {
  id: string;
  label: string;
  startDate: string;
  endDate: string;
  status: string;
}

export default function AcademicYears() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({
    label: '2025/2026 Academic Year',
    startDate: '2025-09-01',
    endDate: '2026-07-31',
  });

  const { data, isLoading } = useQuery<{ content: AcademicYearItem[] }>({
    queryKey: ['academic-years'],
    queryFn: () => apiClient('/academic-years'),
  });

  const createMutation = useMutation({
    mutationFn: () => {
      const startYear = new Date(form.startDate).getFullYear() || 2025;
      const endYear = new Date(form.endDate).getFullYear() || 2026;

      const payload = {
        label: form.label,
        startDate: form.startDate,
        endDate: form.endDate,
        terms: [
          {
            termNumber: 1,
            startDate: `${startYear}-09-01`,
            endDate: `${startYear}-12-15`,
            expectedSchoolDays: 70,
          },
          {
            termNumber: 2,
            startDate: `${endYear}-01-10`,
            endDate: `${endYear}-04-15`,
            expectedSchoolDays: 65,
          },
          {
            termNumber: 3,
            startDate: `${endYear}-05-02`,
            endDate: `${endYear}-07-31`,
            expectedSchoolDays: 60,
          },
        ],
      };

      return apiClient('/academic-years', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    },
    onSuccess: () => {
      toast.success('Academic Year created with 3 standard terms');
      queryClient.invalidateQueries({ queryKey: ['academic-years'] });
      setForm({ label: '', startDate: '', endDate: '' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  const activateMutation = useMutation({
    mutationFn: (id: string) =>
      apiClient(`/academic-years/${id}/activate`, {
        method: 'POST',
      }),
    onSuccess: () => {
      toast.success('Academic Year activated');
      queryClient.invalidateQueries({ queryKey: ['academic-years'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-head'] });
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
            createMutation.mutate();
          }}
          className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs grid grid-cols-1 md:grid-cols-4 gap-4 items-end"
        >
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Academic Year Title</label>
            <input
              required
              value={form.label}
              onChange={(e) => setForm((f) => ({ ...f, label: e.target.value }))}
              placeholder="e.g. 2025/2026 Academic Year"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Session Start Date</label>
            <input
              required
              type="date"
              value={form.startDate}
              onChange={(e) => setForm((f) => ({ ...f, startDate: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Session End Date</label>
            <input
              required
              type="date"
              value={form.endDate}
              onChange={(e) => setForm((f) => ({ ...f, endDate: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold focus:outline-none"
            />
          </div>

          <Button type="submit" disabled={createMutation.isPending} variant="default">
            {createMutation.isPending ? 'Saving...' : 'Save Academic Year'}
          </Button>
        </form>
      )}

      {/* Academic Years Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Academic Year Title</th>
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
            ) : !data?.content || data.content.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  No academic years configured yet.
                </td>
              </tr>
            ) : (
              data.content.map((y) => (
                <tr key={y.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">{y.label}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{y.startDate}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{y.endDate}</td>
                  <td className="px-5 py-3.5">
                    <StatusBadge status={y.status} />
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    {y.status !== 'ACTIVE' ? (
                      <button
                        type="button"
                        onClick={() => activateMutation.mutate(y.id)}
                        disabled={activateMutation.isPending}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-800 font-bold text-xs transition-colors"
                      >
                        <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
                        <span>Activate</span>
                      </button>
                    ) : (
                      <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
                        Current Session
                      </span>
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
