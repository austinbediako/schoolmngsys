import React from 'react';
import { useParams } from 'wouter';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { Button } from '@/components/ui/button';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { toast } from 'sonner';
import { Check, Plus, RefreshCw, CheckSquare, BookOpen, Layers, CheckCircle2 } from 'lucide-react';

interface SubjectItem {
  id: string;
  name: string;
  code: string;
}

interface SubjectCategory {
  id: string;
  name: string;
  description: string;
  subjectCodes: string[];
}

const SUBJECT_CATEGORIES: SubjectCategory[] = [
  {
    id: 'LANG',
    name: 'Languages & Literacy',
    description: 'English, Ghanaian Languages, French, Arabic & Early Literacy',
    subjectCodes: ['ENG', 'GHL', 'FRE', 'ARAB', 'LIT'],
  },
  {
    id: 'STEM',
    name: 'STEM & Digital Skills',
    description: 'Mathematics, Sciences, Computing & Early Numeracy',
    subjectCodes: ['MATH', 'SCI', 'ISCI', 'ICT', 'NUM'],
  },
  {
    id: 'HUM',
    name: 'Humanities & Social Sciences',
    description: 'Our World Our People, RME, History of Ghana & Social Studies',
    subjectCodes: ['OWOP', 'RME', 'HIST', 'SOC'],
  },
  {
    id: 'ARTS',
    name: 'Creative Arts & Life Skills',
    description: 'Creative Arts, Career Tech, Design & Physical Education',
    subjectCodes: ['CART', 'CAD', 'CTECH', 'PE'],
  },
];

const DEFAULT_SUBJECTS_BY_LEVEL: Record<string, string[]> = {
  N1: ['LIT', 'NUM', 'CART', 'OWOP'],
  N2: ['LIT', 'NUM', 'CART', 'OWOP'],
  KG1: ['LIT', 'NUM', 'CART', 'OWOP'],
  KG2: ['LIT', 'NUM', 'CART', 'OWOP'],
  B1: ['ENG', 'MATH', 'SCI', 'GHL', 'CART', 'OWOP', 'RME', 'HIST', 'PE'],
  B2: ['ENG', 'MATH', 'SCI', 'GHL', 'CART', 'OWOP', 'RME', 'HIST', 'PE'],
  B3: ['ENG', 'MATH', 'SCI', 'GHL', 'CART', 'OWOP', 'RME', 'HIST', 'PE'],
  B4: ['ENG', 'MATH', 'SCI', 'ICT', 'GHL', 'FRE', 'CART', 'OWOP', 'RME', 'HIST', 'PE'],
  B5: ['ENG', 'MATH', 'SCI', 'ICT', 'GHL', 'FRE', 'CART', 'OWOP', 'RME', 'HIST', 'PE'],
  B6: ['ENG', 'MATH', 'SCI', 'ICT', 'GHL', 'FRE', 'CART', 'OWOP', 'RME', 'HIST', 'PE'],
  B7: ['ENG', 'MATH', 'ISCI', 'SOC', 'ICT', 'CTECH', 'CAD', 'GHL', 'FRE', 'RME', 'PE', 'ARAB'],
  B8: ['ENG', 'MATH', 'ISCI', 'SOC', 'ICT', 'CTECH', 'CAD', 'GHL', 'FRE', 'RME', 'PE', 'ARAB'],
  B9: ['ENG', 'MATH', 'ISCI', 'SOC', 'ICT', 'CTECH', 'CAD', 'GHL', 'FRE', 'RME', 'PE', 'ARAB'],
};

