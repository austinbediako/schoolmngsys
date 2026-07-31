import React from 'react';
import { Link } from 'wouter';
import { ChevronRight } from 'lucide-react';

interface PageHeaderProps {
  title: string;
  description?: string;
  breadcrumbs?: { label: string; href?: string }[];
  children?: React.ReactNode;
}

export function PageHeader({ title, description, breadcrumbs, children }: PageHeaderProps) {
  return (
    <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between mb-8 pb-4 border-b border-slate-200/80">
      <div className="flex flex-col gap-1 min-w-0">
        {breadcrumbs && breadcrumbs.length > 0 && (
          <nav className="flex items-center gap-1 text-xs font-medium text-slate-400 mb-1">
            {breadcrumbs.map((bc, i) => (
              <React.Fragment key={i}>
                {i > 0 && <ChevronRight className="w-3.5 h-3.5 text-slate-300 shrink-0" />}
                {bc.href ? (
                  <Link href={bc.href} className="hover:text-indigo-600 transition-colors truncate">
                    {bc.label}
                  </Link>
                ) : (
                  <span className="text-slate-700 font-semibold truncate">{bc.label}</span>
                )}
              </React.Fragment>
            ))}
          </nav>
        )}

        <h1 className="font-display text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900">
          {title}
        </h1>

        {description && (
          <p className="text-xs sm:text-sm text-slate-500 font-medium max-w-3xl leading-relaxed">
            {description}
          </p>
        )}
      </div>

      {children && (
        <div className="flex flex-wrap items-center gap-2.5 shrink-0 pt-2 md:pt-0">
          {children}
        </div>
      )}
    </div>
  );
}
