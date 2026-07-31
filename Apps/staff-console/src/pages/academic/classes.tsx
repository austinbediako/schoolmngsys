import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'wouter';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Class } from '@/types';
import { toast } from 'sonner';
import { Plus, ArrowRight } from 'lucide-react';

export default function AcademicClasses() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ level: '', stream: '', capacity: '30' });

  const { data, isLoading } = useQuery<{ content: Class[] }>({
    queryKey: ['academic-classes'],
    queryFn: () => apiClient('/academic/classes'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/academic/classes', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Class created');
      queryClient.invalidateQueries({ queryKey: ['academic-classes'] });
      setForm({ level: '', stream: '', capacity: '30' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Class Levels & Streams"
        description="Configure school grade levels, class streams (e.g. Primary 4A), and teacher assignments."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Academic', href: '/academic/years' },
          { label: 'Classes' },
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
          <span>Add Class Level</span>
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
            <label className="text-xs font-semibold text-slate-700">Level</label>
            <input
              required
              value={form.level}
              onChange={(e) => setForm((f) => ({ ...f, level: e.target.value }))}
              placeholder="Primary 4"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Stream</label>
            <input
              required
              value={form.stream}
              onChange={(e) => setForm((f) => ({ ...f, stream: e.target.value }))}
              placeholder="A"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Student Capacity</label>
            <input
              required
              type="number"
              min={1}
              value={form.capacity}
              onChange={(e) => setForm((f) => ({ ...f, capacity: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <Button type="submit" disabled={createMutation.isPending} variant="default">
            {createMutation.isPending ? 'Saving...' : 'Save Class'}
          </Button>
        </form>
      )}

      {/* Class Levels Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Class Name</th>
              <th className="px-5 py-3.5">Max Capacity</th>
              <th className="px-5 py-3.5">Assigned Class Teacher</th>
              <th className="px-5 py-3.5 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  Loading classes...
                </td>
              </tr>
            ) : data?.content.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  No classes configured yet.
                </td>
              </tr>
            ) : (
              data?.content.map((c) => (
                <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">
                    {c.level} {c.stream}
                  </td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{c.capacity} Students</td>
                  <td className="px-5 py-3.5 text-slate-600">
                    {c.classTeacherId ? (
                      <span className="text-emerald-700 font-semibold">Assigned</span>
                    ) : (
                      <span className="text-slate-400 font-medium">Unassigned</span>
                    )}
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <Link
                      href={`/academic/classes/${c.id}/subjects`}
                      className="inline-flex items-center gap-1 text-xs font-bold text-slate-900 hover:text-indigo-600 transition-colors"
                    >
                      <span>Manage Subjects</span>
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
