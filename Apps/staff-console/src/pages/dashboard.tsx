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
  CheckCircle2,
  BellRing,
  ChevronRight,
  Calendar,
  AlertCircle
} from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuth();

  const { data, isLoading } = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: () => apiClient('/dashboard/summary'),
  });

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
              2024-25 Term 1 Active
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 font-medium">
            Unibridge Basic School Management Console. Here is your daily operational summary.
          </p>
        </div>

        {/* Solid Professional Action Buttons */}
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

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Total Enrollment"
          value={isLoading ? '-' : data?.enrollmentCount ?? 45}
          subtitle="+4.2% vs last term"
          badgeText="Active Students"
          badgeColor="bg-slate-100 text-slate-700 border-slate-200"
          icon={<Users className="w-5 h-5 text-indigo-600" />}
        />

        <MetricCard
          title="Today's Attendance"
          value={isLoading ? '-' : `${data?.attendancePercentage ?? 94.2}%`}
          subtitle="94 of 100 present today"
          badgeText="High Rate"
          badgeColor="bg-emerald-50 text-emerald-700 border-emerald-200"
          icon={<CalendarCheck className="w-5 h-5 text-emerald-600" />}
        />

        <MetricCard
          title="Fee Collection"
          value={isLoading ? '-' : `${data?.feeCollectionPercentage ?? 68.5}%`}
          subtitle="Term 1 collection rate"
          badgeText="On Track"
          badgeColor="bg-amber-50 text-amber-700 border-amber-200"
          icon={<CreditCard className="w-5 h-5 text-amber-600" />}
        />

        <MetricCard
          title="Pending Approvals"
          value={isLoading ? '-' : data?.pendingApprovals ?? 3}
          subtitle="SBA & Result reviews"
          badgeText="Action Needed"
          badgeColor="bg-rose-50 text-rose-700 border-rose-200"
          icon={<Clock className="w-5 h-5 text-rose-600" />}
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

      {/* Operational Logs & Academic Calendar Deadlines */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Activity Timeline */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200/90 p-5 shadow-2xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-lg bg-slate-100 text-slate-700">
                <BellRing className="w-4 h-4" />
              </div>
              <div>
                <h3 className="font-display font-bold text-slate-900 text-sm">Recent Activity Logs</h3>
                <p className="text-xs text-slate-500">Live system events across departments</p>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <ActivityItem
              title="Term 1 SBA Assessment Scores Uploaded"
              description="JHS 1 Mathematics & Integrated Science scores recorded by Subject Teacher"
              time="10 mins ago"
              icon={<CheckCircle2 className="w-4 h-4 text-emerald-600" />}
            />

            <ActivityItem
              title="Fee Payment Receipt #GHS-8492 Issued"
              description="GHS 450.00 payment confirmed for Student Kwabena Mensah (Primary 4)"
              time="45 mins ago"
              icon={<CreditCard className="w-4 h-4 text-blue-600" />}
            />

            <ActivityItem
              title="New Student Enrollment Completed"
              description="Akosua Ampofo registered into Nursery 2 • Guardian linked"
              time="2 hours ago"
              icon={<Users className="w-4 h-4 text-indigo-600" />}
            />
          </div>
        </div>

        {/* Right 1 Col: Academic Calendar & Deadlines (Replaces System Info) */}
        <div className="bg-white rounded-2xl border border-slate-200/90 p-5 shadow-2xs space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-slate-700" />
                <h3 className="font-display font-bold text-slate-900 text-sm">Upcoming Key Dates</h3>
              </div>
              <span className="text-[11px] font-semibold text-slate-500">Term 1</span>
            </div>

            <div className="space-y-3 pt-1">
              <DeadlineItem
                title="Mid-Term SBA Score Submission"
                date="Aug 15, 2026"
                status="Upcoming"
                urgent={true}
              />
              <DeadlineItem
                title="Parent-Teacher Association (PTA)"
                date="Aug 22, 2026"
                status="Scheduled"
                urgent={false}
              />
              <DeadlineItem
                title="End-of-Term Examinations"
                date="Sept 05, 2026"
                status="Scheduled"
                urgent={false}
              />
              <DeadlineItem
                title="Term 1 Report Card Approvals"
                date="Sept 18, 2026"
                status="Pending"
                urgent={false}
              />
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
    <div className="flex items-start gap-3 p-2.5 rounded-xl hover:bg-slate-50 transition-colors">
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
        urgent ? 'bg-amber-100 text-amber-800 border border-amber-200' : 'bg-slate-200 text-slate-700'
      }`}>
        {status}
      </span>
    </div>
  );
}
