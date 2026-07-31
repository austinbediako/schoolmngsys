import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { useAuth } from '@/contexts/auth-context';
import { Link } from 'wouter';
import {
  Users,
  CalendarCheck,
  CreditCard,
  Clock,
  Plus,
  BookOpen,
  BellRing,
  ChevronRight,
  Calendar,
  AlertCircle,
  FileText
} from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuth();

  const { data: dashboardData, isLoading: isDashboardLoading } = useQuery({
    queryKey: ['dashboard-head'],
    queryFn: () => apiClient('/dashboard/head'),
  });

  const { data: announcementsData } = useQuery({
    queryKey: ['announcements'],
    queryFn: () => apiClient('/announcements'),
  });

  const { data: academicYearsData } = useQuery({
    queryKey: ['academic-years'],
    queryFn: () => apiClient('/academic-years'),
  });

  const activeYearName = dashboardData?.activeAcademicYearName ?? 'No Active Year';
  const totalEnrollment = dashboardData?.enrollment?.totalActiveEnrollments ?? 0;
  const attendanceRate = dashboardData?.attendance?.attendanceRatePercentage ?? 0;
  const presentCount = dashboardData?.attendance?.presentCount ?? 0;
  const totalAttendanceRecords = dashboardData?.attendance?.totalRecords ?? 0;
  const feeCollectionRate = dashboardData?.finance?.collectionPercentage ?? 0;
  const totalCollected = dashboardData?.finance?.totalCollectedAmount ?? 0;
  const totalResults = dashboardData?.resultsDistribution?.totalResultsCount ?? 0;

  const announcements = Array.isArray(announcementsData) ? announcementsData : [];
  const academicYears = Array.isArray(academicYearsData?.content) ? academicYearsData.content : [];

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-6">
      {/* Clean Professional Staff Header */}
      <div className="bg-white rounded-2xl border border-slate-200/90 p-6 shadow-2xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h1 className="font-display text-2xl font-bold tracking-tight text-slate-900">
              Welcome back, {user?.firstName || 'Staff'}
            </h1>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
              {activeYearName}
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 font-medium">
            University Basic School Management Console. Operational summary driven live by the system.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-3 shrink-0">
          <Link
            href="/attendance/mark"
            className="inline-flex items-center justify-center gap-2 px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-semibold text-xs shadow-xs transition-all active:scale-98"
          >
            <CalendarCheck className="w-4 h-4 text-emerald-400" />
            <span>Mark Attendance</span>
          </Link>

          <Link
            href="/students/new"
            className="inline-flex items-center justify-center gap-2 px-4 py-2 rounded-xl bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs border border-slate-300 shadow-2xs transition-all active:scale-98"
          >
            <Plus className="w-4 h-4 text-slate-500" />
            <span>Register Student</span>
          </Link>
        </div>
      </div>

      {/* Metric Cards Grid - Live Backend Data Only */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Total Active Enrollment"
          value={isDashboardLoading ? '-' : totalEnrollment}
          subtitle={`${dashboardData?.enrollment?.maleCount ?? 0} Male • ${dashboardData?.enrollment?.femaleCount ?? 0} Female`}
          badgeText="Live Students"
          badgeColor="bg-slate-100 text-slate-700 border-slate-200"
          icon={<Users className="w-5 h-5 text-indigo-600" />}
        />

        <MetricCard
          title="Attendance Rate"
          value={isDashboardLoading ? '-' : `${attendanceRate}%`}
          subtitle={`${presentCount} of ${totalAttendanceRecords} present recorded`}
          badgeText="Today"
          badgeColor="bg-emerald-50 text-emerald-700 border-emerald-200"
          icon={<CalendarCheck className="w-5 h-5 text-emerald-600" />}
        />

        <MetricCard
          title="Fee Collection Rate"
          value={isDashboardLoading ? '-' : `${feeCollectionRate}%`}
          subtitle={`GHS ${totalCollected.toLocaleString()} collected`}
          badgeText="Finances"
          badgeColor="bg-amber-50 text-amber-700 border-amber-200"
          icon={<CreditCard className="w-5 h-5 text-amber-600" />}
        />

        <MetricCard
          title="Recorded Results"
          value={isDashboardLoading ? '-' : totalResults}
          subtitle="Total SBA & exam marks"
          badgeText="Assessments"
          badgeColor="bg-blue-50 text-blue-700 border-blue-200"
          icon={<BookOpen className="w-5 h-5 text-blue-600" />}
        />
      </div>

      {/* Quick Operations Shortcuts */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="font-display text-base font-bold text-slate-900 tracking-tight">Quick Operations</h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <QuickShortcutCard
            title="Mark Attendance"
            description="Daily class register for Nursery to JHS 3"
            href="/attendance/mark"
            icon={<CalendarCheck className="w-4 h-4 text-emerald-600" />}
          />

          <QuickShortcutCard
            title="Student Directory"
            description="Manage profiles, enrollment & guardians"
            href="/students"
            icon={<Users className="w-4 h-4 text-indigo-600" />}
          />

          <QuickShortcutCard
            title="SBA & Results"
            description="Record assessment scores & exam marks"
            href="/results"
            icon={<BookOpen className="w-4 h-4 text-blue-600" />}
          />

          <QuickShortcutCard
            title="Fee Schedules"
            description="Billing, receipts & fee payment tracking"
            href="/finance/reports"
            icon={<CreditCard className="w-4 h-4 text-amber-600" />}
          />
        </div>
      </div>

      {/* Operational Logs & System Announcements */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Live System Announcements & Bulletins */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200/90 p-5 shadow-2xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-lg bg-slate-100 text-slate-700">
                <BellRing className="w-4 h-4" />
              </div>
              <div>
                <h3 className="font-display font-bold text-slate-900 text-sm">System Announcements</h3>
                <p className="text-xs text-slate-500">Official bulletins & broadcast communications</p>
              </div>
            </div>

            <Link
              href="/communication/announcements"
              className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 hover:underline"
            >
              Manage
            </Link>
          </div>

          <div className="space-y-3">
            {announcements.length > 0 ? (
              announcements.slice(0, 5).map((ann: any) => (
                <ActivityItem
                  key={ann.id}
                  title={ann.title}
                  description={ann.content || ann.targetAudience}
                  time={ann.createdAt ? new Date(ann.createdAt).toLocaleDateString() : 'Recent'}
                  icon={<BellRing className="w-4 h-4 text-indigo-600" />}
                />
              ))
            ) : (
              <div className="py-8 text-center space-y-2">
                <FileText className="w-8 h-8 mx-auto text-slate-300" />
                <p className="text-xs text-slate-500 font-medium">No announcements published yet.</p>
              </div>
            )}
          </div>
        </div>

        {/* Right 1 Col: Configured Academic Structure */}
        <div className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-2xs space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-slate-700" />
                <h3 className="font-display font-bold text-slate-900 text-sm">Academic Structure</h3>
              </div>
              <Link
                href="/academic/years"
                className="text-[11px] font-semibold text-indigo-600 hover:underline"
              >
                Setup
              </Link>
            </div>

            <div className="space-y-2.5 pt-1">
              {academicYears.length > 0 ? (
                academicYears.slice(0, 4).map((year: any) => (
                  <DeadlineItem
                    key={year.id}
                    title={year.name}
                    date={`${year.startDate || ''} to ${year.endDate || ''}`}
                    status={year.status || 'ACTIVE'}
                    urgent={year.status === 'ACTIVE'}
                  />
                ))
              ) : (
                <div className="py-6 text-center space-y-2">
                  <AlertCircle className="w-6 h-6 mx-auto text-amber-500" />
                  <p className="text-xs text-slate-500 font-medium">No academic years configured.</p>
                  <Link
                    href="/academic/years"
                    className="inline-block text-xs font-bold text-indigo-600 hover:underline"
                  >
                    Create Academic Year
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function MetricCard({
  title,
  value,
  subtitle,
  badgeText,
  badgeColor,
  icon
}: {
  title: string;
  value: React.ReactNode;
  subtitle: string;
  badgeText: string;
  badgeColor: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200/90 p-4 shadow-2xs hover:border-slate-300 transition-all">
      <div className="flex items-start justify-between">
        <div className="p-2.5 rounded-xl bg-slate-50 border border-slate-100">
          {icon}
        </div>
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${badgeColor}`}>
          {badgeText}
        </span>
      </div>

      <div className="mt-3 space-y-0.5">
        <h3 className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">{title}</h3>
        <div className="font-display text-2xl font-bold text-slate-900 tracking-tight">{value}</div>
        <p className="text-xs font-medium text-slate-400">{subtitle}</p>
      </div>
    </div>
  );
}

function QuickShortcutCard({
  title,
  description,
  href,
  icon
}: {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      className="group bg-white rounded-2xl border border-slate-200/90 p-4 shadow-2xs hover:border-slate-300 transition-all flex flex-col justify-between space-y-3"
    >
      <div className="space-y-2">
        <div className="w-8 h-8 rounded-lg bg-slate-50 border border-slate-100 flex items-center justify-center">
          {icon}
        </div>
        <div>
          <h4 className="font-display text-xs font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">
            {title}
          </h4>
          <p className="text-[11px] text-slate-500 mt-0.5 leading-normal">{description}</p>
        </div>
      </div>

      <div className="flex items-center text-[11px] font-semibold text-slate-600 group-hover:text-indigo-600 transition-colors">
        <span>Open</span>
        <ChevronRight className="w-3.5 h-3.5 ml-0.5 group-hover:translate-x-0.5 transition-transform" />
      </div>
    </Link>
  );
}

function ActivityItem({
  title,
  description,
  time,
  icon
}: {
  title: string;
  description: string;
  time: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="flex items-start gap-3 p-2.5 rounded-xl hover:bg-slate-50 transition-colors border border-slate-100/60">
      <div className="p-1.5 rounded-lg bg-slate-100 shrink-0 mt-0.5">
        {icon}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between gap-2">
          <h5 className="text-xs font-bold text-slate-900 truncate">{title}</h5>
          <span className="text-[10px] text-slate-400 font-medium shrink-0">{time}</span>
        </div>
        <p className="text-xs text-slate-500 mt-0.5 truncate">{description}</p>
      </div>
    </div>
  );
}

function DeadlineItem({
  title,
  date,
  status,
  urgent
}: {
  title: string;
  date: string;
  status: string;
  urgent: boolean;
}) {
  return (
    <div className="p-2.5 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
      <div>
        <div className="text-xs font-bold text-slate-900">{title}</div>
        <div className="text-[11px] text-slate-500 font-medium mt-0.5">{date}</div>
      </div>
      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
        urgent ? 'bg-emerald-100 text-emerald-800 border border-emerald-200' : 'bg-slate-200 text-slate-700'
      }`}>
        {status}
      </span>
    </div>
  );
}
