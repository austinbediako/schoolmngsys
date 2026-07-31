import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'sonner';
import { Route, Switch, Router as WouterRouter, Redirect } from 'wouter';
import { AuthProvider } from '@/contexts/auth-context';
import { setupMockApi } from '@/lib/mock-api';
import AppShell from '@/components/layout/app-shell';
import Login from '@/pages/login';
import Dashboard from '@/pages/dashboard';
import StudentsList from '@/pages/students/list';
import StudentDetail from '@/pages/students/detail';
import NewStudent from '@/pages/students/new';
import EditStudent from '@/pages/students/edit';
import EnrollStudent from '@/pages/students/enroll';
import ExitStudent from '@/pages/students/exit';
import MarkAttendance from '@/pages/attendance/mark';
import AttendanceSummary from '@/pages/attendance/summary';
import ResultsList from '@/pages/results/index';
import ScoreEntry from '@/pages/results/entry';
import PromotionRun from '@/pages/promotion/index';
import FinanceReports from '@/pages/finance/reports';
import FinanceBilling from '@/pages/finance/billing';
import FinanceSchedules from '@/pages/finance/schedules';
import FinancePayments from '@/pages/finance/payments';
import PaymentReceipt from '@/pages/finance/payment-receipt';
import FinanceAdjustments from '@/pages/finance/adjustments';
import AcademicYears from '@/pages/academic/years';
import AcademicTerms from '@/pages/academic/terms';
import AcademicClasses from '@/pages/academic/classes';
import ClassSubjects from '@/pages/academic/class-subjects';
import AdminAccounts from '@/pages/admin/accounts';
import AuditLogPage from '@/pages/admin/audit-log';
import Announcements from '@/pages/communication/announcements';
import MessageTemplates from '@/pages/communication/templates';
import NotFound from '@/pages/not-found';

// Initialise mock fetch interceptor before any rendering
setupMockApi();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
});

function Forbidden() {
  return (
    <div className="flex flex-col items-center justify-center h-full py-24 gap-4">
      <div className="text-5xl font-bold text-slate-300">403</div>
      <h1 className="text-xl font-semibold text-slate-700">Access Denied</h1>
      <p className="text-slate-500 text-sm">You don&apos;t have permission to view this page.</p>
      <a href="/dashboard" className="text-primary text-sm underline">Go to dashboard</a>
    </div>
  );
}

function Router() {
  return (
    <Switch>
      <Route path="/login" component={Login} />
      <Route path="/">
        <Redirect to="/dashboard" />
      </Route>
      <Route>
        <AppShell>
          <Switch>
            <Route path="/dashboard" component={Dashboard} />
            <Route path="/students" component={StudentsList} />
            <Route path="/students/new" component={NewStudent} />
            <Route path="/students/:id/edit" component={EditStudent} />
            <Route path="/students/:id/enroll" component={EnrollStudent} />
            <Route path="/students/:id/exit" component={ExitStudent} />
            <Route path="/students/:id" component={StudentDetail} />
            <Route path="/attendance/mark" component={MarkAttendance} />
            <Route path="/attendance/summary" component={AttendanceSummary} />
            <Route path="/results" component={ResultsList} />
            <Route path="/results/entry" component={ScoreEntry} />
            <Route path="/promotion" component={PromotionRun} />
            <Route path="/finance/schedules" component={FinanceSchedules} />
            <Route path="/finance/billing" component={FinanceBilling} />
            <Route path="/finance/payments/:id" component={PaymentReceipt} />
            <Route path="/finance/payments" component={FinancePayments} />
            <Route path="/finance/adjustments" component={FinanceAdjustments} />
            <Route path="/finance/reports" component={FinanceReports} />
            <Route path="/academic/years/:id/terms" component={AcademicTerms} />
            <Route path="/academic/years" component={AcademicYears} />
            <Route path="/academic/classes/:id/subjects" component={ClassSubjects} />
            <Route path="/academic/classes" component={AcademicClasses} />
            <Route path="/admin/accounts" component={AdminAccounts} />
            <Route path="/admin/audit-log" component={AuditLogPage} />
            <Route path="/communication/announcements" component={Announcements} />
            <Route path="/communication/templates" component={MessageTemplates} />
            <Route path="/forbidden" component={Forbidden} />
            <Route component={NotFound} />
          </Switch>
        </AppShell>
      </Route>
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <WouterRouter base={import.meta.env.BASE_URL.replace(/\/$/, '')}>
          <Router />
        </WouterRouter>
        <Toaster richColors position="top-right" />
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
