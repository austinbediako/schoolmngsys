import React from 'react';
import { useParams } from 'wouter';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Class, Subject, Term } from '@/types';
import { Icon } from '@/components/icon';
import { toast } from 'sonner';

type Offering = { id: string; subjectId: string; termId: string; teacherId: string; subjectName?: string; teacherName?: string };

export default function ClassSubjects() {
  const { id: classId } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [form, setForm] = React.useState({ subjectId: '', termId: '', teacherId: '' });

  const { data: classes } = useQuery<{ content: Class[] }>({
    queryKey: ['academic-classes'],
    queryFn: () => apiClient('/academic/classes'),
  });
  const cls = classes?.content.find(c => c.id === classId);

  const { data: subjects } = useQuery<{ content: Subject[] }>({
    queryKey: ['academic-subjects'],
    queryFn: () => apiClient('/academic/subjects'),
  });

  const { data: terms } = useQuery<{ content: Term[] }>({
    queryKey: ['academic-terms', 'y2'],
    queryFn: () => apiClient('/academic/years/y2/terms'),
  });

  const { data: offerings, isLoading } = useQuery<{ content: Offering[] }>({
    queryKey: ['class-subject-offerings', classId],
    queryFn: () => apiClient(`/academic/classes/${classId}/subject-offerings`),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient(`/academic/classes/${classId}/subject-offerings`, { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Subject assigned');
      queryClient.invalidateQueries({ queryKey: ['class-subject-offerings', classId] });
      setForm({ subjectId: '', termId: '', teacherId: '' });
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-8 max-w-4xl mx-auto space-y-6">
      <PageHeader
        title={`Subjects — ${cls ? `${cls.level} ${cls.stream}` : classId}`}
        description="Assign subjects and teachers to this class for a term."
        breadcrumbs={[{ label: 'Classes', href: '/academic/classes' }, { label: cls ? `${cls.level} ${cls.stream}` : '' }]}
      />

      <form
        onSubmit={e => { e.preventDefault(); createMutation.mutate(undefined); }}
        className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-4 items-end"
      >
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Subject</label>
          <select required value={form.subjectId} onChange={e => setForm(f => ({ ...f, subjectId: e.target.value }))}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white">
            <option value="">Select...</option>
            {subjects?.content.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Term</label>
          <select required value={form.termId} onChange={e => setForm(f => ({ ...f, termId: e.target.value }))}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white">
            <option value="">Select...</option>
            {terms?.content.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Teacher ID</label>
          <input required value={form.teacherId} onChange={e => setForm(f => ({ ...f, teacherId: e.target.value }))}
            placeholder="u2" className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
        </div>
        <button type="submit" disabled={createMutation.isPending} className="px-4 py-2 bg-primary text-white rounded-md text-sm font-medium hover:bg-primary/90 disabled:opacity-50 inline-flex items-center justify-center">
          <Icon name="add-01" className="text-base mr-2" /> Assign
        </button>
      </form>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-3 font-medium text-slate-500">Subject</th>
              <th className="px-4 py-3 font-medium text-slate-500">Term</th>
              <th className="px-4 py-3 font-medium text-slate-500">Teacher</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {isLoading ? (
              <tr><td colSpan={3} className="px-4 py-8 text-center text-slate-500">Loading...</td></tr>
            ) : offerings?.content.length === 0 ? (
              <tr><td colSpan={3} className="px-4 py-8 text-center text-slate-500">No subjects assigned yet.</td></tr>
            ) : (
              offerings?.content.map(o => (
                <tr key={o.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{o.subjectName}</td>
                  <td className="px-4 py-3 text-slate-500">{terms?.content.find(t => t.id === o.termId)?.name || o.termId}</td>
                  <td className="px-4 py-3 text-slate-500">{o.teacherName}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
