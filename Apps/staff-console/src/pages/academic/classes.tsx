import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'wouter';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { Plus, ArrowRight } from 'lucide-react';

interface ClassItem {
  id: string;
  classLevelCode: string;
  classLevelName?: string;
  stream: string;
  capacity: number;
}

const CLASS_LEVEL_OPTIONS = [
  { code: 'N1', label: 'Nursery 1' },
  { code: 'N2', label: 'Nursery 2' },
  { code: 'KG1', label: 'Kindergarten 1' },
  { code: 'KG2', label: 'Kindergarten 2' },
  { code: 'B1', label: 'Basic 1 (Primary 1)' },
  { code: 'B2', label: 'Basic 2 (Primary 2)' },
  { code: 'B3', label: 'Basic 3 (Primary 3)' },
  { code: 'B4', label: 'Basic 4 (Primary 4)' },
  { code: 'B5', label: 'Basic 5 (Primary 5)' },
  { code: 'B6', label: 'Basic 6 (Primary 6)' },
  { code: 'B7', label: 'Basic 7 (JHS 1)' },
  { code: 'B8', label: 'Basic 8 (JHS 2)' },
  { code: 'B9', label: 'Basic 9 (JHS 3)' },
];

export default function AcademicClasses() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ classLevelCode: 'B1', stream: 'A', capacity: '35' });

  const { data, isLoading } = useQuery<{ content: ClassItem[] }>({
    queryKey: ['classes'],
    queryFn: () => apiClient('/classes'),
  });

  const createMutation = useMutation({
    mutationFn: () =>
      apiClient('/classes', {
        method: 'POST',
        body: JSON.stringify({
          classLevelCode: form.classLevelCode,
          stream: form.stream,
          capacity: parseInt(form.capacity, 10) || 35,
        }),
      }),
    onSuccess: () => {
      toast.success('Class created successfully');
      queryClient.invalidateQueries({ queryKey: ['classes'] });
      setForm({ classLevelCode: 'B1', stream: 'A', capacity: '35' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Class Levels & Streams"
        description="Configure school grade levels, class streams (e.g. Basic 4A), and capacity."
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
            createMutation.mutate();
          }}
          className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs grid grid-cols-1 md:grid-cols-4 gap-4 items-end"
        >
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Class Level</label>
            <select
              required
              value={form.classLevelCode}
              onChange={(e) => setForm((f) => ({ ...f, classLevelCode: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 bg-white focus:outline-none"
            >
              {CLASS_LEVEL_OPTIONS.map((opt) => (
                <option key={opt.code} value={opt.code}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Stream Code</label>
            <input
              required
              value={form.stream}
              onChange={(e) => setForm((f) => ({ ...f, stream: e.target.value }))}
              placeholder="e.g. A, B, Gold"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Max Capacity</label>
            <input
              required
              type="number"
              min={1}
              value={form.capacity}
              onChange={(e) => setForm((f) => ({ ...f, capacity: e.target.value }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold focus:outline-none"
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
              <th className="px-5 py-3.5">Stream</th>
              <th className="px-5 py-3.5">Capacity</th>
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
            ) : !data?.content || data.content.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  No classes configured yet.
                </td>
              </tr>
            ) : (
              data.content.map((c) => (
                <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">
                    {c.classLevelName || c.classLevelCode}
                  </td>
                  <td className="px-5 py-3.5 text-slate-600 font-semibold">{c.stream}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{c.capacity} Students</td>
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
