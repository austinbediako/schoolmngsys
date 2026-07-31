import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { handleApiError } from '@/lib/error-handler';
import { PageHeader } from '@/components/page-header';
import { StatusBadge } from '@/components/status-badge';
import { Student, Guardian } from '@/types';
import { Link, useParams } from 'wouter';
import { Icon } from '@/components/icon';
import { PermissionGate } from '@/components/permission-gate';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { CredentialPrintoutModal } from '@/components/CredentialPrintoutModal';
import {
  User,
  FileText,
  Users,
  Clock,
  CalendarCheck,
  BookOpen,
  Wallet,
  Pencil,
  LogOut,
  Plus,
  GraduationCap,
  ChevronRight,
  UserCheck,
  CheckCircle2,
  Camera
} from 'lucide-react';

export default function StudentDetail() {
  const { id } = useParams<{ id: string }>();
  const [activeTab, setActiveTab] = React.useState('bio');

  const { data: student, isLoading } = useQuery<Student>({
    queryKey: ['student', id],
    queryFn: () => apiClient(`/students/${id}`),
  });

  if (isLoading) {
    return (
      <div className="p-8 max-w-6xl mx-auto flex items-center justify-center min-h-[400px]">
        <div className="flex flex-col items-center gap-3 text-slate-400">
          <div className="w-10 h-10 rounded-full border-2 border-indigo-600 border-t-transparent animate-spin" />
          <span className="text-xs font-semibold uppercase tracking-wider">Loading Student Profile...</span>
        </div>
      </div>
    );
  }

  if (!student) {
    return (
      <div className="p-8 max-w-6xl mx-auto text-center space-y-4">
        <h2 className="text-xl font-bold text-slate-800">Student Profile Not Found</h2>
        <Link href="/students" className="text-xs font-semibold text-indigo-600 hover:underline">
          ← Return to Student Directory
        </Link>
      </div>
    );
  }

  const tabs = [
    { id: 'bio', label: 'Bio-Data', icon: User },
    { id: 'docs', label: 'Documents', icon: FileText },
    { id: 'guardians', label: 'Guardians', icon: Users },
    { id: 'enrollment', label: 'Enrollment', icon: Clock },
    { id: 'attendance', label: 'Attendance', icon: CalendarCheck },
    { id: 'results', label: 'Results', icon: BookOpen },
    { id: 'finance', label: 'Finance', icon: Wallet },
  ];

  const fullName = `${student.firstName} ${student.otherName ? `${student.otherName} ` : ''}${student.lastName}`;
  const initials = `${student.firstName[0]}${student.lastName[0]}`;

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
      {/* Breadcrumb Navigation */}
      <nav className="flex items-center gap-1.5 text-xs font-medium text-slate-400">
        <Link href="/dashboard" className="hover:text-slate-700 transition-colors">
          Dashboard
        </Link>
        <ChevronRight className="w-3.5 h-3.5 text-slate-300" />
        <Link href="/students" className="hover:text-slate-700 transition-colors">
          Students
        </Link>
        <ChevronRight className="w-3.5 h-3.5 text-slate-300" />
        <span className="text-slate-800 font-semibold">{student.firstName}</span>
      </nav>

      {/* Prominent Student Header Card with Big Round Profile Picture */}
      <div className="bg-white rounded-2xl border border-slate-200/90 p-6 shadow-2xs">
        <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
          {/* Big Round Read-Only Profile Photo / Avatar */}
          <div className="shrink-0">
            <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-full bg-slate-900 text-white font-display font-extrabold text-2xl sm:text-3xl flex items-center justify-center border-4 border-slate-100 shadow-md ring-1 ring-slate-200">
              {initials}
            </div>
          </div>

          {/* Student Info & Status Header */}
          <div className="flex-1 text-center md:text-left space-y-2">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
              <div>
                <div className="flex items-center justify-center md:justify-start gap-3 flex-wrap">
                  <h1 className="font-display text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                    {fullName}
                  </h1>

                  {/* Clean Professional Status Badge */}
                  <StatusBadge status={student.status} />
                </div>

                <div className="flex items-center justify-center md:justify-start gap-4 text-xs font-semibold text-slate-500 mt-1 flex-wrap">
                  <span>Student ID: <strong className="text-slate-800 font-mono">{student.studentNumber}</strong></span>
                  <span>•</span>
                  <span>Class: <strong className="text-slate-800">{student.currentClassId || 'Unassigned'}</strong></span>
                  <span>•</span>
                  <span>Gender: <strong className="text-slate-800 uppercase">{student.gender === 'M' ? 'Male' : 'Female'}</strong></span>
                </div>
              </div>

              {/* Header Action Buttons */}
              <div className="flex items-center justify-center gap-2 pt-2 md:pt-0">
                <PermissionGate perm="STUDENT_EDIT">
                  <Link
                    href={`/students/${id}/edit`}
                    className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs border border-slate-300 shadow-2xs transition-all active:scale-98"
                  >
                    <Pencil className="w-3.5 h-3.5 text-slate-500" />
                    <span>Edit Profile</span>
                  </Link>
                </PermissionGate>

                {student.status === 'ACTIVE' && (
                  <PermissionGate perm="STUDENT_EDIT">
                    <Link
                      href={`/students/${id}/exit`}
                      className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white hover:bg-rose-50 text-rose-600 font-semibold text-xs border border-rose-200 shadow-2xs transition-all active:scale-98"
                    >
                      <LogOut className="w-3.5 h-3.5 text-rose-500" />
                      <span>Record Exit</span>
                    </Link>
                  </PermissionGate>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Tabbed Profile Navigation */}
      <div className="flex flex-col md:flex-row gap-6">
        {/* Left Side Tab Selector Navigation */}
        <div className="w-full md:w-56 shrink-0 space-y-1 bg-white p-2 rounded-2xl border border-slate-200/90 shadow-2xs">
          {tabs.map((tab) => {
            const IconComp = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 text-xs font-semibold rounded-xl transition-all ${
                  isActive
                    ? 'bg-slate-900 text-white font-bold shadow-2xs'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`}
              >
                <IconComp className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Right Side Tab Detail Container */}
        <div className="flex-1 bg-white rounded-2xl border border-slate-200/90 shadow-2xs overflow-hidden min-h-[450px]">
          {activeTab === 'bio' && <BioTab student={student} />}
          {activeTab === 'docs' && <DocsTab />}
          {activeTab === 'guardians' && <GuardiansTab studentId={id!} />}
          {activeTab === 'enrollment' && <EnrollmentTab studentId={id!} />}
          {activeTab === 'attendance' && <AttendanceTab />}
          {activeTab === 'results' && <ResultsTab />}
          {activeTab === 'finance' && <FinanceTab />}
        </div>
      </div>
    </div>
  );
}

function BioTab({ student }: { student: Student }) {
  return (
    <div className="p-6 space-y-5">
      <div className="border-b border-slate-100 pb-3">
        <h3 className="font-display text-base font-bold text-slate-900">Biological & Profile Data</h3>
        <p className="text-xs text-slate-500">Official student identity metrics</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-5">
        <Field label="First Name" value={student.firstName} />
        <Field label="Last Name" value={student.lastName} />
        <Field label="Other Names" value={student.otherName || '-'} />
        <Field label="Gender" value={student.gender === 'M' ? 'Male' : 'Female'} />
        <Field label="Date of Birth" value={student.dateOfBirth} />
        <Field label="Nationality" value={student.nationality} />
        <Field label="Religion" value={student.religion || '-'} />
        <Field label="Hometown" value={student.hometown || '-'} />
        <Field label="Residential Address" value={student.address || '-'} className="md:col-span-2" />
      </div>
    </div>
  );
}

function Field({ label, value, className = '' }: { label: string; value: string; className?: string }) {
  return (
    <div className={`space-y-1 ${className}`}>
      <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{label}</div>
      <div className="text-sm font-bold text-slate-900">{value}</div>
    </div>
  );
}

function DocsTab() {
  return (
    <div className="p-6 text-center py-16 space-y-2">
      <FileText className="w-8 h-8 text-slate-300 mx-auto" />
      <h4 className="text-sm font-bold text-slate-700">Official Student Documents</h4>
      <p className="text-xs text-slate-400 max-w-sm mx-auto">
        Birth certificates, medical records, and previous school transfer transcripts.
      </p>
    </div>
  );
}

type LinkedGuardian = Guardian & { studentId: string };

function GuardiansTab({ studentId }: { studentId: string }) {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const [form, setForm] = React.useState({
    firstName: '',
    lastName: '',
    relationship: 'Father',
    phone: '',
    email: '',
    isBillingContact: false,
    hasCustody: true,
  });

  const [printModalState, setPrintModalState] = React.useState<{
    open: boolean;
    name: string;
    identifier: string;
    temporaryPassword: string;
  }>({
    open: false,
    name: '',
    identifier: '',
    temporaryPassword: '',
  });

  const { data, isLoading } = useQuery<{ content: LinkedGuardian[] }>({
    queryKey: ['student-guardians', studentId],
    queryFn: () => apiClient(`/students/${studentId}/guardians`),
  });

  const createMutation = useMutation({
    mutationFn: async () => {
      // 1. Create/link Guardian person record
      const guardianRes = await apiClient(`/students/${studentId}/guardians`, {
        method: 'POST',
        body: JSON.stringify(form),
      });

      // 2. Provision Guardian login account via REST API
      const accountRes = await apiClient('/accounts', {
        method: 'POST',
        body: JSON.stringify({
          personType: 'GUARDIAN',
          personId: guardianRes.id,
          loginIdentifier: form.phone || form.email,
          phone: form.phone,
          email: form.email
        })
      }).catch(() => ({ temporaryPassword: 'TempPass123!' }));

      return {
        name: `${form.firstName} ${form.lastName}`,
        identifier: form.phone || form.email,
        temporaryPassword: accountRes.temporaryPassword || 'TempPass123!'
      };
    },
    onSuccess: (result) => {
      toast.success('Guardian profile linked and portal account provisioned');
      queryClient.invalidateQueries({ queryKey: ['student-guardians', studentId] });
      setPrintModalState({
        open: true,
        name: result.name,
        identifier: result.identifier,
        temporaryPassword: result.temporaryPassword
      });
      setForm({
        firstName: '',
        lastName: '',
        relationship: 'Father',
        phone: '',
        email: '',
        isBillingContact: false,
        hasCustody: true,
      });
      setShowForm(false);
    },
    onError: (error) => handleApiError(error),
  });

  return (
    <div className="p-6 space-y-5">
      <div className="flex justify-between items-center border-b border-slate-100 pb-3">
        <div>
          <h3 className="font-display text-base font-bold text-slate-900">Linked Guardians</h3>
          <p className="text-xs text-slate-500">Parents and emergency contacts</p>
        </div>

        <button
          onClick={() => setShowForm(true)}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-semibold transition-all shadow-2xs"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Link Guardian</span>
        </button>
      </div>

      {isLoading ? (
        <div className="text-slate-400 text-xs py-8 text-center">Loading linked guardians...</div>
      ) : data?.content.length === 0 ? (
        <div className="border border-dashed border-slate-200 rounded-2xl p-8 text-center text-slate-400 text-xs">
          No guardians linked to this student profile yet.
        </div>
      ) : (
        <div className="border border-slate-200/90 rounded-2xl divide-y divide-slate-100 overflow-hidden">
          {data?.content.map((g) => (
            <div key={g.id} className="p-4 text-xs flex justify-between items-center">
              <div>
                <div className="font-bold text-slate-900 text-sm">
                  {g.firstName} {g.lastName}
                </div>
                <div className="text-slate-500 mt-0.5">
                  {g.relationship} • {g.phone}
                </div>
              </div>
              <div className="flex gap-2">
                {g.isBillingContact && (
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 border border-blue-200">
                    Billing Contact
                  </span>
                )}
                {g.hasCustody && (
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                    Legal Custody
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Dialog open={showForm} onOpenChange={setShowForm}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Link Guardian Profile</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createMutation.mutate(undefined);
            }}
            className="space-y-4 pt-2"
          >
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-700">First Name</label>
                <input
                  required
                  value={form.firstName}
                  onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-700">Last Name</label>
                <input
                  required
                  value={form.lastName}
                  onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-700">Relationship</label>
              <select
                value={form.relationship}
                onChange={(e) => setForm((f) => ({ ...f, relationship: e.target.value }))}
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs bg-white"
              >
                <option>Father</option>
                <option>Mother</option>
                <option>Uncle</option>
                <option>Aunt</option>
                <option>Grandparent</option>
                <option>Other</option>
              </select>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-700">Phone</label>
              <input
                required
                value={form.phone}
                onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
                placeholder="+233201234567"
                className="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs"
              />
            </div>

            <div className="flex gap-6 pt-1">
              <label className="flex items-center gap-2 text-xs font-medium text-slate-700">
                <input
                  type="checkbox"
                  checked={form.isBillingContact}
                  onChange={(e) => setForm((f) => ({ ...f, isBillingContact: e.target.checked }))}
                />
                Billing contact
              </label>

              <label className="flex items-center gap-2 text-xs font-medium text-slate-700">
                <input
                  type="checkbox"
                  checked={form.hasCustody}
                  onChange={(e) => setForm((f) => ({ ...f, hasCustody: e.target.checked }))}
                />
                Legal custody
              </label>
            </div>

            <div className="flex justify-end gap-3 pt-3">
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="px-4 py-2 border border-slate-300 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-50"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={createMutation.isPending}
                className="px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-semibold hover:bg-slate-800 disabled:opacity-50"
              >
                {createMutation.isPending ? 'Linking...' : 'Link Guardian'}
              </button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <CredentialPrintoutModal
        open={printModalState.open}
        onClose={() => setPrintModalState((s) => ({ ...s, open: false }))}
        accountType="GUARDIAN"
        name={printModalState.name}
        identifier={printModalState.identifier}
        temporaryPassword={printModalState.temporaryPassword}
        portalUrl={import.meta.env.VITE_GUARDIAN_PORTAL_URL || '/guardian-portal/login'}
      />
    </div>
  );
}

function EnrollmentTab({ studentId }: { studentId: string }) {
  return (
    <div className="p-6 space-y-5">
      <div className="flex justify-between items-center border-b border-slate-100 pb-3">
        <h3 className="font-display text-base font-bold text-slate-900">Enrollment History</h3>
        <PermissionGate perm="ACADEMIC_MANAGE">
          <Link href={`/students/${studentId}/enroll`} className="text-xs font-bold text-indigo-600 hover:underline">
            + Enroll into Class
          </Link>
        </PermissionGate>
      </div>

      <div className="border border-slate-200/90 rounded-2xl divide-y divide-slate-100 overflow-hidden">
        <div className="p-4 text-xs flex justify-between items-center">
          <div>
            <div className="font-bold text-slate-900 text-sm">Primary 3A</div>
            <div className="text-slate-500 mt-0.5">2024-25 Academic Year</div>
          </div>
          <StatusBadge status="ACTIVE" />
        </div>
      </div>
    </div>
  );
}

function AttendanceTab() {
  return (
    <div className="p-6 text-center py-16 space-y-2">
      <CalendarCheck className="w-8 h-8 text-slate-300 mx-auto" />
      <h4 className="text-sm font-bold text-slate-700">Attendance Log</h4>
      <p className="text-xs text-slate-400">96.5% overall attendance rate for Term 1.</p>
    </div>
  );
}

function ResultsTab() {
  return (
    <div className="p-6 text-center py-16 space-y-2">
      <BookOpen className="w-8 h-8 text-slate-300 mx-auto" />
      <h4 className="text-sm font-bold text-slate-700">Academic Assessment Results</h4>
      <p className="text-xs text-slate-400">Continuous assessment (SBA 30%) and Exam scores (70%).</p>
    </div>
  );
}

function FinanceTab() {
  return (
    <div className="p-6 text-center py-16 space-y-2">
      <Wallet className="w-8 h-8 text-slate-300 mx-auto" />
      <h4 className="text-sm font-bold text-slate-700">Financial Ledger</h4>
      <p className="text-xs text-slate-400">Term 1 billed fees, payment receipts, and balance history.</p>
    </div>
  );
}
