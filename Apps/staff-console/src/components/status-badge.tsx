import React from 'react';

type StatusType = 'ACTIVE' | 'INACTIVE' | 'CLOSED' | 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'PUBLISHED' | 'PENDING' | 'FAILED' | 'COMPLETED' | 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED' | string;

export function StatusBadge({ status, className = '' }: { status: StatusType, className?: string }) {
  let badgeStyle = 'bg-slate-100 text-slate-700 border-slate-200/80';
  let dotColor = 'bg-slate-400';
  
  const s = status.toUpperCase();
  if (['ACTIVE', 'APPROVED', 'PUBLISHED', 'COMPLETED', 'PRESENT'].includes(s)) {
    badgeStyle = 'bg-emerald-50 text-emerald-700 border-emerald-200/80';
    dotColor = 'bg-emerald-500';
  } else if (['DRAFT', 'PENDING', 'SUBMITTED', 'LATE', 'EXCUSED'].includes(s)) {
    badgeStyle = 'bg-amber-50 text-amber-700 border-amber-200/80';
    dotColor = 'bg-amber-500';
  } else if (['INACTIVE', 'CLOSED', 'FAILED', 'ABSENT'].includes(s)) {
    badgeStyle = 'bg-rose-50 text-rose-700 border-rose-200/80';
    dotColor = 'bg-rose-500';
  }

  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold tracking-tight border shadow-2xs ${badgeStyle} ${className}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${dotColor}`} />
      <span>{status}</span>
    </span>
  );
}
