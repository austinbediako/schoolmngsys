import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { SectionTabs } from '@/components/section-tabs';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { AuditLog, PaginatedResponse } from '@/types';
import { Filter, ChevronLeft, ChevronRight, RefreshCw, Activity } from 'lucide-react';

const ENTITIES = [
  'Student',
  'Guardian',
  'Enrollment',
  'AcademicYear',
  'Term',
  'SchoolClass',
  'ClassSubjectOffering',
  'FeeSchedule',
  'Payment',
  'Adjustment',
  'Announcement',
  'MessageTemplate',
  'Account',
  'SchoolSettings',
  'StaffDocument',
  'Staff',
];

export default function AuditLogPage() {
  const [filters, setFilters] = React.useState({ from: '', to: '', entityType: '' });
  const [page, setPage] = React.useState(0);

  const query = new URLSearchParams();
  query.set('page', String(page));
  query.set('size', '20');
  if (filters.entityType) query.set('entityType', filters.entityType);
  if (filters.from) query.set('fromDate', `${filters.from}T00:00:00Z`);
  if (filters.to) query.set('toDate', `${filters.to}T23:59:59Z`);

  const { data, isLoading, refetch, isRefetching } = useQuery<PaginatedResponse<AuditLog>>({
    queryKey: ['audit-logs', filters, page],
    queryFn: () => apiClient(`/audit-logs?${query.toString()}`),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Audit Trail Logs"
        description="Immutable, append-only audit trail logging every state-mutating operation across the system."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Admin', href: '/admin/accounts' },
          { label: 'Audit Trail Logs' },
        ]}
      />
      <SectionTabs
        tabs={[
          { label: 'Staff Accounts', href: '/admin/accounts' },
          { label: 'Audit Trail Logs', href: '/admin/audit-log' },
        ]}
      />

      {/* Filter Control Bar */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2 text-xs font-bold text-slate-800">
            <Filter className="w-4 h-4 text-indigo-600" />
            <span>Filter Audit Trail</span>
          </div>

          <Button
            onClick={() => refetch()}
            disabled={isRefetching}
            variant="outline"
            size="sm"
            className="text-xs font-semibold"
          >
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isRefetching ? 'animate-spin' : ''}`} />
            <span>Refresh Logs</span>
          </Button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">From Date</label>
            <input
              type="date"
              value={filters.from}
              onChange={(e) => {
                setFilters((f) => ({ ...f, from: e.target.value }));
                setPage(0);
              }}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 bg-white focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">To Date</label>
            <input
              type="date"
              value={filters.to}
              onChange={(e) => {
                setFilters((f) => ({ ...f, to: e.target.value }));
                setPage(0);
              }}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 bg-white focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Entity Type</label>
            <select
              value={filters.entityType}
              onChange={(e) => {
                setFilters((f) => ({ ...f, entityType: e.target.value }));
                setPage(0);
              }}
              className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 bg-white focus:outline-none"
            >
              <option value="">All Entities</option>
              {ENTITIES.map((e) => (
                <option key={e} value={e}>
                  {e}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Audit Log Data Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Timestamp</th>
              <th className="px-5 py-3.5">Actor Account</th>
              <th className="px-5 py-3.5">Entity</th>
              <th className="px-5 py-3.5">Action</th>
              <th className="px-5 py-3.5">Detail / IP</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  Loading audit logs...
                </td>
              </tr>
            ) : !data?.content || data.content.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-5 py-8 text-center text-slate-400">
                  No audit log entries matching filters.
                </td>
              </tr>
            ) : (
              data.content.map((entry) => {
                const ts = entry.occurredAt || entry.timestamp;
                const formattedDate = ts
                  ? new Date(ts).toLocaleString('en-GB', {
                      year: 'numeric',
                      month: 'short',
                      day: '2-digit',
                      hour: '2-digit',
                      minute: '2-digit',
                      second: '2-digit',
                    })
                  : '—';

                return (
                  <tr key={entry.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-5 py-3.5 text-slate-600 font-mono text-[11px] whitespace-nowrap">
                      {formattedDate}
                    </td>

                    <td className="px-5 py-3.5 font-mono text-[11px] text-slate-700 truncate max-w-[140px]">
                      {entry.actorAccountId || entry.actor || 'System'}
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-lg bg-indigo-50 text-indigo-700 border border-indigo-200 font-mono text-[11px] font-semibold">
                        {entry.entityType || entry.entity}
                      </span>
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-lg bg-slate-100 text-slate-700 border border-slate-200 font-mono text-[11px] font-bold">
                        {entry.action}
                      </span>
                    </td>

                    <td className="px-5 py-3.5 text-slate-600 font-mono text-[11px]">
                      <div>{entry.summary ? JSON.stringify(entry.summary) : entry.detail || (entry.entityId ? `Entity ID: ${entry.entityId}` : '—')}</div>
                      {entry.ip && <div className="text-[10px] text-slate-400 mt-0.5">IP: {entry.ip}</div>}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {/* Table Footer & Pagination */}
        {data && (
          <div className="p-4 border-t border-slate-200/90 bg-slate-50/50 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs font-semibold text-slate-600">
            <div>
              Showing {data.totalElements === 0 ? 0 : page * data.size + 1} to{' '}
              {Math.min((page + 1) * data.size, data.totalElements)} of {data.totalElements} entries
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                <ChevronLeft className="w-4 h-4 mr-1" />
                <span>Previous</span>
              </Button>

              <span className="px-3 py-1 bg-white border border-slate-200 rounded-xl font-mono text-xs font-bold text-slate-800">
                {page + 1} / {data.totalPages || 1}
              </span>

              <Button
                variant="outline"
                size="sm"
                disabled={page >= (data.totalPages || 1) - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                <span>Next</span>
                <ChevronRight className="w-4 h-4 ml-1" />
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
