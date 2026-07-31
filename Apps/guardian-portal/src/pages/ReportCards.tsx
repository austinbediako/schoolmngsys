import React, { useEffect, useState } from 'react';
import { useWard } from '../contexts/WardContext';
import { fetchReportCards } from '../lib/api';
import { ReportCard } from '../lib/types';
import { Award, BookOpen, UserCheck, ShieldCheck, Printer } from 'lucide-react';
import { Button } from '../components/ui/button';

export const ReportCardsPage: React.FC = () => {
  const { selectedWard } = useWard();
  const [reports, setReports] = useState<ReportCard[]>([]);
  const [selectedReport, setSelectedReport] = useState<ReportCard | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedWard) return;
    setLoading(true);
    fetchReportCards(selectedWard.id)
      .then((data) => {
        setReports(data);
        if (data.length > 0) setSelectedReport(data[0]);
      })
      .finally(() => setLoading(false));
  }, [selectedWard]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-slate-900"></div>
      </div>
    );
  }

  if (reports.length === 0 || !selectedReport) {
    return (
      <div className="text-center py-16 bg-white border border-slate-200 rounded-3xl p-6 shadow-xs">
        <Award className="w-12 h-12 text-slate-400 mx-auto mb-3" />
        <h3 className="text-base font-bold text-slate-900">No Published Report Cards</h3>
        <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
          Terminal report cards for {selectedWard?.firstName} are pending publication by the Head of School.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header & Term Selector */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white border border-slate-200 rounded-3xl p-5 shadow-xs">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Award className="w-5 h-5 text-amber-600" />
            Terminal Academic Report
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            {selectedWard?.firstName} {selectedWard?.lastName} — {selectedReport.className} ({selectedReport.academicYearLabel})
          </p>
        </div>

        <div className="flex items-center gap-2">
          {reports.map((report) => (
            <Button
              key={report.id}
              variant={selectedReport.id === report.id ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSelectedReport(report)}
            >
              {report.termName}
            </Button>
          ))}
          <Button
            variant="outline"
            size="icon"
            onClick={() => window.print()}
            title="Print Report Card"
          >
            <Printer className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Summary Badges */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Class Rank</p>
          <p className="text-xl font-extrabold text-amber-600 mt-0.5">
            #{selectedReport.classPosition || 'N/A'}
            <span className="text-xs font-normal text-slate-500"> / {selectedReport.totalStudentsInClass || 0}</span>
          </p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Total Subjects</p>
          <p className="text-xl font-extrabold text-slate-900 mt-0.5">{selectedReport.subjects.length}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Academic Year</p>
          <p className="text-xl font-extrabold text-slate-900 mt-0.5">{selectedReport.academicYearLabel}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-xs">
          <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Published Date</p>
          <p className="text-xs font-bold text-emerald-700 mt-1.5">{selectedReport.publishedAt}</p>
        </div>
      </div>

      {/* Subject Marks Table */}
      <div className="bg-white border border-slate-200 rounded-3xl overflow-hidden shadow-xs">
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-amber-600" />
            Subject Breakdown (SBA 30% + EXAM 70%)
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/70 text-slate-500 uppercase text-[10px] tracking-wider border-b border-slate-200">
              <tr>
                <th className="py-3.5 px-4 font-bold">Subject</th>
                <th className="py-3.5 px-3 font-bold text-center">SBA (30%)</th>
                <th className="py-3.5 px-3 font-bold text-center">Exam (70%)</th>
                <th className="py-3.5 px-3 font-bold text-center">Total (100%)</th>
                <th className="py-3.5 px-3 font-bold text-center">Grade</th>
                <th className="py-3.5 px-3 font-bold text-center">Rank</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {selectedReport.subjects.map((sub, idx) => (
                <tr key={idx} className="hover:bg-slate-50 transition-all">
                  <td className="py-3.5 px-4 font-semibold text-slate-900">
                    {sub.subjectName}
                    <span className="text-[10px] text-slate-400 block font-mono">{sub.subjectCode}</span>
                  </td>
                  <td className="py-3.5 px-3 text-center text-slate-600 font-mono">{sub.sbaScore}</td>
                  <td className="py-3.5 px-3 text-center text-slate-600 font-mono">{sub.examScore}</td>
                  <td className="py-3.5 px-3 text-center font-bold text-slate-900 font-mono">{sub.weightedTotal}</td>
                  <td className="py-3.5 px-3 text-center">
                    <span className="inline-block px-2.5 py-0.5 rounded-lg bg-amber-50 text-amber-800 font-extrabold text-xs border border-amber-200">
                      {sub.grade}
                    </span>
                  </td>
                  <td className="py-3.5 px-3 text-center text-slate-500 font-medium">
                    {sub.subjectPosition ? `#${sub.subjectPosition}` : '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Remarks Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {selectedReport.classTeacherRemarks && (
          <div className="bg-white border border-slate-200 rounded-3xl p-5 space-y-2 shadow-xs">
            <h4 className="text-xs font-bold uppercase tracking-wider text-amber-700 flex items-center gap-2">
              <UserCheck className="w-4 h-4" />
              Class Teacher Remarks
            </h4>
            <p className="text-xs text-slate-600 italic leading-relaxed">
              "{selectedReport.classTeacherRemarks}"
            </p>
          </div>
        )}

        {selectedReport.headRemarks && (
          <div className="bg-white border border-slate-200 rounded-3xl p-5 space-y-2 shadow-xs">
            <h4 className="text-xs font-bold uppercase tracking-wider text-emerald-700 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4" />
              Head of School Remarks
            </h4>
            <p className="text-xs text-slate-600 italic leading-relaxed">
              "{selectedReport.headRemarks}"
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
