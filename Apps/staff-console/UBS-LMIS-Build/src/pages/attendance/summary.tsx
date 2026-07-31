import React from 'react';
import { PageHeader } from '@/components/page-header';
import { StatusBadge } from '@/components/status-badge';
import { Link } from 'wouter';
import { CalendarCheck, Calendar, Users, CheckCircle2, XCircle, Clock, ArrowRight, Sparkles } from 'lucide-react';

export default function AttendanceSummary() {
  const [selectedDate, setSelectedDate] = React.useState(new Date().toISOString().split('T')[0]);

  // Mock class attendance summary records
  const classRecords = [
    { classId: 'NUR-1', name: 'Nursery 1', total: 15, present: 14, absent: 1, late: 0, rate: 93.3 },
    { classId: 'NUR-2', name: 'Nursery 2', total: 18, present: 17, absent: 0, late: 1, rate: 94.4 },
    { classId: 'KG-1', name: 'Kindergarten 1', total: 20, present: 19, absent: 1, late: 0, rate: 95.0 },
    { classId: 'KG-2', name: 'Kindergarten 2', total: 22, present: 21, absent: 1, late: 0, rate: 95.5 },
    { classId: 'PRI-1', name: 'Primary 1', total: 25, present: 24, absent: 0, late: 1, rate: 96.0 },
    { classId: 'PRI-2', name: 'Primary 2', total: 24, present: 22, absent: 2, late: 0, rate: 91.7 },
    { classId: 'PRI-3', name: 'Primary 3', total: 26, present: 25, absent: 1, late: 0, rate: 96.2 },
    { classId: 'JHS-1', name: 'Junior High 1', total: 30, present: 28, absent: 1, late: 1, rate: 93.3 },
    { classId: 'JHS-2', name: 'Junior High 2', total: 28, present: 27, absent: 1, late: 0, rate: 96.4 },
    { classId: 'JHS-3', name: 'Junior High 3', total: 32, present: 31, absent: 0, late: 1, rate: 96.8 },
  ];

  const totalStudents = classRecords.reduce((acc, c) => acc + c.total, 0);
  const totalPresent = classRecords.reduce((acc, c) => acc + c.present, 0);
  const totalAbsent = classRecords.reduce((acc, c) => acc + c.absent, 0);
  const totalLate = classRecords.reduce((acc, c) => acc + c.late, 0);
  const overallRate = ((totalPresent / totalStudents) * 100).toFixed(1);

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-8 animate-fade-in">
      <PageHeader
        title="Daily Attendance Intelligence"
        description="Monitor daily roll call records, absence trends, and class breakdown across all levels."
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

      {/* Top Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs card-glow-emerald">
          <div className="flex items-start justify-between">
            <div className="p-3 rounded-2xl bg-emerald-50 text-emerald-600">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200/80">
              High Attendance
            </span>
          </div>
          <div className="mt-4 space-y-1">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Overall Attendance</span>
            <div className="font-display text-3xl font-extrabold text-slate-900">{overallRate}%</div>
            <p className="text-xs text-slate-400 font-medium">{totalPresent} of {totalStudents} present today</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs card-glow-indigo">
          <div className="flex items-start justify-between">
            <div className="p-3 rounded-2xl bg-indigo-50 text-indigo-600">
              <Users className="w-6 h-6" />
            </div>
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200/80">
              Enrolled
            </span>
          </div>
          <div className="mt-4 space-y-1">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Present</span>
            <div className="font-display text-3xl font-extrabold text-slate-900">{totalPresent}</div>
            <p className="text-xs text-slate-400 font-medium">In active sessions</p>
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
            <div className="font-display text-3xl font-extrabold text-slate-900">{totalAbsent}</div>
            <p className="text-xs text-slate-400 font-medium">Unexcused / Reported</p>
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
            <div className="font-display text-3xl font-extrabold text-slate-900">{totalLate}</div>
            <p className="text-xs text-slate-400 font-medium">Arrival after 8:00 AM</p>
          </div>
        </div>
      </div>

      {/* Class Roll Call Breakdown Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="p-5 border-b border-slate-200/80 flex items-center justify-between">
          <div>
            <h2 className="font-display text-base font-bold text-slate-900">Class Roll Call Breakdown</h2>
            <p className="text-xs text-slate-500 mt-0.5">Select a class to take or review student attendance</p>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-slate-50/80 text-slate-500 font-bold uppercase tracking-wider border-b border-slate-200/80">
              <tr>
                <th className="px-5 py-3.5">Class Name</th>
                <th className="px-5 py-3.5">Enrolled</th>
                <th className="px-5 py-3.5">Present</th>
                <th className="px-5 py-3.5">Absent</th>
                <th className="px-5 py-3.5">Late</th>
                <th className="px-5 py-3.5">Rate %</th>
                <th className="px-5 py-3.5 text-right">Roll Call Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium">
              {classRecords.map((c) => (
                <tr key={c.classId} className="hover:bg-indigo-50/30 transition-colors">
                  <td className="px-5 py-3.5">
                    <span className="font-bold text-slate-900 text-sm">{c.name}</span>
                  </td>

                  <td className="px-5 py-3.5 text-slate-600 font-semibold">{c.total}</td>
                  <td className="px-5 py-3.5 text-emerald-600 font-bold">{c.present}</td>
                  <td className="px-5 py-3.5 text-rose-600 font-bold">{c.absent}</td>
                  <td className="px-5 py-3.5 text-amber-600 font-bold">{c.late}</td>

                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-2">
                      <div className="w-16 h-2 bg-slate-100 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-emerald-500 rounded-full"
                          style={{ width: `${c.rate}%` }}
                        />
                      </div>
                      <span className="font-bold text-slate-900">{c.rate}%</span>
                    </div>
                  </td>

                  <td className="px-5 py-3.5 text-right">
                    <Link
                      href={`/attendance/roll-call?classId=${c.classId}`}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-xs transition-colors"
                    >
                      <span>Take Roll Call</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
