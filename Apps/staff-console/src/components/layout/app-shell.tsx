import React from 'react';
import { useAuth } from '@/contexts/auth-context';
import { Link, Redirect, useLocation } from 'wouter';
import { Icon } from '@/components/icon';
import { apiClient } from '@/lib/api-client';
import { Student } from '@/types';
import { Search, LogOut, Menu, User, X, ChevronRight } from 'lucide-react';

const navigation = [
  { name: 'Dashboard', href: '/dashboard', icon: 'dashboard-square-01', perm: null },
  { name: 'Students', href: '/students', icon: 'user-multiple', perm: 'STUDENT_VIEW' },
  { name: 'Attendance', href: '/attendance/summary', icon: 'calendar-check-in-01', perm: 'ATTENDANCE_VIEW' },
  { name: 'Results', href: '/results', icon: 'book-open-01', perm: 'RESULT_VIEW' },
  { name: 'Promotion', href: '/promotion', icon: 'chart-increase', perm: 'PROMOTION_VIEW' },
  { name: 'Finance', href: '/finance/reports', icon: 'wallet-01', perm: 'FINANCE_VIEW' },
  { name: 'Academic', href: '/academic/years', icon: 'settings-01', perm: 'ACADEMIC_MANAGE' },
  { name: 'Communication', href: '/communication/announcements', icon: 'notification-01', perm: 'COMMS_SEND' },
  { name: 'Admin', href: '/admin/accounts', icon: 'shield-01', perm: 'ADMIN_ACCOUNTS' },
];

