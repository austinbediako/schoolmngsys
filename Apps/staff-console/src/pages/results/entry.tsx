import React from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { Save, Grid, RefreshCw } from 'lucide-react';

export default function ScoreEntry() {
  const [selectedClass, setSelectedClass] = React.useState('');
  const [selectedSubject, setSelectedSubject] = React.useState('');
  const [selectedComponent, setSelectedComponent] = React.useState('');

  const [scores, setScores] = React.useState<Record<string, string>>({});

  const { data: students } = useQuery<any>({
    queryKey: ['students-by-class', selectedClass],
    queryFn: () => apiClient(`/students?classId=${selectedClass}&size=100`),
    enabled: !!selectedClass,
  });

  const handleScoreChange = (studentId: string, value: string) => {
    if (value && isNaN(Number(value))) return;
    setScores((prev) => ({ ...prev, [studentId]: value }));
  };

  const handleScoreBlur = async (studentId: string, value: string) => {
    if (!value || isNaN(Number(value))) return;
    try {
      const idempotencyKey = crypto.randomUUID();
      await apiClient('/results/scores', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey },
        body: JSON.stringify({ studentId, componentId: selectedComponent, score: Number(value) }),
      });
    } catch (e) {
      toast.error('Failed to save score for row. Please retry.');
    }
  };

  const submitBulkMutation = useMutation({
    mutationFn: async () => {
      const idempotencyKey = crypto.randomUUID();
      const payload = Object.entries(scores).map(([studentId, score]) => ({
        studentId,
        componentId: selectedComponent,
        score: Number(score),
      }));
      await apiClient('/results/scores/bulk', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey },
        body: JSON.stringify(payload),
      });
    },
    onSuccess: () => toast.success('All scores saved successfully.'),
    onError: handleApiError,
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title="Score Entry Grid"
        description="Spreadsheet-like score entry interface for continuous assessments (SBA) and examinations."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Results', href: '/results' }, { label: 'Entry' }]}
      />

      {/* Filter Parameters Header */}
      <div className="bg-white p-5 border border-slate-200/90 rounded-2xl shadow-2xs grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">Class Level</label>
          <select
            value={selectedClass}
            onChange={(e) => setSelectedClass(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-xl bg-white text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="">Select class...</option>
            <option value="c1">Primary 1A</option>
            <option value="c2">Primary 2A</option>
          </select>
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">Subject</label>
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-xl bg-white text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="">Select subject...</option>
            <option value="math">Mathematics</option>
            <option value="eng">English Language</option>
          </select>
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">Assessment Component</label>
          <select
            value={selectedComponent}
            onChange={(e) => setSelectedComponent(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-xl bg-white text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="">Select component...</option>
            <option value="ca1">Class Test 1 (20%)</option>
            <option value="exam">End of Term Exam (50%)</option>
          </select>
        </div>
      </div>

      {!selectedClass || !selectedSubject || !selectedComponent ? (
        <div className="bg-white rounded-2xl border border-slate-200/90 p-12 text-center flex flex-col items-center justify-center space-y-3">
          <div className="w-14 h-14 bg-slate-100 rounded-full flex items-center justify-center border border-slate-200">
            <Grid className="w-6 h-6 text-slate-400" />
          </div>
          <h3 className="font-display font-bold text-slate-900 text-base">Select parameters to load grid</h3>
          <p className="text-xs text-slate-500 font-medium max-w-sm">
            Choose a class, subject, and assessment component above to start entering scores.
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden flex flex-col">
          <div className="p-4 border-b border-slate-200 flex justify-between items-center bg-slate-50/50">
            <div className="text-xs font-semibold text-slate-700">
              Entering scores out of <span className="font-mono font-bold text-slate-900">20</span>
            </div>
            <Button
              onClick={() => submitBulkMutation.mutate(undefined)}
              disabled={submitBulkMutation.isPending}
              variant="default"
            >
              {submitBulkMutation.isPending ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  <span>Saving Scores...</span>
                </>
              ) : (
                <>
                  <Save className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Save All Scores</span>
                </>
              )}
            </Button>
          </div>

          <div className="overflow-x-auto max-h-[600px] overflow-y-auto">
            <table className="w-full text-xs text-left border-collapse">
              <thead className="bg-slate-50 sticky top-0 border-b border-slate-200 z-10 text-slate-500 font-bold uppercase tracking-wider">
                <tr>
                  <th className="px-5 py-3.5 w-16 text-center">#</th>
                  <th className="px-5 py-3.5">Student Name</th>
                  <th className="px-5 py-3.5 w-48 text-right">Score</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {students?.content?.map((s: any, i: number) => {
                  const val = scores[s.id] || '';
                  const isMissing = val === '';
                  return (
                    <tr key={s.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-5 py-3 text-center text-slate-400 font-mono">{i + 1}</td>
                      <td className="px-5 py-3 font-bold text-slate-900">{s.firstName} {s.lastName}</td>
                      <td className="px-5 py-3 text-right">
                        <input
                          type="text"
                          value={val}
                          onChange={(e) => handleScoreChange(s.id, e.target.value)}
                          onBlur={(e) => handleScoreBlur(s.id, e.target.value)}
                          className={`w-24 text-right px-3 py-1.5 border rounded-xl font-mono font-bold text-xs focus:outline-none focus:ring-2 focus:ring-slate-900 ${
                            isMissing ? 'bg-amber-50/60 border-amber-300 text-amber-900 placeholder-amber-400' : 'bg-white border-slate-300 text-slate-900'
                          }`}
                          placeholder="-"
                        />
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
