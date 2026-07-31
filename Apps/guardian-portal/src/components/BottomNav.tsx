import React from 'react';
import { useLocation, Link } from 'wouter';
import { Home, Calendar, Award, CreditCard, Megaphone } from 'lucide-react';

export const BottomNav: React.FC = () => {
  const [location] = useLocation();

  const navItems = [
    { href: '/', label: 'Overview', icon: Home },
    { href: '/report-cards', label: 'Reports', icon: Award },
    { href: '/attendance', label: 'Attendance', icon: Calendar },
    { href: '/finance', label: 'Fees', icon: CreditCard },
    { href: '/announcements', label: 'Notices', icon: Megaphone }
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200 px-3 py-2 pb-safe shadow-lg">
      <div className="max-w-md mx-auto flex items-center justify-around">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location === item.href;
          return (
            <Link key={item.href} href={item.href}>
              <div
                className={`flex flex-col items-center gap-1 px-3 py-1.5 rounded-xl cursor-pointer transition-all ${
                  isActive
                    ? 'text-slate-900 font-bold bg-slate-100'
                    : 'text-slate-500 hover:text-slate-900'
                }`}
              >
                <Icon className={`w-5 h-5 ${isActive ? 'text-amber-600' : 'text-slate-500'}`} />
                <span className="text-[10px] font-semibold tracking-tight">{item.label}</span>
              </div>
            </Link>
          );
        })}
      </div>
    </nav>
  );
};
