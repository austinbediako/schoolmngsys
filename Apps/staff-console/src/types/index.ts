export type User = {
  id: string;
  staffNumber?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role?: string;
  roles?: string[];
  permissions: string[];
  mustChangePassword?: boolean;
};

export type AuthTokens = { accessToken: string; refreshToken: string };

export type AcademicYear = {
  id: string;
  name?: string;
  label?: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'CLOSED';
  version?: number;
};

export type Term = {
  id: string;
  yearId?: string;
  name?: string;
  termNumber: number;
  officialStartDate?: string;
  officialEndDate?: string;
  startDate?: string;
  endDate?: string;
  expectedSchoolDays?: number;
  status?: 'PLANNED' | 'ACTIVE' | 'CLOSED';
  version?: number;
};

export type Class = {
  id: string;
  level?: string;
  classLevelCode?: string;
  classLevelName?: string;
  stream: string;
  capacity?: number;
  classTeacherId?: string;
  version?: number;
};

export type Subject = { id: string; name: string; code: string };

export type SubjectOffering = {
  id: string;
  classId: string;
  subjectId: string;
  academicYearId?: string;
  teacherStaffId?: string;
};

export type Student = {
  id: string;
  studentNumber: string;
  firstName: string;
  lastName: string;
  otherName?: string;
  gender: 'M' | 'F';
  dateOfBirth: string;
  nationality?: string;
  religion?: string;
  hometown?: string;
  address?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'TRANSFERRED';
  photoUrl?: string;
  currentClassId?: string;
  version?: number;
};

export type Guardian = {
  id: string;
  firstName: string;
  lastName: string;
  relationship: string;
  phone: string;
  email?: string;
  isBillingContact: boolean;
  hasCustody: boolean;
};

export type StudentGuardian = {
  studentId: string;
  guardianId: string;
  relationship: string;
  isBillingContact: boolean;
  hasCustody: boolean;
};

export type EnrollmentHistory = {
  id: string;
  studentId: string;
  classId: string;
  yearId: string;
  enrolledAt: string;
  exitedAt?: string;
  exitReason?: string;
};

export type AttendanceRecord = {
  id: string;
  studentId: string;
  classId: string;
  date: string;
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
  isCorrection?: boolean;
  correctionReason?: string;
};

export type AssessmentComponent = {
  id: string;
  subjectId: string;
  termId: string;
  name: string;
  weight: number;
  maxScore: number;
};

export type Score = { id: string; studentId: string; componentId: string; score: number; version?: number };

export type TermResult = {
  id: string;
  studentId: string;
  classId: string;
  termId: string;
  subjectId: string;
  status: 'DRAFT' | 'SUBMITTED' | 'HOD_APPROVED' | 'PUBLISHED';
  totalScore?: number;
  grade?: string;
  position?: number;
  remark?: string;
  version?: number;
};

export type FeeSchedule = {
  id: string;
  levelId: string;
  termId: string;
  yearId: string;
  items: FeeItem[];
  status: 'DRAFT' | 'APPROVED';
  version?: number;
};

export type FeeItem = { id: string; name: string; amount: string };

export type Invoice = {
  id: string;
  studentId: string;
  termId: string;
  totalAmount: string;
  paidAmount: string;
  balance: string;
  status: 'UNPAID' | 'PARTIAL' | 'PAID';
};

export type Payment = {
  id: string;
  studentId: string;
  invoiceId: string;
  amount: string;
  method: 'CASH' | 'BANK' | 'CHEQUE' | 'MOMO';
  reference?: string;
  receiptNumber: string;
  createdAt: string;
  createdBy: string;
};

export type Adjustment = {
  id: string;
  studentId: string;
  amount: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  version?: number;
};

export type Job = {
  id: string;
  type: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  progress?: number;
  message?: string;
  result?: any;
  createdAt: string;
};

export type Announcement = {
  id: string;
  title: string;
  body: string;
  scope: 'SCHOOL' | 'DEPARTMENT' | 'CLASS';
  targetId?: string;
  createdBy: string;
  createdAt: string;
  readCount?: number;
};

export type MessageTemplate = { id: string; name: string; subject: string; body: string };

export type Account = {
  id: string;
  staffNumber?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  roles: string[];
  status: 'ACTIVE' | 'INACTIVE';
  createdAt?: string;
};

export type AuditLog = {
  id: string;
  actor: string;
  actorId: string;
  entity: string;
  entityId: string;
  action: string;
  detail: string;
  timestamp: string;
};

export type PaginatedResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ApiError = {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  traceId: string;
  errors?: { field: string; message: string }[];
};
