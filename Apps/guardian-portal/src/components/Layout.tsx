import React from 'react';
import { Header } from './Header';
import { BottomNav } from './BottomNav';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col pb-20">
      <Header />
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 space-y-6">
        {children}
      </main>
      <BottomNav />
    </div>
  );
};
