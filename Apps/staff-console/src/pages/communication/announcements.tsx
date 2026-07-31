import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Announcement } from '@/types';
import { toast } from 'sonner';
import { Plus, Megaphone, Send, Clock, UserCheck } from 'lucide-react';

const commsTabs = [
  { label: 'School Announcements', href: '/communication/announcements' },
  { label: 'Message Templates', href: '/communication/templates' },
];

export function CommsTabs() {
  return <SectionTabs tabs={commsTabs} />;
}

export default function Announcements() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ title: '', body: '', scope: 'SCHOOL' as Announcement['scope'] });

  const { data, isLoading } = useQuery<{ content: Announcement[] }>({
    queryKey: ['announcements'],
    queryFn: () => apiClient('/communication/announcements'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/communication/announcements', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Announcement published');
      queryClient.invalidateQueries({ queryKey: ['announcements'] });
      setForm({ title: '', body: '', scope: 'SCHOOL' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-5xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="School Communication & Broadcasts"
        description="Publish official school announcements and broadcast notices to staff, guardians, or students."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Communication', href: '/communication/announcements' },
          { label: 'Announcements' },
        ]}
      />
      <CommsTabs />

      <div className="flex justify-end">
        <Button onClick={() => setShowForm((s) => !s)} variant="default">
          <Plus className="w-4 h-4 text-emerald-400" />
          <span>New Announcement</span>
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate(undefined);
          }}
          className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs space-y-4"
        >
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Announcement Title</label>
            <input
              required
              value={form.title}
              onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
              placeholder="e.g. End of Term Mid-Quarter PTA Assembly"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Message Content</label>
            <textarea
              required
              rows={3}
              value={form.body}
              onChange={(e) => setForm((f) => ({ ...f, body: e.target.value }))}
              placeholder="Type official broadcast message here..."
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-medium"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Target Audience Scope</label>
            <select
              value={form.scope}
              onChange={(e) => setForm((f) => ({ ...f, scope: e.target.value as Announcement['scope'] }))}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold bg-white"
            >
              <option value="SCHOOL">Whole School</option>
              <option value="DEPARTMENT">Department</option>
              <option value="CLASS">Specific Class</option>
            </select>
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" disabled={createMutation.isPending} variant="default">
              <Send className="w-3.5 h-3.5 text-emerald-400" />
              <span>{createMutation.isPending ? 'Publishing...' : 'Publish Announcement'}</span>
            </Button>
          </div>
        </form>
      )}

      <div className="space-y-4">
        {isLoading ? (
          <div className="text-xs font-semibold text-slate-400 p-8 text-center bg-white rounded-2xl border border-slate-200">
            Loading announcements...
          </div>
        ) : data?.content.length === 0 ? (
          <div className="bg-white rounded-2xl border border-slate-200/90 p-12 text-center text-xs font-semibold text-slate-400">
            No active announcements published yet.
          </div>
        ) : (
          data?.content.map((a) => (
            <div
              key={a.id}
              className="bg-white border border-slate-200/90 rounded-2xl shadow-2xs p-6 space-y-3 hover:border-slate-300 transition-colors"
            >
              <div className="flex justify-between items-start">
                <h3 className="font-display font-bold text-slate-900 text-base">{a.title}</h3>
                <span className="text-[11px] font-bold text-slate-700 bg-slate-100 px-2.5 py-0.5 rounded-full border border-slate-200 uppercase tracking-wider">
                  {a.scope}
                </span>
              </div>

              <p className="text-xs text-slate-600 font-medium leading-relaxed">{a.body}</p>

              <div className="text-[11px] font-mono text-slate-400 pt-2 border-t border-slate-100 flex items-center gap-3">
                <span>By {a.createdBy}</span>
                <span>•</span>
                <span>{new Date(a.createdAt).toLocaleDateString('en-GB')}</span>
                {typeof a.readCount === 'number' && (
                  <>
                    <span>•</span>
                    <span className="font-semibold text-slate-600">{a.readCount} verified reads</span>
                  </>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
