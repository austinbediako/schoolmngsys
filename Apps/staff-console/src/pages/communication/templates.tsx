import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { MessageTemplate } from '@/types';
import { toast } from 'sonner';
import { CommsTabs } from '@/pages/communication/announcements';
import { Plus, Save, FileText } from 'lucide-react';

export default function MessageTemplates() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({ name: '', subject: '', body: '' });

  const { data, isLoading } = useQuery<{ content: MessageTemplate[] }>({
    queryKey: ['message-templates'],
    queryFn: () => apiClient('/communication/templates'),
  });

  const createMutation = useMutation({
    mutationFn: () => apiClient('/communication/templates', { method: 'POST', body: JSON.stringify(form) }),
    onSuccess: () => {
      toast.success('Template created');
      queryClient.invalidateQueries({ queryKey: ['message-templates'] });
      setForm({ name: '', subject: '', body: '' });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-5xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="SMS & Email Notification Templates"
        description="Configure standardized notification templates for fee alerts, attendance warnings, and academic notices."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Communication', href: '/communication/announcements' },
          { label: 'Templates' },
        ]}
      />
      <CommsTabs />

      <div className="flex justify-end">
        <Button onClick={() => setShowForm((s) => !s)} variant="default">
          <Plus className="w-4 h-4 text-emerald-400" />
          <span>New Template</span>
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
            <label className="text-xs font-semibold text-slate-700">Template Title</label>
            <input
              required
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              placeholder="e.g. Termly Fee Arrears Reminder"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Email/SMS Subject Header</label>
            <input
              required
              value={form.subject}
              onChange={(e) => setForm((f) => ({ ...f, subject: e.target.value }))}
              placeholder="e.g. Important Notice: Outstanding Fee Balance"
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Template Body (Use {"{{placeholders}}"})</label>
            <textarea
              required
              rows={3}
              value={form.body}
              onChange={(e) => setForm((f) => ({ ...f, body: e.target.value }))}
              placeholder="Dear {{guardian_name}}, your ward {{student_name}} has an outstanding balance of {{balance}}..."
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-medium font-mono"
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" disabled={createMutation.isPending} variant="default">
              <Save className="w-3.5 h-3.5 text-emerald-400" />
              <span>{createMutation.isPending ? 'Saving...' : 'Save Template'}</span>
            </Button>
          </div>
        </form>
      )}

      <div className="space-y-4">
        {isLoading ? (
          <div className="text-xs font-semibold text-slate-400 p-8 text-center bg-white rounded-2xl border border-slate-200">
            Loading message templates...
          </div>
        ) : data?.content.length === 0 ? (
          <div className="bg-white rounded-2xl border border-slate-200/90 p-12 text-center text-xs font-semibold text-slate-400">
            No message templates created yet.
          </div>
        ) : (
          data?.content.map((t) => (
            <div
              key={t.id}
              className="bg-white border border-slate-200/90 rounded-2xl shadow-2xs p-6 space-y-2 hover:border-slate-300 transition-colors"
            >
              <h3 className="font-display font-bold text-slate-900 text-base">{t.name}</h3>
              <div className="text-xs text-slate-500 font-semibold">Subject Header: <span className="text-slate-800">{t.subject}</span></div>
              <p className="text-xs text-slate-700 font-mono bg-slate-50 p-3 rounded-xl border border-slate-200/80 mt-2 whitespace-pre-wrap">
                {t.body}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
