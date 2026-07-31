import React from 'react';
import { Link, useLocation } from 'wouter';

export type SectionTab = { label: string; href: string };

export function SectionTabs({ tabs }: { tabs: SectionTab[] }) {
  const [location] = useLocation();

  return (
    <div className="flex items-center gap-1.5 p-1.5 bg-slate-200/60 rounded-2xl border border-slate-200/80 mb-6 overflow-x-auto custom-scrollbar">
      {tabs.map((tab) => {
        const isActive = location === tab.href || location.startsWith(tab.href + '/');
        return (
          <Link
            key={tab.href}
            href={tab.href}
            className={`px-4 py-2 text-xs font-bold rounded-xl transition-all duration-200 shrink-0 ${
              isActive
                ? 'bg-white text-indigo-700 shadow-sm shadow-slate-200/60 border border-slate-200/80'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80'
            }`}
          >
            {tab.label}
          </Link>
        );
      })}
    </div>
  );
}
