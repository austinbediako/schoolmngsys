import React from 'react';
import { useLocation, useParams } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Class, Student } from '@/types';
import { toast } from 'sonner';

export default function EnrollStudent() {
  const { id } = useParams<{ id: string }>();
  const [, setLocation] = useLocation();
  const [classId, setClassId] = React.useState('');
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  const { data: student } = useQuery<Student>({
    queryKey: ['student', id],
    queryFn: () => apiClient(`/students/${id}`)
  });

  const { data: classes } = useQuery<any>({
    queryKey: ['classes'],
    queryFn: () => apiClient('/academic/classes')
  });

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!classId) return;
    setIsSubmitting(true);
    try {
      await apiClient('/enrollments', {
        method: 'POST',
        body: JSON.stringify({ studentId: id, classId }),
      });
      toast.success('Student enrolled successfully');
      setLocation(`/students/${id}`);
    } catch (e) {
      handleApiError(e);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <PageHeader
        title="Enroll into Class"
        breadcrumbs={[
          { label: 'Students', href: '/students' },
          { label: student ? `${student.firstName} ${student.lastName}` : '', href: `/students/${id}` },
          { label: 'Enroll' }
        ]}
      />

      <form onSubmit={onSubmit} className="space-y-6 bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700">Class <span className="text-red-500">*</span></label>
          <select
            value={classId}
            onChange={e => setClassId(e.target.value)}
            required
            className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm bg-white"
          >
            <option value="">Select a class...</option>
            {classes?.content?.map((c: Class) => (
              <option key={c.id} value={c.id}>{c.level} {c.stream}</option>
            ))}
          </select>
        </div>

        <div className="pt-4 border-t border-slate-200 flex justify-end gap-3">
          <button
            type="button"
            onClick={() => setLocation(`/students/${id}`)}
            className="px-4 py-2 border border-slate-300 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting || !classId}
            className="px-4 py-2 bg-primary text-white rounded-md text-sm font-medium hover:bg-primary/90 disabled:opacity-50"
          >
            {isSubmitting ? 'Enrolling...' : 'Enroll Student'}
          </button>
        </div>
      </form>
    </div>
  );
}