export default function ClassSubjects() {
  const { id: classId } = useParams<{ id: string }>();
  const queryClient = useQueryClient();

  const { data: classes } = useQuery<any>({
    queryKey: ['classes'],
    queryFn: () => apiClient('/classes'),
  });
  const classesList = Array.isArray(classes) ? classes : (classes?.content || []);
  const cls = classesList.find((c: any) => c.id === classId);

  const { data: subjects } = useQuery<any>({
    queryKey: ['subjects'],
    queryFn: () => apiClient('/subjects'),
  });
  const subjectsList: SubjectItem[] = Array.isArray(subjects) ? subjects : (subjects?.content || []);

  const { data: academicYears } = useQuery<any>({
    queryKey: ['academic-years'],
    queryFn: () => apiClient('/academic-years'),
  });
  const academicYearsList = Array.isArray(academicYears) ? academicYears : (academicYears?.content || []);
  const activeYear = academicYearsList.find((y: any) => y.status === 'ACTIVE') || academicYearsList[0];

  const [selectedSubjectIds, setSelectedSubjectIds] = React.useState<string[]>([]);
  const [selectedYearId, setSelectedYearId] = React.useState<string>('');

  React.useEffect(() => {
    if (activeYear?.id && !selectedYearId) {
      setSelectedYearId(activeYear.id);
    }
  }, [activeYear, selectedYearId]);

  // Query existing offerings to identify already-assigned subjects
  const { data: existingOfferings } = useQuery<any>({
    queryKey: ['subject-offerings', classId, selectedYearId],
    queryFn: () => apiClient(`/classes/${classId}/subject-offerings?academicYearId=${selectedYearId}`),
    enabled: Boolean(classId && selectedYearId),
  });
  const existingList = Array.isArray(existingOfferings) ? existingOfferings : [];
  const assignedSubjectIds = new Set(existingList.map((offering: any) => offering.subjectId));

  const classCode = cls?.classLevelCode || '';
  const defaultCodes = DEFAULT_SUBJECTS_BY_LEVEL[classCode] || [];

  const autoSelectDefaultSubjects = () => {
    if (defaultCodes.length === 0) return;
    const matchingIds = subjectsList
      .filter((s) => defaultCodes.includes(s.code) && !assignedSubjectIds.has(s.id))
      .map((s) => s.id);

    if (matchingIds.length === 0) {
      toast.info(`All default NaCCA subjects are already assigned to ${cls?.classLevelName || classCode}`);
      return;
    }

    setSelectedSubjectIds(matchingIds);
    toast.success(`Selected ${matchingIds.length} new default subjects for ${cls?.classLevelName || classCode}`);
  };

  const toggleSubject = (subjectId: string) => {
    if (assignedSubjectIds.has(subjectId)) {
      toast.info('This subject is already offered to this class for the selected academic year.');
      return;
    }
    setSelectedSubjectIds((prev) =>
      prev.includes(subjectId) ? prev.filter((id) => id !== subjectId) : [...prev, subjectId]
    );
  };

  const selectAllCategorySubjects = (categoryCodes: string[]) => {
    const matchingIds = subjectsList
      .filter((s) => categoryCodes.includes(s.code) && !assignedSubjectIds.has(s.id))
      .map((s) => s.id);
    setSelectedSubjectIds((prev) => Array.from(new Set([...prev, ...matchingIds])));
  };

  const bulkAssignMutation = useMutation({
    mutationFn: async () => {
      if (!selectedYearId) {
        throw new Error('Please select an active academic year');
      }

      // Filter out already assigned subjects
      const toAssign = selectedSubjectIds.filter((id) => !assignedSubjectIds.has(id));

      if (toAssign.length === 0) {
        toast.info('Selected subjects are already assigned to this class.');
        setSelectedSubjectIds([]);
        return;
      }

      let successCount = 0;
      let duplicateCount = 0;

      for (const subjectId of toAssign) {
        try {
          await apiClient(`/classes/${classId}/subject-offerings`, {
            method: 'POST',
            body: JSON.stringify({
              subjectId,
              academicYearId: selectedYearId,
            }),
          });
          successCount++;
        } catch (err: any) {
          if (err?.status === 422 || err?.message?.includes('already offered')) {
            duplicateCount++;
          } else {
            throw err;
          }
        }
      }

      if (successCount > 0) {
        toast.success(`Assigned ${successCount} new subject(s) to ${cls?.classLevelName || 'Class'}`);
      }
      if (duplicateCount > 0) {
        toast.warning(`${duplicateCount} subject(s) were already assigned and skipped.`);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subject-offerings', classId, selectedYearId] });
      queryClient.invalidateQueries({ queryKey: ['classes'] });
      setSelectedSubjectIds([]);
    },
    onError: (err) => handleApiError(err),
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title={`NaCCA Subject Offering — ${cls ? `${cls.classLevelName || cls.classLevelCode} ${cls.stream}` : 'Class'}`}
        description="Configure curriculum subject offerings grouped by domain category with duplicate prevention."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Classes', href: '/academic/classes' },
          { label: cls ? `${cls.classLevelName || cls.classLevelCode} ${cls.stream}` : 'Class' },
        ]}
      />

      {/* Control Bar */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-2xs space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-100 pb-4">
          <div className="space-y-1">
            <h2 className="font-display font-bold text-slate-900 text-sm">Academic Year Target</h2>
            <p className="text-xs text-slate-500">
              Selected subjects will be assigned to this class for assessment scoring.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <select
              value={selectedYearId}
              onChange={(e) => setSelectedYearId(e.target.value)}
              className="px-3 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 bg-white focus:outline-none"
            >
              <option value="">Select Academic Year...</option>
              {academicYearsList.map((y: any) => (
                <option key={y.id} value={y.id}>
                  {y.name || y.label} {y.status === 'ACTIVE' ? '(Active)' : ''}
                </option>
              ))}
            </select>

            {defaultCodes.length > 0 && (
              <Button
                onClick={autoSelectDefaultSubjects}
                variant="outline"
                className="border-emerald-200 bg-emerald-50 text-emerald-800 hover:bg-emerald-100 font-bold text-xs"
              >
                <span>Auto-Select Default {cls?.classLevelName || classCode} Subjects</span>
              </Button>
            )}
          </div>
        </div>

        {/* Selected Counter & Save Action */}
        <div className="flex items-center justify-between pt-1">
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-700">
            <CheckSquare className="w-4 h-4 text-indigo-600" />
            <span>
              {selectedSubjectIds.length} {selectedSubjectIds.length === 1 ? 'New Subject' : 'New Subjects'} Selected
            </span>
            {assignedSubjectIds.size > 0 && (
              <span className="text-[11px] font-medium text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                {assignedSubjectIds.size} Already Offered
              </span>
            )}
          </div>

          <div className="flex items-center gap-2">
            {selectedSubjectIds.length > 0 && (
              <button
                type="button"
                onClick={() => setSelectedSubjectIds([])}
                className="text-xs font-semibold text-slate-500 hover:text-slate-700 hover:underline mr-2"
              >
                Clear Selection
              </button>
            )}

            <Button
              onClick={() => bulkAssignMutation.mutate()}
              disabled={bulkAssignMutation.isPending || selectedSubjectIds.length === 0}
              variant="default"
            >
              {bulkAssignMutation.isPending ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin mr-1.5" />
                  <span>Saving...</span>
                </>
              ) : (
                <>
                  <Plus className="w-4 h-4 text-emerald-400 mr-1.5" />
                  <span>Save Selected Subjects ({selectedSubjectIds.length})</span>
                </>
              )}
            </Button>
          </div>
        </div>
      </div>

      {/* Categorized Subject Grid (Unique Subjects Only with Duplicate Badges) */}
      <div className="space-y-6">
        {SUBJECT_CATEGORIES.map((cat) => {
          const catSubjects = subjectsList.filter((s) => cat.subjectCodes.includes(s.code));
          if (catSubjects.length === 0) return null;

          return (
            <div
              key={cat.id}
              className="bg-white rounded-2xl border border-slate-200/90 transition-all shadow-2xs overflow-hidden"
            >
              {/* Category Header */}
              <div className="p-4 bg-slate-50/80 border-b border-slate-200/80 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <div className="p-2 rounded-lg bg-indigo-50 text-indigo-700 font-bold text-xs">
                    <Layers className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="font-display font-bold text-slate-900 text-sm">{cat.name}</h3>
                    <p className="text-[11px] text-slate-500 font-medium">{cat.description}</p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => selectAllCategorySubjects(cat.subjectCodes)}
                  className="text-xs font-bold text-indigo-600 hover:text-indigo-800 hover:underline"
                >
                  Select Unassigned {cat.name}
                </button>
              </div>

              {/* Subject Badges Grid */}
              <div className="p-5 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {catSubjects.map((s) => {
                  const isAssigned = assignedSubjectIds.has(s.id);
                  const isSelected = selectedSubjectIds.includes(s.id);
                  const isDefaultForClass = defaultCodes.includes(s.code);

                  return (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => toggleSubject(s.id)}
                      className={`p-3 rounded-xl border text-left transition-all flex items-start justify-between gap-2 active:scale-98 ${
                        isAssigned
                          ? 'bg-emerald-50/60 border-emerald-300 text-emerald-950 font-bold cursor-default'
                          : isSelected
                          ? 'bg-indigo-50/80 border-indigo-500 text-indigo-950 shadow-2xs font-bold'
                          : 'bg-white border-slate-200/90 text-slate-700 hover:border-slate-300 hover:bg-slate-50 font-semibold'
                      }`}
                    >
                      <div>
                        <div className="flex items-center gap-1.5 flex-wrap">
                          <span className="text-xs">{s.name}</span>
                          {isAssigned ? (
                            <span className="px-1.5 py-0.2 rounded text-[9px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-300 inline-flex items-center gap-0.5">
                              <CheckCircle2 className="w-2.5 h-2.5 text-emerald-600" /> Offered
                            </span>
                          ) : isDefaultForClass ? (
                            <span className="px-1.5 py-0.2 rounded text-[9px] font-bold bg-blue-100 text-blue-800">
                              Default
                            </span>
                          ) : null}
                        </div>
                        <div className="text-[10px] font-mono text-slate-400 mt-0.5">Code: {s.code}</div>
                      </div>

                      <div
                        className={`w-5 h-5 rounded-md border flex items-center justify-center shrink-0 transition-colors ${
                          isAssigned
                            ? 'bg-emerald-600 border-emerald-600 text-white'
                            : isSelected
                            ? 'bg-indigo-600 border-indigo-600 text-white'
                            : 'border-slate-300 bg-white text-transparent'
                        }`}
                      >
                        <Check className="w-3.5 h-3.5 stroke-[3]" />
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
