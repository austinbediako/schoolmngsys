import React from 'react';
import { useParams } from 'wouter';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { StatusBadge } from '@/components/status-badge';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { AcademicYear, Term } from '@/types';
import { Icon } from '@/components/icon';
import { toast } from 'sonner';

const NEXT_STATUS: Record<Term['status'], Term['status'] | null> = {
  PLANNED: 'ACTIVE',
  ACTIVE: 'CLOSED',
  CLOSED: null,
};

export default function AcademicTerms() {
  const { id: yearId } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ name: '', startDate: '', endDate: '' });

  const { data: years } = useQuery<{ content: AcademicYear[] }>({
    queryKey: ['academic-years'],
    queryFn: () => apiClient('/academic/years'),
  });
  const year = years?.content.find(y => y.id === yearId);

  const { data, isLoading } = useQuery<{ content: Term[] }>({
    queryKey: ['academic-terms', yearId],
    queryFn: () => apiClient(`/academic/years/${yearId}/terms`),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient(`/academic/years/${yearId}/terms`, { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Term created');
      queryClient.invalidateQueries({ queryKey: ['academic-terms', yearId] });
      setForm({ name: '', startDate: '', endDate: '' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  const advanceMutation = useMutation({
    mutationFn: (term: Term) => apiClient(`/academic/terms/${term.id}`, { method: 'PUT', body: JSON.stringify({ status: NEXT_STATUS[term.status] }) }),
    onSuccess: () => {
      toast.success('Term status updated');
      queryClient.invalidateQueries({ queryKey: ['academic-terms', yearId] });
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-8 max-w-5xl mx-auto space-y-6">
      <PageHeader
        title={`Terms — ${year?.name || yearId}`}
        description="Manage the three-term calendar for this academic year."
        breadcrumbs={[{ label: 'Academic Years', href: '/academic/years' }, { label: year?.name || '' }]}
      />

      <div className="flex justify-end">
        <button
          onClick={() => setShowForm(s => !s)}
          className="inline-flex items-center px-4 py-2 bg-primary text-white text-sm font-medium rounded-md hover:bg-primary/90"
        >
          <Icon name="add-01" className="text-base mr-2" /> Add Term
        </button>
      </div>

      {showForm && (
        <form
          onSubmit={e => { e.preventDefault(); createMutation.mutate(undefined); }}
          className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-4 items-end"
        >
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">Name</label>
            <input required value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              placeholder="Term 1" className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">Start Date</label>
            <input required type="date" value={form.startDate} onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">End Date</label>
            <input required type="date" value={form.endDate} onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
          </div>
          <button type="submit" disabled={createMutation.isPending} className="px-4 py-2 bg-primary text-white rounded-md text-sm font-medium hover:bg-primary/90 disabled:opacity-50">
            {createMutation.isPending ? 'Saving...' : 'Save'}
          </button>
        </form>
      )}

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-3 font-medium text-slate-500">Term</th>
              <th className="px-4 py-3 font-medium text-slate-500">Start Date</th>
              <th className="px-4 py-3 font-medium text-slate-500">End Date</th>
              <th className="px-4 py-3 font-medium text-slate-500">Status</th>
              <th className="px-4 py-3 font-medium text-slate-500"></th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {isLoading ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-500">Loading...</td></tr>
            ) : data?.content.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-500">No terms yet for this year.</td></tr>
            ) : (
              data?.content.map(t => (
                <tr key={t.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{t.name}</td>
                  <td className="px-4 py-3 text-slate-500">{t.startDate}</td>
                  <td className="px-4 py-3 text-slate-500">{t.endDate}</td>
                  <td className="px-4 py-3"><StatusBadge status={t.status} /></td>
                  <td className="px-4 py-3 text-right">
                    {NEXT_STATUS[t.status] && (
                      <button
                        onClick={() => advanceMutation.mutate(t)}
                        disabled={advanceMutation.isPending}
                        className="text-primary text-sm font-medium hover:underline disabled:opacity-50"
                      >
                        Advance to {NEXT_STATUS[t.status]} &rarr;
                      </button>
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
