import React from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { PageHeader } from '@/components/page-header';
import { Button } from '@/components/ui/button';
import { Student } from '@/types';
import { Lock, ShieldCheck, RefreshCw, CheckCircle2 } from 'lucide-react';

export default function MarkAttendance() {
  const [selectedClass, setSelectedClass] = React.useState<string>('');
  const [date, setDate] = React.useState<string>(new Date().toISOString().split('T')[0]);

  // Track submitted registers (One register submission per class per day)
  const [submittedRegisters, setSubmittedRegisters] = React.useState<Record<string, boolean>>(() => {
    try {
      const stored = localStorage.getItem('submitted_registers');
      return stored ? JSON.parse(stored) : {};
    } catch {
      return {};
    }
  });

  const registerKey = `${selectedClass}_${date}`;
  const isAlreadySubmitted = Boolean(selectedClass && date && submittedRegisters[registerKey]);

  const { data: classes } = useQuery<any>({
    queryKey: ['classes'],
    queryFn: () => apiClient('/classes'),
  });
  const classesList = Array.isArray(classes) ? classes : (classes?.content || []);

  const { data: students, isLoading: loadingStudents } = useQuery<any>({
    queryKey: ['students-by-class', selectedClass],
    queryFn: () => apiClient(`/students?classId=${selectedClass}&size=100`),
    enabled: !!selectedClass,
  });

  // Local state for the register
  const [register, setRegister] = React.useState<Record<string, string>>({});

  React.useEffect(() => {
    if (students?.content) {
      const initial: Record<string, string> = {};
      students.content.forEach((s: Student) => {
        initial[s.id] = 'PRESENT'; // Default
      });
      setRegister(initial);
    }
  }, [students]);

  const setStatus = (studentId: string, status: string) => {
    if (isAlreadySubmitted) {
      toast.error(`Attendance for ${date} has already been submitted and finalized for today.`);
      return;
    }
    setRegister((prev) => ({ ...prev, [studentId]: status }));
  };

  const submitMutation = useMutation({
    mutationFn: async () => {
      const payload = Object.entries(register).map(([studentId, status]) => ({
        studentId,
        classId: selectedClass,
        date,
        status,
      }));
      const idempotencyKey = crypto.randomUUID();
      await apiClient('/attendance/bulk', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey },
        body: JSON.stringify(payload),
      });
    },
    onSuccess: () => {
      toast.success('Daily attendance submitted and locked for today.');
      const updated = { ...submittedRegisters, [registerKey]: true };
      setSubmittedRegisters(updated);
      try {
        localStorage.setItem('submitted_registers', JSON.stringify(updated));
      } catch {}
    },
    onError: (err) => handleApiError(err),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-5xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Mark Daily Attendance"
        description="Select class level and record daily roll call. Submissions are restricted to one entry per class per day."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Attendance', href: '/attendance/summary' },
          { label: 'Mark Attendance' },
        ]}
      />

      {/* Class Selection Filter Header */}
      <div className="bg-white p-5 border border-slate-200/90 rounded-2xl shadow-2xs flex flex-col md:flex-row gap-4 items-end justify-between">
        <div className="w-full md:w-64 space-y-1">
          <label className="text-xs font-semibold text-slate-700">Select Class Level</label>
          <select
            value={selectedClass}
            onChange={(e) => setSelectedClass(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-xl bg-white text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="">Select a class...</option>
            {classesList.map((c: any) => (
              <option key={c.id} value={c.id}>
                {c.classLevelName || c.level || c.classLevelCode} {c.stream}
              </option>
            ))}
          </select>
        </div>

        <div className="w-full md:w-48 space-y-1">
          <label className="text-xs font-semibold text-slate-700">Attendance Date</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 focus:outline-none"
          />
        </div>
      </div>

      {/* Daily Submission Rule Notice Banner */}
      {isAlreadySubmitted && (
        <div className="bg-emerald-50 border border-emerald-200/90 text-emerald-950 p-4 rounded-2xl flex items-center justify-between text-xs font-semibold shadow-2xs animate-fade-in">
          <div className="flex items-center gap-3">
            <ShieldCheck className="w-5 h-5 text-emerald-600 shrink-0" />
            <div>
              <div className="font-bold text-sm text-emerald-900">Class Register Finalized for {date}</div>
              <div className="text-[11px] text-emerald-700 font-medium mt-0.5">
                Attendance roll call for this class has already been recorded today. Daily submission rule enforces one register entry per day.
              </div>
            </div>
          </div>
          <span className="px-3 py-1 rounded-lg bg-emerald-100 text-emerald-800 border border-emerald-300 font-mono font-bold uppercase text-[10px] shrink-0">
            Locked for {date}
          </span>
        </div>
      )}

      {/* Class Roll Call Sheet Table */}
      {selectedClass && students?.content && (
        <div className="bg-white border border-slate-200/90 rounded-2xl shadow-2xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
                <tr>
                  <th className="px-5 py-3.5">Student Name</th>
                  <th className="px-5 py-3.5 w-[420px]">Attendance Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {students.content.map((s: Student) => (
                  <tr key={s.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-5 py-3.5 font-bold text-slate-900">
                      <div>{s.firstName} {s.lastName}</div>
                      <div className="text-[11px] text-slate-400 font-mono font-medium mt-0.5">{s.studentNumber}</div>
                    </td>

                    <td className="px-5 py-3.5">
                      <div className={`flex bg-slate-100 p-1 rounded-xl w-fit gap-1 border border-slate-200/80 ${isAlreadySubmitted ? 'opacity-70 cursor-not-allowed' : ''}`}>
                        <StatusButton
                          disabled={isAlreadySubmitted}
                          active={register[s.id] === 'PRESENT'}
                          onClick={() => setStatus(s.id, 'PRESENT')}
                          activeClass="bg-emerald-600 text-white shadow-2xs font-bold"
                          defaultClass="text-slate-600 hover:bg-slate-200"
                        >
                          Present
                        </StatusButton>
                        <StatusButton
                          disabled={isAlreadySubmitted}
                          active={register[s.id] === 'ABSENT'}
                          onClick={() => setStatus(s.id, 'ABSENT')}
                          activeClass="bg-rose-600 text-white shadow-2xs font-bold"
                          defaultClass="text-slate-600 hover:bg-slate-200"
                        >
                          Absent
                        </StatusButton>
                        <StatusButton
                          disabled={isAlreadySubmitted}
                          active={register[s.id] === 'LATE'}
                          onClick={() => setStatus(s.id, 'LATE')}
                          activeClass="bg-amber-600 text-white shadow-2xs font-bold"
                          defaultClass="text-slate-600 hover:bg-slate-200"
                        >
                          Late
                        </StatusButton>
                        <StatusButton
                          disabled={isAlreadySubmitted}
                          active={register[s.id] === 'EXCUSED'}
                          onClick={() => setStatus(s.id, 'EXCUSED')}
                          activeClass="bg-indigo-600 text-white shadow-2xs font-bold"
                          defaultClass="text-slate-600 hover:bg-slate-200"
                        >
                          Excused
                        </StatusButton>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="p-4 border-t border-slate-200 bg-slate-50/50 flex justify-end items-center gap-3">
            {isAlreadySubmitted ? (
              <Button disabled variant="outline">
                <Lock className="w-3.5 h-3.5 text-slate-400" />
                <span>Already Submitted for {date}</span>
              </Button>
            ) : (
              <Button
                onClick={() => submitMutation.mutate()}
                disabled={submitMutation.isPending}
                variant="default"
                size="default"
              >
                {submitMutation.isPending ? (
                  <>
                    <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                    <span>Saving Register...</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Submit Class Register</span>
                  </>
                )}
              </Button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function StatusButton({ active, onClick, disabled, activeClass, defaultClass, children }: any) {
  return (
    <button
      disabled={disabled}
      onClick={onClick}
      className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${active ? activeClass : defaultClass} ${disabled ? 'cursor-not-allowed' : ''}`}
    >
      {children}
    </button>
  );
}
