import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { PageHeader } from '@/components/page-header';
import { StatusBadge } from '@/components/status-badge';
import { PermissionGate } from '@/components/permission-gate';
import { toast } from 'sonner';
import { TrendingUp, Award, CheckCircle2, AlertTriangle, ArrowRight, ShieldCheck, RefreshCw } from 'lucide-react';

type PromotionStudentRow = {
  id: string;
  name: string;
  studentNumber: string;
  currentClass: string;
  averageScore: number;
  attendanceRate: number;
  status: 'PROMOTED' | 'REPEAT' | 'GRADUATED' | 'PENDING';
};

export default function PromotionRun() {
  const queryClient = useQueryClient();
  const [selectedSourceClass, setSelectedSourceClass] = React.useState('Primary 4');
  const [targetClass, setTargetClass] = React.useState('Primary 5');

  // Mock student promotion data for demonstration
  const [students, setStudents] = React.useState<PromotionStudentRow[]>([
    { id: '1', name: 'Kwabena Mensah', studentNumber: 'UBS-2024-001', currentClass: 'Primary 4', averageScore: 78.5, attendanceRate: 96.0, status: 'PROMOTED' },
    { id: '2', name: 'Abena Osei', studentNumber: 'UBS-2024-002', currentClass: 'Primary 4', averageScore: 84.0, attendanceRate: 98.2, status: 'PROMOTED' },
    { id: '3', name: 'Kofi Owusu', studentNumber: 'UBS-2024-003', currentClass: 'Primary 4', averageScore: 48.0, attendanceRate: 85.0, status: 'REPEAT' },
    { id: '4', name: 'Ama Addo', studentNumber: 'UBS-2024-004', currentClass: 'Primary 4', averageScore: 91.2, attendanceRate: 99.0, status: 'PROMOTED' },
    { id: '5', name: 'Yaa Asantewaa', studentNumber: 'UBS-2024-005', currentClass: 'Primary 4', averageScore: 65.4, attendanceRate: 92.5, status: 'PROMOTED' },
  ]);

  const [isProcessing, setIsProcessing] = React.useState(false);

  const handlePromoteBatch = () => {
    setIsProcessing(true);
    setTimeout(() => {
      setIsProcessing(false);
      toast.success(`End-of-Year Promotion executed for ${selectedSourceClass} → ${targetClass}`);
    }, 1200);
  };

  const toggleStudentStatus = (id: string) => {
    setStudents(prev => prev.map(s => {
      if (s.id === id) {
        const nextStatus: PromotionStudentRow['status'] = s.status === 'PROMOTED' ? 'REPEAT' : 'PROMOTED';
        return { ...s, status: nextStatus };
      }
      return s;
    }));
  };

  const promotedCount = students.filter(s => s.status === 'PROMOTED').length;
  const repeatCount = students.filter(s => s.status === 'REPEAT').length;

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Student Progression & Promotion"
        description="Review end-of-year academic aggregates, apply promotion thresholds, and execute class progressions."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Promotion' },
        ]}
      />

      {/* Promotion Configuration Controls */}
      <div className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-2xs space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2">
            <TrendingUp className="w-4 h-4 text-indigo-600" />
            <h2 className="font-display text-sm font-bold text-slate-900">Academic Batch Transition</h2>
          </div>
          <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-2.5 py-0.5 rounded-full border border-slate-200">
            2024-25 → 2025-26 Transition
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Source Class</label>
            <select
              value={selectedSourceClass}
              onChange={(e) => setSelectedSourceClass(e.target.value)}
              className="w-full px-3 py-2 text-xs font-semibold rounded-xl border border-slate-300 bg-white text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
            >
              <option value="Primary 4">Primary 4 (Current)</option>
              <option value="Primary 5">Primary 5</option>
              <option value="JHS 1">JHS 1</option>
              <option value="JHS 2">JHS 2</option>
              <option value="JHS 3">JHS 3 (BECE Exit)</option>
            </select>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-700">Target Level Destination</label>
            <select
              value={targetClass}
              onChange={(e) => setTargetClass(e.target.value)}
              className="w-full px-3 py-2 text-xs font-semibold rounded-xl border border-slate-300 bg-white text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
            >
              <option value="Primary 5">Primary 5 (Next Level)</option>
              <option value="Primary 6">Primary 6</option>
              <option value="JHS 2">JHS 2</option>
              <option value="Graduated">Graduated (BECE Certificate)</option>
            </select>
          </div>

          <PermissionGate perm="PROMOTION_MANAGE">
            <button
              onClick={handlePromoteBatch}
              disabled={isProcessing}
              className="w-full px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white font-semibold text-xs rounded-xl shadow-xs transition-all flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {isProcessing ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  <span>Processing Transition...</span>
                </>
              ) : (
                <>
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Execute Promotion Batch</span>
                </>
              )}
            </button>
          </PermissionGate>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200/90 shadow-2xs space-y-1">
          <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Total Class Candidates</span>
          <div className="font-display text-2xl font-bold text-slate-900">{students.length}</div>
          <p className="text-xs text-slate-400">Enrolled in {selectedSourceClass}</p>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200/90 shadow-2xs space-y-1">
          <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Eligible for Promotion</span>
          <div className="font-display text-2xl font-bold text-emerald-600">{promotedCount}</div>
          <p className="text-xs text-slate-400">Meets &gt;= 50% aggregate pass threshold</p>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200/90 shadow-2xs space-y-1">
          <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Conditional Repeat</span>
          <div className="font-display text-2xl font-bold text-rose-600">{repeatCount}</div>
          <p className="text-xs text-slate-400">Requires academic intervention</p>
        </div>
      </div>

      {/* Student Progression Matrix Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-display text-xs font-bold text-slate-900 uppercase tracking-wider">
            Student Decision Roster — {selectedSourceClass}
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-slate-50 text-slate-500 font-bold uppercase tracking-wider border-b border-slate-200/80">
              <tr>
                <th className="px-5 py-3">Student Name</th>
                <th className="px-5 py-3">ID Number</th>
                <th className="px-5 py-3">Term Aggregate %</th>
                <th className="px-5 py-3">Attendance Rate</th>
                <th className="px-5 py-3">Promotion Status</th>
                <th className="px-5 py-3 text-right">Manual Override</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium">
              {students.map((s) => (
                <tr key={s.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-5 py-3.5 font-bold text-slate-900">{s.name}</td>
                  <td className="px-5 py-3.5 text-slate-500 font-mono text-[11px]">{s.studentNumber}</td>

                  <td className="px-5 py-3.5 font-bold">
                    <span className={s.averageScore >= 50 ? 'text-emerald-600' : 'text-rose-600'}>
                      {s.averageScore}%
                    </span>
                  </td>

                  <td className="px-5 py-3.5 text-slate-600">{s.attendanceRate}%</td>

                  <td className="px-5 py-3.5">
                    <StatusBadge status={s.status} />
                  </td>

                  <td className="px-5 py-3.5 text-right">
                    <button
                      onClick={() => toggleStudentStatus(s.id)}
                      className="px-3 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold transition-colors"
                    >
                      Toggle ({s.status === 'PROMOTED' ? 'Set Repeat' : 'Set Promote'})
                    </button>
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
