import React, { useEffect, useState } from 'react';
import { useWard } from '../contexts/WardContext';
import { fetchAttendance, fetchInvoices, fetchReportCards } from '../lib/api';
import { AttendanceSummary, FeeInvoice, ReportCard } from '../lib/types';
import { UserCheck, Award, CreditCard, Sparkles, BookOpen, AlertCircle, ArrowRight } from 'lucide-react';
import { Link } from 'wouter';
import { Button } from '../components/ui/button';

export const Dashboard: React.FC = () => {
  const { selectedWard, loading: wardLoading } = useWard();
  const [attendance, setAttendance] = useState<AttendanceSummary | null>(null);
  const [reportCards, setReportCards] = useState<ReportCard[]>([]);
  const [invoices, setInvoices] = useState<FeeInvoice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedWard) return;
    setLoading(true);
    Promise.all([
      fetchAttendance(selectedWard.id),
      fetchReportCards(selectedWard.id),
      fetchInvoices(selectedWard.id)
    ])
      .then(([attData, rcData, invData]) => {
        setAttendance(attData);
        setReportCards(rcData);
        setInvoices(invData);
      })
      .finally(() => setLoading(false));
  }, [selectedWard]);

  if (wardLoading || loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-slate-900"></div>
      </div>
    );
  }

  if (!selectedWard) {
    return (
      <div className="text-center py-12 bg-white border border-slate-200 rounded-3xl p-6 shadow-xs">
        <AlertCircle className="w-12 h-12 text-amber-500 mx-auto mb-3" />
        <h2 className="text-lg font-bold text-slate-900">No Linked Ward Found</h2>
        <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
          No students are currently linked to your guardian account. Please contact the UBS Legon administration office.
        </p>
      </div>
    );
  }

  const latestReport = reportCards[0];
  const activeInvoice = invoices[0];

  return (
    <div className="space-y-6">
      {/* Student Hero Header Card */}
      <div className="relative overflow-hidden bg-white border border-slate-200 rounded-3xl p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 relative z-10">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-2xl bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-700 font-extrabold text-2xl shadow-xs">
              {selectedWard.firstName[0]}
              {selectedWard.lastName[0]}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-xl font-bold text-slate-900">{selectedWard.firstName} {selectedWard.lastName}</h2>
                <span className="px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase bg-emerald-50 text-emerald-700 border border-emerald-200">
                  {selectedWard.status}
                </span>
              </div>
              <p className="text-xs text-slate-600 mt-0.5">
                Class: <span className="text-amber-800 font-semibold">{selectedWard.className || selectedWard.classLevelName}</span>
              </p>
              <p className="text-[11px] text-slate-400 font-mono mt-0.5">
                ID: {selectedWard.studentNumber}
              </p>
            </div>
          </div>

          <div className="bg-slate-50 border border-slate-200 rounded-2xl p-3 flex items-center gap-3">
            <img src="/logo.png" alt="UBS" className="w-8 h-8 object-contain" />
            <div>
              <p className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">University Basic School</p>
              <p className="text-xs font-bold text-slate-900">Legon Campus, Accra</p>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Summary Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Attendance Summary */}
        <div className="bg-white border border-slate-200 rounded-3xl p-5 hover:border-slate-300 transition-all shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <span className="p-2.5 rounded-2xl bg-blue-50 text-blue-600 border border-blue-100">
              <UserCheck className="w-5 h-5" />
            </span>
            <span className="text-xs font-bold text-blue-700 bg-blue-50 px-2.5 py-1 rounded-full border border-blue-200">
              {attendance?.ratePercentage || 0}% Rate
            </span>
          </div>
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Class Attendance</h3>
            <p className="text-2xl font-extrabold text-slate-900 mt-1">
              {attendance?.presentDays || 0} <span className="text-xs font-normal text-slate-500">/ {attendance?.totalDays || 0} Days</span>
            </p>
          </div>
          <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
            <span className="text-slate-500 font-medium">Late: {attendance?.lateDays || 0}d | Absent: {attendance?.absentDays || 0}d</span>
            <Link href="/attendance">
              <span className="text-slate-900 hover:text-amber-600 font-bold flex items-center gap-1 cursor-pointer">
                View <ArrowRight className="w-3.5 h-3.5" />
              </span>
            </Link>
          </div>
        </div>

        {/* Academic Performance */}
        <div className="bg-white border border-slate-200 rounded-3xl p-5 hover:border-slate-300 transition-all shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <span className="p-2.5 rounded-2xl bg-amber-50 text-amber-600 border border-amber-100">
              <Award className="w-5 h-5" />
            </span>
            {latestReport?.classPosition && (
              <span className="text-xs font-bold text-amber-800 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
                Rank #{latestReport.classPosition}
              </span>
            )}
          </div>
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Latest Term Report</h3>
            <p className="text-2xl font-extrabold text-slate-900 mt-1">
              {latestReport ? latestReport.termName : 'No Results'}
            </p>
          </div>
          <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
            <span className="text-slate-500 font-medium">
              {latestReport ? `${latestReport.subjects.length} Subjects` : 'Pending Term Close'}
            </span>
            <Link href="/report-cards">
              <span className="text-slate-900 hover:text-amber-600 font-bold flex items-center gap-1 cursor-pointer">
                View Report <ArrowRight className="w-3.5 h-3.5" />
              </span>
            </Link>
          </div>
        </div>

        {/* Fee Billing Status */}
        <div className="bg-white border border-slate-200 rounded-3xl p-5 hover:border-slate-300 transition-all shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <span className="p-2.5 rounded-2xl bg-emerald-50 text-emerald-600 border border-emerald-100">
              <CreditCard className="w-5 h-5" />
            </span>
            <span className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
              activeInvoice?.balance === 0
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-amber-50 text-amber-800 border-amber-200'
            }`}>
              {activeInvoice?.balance === 0 ? 'Fully Paid' : 'Balance Due'}
            </span>
          </div>
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Current Fee Balance</h3>
            <p className="text-2xl font-extrabold text-slate-900 mt-1">
              GHS {activeInvoice ? activeInvoice.balance.toFixed(2) : '0.00'}
            </p>
          </div>
          <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
            <span className="text-slate-500 font-medium">
              Total: GHS {activeInvoice ? activeInvoice.totalAmount.toFixed(2) : '0.00'}
            </span>
            <Link href="/finance">
              <span className="text-slate-900 hover:text-amber-600 font-bold flex items-center gap-1 cursor-pointer">
                Fee Details <ArrowRight className="w-3.5 h-3.5" />
              </span>
            </Link>
          </div>
        </div>
      </div>

      {/* Class Teacher Remarks Box */}
      {latestReport?.classTeacherRemarks && (
        <div className="bg-white border border-slate-200 rounded-3xl p-5 space-y-2 shadow-xs">
          <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
            <BookOpen className="w-4 h-4 text-amber-600" />
            <h3>Class Teacher Remarks</h3>
          </div>
          <p className="text-xs text-slate-600 italic leading-relaxed pl-4 border-l-2 border-amber-400">
            "{latestReport.classTeacherRemarks}"
          </p>
        </div>
      )}
    </div>
  );
};
