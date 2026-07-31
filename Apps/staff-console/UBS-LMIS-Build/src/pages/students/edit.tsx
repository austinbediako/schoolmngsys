import React from 'react';
import { useForm } from 'react-hook-form';
import { useLocation, useParams } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { Student } from '@/types';
import { toast } from 'sonner';

export default function EditStudent() {
  const { id } = useParams<{ id: string }>();
  const [, setLocation] = useLocation();

  const { data: student, isLoading } = useQuery<Student>({
    queryKey: ['student', id],
    queryFn: () => apiClient(`/students/${id}`)
  });

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();

  React.useEffect(() => {
    if (student) reset(student);
  }, [student, reset]);

  const onSubmit = async (data: any) => {
    try {
      await apiClient(`/students/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...data, version: student?.version }),
      });
      toast.success('Student updated successfully');
      setLocation(`/students/${id}`);
    } catch (e) {
      handleApiError(e);
    }
  };

  if (isLoading) return <div className="p-8">Loading...</div>;

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <PageHeader
        title="Edit Student"
        breadcrumbs={[
          { label: 'Students', href: '/students' },
          { label: student ? `${student.firstName} ${student.lastName}` : '', href: `/students/${id}` },
          { label: 'Edit' }
        ]}
      />

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8 bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">First Name <span className="text-red-500">*</span></label>
            <input
              {...register('firstName', { required: true })}
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Last Name <span className="text-red-500">*</span></label>
            <input
              {...register('lastName', { required: true })}
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Other Names</label>
            <input
              {...register('otherName')}
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Gender <span className="text-red-500">*</span></label>
            <select
              {...register('gender', { required: true })}
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm bg-white"
            >
              <option value="M">Male</option>
              <option value="F">Female</option>
            </select>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Date of Birth <span className="text-red-500">*</span></label>
            <input
              type="date"
              {...register('dateOfBirth', { required: true })}
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Nationality <span className="text-red-500">*</span></label>
            <input
              {...register('nationality', { required: true })}
              className="w-full px-3 py-2 border border-slate-300 rounded-md focus:ring-primary focus:border-primary sm:text-sm"
            />
          </div>
        </div>

        <div className="pt-6 border-t border-slate-200 flex justify-end gap-3">
          <button
            type="button"
            onClick={() => setLocation(`/students/${id}`)}
            className="px-4 py-2 border border-slate-300 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-4 py-2 bg-primary text-white rounded-md text-sm font-medium hover:bg-primary/90 disabled:opacity-50"
          >
            {isSubmitting ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  );
}
