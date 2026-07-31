import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { apiClient } from '@/lib/api-client';
import { AuditLog, PaginatedResponse } from '@/types';

export default function AuditLogPage() {
  const [filters, setFilters] = React.useState({ from: '', to: '', actor: '', entity: '' });
  const [page, setPage] = React.useState(0);

  const query = new URLSearchParams();
  query.set('page', String(page));
  query.set('size', '20');
  if (filters.from) query.set('from', filters.from);
  if (filters.to) query.set('to', filters.to);
  if (filters.actor) query.set('actor', filters.actor);
  if (filters.entity) query.set('entity', filters.entity);

  const { data, isLoading } = useQuery<PaginatedResponse<AuditLog>>({
    queryKey: ['audit-log', filters, page],
    queryFn: () => apiClient(`/audit-log?${query.toString()}`),
  });

  const entities = ['Student', 'Guardian', 'Enrollment', 'AcademicYear', 'Term', 'Class', 'SubjectOffering', 'FeeSchedule', 'Payment', 'Adjustment', 'Announcement', 'MessageTemplate', 'Account'];

  return (
    <div className="p-8 max-w-6xl mx-auto space-y-6">
      <PageHeader title="Audit Log" description="Every mutating action taken in the system, append-only." />
      <SectionTabs tabs={[
        { label: 'Accounts', href: '/admin/accounts' },
        { label: 'Audit Log', href: '/admin/audit-log' },
      ]} />

      <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">From</label>
          <input type="date" value={filters.from} onChange={e => { setFilters(f => ({ ...f, from: e.target.value })); setPage(0); }}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">To</label>
          <input type="date" value={filters.to} onChange={e => { setFilters(f => ({ ...f, to: e.target.value })); setPage(0); }}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Actor</label>
          <input value={filters.actor} onChange={e => { setFilters(f => ({ ...f, actor: e.target.value })); setPage(0); }}
            placeholder="Search actor..." className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm" />
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Entity Type</label>
          <select value={filters.entity} onChange={e => { setFilters(f => ({ ...f, entity: e.target.value })); setPage(0); }}
            className="w-full px-3 py-2 border border-slate-300 rounded-md text-sm bg-white">
            <option value="">All</option>
            {entities.map(e => <option key={e} value={e}>{e}</option>)}
          </select>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-3 font-medium text-slate-500">Timestamp</th>
              <th className="px-4 py-3 font-medium text-slate-500">Actor</th>
              <th className="px-4 py-3 font-medium text-slate-500">Entity</th>
              <th className="px-4 py-3 font-medium text-slate-500">Action</th>
              <th className="px-4 py-3 font-medium text-slate-500">Detail</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {isLoading ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-500">Loading...</td></tr>
            ) : data?.content.length === 0 ? (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-500">No matching audit entries.</td></tr>
            ) : (
              data?.content.map(entry => (
                <tr key={entry.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-500 whitespace-nowrap">{new Date(entry.timestamp).toLocaleString('en-GB')}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{entry.actor}</td>
                  <td className="px-4 py-3 text-slate-600">{entry.entity}</td>
                  <td className="px-4 py-3 text-slate-600">{entry.action}</td>
                  <td className="px-4 py-3 text-slate-500">{entry.detail}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        {data && (
          <div className="p-4 border-t flex items-center justify-between text-sm text-slate-500">
            <div>Showing {data.totalElements === 0 ? 0 : page * data.size + 1} to {Math.min((page + 1) * data.size, data.totalElements)} of {data.totalElements} entries</div>
            <div className="flex gap-1">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-3 py-1 border rounded disabled:opacity-50">Prev</button>
              <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)} className="px-3 py-1 border rounded disabled:opacity-50">Next</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
