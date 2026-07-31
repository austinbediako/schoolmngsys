import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { Student, PaginatedResponse } from '@/types';
import { Link, useLocation } from 'wouter';
import { PageHeader } from '@/components/page-header';
import { StatusBadge } from '@/components/status-badge';
import { PermissionGate } from '@/components/permission-gate';
import { Search, Plus, Filter, ChevronLeft, ChevronRight, UserCheck, GraduationCap } from 'lucide-react';

export default function StudentsList() {
  const [, setLocation] = useLocation();
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');

  const { data, isLoading } = useQuery<PaginatedResponse<Student>>({
    queryKey: ['students', page, search],
    queryFn: () => apiClient(`/students?page=${page}&size=20&search=${encodeURIComponent(search)}`),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Student Directory"
        description="Comprehensive records, enrollment history, and guardian profiles across all grade levels."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Students' },
        ]}
      >
        <PermissionGate perm="STUDENT_CREATE">
          <Link
            href="/students/new"
            className="inline-flex items-center justify-center gap-2 px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-semibold text-xs border border-slate-900 shadow-xs transition-all active:scale-98"
          >
            <Plus className="w-4 h-4 text-emerald-400" />
            <span>Admit New Student</span>
          </Link>
        </PermissionGate>
      </PageHeader>

      {/* Directory Table Card Container */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs flex flex-col overflow-hidden">
        {/* Search & Filter Header Toolbar */}
        <div className="p-4 bg-slate-50/50 border-b border-slate-200/80 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="relative w-full sm:max-w-md">
            <Search className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search by student name or ID number..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2 text-xs font-medium rounded-xl border border-slate-200/80 bg-white text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all shadow-2xs"
            />
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
            <button className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold rounded-xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 transition-all shadow-2xs">
              <Filter className="w-3.5 h-3.5 text-slate-500" />
              <span>Filter Class</span>
            </button>
          </div>
        </div>

        {/* Data Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-slate-50/80 text-slate-500 font-bold uppercase tracking-wider border-b border-slate-200/80">
              <tr>
                <th className="px-5 py-3.5">Student Profile</th>
                <th className="px-5 py-3.5">Student ID</th>
                <th className="px-5 py-3.5">Class / Level</th>
                <th className="px-5 py-3.5">Gender</th>
                <th className="px-5 py-3.5">Status</th>
                <th className="px-5 py-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium">
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    <td className="px-5 py-4"><div className="h-5 bg-slate-100 rounded-lg w-40 animate-pulse" /></td>
                    <td className="px-5 py-4"><div className="h-5 bg-slate-100 rounded-lg w-24 animate-pulse" /></td>
                    <td className="px-5 py-4"><div className="h-5 bg-slate-100 rounded-lg w-20 animate-pulse" /></td>
                    <td className="px-5 py-4"><div className="h-5 bg-slate-100 rounded-lg w-12 animate-pulse" /></td>
                    <td className="px-5 py-4"><div className="h-5 bg-slate-100 rounded-lg w-16 animate-pulse" /></td>
                    <td className="px-5 py-4"><div className="h-5 bg-slate-100 rounded-lg w-12 animate-pulse ml-auto" /></td>
                  </tr>
                ))
              ) : data?.content.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-5 py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <GraduationCap className="w-8 h-8 text-slate-300" />
                      <p className="font-semibold text-slate-600">No student records found.</p>
                      <p className="text-xs text-slate-400">Try adjusting your search criteria or add a student.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                data?.content.map((student) => (
                  <tr
                    key={student.id}
                    onClick={() => setLocation(`/students/${student.id}`)}
                    className="hover:bg-slate-50/80 transition-colors cursor-pointer group"
                  >
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-100 to-blue-50 text-indigo-700 font-bold text-xs flex items-center justify-center border border-indigo-200/60 shadow-2xs group-hover:scale-105 transition-transform">
                          {student.firstName[0]}{student.lastName[0]}
                        </div>
                        <span className="font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">
                          {student.firstName} {student.otherName ? `${student.otherName} ` : ''}{student.lastName}
                        </span>
                      </div>
                    </td>

                    <td className="px-5 py-3.5 text-slate-600 font-mono text-[11px] font-semibold">
                      {student.studentNumber}
                    </td>

                    <td className="px-5 py-3.5">
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-200">
                        {student.currentClassId || 'Unassigned'}
                      </span>
                    </td>

                    <td className="px-5 py-3.5 text-slate-500 uppercase font-semibold text-[11px]">
                      {student.gender}
                    </td>

                    <td className="px-5 py-3.5">
                      <StatusBadge status={student.status} />
                    </td>

                    <td className="px-5 py-3.5 text-right">
                      <Link
                        href={`/students/${student.id}`}
                        className="inline-flex items-center gap-1 text-xs font-bold text-indigo-600 hover:text-indigo-700 hover:underline"
                      >
                        View Profile →
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Table Footer Pagination */}
        {data && (
          <div className="p-4 bg-slate-50/50 border-t border-slate-200/80 flex items-center justify-between text-xs text-slate-500 font-medium">
            <div>
              Showing <span className="font-bold text-slate-800">{page * data.size + 1}</span> to{' '}
              <span className="font-bold text-slate-800">{Math.min((page + 1) * data.size, data.totalElements)}</span> of{' '}
              <span className="font-bold text-slate-800">{data.totalElements}</span> students
            </div>

            <div className="flex items-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl border border-slate-200 bg-white font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-2xs"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
                <span>Prev</span>
              </button>

              <button
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl border border-slate-200 bg-white font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-2xs"
              >
                <span>Next</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
