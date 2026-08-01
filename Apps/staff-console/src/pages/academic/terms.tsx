import React from 'react';
import { useParams } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '@/components/page-header';
import { apiClient } from '@/lib/api-client';

export default function AcademicTerms() {
  const { id: yearId } = useParams<{ id: string }>();

  const { data: years } = useQuery<any>({
    queryKey: ['academic-years'],
    queryFn: () => apiClient('/academic-years'),
  });
  const yearsList = Array.isArray(years) ? years : (years?.content || []);
  const year = yearsList.find((y: any) => y.id === yearId);

  const { data: terms, isLoading } = useQuery<any>({
    queryKey: ['academic-terms', yearId],
    queryFn: () => apiClient(`/academic-years/${yearId}/terms`),
    enabled: Boolean(yearId),
  });
  const termsList = Array.isArray(terms) ? terms : [];

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-5xl mx-auto space-y-6 animate-fade-in">
      <PageHeader
        title={`Academic Terms — ${year?.label || year?.name || 'Year Session'}`}
        description="View the three-term calendar structure for this academic year session."
        breadcrumbs={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Academic Years', href: '/academic/years' },
          { label: year?.label || year?.name || 'Terms' },
        ]}
      />

      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden">
        <table className="w-full text-xs text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
            <tr>
              <th className="px-5 py-3.5">Term</th>
              <th className="px-5 py-3.5">Official Start Date</th>
              <th className="px-5 py-3.5">Official End Date</th>
              <th className="px-5 py-3.5">Expected School Days</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium">
            {isLoading ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  Loading terms...
                </td>
              </tr>
            ) : termsList.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-5 py-8 text-center text-slate-400">
                  No terms configured for this academic year.
                </td>
              </tr>
            ) : (
              termsList.map((t: any) => (
                <tr key={t.id || t.termNumber} className="hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-3.5 font-bold text-slate-900 text-sm">Term {t.termNumber}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{t.officialStartDate || t.startDate || '—'}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{t.officialEndDate || t.endDate || '—'}</td>
                  <td className="px-5 py-3.5 text-slate-600 font-mono">{t.expectedSchoolDays} Days</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
