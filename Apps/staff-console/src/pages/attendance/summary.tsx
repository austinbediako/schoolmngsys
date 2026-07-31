import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { PageHeader } from '@/components/page-header';
import { Link } from 'wouter';
import { CalendarCheck, Calendar, Users, CheckCircle2, XCircle, Clock, ArrowRight, BookOpen, AlertCircle } from 'lucide-react';
import { Class } from '@/types';

export default function AttendanceSummary() {
  const [selectedDate, setSelectedDate] = React.useState(new Date().toISOString().split('T')[0]);

  const { data: dashboardData, isLoading: isDashboardLoading } = useQuery({
    queryKey: ['dashboard-head'],
    queryFn: () => apiClient('/dashboard/head'),
  });

  const { data: classesData, isLoading: isClassesLoading } = useQuery<any>({
    queryKey: ['classes'],
    queryFn: () => apiClient('/classes'),
  });

  const overallRate = dashboardData?.attendance?.attendanceRatePercentage ?? 0;
  const totalPresent = dashboardData?.attendance?.presentCount ?? 0;
  const totalAbsent = dashboardData?.attendance?.absentCount ?? 0;
  const totalLate = dashboardData?.attendance?.lateCount ?? 0;
  const totalRecords = dashboardData?.attendance?.totalRecords ?? 0;

  const classesList: Class[] = Array.isArray(classesData?.content) ? classesData.content : [];

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-8 animate-fade-in">
      <PageHeader
        title="Daily Attendance Intelligence"
        description="Monitor daily roll call records, absence trends, and class breakdown across all configured levels."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Attendance Summary' },
        ]}
      >
        <div className="flex items-center gap-3 bg-white p-1.5 rounded-xl border border-slate-200 shadow-2xs">
          <Calendar className="w-4 h-4 text-indigo-600 ml-2" />
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="text-xs font-semibold text-slate-800 bg-transparent border-none focus:outline-none pr-2"
          />
        </div>
      </PageHeader>

      {/* Top Metric Cards - Live Data */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="p-3 rounded-2xl bg-emerald-50 text-emerald-600">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200/80">
              Overall Rate
            </span>
          </div>
          <div className="mt-4 space-y-1">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Attendance Rate</span>
            <div className="font-display text-3xl font-extrabold text-slate-900">
              {isDashboardLoading ? '-' : `${overallRate}%`}
            </div>
            <p className="text-xs text-slate-400 font-medium">{totalPresent} of {totalRecords} recorded present</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="p-3 rounded-2xl bg-indigo-50 text-indigo-600">
              <Users className="w-6 h-6" />
            </div>
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200/80">
              Present
            </span>
          </div>
          <div className="mt-4 space-y-1">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Present</span>
            <div className="font-display text-3xl font-extrabold text-slate-900">
              {isDashboardLoading ? '-' : totalPresent}
            </div>
            <p className="text-xs text-slate-400 font-medium">In active class sessions</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="p-3 rounded-2xl bg-rose-50 text-rose-600">
              <XCircle className="w-6 h-6" />
            </div>
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-rose-50 text-rose-700 border border-rose-200/80">
              Absences
            </span>
          </div>
          <div className="mt-4 space-y-1">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Absent</span>
            <div className="font-display text-3xl font-extrabold text-slate-900">
              {isDashboardLoading ? '-' : totalAbsent}
            </div>
            <p className="text-xs text-slate-400 font-medium">Recorded unexcused</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="p-3 rounded-2xl bg-amber-50 text-amber-600">
              <Clock className="w-6 h-6" />
            </div>
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-50 text-amber-700 border border-amber-200/80">
              Tardy
            </span>
          </div>
          <div className="mt-4 space-y-1">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Late</span>
            <div className="font-display text-3xl font-extrabold text-slate-900">
              {isDashboardLoading ? '-' : totalLate}
            </div>
            <p className="text-xs text-slate-400 font-medium">Arrival after start time</p>
          </div>
        </div>
      </div>

      {/* Class Roll Call Breakdown Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="p-5 border-b border-slate-200/80 flex items-center justify-between">
          <div>
            <h2 className="font-display text-base font-bold text-slate-900">Class Roll Call Directory</h2>
            <p className="text-xs text-slate-500 mt-0.5">Select an active class level to mark or review daily register</p>
          </div>

          <Link
            href="/attendance/mark"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-semibold text-xs shadow-xs transition-all"
          >
            <CalendarCheck className="w-4 h-4 text-emerald-400" />
            <span>Mark Register</span>
          </Link>
        </div>

        {isClassesLoading ? (
          <div className="p-8 text-center text-xs text-slate-500 font-medium">Loading configured classes...</div>
        ) : classesList.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50/80 text-slate-500 font-bold uppercase tracking-wider border-b border-slate-200/80">
                <tr>
                  <th className="px-5 py-3.5">Class Level</th>
                  <th className="px-5 py-3.5">Stream</th>
                  <th className="px-5 py-3.5">Code</th>
                  <th className="px-5 py-3.5 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {classesList.map((c) => (
                  <tr key={c.id} className="hover:bg-indigo-50/30 transition-colors">
                    <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">
                      {c.level}
                    </td>

                    <td className="px-5 py-3.5 text-slate-600 font-semibold">{c.stream || '-'}</td>
                    <td className="px-5 py-3.5 text-slate-400 font-mono text-[11px]">{c.code || '-'}</td>

                    <td className="px-5 py-3.5 text-right">
                      <Link
                        href={`/attendance/mark?classId=${c.id}`}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-xs transition-colors"
                      >
                        <span>Mark Roll Call</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="py-12 text-center space-y-3">
            <AlertCircle className="w-8 h-8 text-amber-500 mx-auto" />
            <div>
              <p className="text-sm font-bold text-slate-900">No Classes Configured</p>
              <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
                Please set up your school classes first in the Academic Setup section before taking attendance.
              </p>
            </div>
            <Link
              href="/academic/classes"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-indigo-600 text-white font-semibold text-xs shadow-xs hover:bg-indigo-700 transition-all"
            >
              <span>Set Up Classes</span>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}