export default function AppShell({ children }: { children: React.ReactNode }) {
  const { user, hasPermission, logout, isLoading } = useAuth();
  const [location, setLocation] = useLocation();
  const [sidebarOpen, setSidebarOpen] = React.useState(() => typeof window !== 'undefined' ? window.innerWidth >= 768 : true);

  // Header Quick Search State
  const [searchOpen, setSearchOpen] = React.useState(false);
  const [searchQuery, setSearchQuery] = React.useState('');
  const [searchResults, setSearchResults] = React.useState<Student[]>([]);
  const [searching, setSearching] = React.useState(false);

  React.useEffect(() => {
    if (window.innerWidth < 768) setSidebarOpen(false);
  }, [location]);

  // Cmd+K / Ctrl+K keyboard shortcut to open search; ESC key to close
  React.useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setSearchOpen((prev) => !prev);
      } else if (e.key === 'Escape') {
        setSearchOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Fetch live search results when query changes
  React.useEffect(() => {
    if (!searchOpen) return;
    const timer = setTimeout(() => {
      setSearching(true);
      apiClient(`/students?page=0&size=8&search=${encodeURIComponent(searchQuery)}`)
        .then((res: any) => {
          setSearchResults(res?.content || []);
        })
        .catch(() => setSearchResults([]))
        .finally(() => setSearching(false));
    }, 150);

    return () => clearTimeout(timer);
  }, [searchQuery, searchOpen]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white">
        <div className="w-12 h-12 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center p-2 animate-pulse mb-3">
          <img src="/logo.png" alt="UBS Logo" className="w-full h-full object-contain" />
        </div>
        <p className="text-xs font-semibold text-slate-400 tracking-wider uppercase">Loading UBS-LMIS Portal...</p>
      </div>
    );
  }

  if (!user) return <Redirect to="/login" />;

  const navItems = navigation.filter((item) => !item.perm || hasPermission(item.perm));
  const userRolesDisplay = user.roles ? user.roles.map(r => r.replace(/_/g, ' ')).join(' • ') : (user.role || 'Staff');

  const handleSelectStudent = (studentId: string) => {
    setSearchOpen(false);
    setSearchQuery('');
    setLocation(`/students/${studentId}`);
  };

  return (
    <div className="h-screen w-screen bg-[#f8fafc] text-slate-900 flex flex-col font-sans overflow-hidden">
      {/* Top Header Navbar */}
      <header className="h-14 bg-white border-b border-slate-200/90 flex items-center justify-between px-4 z-30 shrink-0">
        <div className="flex items-center gap-3 min-w-0">
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-1.5 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 border border-transparent transition-all shrink-0"
            aria-label="Toggle navigation"
          >
            <Menu className="w-5 h-5" />
          </button>

          <Link href="/dashboard" className="flex items-center gap-2.5 group min-w-0">
            <img
              src="/logo.png"
              alt="Unibridge Basic School Logo"
              className="w-8 h-8 rounded-lg object-contain shrink-0"
            />
            <div className="flex items-center gap-2 min-w-0">
              <span className="font-bold text-slate-900 text-sm tracking-tight truncate">
                Unibridge Basic
              </span>
              <span className="hidden sm:inline-block px-2 py-0.5 rounded-full text-[11px] font-medium bg-slate-100 text-slate-600 border border-slate-200 shrink-0">
                UBS-LMIS System
              </span>
            </div>
          </Link>
        </div>

        {/* Header Right Actions */}
        <div className="flex items-center gap-3 shrink-0">
          {/* Interactive Header Quick Search Input */}
          <button
            onClick={() => setSearchOpen(true)}
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-100/90 hover:bg-slate-200/70 border border-slate-200 text-slate-500 text-xs font-medium w-48 sm:w-56 lg:w-64 transition-all text-left"
          >
            <Search className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="flex-1 truncate">Search students...</span>
            <kbd className="hidden sm:inline-block px-1.5 py-0.5 text-[10px] font-semibold text-slate-500 bg-white border border-slate-200 rounded">
              ⌘K
            </kbd>
          </button>

          <div className="h-5 w-px bg-slate-200 hidden sm:block" />

          {/* User Profile */}
          <div className="flex items-center gap-2.5">
            <div className="hidden sm:flex flex-col text-right">
              <span className="text-xs font-semibold text-slate-900 leading-tight">
                {user.firstName} {user.lastName}
              </span>
              <span className="text-[10px] text-slate-500 font-medium capitalize">
                {userRolesDisplay}
              </span>
            </div>

            <div className="w-8 h-8 rounded-full bg-slate-800 text-white text-xs font-bold flex items-center justify-center shrink-0 border border-slate-700">
              {user.firstName[0]}{user.lastName[0]}
            </div>

            <button
              onClick={logout}
              title="Logout"
              className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-all ml-0.5"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Quick Search Palette Modal */}
      {searchOpen && (
        <div className="fixed inset-0 z-50 flex items-start justify-center pt-16 sm:pt-24 px-4 bg-slate-950/60 backdrop-blur-xs animate-fade-in">
          <div
            className="fixed inset-0"
            onClick={() => setSearchOpen(false)}
          />

          <div className="relative w-full max-w-xl bg-white rounded-2xl border border-slate-200 shadow-2xl overflow-hidden z-10 space-y-0">
            {/* Search Input Bar */}
            <div className="p-3 border-b border-slate-200 flex items-center gap-3 bg-slate-50/50">
              <Search className="w-4 h-4 text-slate-400 ml-1 shrink-0" />
              <input
                type="text"
                autoFocus
                placeholder="Type student name or ID number (e.g. Kwame, UBS-2024)..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 bg-transparent text-sm font-semibold text-slate-900 placeholder:text-slate-400 focus:outline-none"
              />
              {searchQuery && (
                <button onClick={() => setSearchQuery('')} className="p-1 text-slate-400 hover:text-slate-600">
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
              <button
                onClick={() => setSearchOpen(false)}
                className="text-[10px] font-semibold text-slate-400 hover:text-slate-600 bg-white hover:bg-slate-100 px-2 py-0.5 border border-slate-200 rounded transition-colors"
                title="Press Esc to close"
              >
                ESC
              </button>
            </div>

            {/* Live Search Results */}
            <div className="max-h-80 overflow-y-auto p-2 divide-y divide-slate-100">
              {searching ? (
                <div className="p-8 text-center text-xs font-semibold text-slate-400">Searching directory...</div>
              ) : searchResults.length === 0 ? (
                <div className="p-8 text-center space-y-1">
                  <User className="w-6 h-6 text-slate-300 mx-auto" />
                  <p className="text-xs font-semibold text-slate-600">No students found matching "{searchQuery}"</p>
                  <p className="text-[11px] text-slate-400">Try searching by first name, last name, or student number.</p>
                </div>
              ) : (
                searchResults.map((s) => (
                  <button
                    key={s.id}
                    onClick={() => handleSelectStudent(s.id)}
                    className="w-full p-3 flex items-center justify-between text-left hover:bg-slate-50 rounded-xl transition-colors group"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-900 text-white font-bold text-xs flex items-center justify-center shrink-0">
                        {s.firstName[0]}{s.lastName[0]}
                      </div>
                      <div>
                        <div className="font-bold text-slate-900 text-xs group-hover:text-indigo-600 transition-colors">
                          {s.firstName} {s.otherName ? `${s.otherName} ` : ''}{s.lastName}
                        </div>
                        <div className="text-[11px] text-slate-500 font-medium mt-0.5">
                          ID: <span className="font-mono">{s.studentNumber}</span> • Class: {s.currentClassId || 'Unassigned'}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        s.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-slate-100 text-slate-600'
                      }`}>
                        {s.status}
                      </span>
                      <ChevronRight className="w-4 h-4 text-slate-400 group-hover:translate-x-0.5 transition-transform" />
                    </div>
                  </button>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* Main Body Container: Fixed Height calc(100vh - 3.5rem) */}
      <div className="flex flex-1 h-[calc(100vh-3.5rem)] overflow-hidden relative">
        {/* Mobile Backdrop */}
        {sidebarOpen && (
          <div
            onClick={() => setSidebarOpen(false)}
            className="fixed inset-0 top-14 bg-slate-950/50 z-30 md:hidden"
            aria-hidden="true"
          />
        )}

        {/* Sidebar: Rich Deep Navy Blue Theme matching Logo */}
        <aside
          className={`bg-[#0B132B] text-slate-300 flex flex-col border-r border-[#1C2A4E]
            fixed md:static inset-y-0 left-0 top-14 md:top-0 z-40 h-full shrink-0
            transition-all duration-200 ease-in-out
            ${sidebarOpen ? 'translate-x-0 w-60' : '-translate-x-full md:translate-x-0 md:w-16'}`}
        >
          <nav className="flex-1 py-3 space-y-1 px-2.5 overflow-y-auto">
            {navItems.map((item) => {
              const sectionRoot = '/' + item.href.split('/')[1];
              const isActive = location.startsWith(item.href) || (sectionRoot !== '/' && location.startsWith(sectionRoot));
              const collapsedOnDesktop = !sidebarOpen;

              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`group relative flex items-center px-3 py-2.5 text-xs font-semibold rounded-xl transition-all duration-150 ${
                    isActive
                      ? 'bg-[#1C2A4E] text-white font-bold border border-blue-400/30 shadow-2xs'
                      : 'text-slate-300 hover:text-white hover:bg-[#162244] border border-transparent'
                  } ${collapsedOnDesktop ? 'md:justify-center md:px-0' : ''}`}
                  title={collapsedOnDesktop ? item.name : undefined}
                >
                  <Icon
                    name={item.icon}
                    className={`flex-shrink-0 transition-transform duration-150 group-hover:scale-105 ${
                      isActive ? 'text-blue-400' : 'text-slate-400 group-hover:text-slate-200'
                    } ${collapsedOnDesktop ? 'w-5 h-5' : 'w-4 h-4 mr-3'}`}
                  />

                  <span className={`truncate ${collapsedOnDesktop ? 'md:hidden' : ''}`}>
                    {item.name}
                  </span>

                  {isActive && !collapsedOnDesktop && (
                    <span className="w-1.5 h-1.5 rounded-full bg-white ml-auto shrink-0" />
                  )}
                </Link>
              );
            })}
          </nav>

          <div className="p-3 border-t border-[#1C2A4E] text-[11px] text-slate-400 flex items-center justify-between">
            <span className={!sidebarOpen ? 'md:hidden' : ''}>UBS-LMIS System</span>
            <span className="w-2 h-2 rounded-full bg-emerald-500" title="Online" />
          </div>
        </aside>

        {/* Scrollable Main Content */}
        <main className="flex-1 h-full overflow-y-auto outline-none focus:outline-none min-w-0 bg-[#f8fafc]">
          {children}
        </main>
      </div>
    </div>
  );
}
