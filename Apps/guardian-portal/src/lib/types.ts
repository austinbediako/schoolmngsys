export interface Ward {
  id: string;
  studentNumber: string;
  firstName: string;
  lastName: string;
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN';
  dateOfBirth?: string;
  className?: string;
  classLevelName?: string;
  status: string;
}

export interface AuthState {
  token: string | null;
  guardianName: string | null;
  email: string | null;
  phone: string | null;
  isAuthenticated: boolean;
}

export interface AttendanceRecord {
  id: string;
  attendanceDate: string;
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
  reason?: string;
}

export interface AttendanceSummary {
  totalDays: number;
  presentDays: number;
  absentDays: number;
  lateDays: number;
  excusedDays: number;
  ratePercentage: number;
  records: AttendanceRecord[];
}

export interface ReportCardSubject {
  subjectName: string;
  subjectCode: string;
  sbaScore: number;
  examScore: number;
  weightedTotal: number;
  grade: string;
  subjectPosition?: number;
}

export interface ReportCard {
  id: string;
  termName: string;
  academicYearLabel: string;
  className: string;
  classPosition?: number;
  totalStudentsInClass?: number;
  subjects: ReportCardSubject[];
  classTeacherRemarks?: string;
  headRemarks?: string;
  publishedAt: string;
}

export interface FeeInvoiceItem {
  description: string;
  amount: number;
}

export interface FeeInvoice {
  id: string;
  issuedAt: string;
  termLabel: string;
  totalAmount: number;
  paidAmount: number;
  balance: number;
  status: 'ISSUED' | 'PAID' | 'CANCELLED';
  items: FeeInvoiceItem[];
}

export interface PaymentReceipt {
  id: string;
  receiptNumber: string;
  paymentDate: string;
  amount: number;
  channel: string;
  reference?: string;
  reversed: boolean;
}

export interface Announcement {
  id: string;
  title: string;
  content: string;
  audienceType: string;
  createdAt: string;
}
