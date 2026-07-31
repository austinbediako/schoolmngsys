import React, { useEffect, useState } from 'react';
import { useWard } from '../contexts/WardContext';
import { fetchAttendance } from '../lib/api';
import { AttendanceSummary } from '../lib/types';
import { Calendar as CalendarIcon, CheckCircle2, XCircle, Clock, AlertCircle } from 'lucide-react';

export const AttendancePage: React.FC = () => {
  const { selectedWard } = useWard();
  const [attendance, setAttendance] = useState<AttendanceSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedWard) return;
    setLoading(true);
    fetchAttendance(selectedWard.id)
      .then(setAttendance)
      .finally(() => setLoading(false));
  }, [selectedWard]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-slate-900"></div>
      </div>
    );
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PRESENT':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200"><CheckCircle2 className="w-3.5 h-3.5" /> Present</span>;
      case 'ABSENT':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200"><XCircle className="w-3.5 h-3.5" /> Absent</span>;
      case 'LATE':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-800 border border-amber-200"><Clock className="w-3.5 h-3.5" /> Late</span>;
      default:
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200"><AlertCircle className="w-3.5 h-3.5" /> Excused</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="bg-white border border-slate-200 rounded-3xl p-5 flex items-center justify-between shadow-xs">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <CalendarIcon className="w-5 h-5 text-amber-600" />
            Class Attendance Tracker
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time daily register logs for {selectedWard?.firstName} {selectedWard?.lastName}
          </p>
        </div>
        <div className="bg-amber-50 border border-amber-200 px-3.5 py-1.5 rounded-2xl text-amber-800 font-extrabold text-sm">
          {attendance?.ratePercentage}% Overall Rate
        </div>
      </div>

      {/* Summary Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-slate-400">Total Days</p>
          <p className="text-xl font-extrabold text-slate-900 mt-1">{attendance?.totalDays || 0}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-emerald-600">Present</p>
          <p className="text-xl font-extrabold text-emerald-700 mt-1">{attendance?.presentDays || 0}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-amber-600">Late</p>
          <p className="text-xl font-extrabold text-amber-700 mt-1">{attendance?.lateDays || 0}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-rose-600">Absent</p>
          <p className="text-xl font-extrabold text-rose-700 mt-1">{attendance?.absentDays || 0}</p>
        </div>
      </div>

      {/* Log Table */}
      <div className="bg-white border border-slate-200 rounded-3xl overflow-hidden shadow-xs">
        <div className="px-5 py-4 border-b border-slate-200 bg-slate-50/50">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
            Daily Register Logs
          </h3>
        </div>

        <div className="divide-y divide-slate-100">
          {attendance?.records.map((rec) => (
            <div key={rec.id} className="p-4 flex items-center justify-between hover:bg-slate-50 transition-all text-xs">
              <div>
                <p className="font-bold text-slate-900 text-sm">{rec.attendanceDate}</p>
                {rec.reason && (
                  <p className="text-slate-500 text-[11px] mt-0.5 italic">Reason: {rec.reason}</p>
                )}
              </div>
              <div>{getStatusBadge(rec.status)}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
