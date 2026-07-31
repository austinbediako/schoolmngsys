import {
  User, Student, AcademicYear, Term, Class, Subject, SubjectOffering,
  Guardian, StudentGuardian, EnrollmentHistory, FeeSchedule, Invoice, Payment,
  Adjustment, Announcement, MessageTemplate, Account, AuditLog,
} from '../types';

// ---------------------------------------------------------------------------
// Mock DB state (in-memory only — resets on every full page reload)
// ---------------------------------------------------------------------------

const users: User[] = [
  {
    id: 'u1',
    staffNumber: 'STF-001',
    firstName: 'System',
    lastName: 'Admin',
    email: 'admin@unibridge.edu.gh',
    phone: '0200000001',
    role: 'SYSTEM_ADMIN',
    permissions: [
      'STUDENT_CREATE', 'STUDENT_EDIT', 'STUDENT_VIEW', 'STUDENT_DELETE',
      'ATTENDANCE_MARK', 'ATTENDANCE_CORRECT', 'ATTENDANCE_VIEW',
      'RESULT_ENTER', 'RESULT_SUBMIT', 'RESULT_APPROVE', 'RESULT_PUBLISH', 'RESULT_VIEW',
      'FINANCE_VIEW', 'FINANCE_PAYMENT_RECORD', 'FINANCE_BILLING_RUN', 'FINANCE_REPORT',
      'FINANCE_SCHEDULE_MANAGE', 'FINANCE_ADJUSTMENT_APPROVE',
      'ACADEMIC_MANAGE', 'COMMS_SEND', 'ADMIN_ACCOUNTS', 'AUDIT_VIEW',
      'PROMOTION_RUN', 'PROMOTION_APPROVE', 'PROMOTION_VIEW',
    ]
  },
  {
    id: 'u2',
    staffNumber: 'STF-002',
    firstName: 'Ama',
    lastName: 'Teacher',
    email: 'teacher@unibridge.edu.gh',
    phone: '0200000002',
    role: 'TEACHER',
    permissions: ['STUDENT_VIEW', 'ATTENDANCE_MARK', 'ATTENDANCE_VIEW', 'RESULT_ENTER', 'RESULT_VIEW']
  }
];

const students: Student[] = Array.from({ length: 45 }).map((_, i) => ({
  id: `s${i + 1}`,
  studentNumber: `UBS-${2024000 + i}`,
  firstName: ['Kwame', 'Kofi', 'Abena', 'Yaa', 'Yaw', 'Afia', 'Kwaku', 'Ama'][i % 8],
  lastName: ['Mensah', 'Osei', 'Appiah', 'Boakye', 'Owusu', 'Boateng'][i % 6],
  gender: i % 2 === 0 ? 'M' : 'F' as 'M' | 'F',
  dateOfBirth: `201${4 + (i % 5)}-0${(i % 9) + 1}-1${(i % 9)}`,
  nationality: 'Ghanaian',
  status: 'ACTIVE' as const,
  currentClassId: `c${(i % 3) + 1}`,
  version: 1,
}));

const academicYears: AcademicYear[] = [
  { id: 'y1', name: '2023-24', startDate: '2023-09-12', endDate: '2024-07-25', status: 'CLOSED', version: 1 },
  { id: 'y2', name: '2024-25', startDate: '2024-09-10', endDate: '2025-07-20', status: 'ACTIVE', version: 1 },
];

const terms: Term[] = [
  { id: 't1', yearId: 'y2', name: 'Term 1', startDate: '2024-09-10', endDate: '2024-12-13', status: 'CLOSED', version: 1 },
  { id: 't2', yearId: 'y2', name: 'Term 2', startDate: '2025-01-07', endDate: '2025-04-04', status: 'ACTIVE', version: 1 },
  { id: 't3', yearId: 'y2', name: 'Term 3', startDate: '2025-04-28', endDate: '2025-07-20', status: 'PLANNED', version: 1 },
  { id: 't0', yearId: 'y1', name: 'Term 3', startDate: '2024-04-29', endDate: '2024-07-25', status: 'CLOSED', version: 1 },
];

const classes: Class[] = [
  { id: 'c1', level: 'Primary 1', stream: 'A', capacity: 30, classTeacherId: 'u2', version: 1 },
  { id: 'c2', level: 'Primary 2', stream: 'A', capacity: 30, version: 1 },
  { id: 'c3', level: 'Primary 3', stream: 'A', capacity: 30, version: 1 },
];

const subjects: Subject[] = [
  { id: 'sub1', name: 'English Language', code: 'ENG' },
  { id: 'sub2', name: 'Mathematics', code: 'MATH' },
  { id: 'sub3', name: 'Integrated Science', code: 'SCI' },
  { id: 'sub4', name: 'Social Studies', code: 'SOC' },
  { id: 'sub5', name: 'Religious and Moral Education', code: 'RME' },
  { id: 'sub6', name: 'Ghanaian Language (Twi)', code: 'GHL' },
  { id: 'sub7', name: 'Creative Arts', code: 'CRA' },
];

const subjectOfferings: SubjectOffering[] = [
  { id: 'off1', classId: 'c1', subjectId: 'sub1', termId: 't2', teacherId: 'u2' },
  { id: 'off2', classId: 'c1', subjectId: 'sub2', termId: 't2', teacherId: 'u2' },
];

const guardianFirstNames = ['Kwesi', 'Efua', 'Kojo', 'Adjoa', 'Kwabena', 'Akosua', 'Yaw', 'Abena'];
const guardianLastNames = ['Amoah', 'Darko', 'Asante', 'Frimpong', 'Sarpong', 'Adjei'];

const guardians: Guardian[] = Array.from({ length: 20 }).map((_, i) => ({
  id: `g${i + 1}`,
  firstName: guardianFirstNames[i % guardianFirstNames.length],
  lastName: guardianLastNames[i % guardianLastNames.length],
  relationship: i % 2 === 0 ? 'Father' : 'Mother',
  phone: `+23320${String(1000000 + i).slice(0, 7)}`,
  email: i % 3 === 0 ? `guardian${i + 1}@example.com` : undefined,
  isBillingContact: i % 2 === 0,
  hasCustody: true,
}));

const studentGuardians: StudentGuardian[] = students.slice(0, 12).map((s, i) => ({
  studentId: s.id,
  guardianId: `g${(i % 20) + 1}`,
  relationship: i % 2 === 0 ? 'Father' : 'Mother',
  isBillingContact: true,
  hasCustody: true,
}));

const enrollmentHistory: EnrollmentHistory[] = students.map((s, i) => ({
  id: `eh${i + 1}`,
  studentId: s.id,
  classId: s.currentClassId!,
  yearId: 'y2',
  enrolledAt: '2024-09-10',
}));

const feeItemsFor = (): FeeSchedule['items'] => ([
  { id: 'fi1', name: 'Tuition', amount: '850.00' },
  { id: 'fi2', name: 'Feeding', amount: '400.00' },
  { id: 'fi3', name: 'Transportation', amount: '150.00' },
  { id: 'fi4', name: 'PTA Dues', amount: '30.00' },
  { id: 'fi5', name: 'Sports Levy', amount: '20.00' },
]);

const feeSchedules: FeeSchedule[] = [
  { id: 'fs1', levelId: 'Primary 1', termId: 't1', yearId: 'y2', items: feeItemsFor(), status: 'APPROVED', version: 1 },
  { id: 'fs2', levelId: 'Primary 2', termId: 't1', yearId: 'y2', items: feeItemsFor(), status: 'APPROVED', version: 1 },
  { id: 'fs3', levelId: 'Primary 3', termId: 't1', yearId: 'y2', items: feeItemsFor(), status: 'DRAFT', version: 1 },
];

const scheduleTotal = (fs: FeeSchedule) => fs.items.reduce((sum, it) => sum + parseFloat(it.amount), 0);

const invoices: Invoice[] = students.map((s, i) => {
  const total = 1450;
  const paid = i % 4 === 0 ? total : i % 4 === 1 ? total * 0.5 : 0;
  const balance = total - paid;
  return {
    id: `inv${i + 1}`,
    studentId: s.id,
    termId: 't2',
    totalAmount: total.toFixed(2),
    paidAmount: paid.toFixed(2),
    balance: balance.toFixed(2),
    status: balance <= 0 ? 'PAID' : paid > 0 ? 'PARTIAL' : 'UNPAID',
  };
});

const payments: Payment[] = [];
let receiptSeq = 1000;
invoices.filter(inv => parseFloat(inv.paidAmount) > 0).forEach((inv, i) => {
  payments.push({
    id: `pay${i + 1}`,
    studentId: inv.studentId,
    invoiceId: inv.id,
    amount: inv.paidAmount,
    method: (['CASH', 'BANK', 'MOMO'] as const)[i % 3],
    reference: i % 3 === 2 ? `MM-${100000 + i}` : undefined,
    receiptNumber: `RCT-${receiptSeq++}`,
    createdAt: '2025-01-15T09:30:00Z',
    createdBy: 'System Admin',
  });
});

const adjustments: Adjustment[] = [
  { id: 'adj1', studentId: 's3', amount: '-100.00', reason: 'Sibling discount', status: 'APPROVED', version: 1 },
  { id: 'adj2', studentId: 's7', amount: '-50.00', reason: 'Hardship waiver (partial)', status: 'PENDING', version: 1 },
];

const messageTemplates: MessageTemplate[] = [
  { id: 'mt1', name: 'Fee Due Reminder', subject: 'Fee payment due', body: 'Dear guardian, this is a reminder that fees for {{term}} are due on {{dueDate}}.' },
  { id: 'mt2', name: 'Result Published', subject: 'Term results published', body: 'Dear guardian, {{studentName}}’s results for {{term}} are now available on the parent portal.' },
];

const announcements: Announcement[] = [
  { id: 'an1', title: 'Term 2 Reopening', body: 'School reopens for Term 2 on Monday, January 6th.', scope: 'SCHOOL', createdBy: 'System Admin', createdAt: '2025-01-02T08:00:00Z', readCount: 120 },
  { id: 'an2', title: 'Primary 1 Excursion', body: 'Primary 1 pupils will visit the Accra Zoo on Friday.', scope: 'CLASS', targetId: 'c1', createdBy: 'Ama Teacher', createdAt: '2025-01-10T10:00:00Z', readCount: 18 },
];

const accounts: Account[] = users.map(u => ({
  id: u.id, staffNumber: u.staffNumber, firstName: u.firstName, lastName: u.lastName,
  email: u.email, phone: u.phone, roles: [u.role], status: 'ACTIVE', createdAt: '2024-09-01T09:00:00Z',
}));

const auditLog: AuditLog[] = [];
let auditSeq = 1;
function logAudit(actor: string, actorId: string, entity: string, entityId: string, action: string, detail: string) {
  auditLog.unshift({
    id: `al${auditSeq++}`,
    actor, actorId, entity, entityId, action, detail,
    timestamp: new Date().toISOString(),
  });
}
// Seed a little history so the log isn't empty on first view
logAudit('System Admin', 'u1', 'Student', 's1', 'CREATE', 'Created student record');
logAudit('System Admin', 'u1', 'FeeSchedule', 'fs1', 'APPROVE', 'Approved fee schedule for Primary 1');
logAudit('Ama Teacher', 'u2', 'Attendance', 'c1', 'MARK', 'Marked attendance for Primary 1A');

// ---------------------------------------------------------------------------

const mockDelay = (ms: number) => new Promise(r => setTimeout(r, ms));

function paginate<T>(items: T[], page: number, size: number) {
  return {
    content: items.slice(page * size, (page + 1) * size),
    page, size, totalElements: items.length, totalPages: Math.max(1, Math.ceil(items.length / size)),
  };
}

export function setupMockApi() {
  const originalFetch = window.fetch;
  window.fetch = async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = typeof input === 'string' ? input : input.toString();
    if (!url.startsWith('/api/v1')) {
      return originalFetch(input, init);
    }

    await mockDelay(400); // Simulate network

    const method = init?.method || 'GET';
    const path = url.replace('/api/v1', '').split('?')[0];
    const params = new URLSearchParams(url.split('?')[1]);
    const body = init?.body ? JSON.parse(init.body as string) : undefined;

    const jsonResponse = (data: any, status = 200) => {
      return new Response(JSON.stringify(data), {
        status,
        headers: { 'Content-Type': 'application/json' }
      });
    };
    const notFound = () => jsonResponse({ type: 'not-found', title: 'Not Found', status: 404, detail: 'Not found', instance: path, traceId: 'x' }, 404);

    const currentUser = () => {
      const authHeader = init?.headers ? new Headers(init.headers).get('Authorization') : null;
      const token = authHeader?.split(' ')[1];
      const uid = token?.split('-')[1];
      return users.find(u => u.id === uid) || users[0];
    };

    // AUTH
    if (path === '/auth/login' && method === 'POST') {
      const u = users.find(u => u.firstName.toLowerCase() === body.username || u.lastName.toLowerCase() === body.username || 'admin' === body.username);
      if (u) {
        return jsonResponse({ accessToken: `token-${u.id}`, refreshToken: `ref-${u.id}` });
      }
      return jsonResponse({ type: 'unauthorized', title: 'Invalid credentials', status: 401, detail: 'Wrong username or password', instance: path, traceId: '1' }, 401);
    }

    if (path === '/auth/me' && method === 'GET') {
      const authHeader = init?.headers ? new Headers(init.headers).get('Authorization') : null;
      const token = authHeader?.split(' ')[1];
      if (!token) return jsonResponse({}, 401);
      const uid = token.split('-')[1];
      const u = users.find(u => u.id === uid);
      if (u) return jsonResponse({ user: u });
      return jsonResponse({}, 401);
    }

    // DASHBOARD SUMMARY
    if (path === '/dashboard/summary' && method === 'GET') {
      return jsonResponse({
        enrollmentCount: students.length,
        attendancePercentage: 94.2,
        feeCollectionPercentage: 68.5,
        pendingApprovals: 3
      });
    }

    // STUDENTS
    if (path === '/students' && method === 'GET') {
      const page = parseInt(params.get('page') || '0');
      const size = parseInt(params.get('size') || '20');
      const classId = params.get('classId');
      const search = params.get('search')?.toLowerCase();
      let filtered = students;
      if (classId) filtered = filtered.filter(s => s.currentClassId === classId);
      if (search) filtered = filtered.filter(s =>
        `${s.firstName} ${s.lastName} ${s.studentNumber}`.toLowerCase().includes(search)
      );
      return jsonResponse(paginate(filtered, page, size));
    }

    if (path.match(/^\/students\/[^\/]+\/guardians$/) && method === 'GET') {
      const studentId = path.split('/')[2];
      const links = studentGuardians.filter(sg => sg.studentId === studentId);
      const content = links.map(link => ({ ...guardians.find(g => g.id === link.guardianId)!, ...link }));
      return jsonResponse({ content });
    }

    if (path.match(/^\/students\/[^\/]+\/guardians$/) && method === 'POST') {
      const studentId = path.split('/')[2];
      const id = `g${guardians.length + 1}`;
      const newGuardian: Guardian = {
        id, firstName: body.firstName, lastName: body.lastName, relationship: body.relationship,
        phone: body.phone, email: body.email, isBillingContact: !!body.isBillingContact, hasCustody: !!body.hasCustody,
      };
      guardians.push(newGuardian);
      studentGuardians.push({ studentId, guardianId: id, relationship: body.relationship, isBillingContact: newGuardian.isBillingContact, hasCustody: newGuardian.hasCustody });
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Guardian', id, 'CREATE', `Linked guardian ${newGuardian.firstName} ${newGuardian.lastName} to student ${studentId}`);
      return jsonResponse(newGuardian, 201);
    }

    if (path.match(/^\/students\/[^\/]+\/exit$/) && method === 'POST') {
      const studentId = path.split('/')[2];
      const s = students.find(s => s.id === studentId);
      if (!s) return notFound();
      s.status = body.type === 'WITHDRAWAL' ? 'INACTIVE' : 'TRANSFERRED';
      const openHistory = enrollmentHistory.find(h => h.studentId === studentId && !h.exitedAt);
      if (openHistory) {
        openHistory.exitedAt = body.date;
        openHistory.exitReason = body.reason;
      }
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Student', studentId, 'EXIT', `${body.type} recorded: ${body.reason}`);
      return jsonResponse({ student: s, exitedAt: body.date, reason: body.reason, type: body.type });
    }

    if (path.match(/^\/students\/[^\/]+$/) && method === 'GET') {
      const id = path.split('/')[2];
      const s = students.find(s => s.id === id);
      if (s) return jsonResponse(s);
      return notFound();
    }

    if (path.match(/^\/students\/[^\/]+$/) && method === 'PUT') {
      const id = path.split('/')[2];
      const s = students.find(s => s.id === id);
      if (!s) return notFound();
      Object.assign(s, body, { version: (s.version || 1) + 1 });
      return jsonResponse(s);
    }

    if (path === '/enrollments' && method === 'POST') {
      const s = students.find(s => s.id === body.studentId);
      if (s) s.currentClassId = body.classId;
      enrollmentHistory.push({ id: `eh${enrollmentHistory.length + 1}`, studentId: body.studentId, classId: body.classId, yearId: 'y2', enrolledAt: new Date().toISOString().slice(0, 10) });
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Enrollment', body.studentId, 'CREATE', `Enrolled into class ${body.classId}`);
      return jsonResponse({ studentId: body.studentId, classId: body.classId, enrolledAt: new Date().toISOString() }, 201);
    }

    if (path === '/students' && method === 'POST') {
      const id = `s${students.length + 1}`;
      const newStudent: Student = {
        id,
        studentNumber: `UBS-${2024000 + students.length}`,
        firstName: body.firstName,
        lastName: body.lastName,
        otherName: body.otherName,
        gender: body.gender,
        dateOfBirth: body.dateOfBirth,
        nationality: body.nationality,
        status: 'ACTIVE',
        version: 1,
      };
      students.push(newStudent);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Student', id, 'CREATE', `Created student ${newStudent.firstName} ${newStudent.lastName}`);
      return jsonResponse(newStudent, 201);
    }

    // ACADEMIC — years & terms
    if (path === '/academic/years' && method === 'GET') {
      return jsonResponse({ content: academicYears });
    }
    if (path === '/academic/years' && method === 'POST') {
      const id = `y${academicYears.length + 1}`;
      const year: AcademicYear = { id, name: body.name, startDate: body.startDate, endDate: body.endDate, status: 'PLANNED', version: 1 };
      academicYears.push(year);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'AcademicYear', id, 'CREATE', `Created academic year ${year.name}`);
      return jsonResponse(year, 201);
    }
    if (path.match(/^\/academic\/years\/[^\/]+$/) && method === 'PUT') {
      const id = path.split('/')[3];
      const y = academicYears.find(y => y.id === id);
      if (!y) return notFound();
      Object.assign(y, body, { version: y.version + 1 });
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'AcademicYear', id, 'UPDATE', `Updated academic year status to ${y.status}`);
      return jsonResponse(y);
    }
    if (path.match(/^\/academic\/years\/[^\/]+\/terms$/) && method === 'GET') {
      const yearId = path.split('/')[3];
      return jsonResponse({ content: terms.filter(t => t.yearId === yearId) });
    }
    if (path.match(/^\/academic\/years\/[^\/]+\/terms$/) && method === 'POST') {
      const yearId = path.split('/')[3];
      const id = `t${terms.length + 1}`;
      const term: Term = { id, yearId, name: body.name, startDate: body.startDate, endDate: body.endDate, status: 'PLANNED', version: 1 };
      terms.push(term);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Term', id, 'CREATE', `Created ${term.name} for year ${yearId}`);
      return jsonResponse(term, 201);
    }
    if (path.match(/^\/academic\/terms\/[^\/]+$/) && method === 'PUT') {
      const id = path.split('/')[3];
      const t = terms.find(t => t.id === id);
      if (!t) return notFound();
      Object.assign(t, body, { version: t.version + 1 });
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Term', id, 'UPDATE', `Updated term status to ${t.status}`);
      return jsonResponse(t);
    }

    // ACADEMIC — classes & subjects
    if (path === '/academic/classes' && method === 'GET') {
      return jsonResponse(paginate(classes, 0, 100));
    }
    if (path === '/academic/classes' && method === 'POST') {
      const id = `c${classes.length + 1}`;
      const cls: Class = { id, level: body.level, stream: body.stream, capacity: Number(body.capacity), classTeacherId: body.classTeacherId, version: 1 };
      classes.push(cls);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Class', id, 'CREATE', `Created class ${cls.level} ${cls.stream}`);
      return jsonResponse(cls, 201);
    }
    if (path.match(/^\/academic\/classes\/[^\/]+$/) && method === 'PUT') {
      const id = path.split('/')[3];
      const c = classes.find(c => c.id === id);
      if (!c) return notFound();
      Object.assign(c, body, { version: c.version + 1 });
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Class', id, 'UPDATE', `Updated class ${c.level} ${c.stream}`);
      return jsonResponse(c);
    }
    if (path === '/academic/subjects' && method === 'GET') {
      return jsonResponse({ content: subjects });
    }
    if (path.match(/^\/academic\/classes\/[^\/]+\/subject-offerings$/) && method === 'GET') {
      const classId = path.split('/')[3];
      const content = subjectOfferings.filter(o => o.classId === classId).map(o => ({
        ...o,
        subjectName: subjects.find(s => s.id === o.subjectId)?.name,
        teacherName: users.find(u => u.id === o.teacherId)?.firstName + ' ' + users.find(u => u.id === o.teacherId)?.lastName,
      }));
      return jsonResponse({ content });
    }
    if (path.match(/^\/academic\/classes\/[^\/]+\/subject-offerings$/) && method === 'POST') {
      const classId = path.split('/')[3];
      const id = `off${subjectOfferings.length + 1}`;
      const offering: SubjectOffering = { id, classId, subjectId: body.subjectId, termId: body.termId, teacherId: body.teacherId };
      subjectOfferings.push(offering);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'SubjectOffering', id, 'CREATE', `Assigned subject to class ${classId}`);
      return jsonResponse(offering, 201);
    }

    // ATTENDANCE
    if (path === '/attendance/bulk' && method === 'POST') {
      return jsonResponse({ success: true, recorded: Array.isArray(body) ? body.length : 0 }, 200);
    }

    // RESULTS
    if (path === '/results/scores' && method === 'POST') {
      return jsonResponse({ success: true }, 200);
    }
    if (path === '/results/scores/bulk' && method === 'POST') {
      return jsonResponse({ success: true }, 200);
    }

    // FINANCE — fee schedules
    if (path === '/finance/fee-schedules' && method === 'GET') {
      return jsonResponse({ content: feeSchedules.map(fs => ({ ...fs, total: scheduleTotal(fs).toFixed(2) })) });
    }
    if (path === '/finance/fee-schedules' && method === 'POST') {
      const id = `fs${feeSchedules.length + 1}`;
      const fs: FeeSchedule = { id, levelId: body.levelId, termId: body.termId, yearId: body.yearId, items: body.items || feeItemsFor(), status: 'DRAFT', version: 1 };
      feeSchedules.push(fs);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'FeeSchedule', id, 'CREATE', `Created fee schedule for ${fs.levelId}`);
      return jsonResponse(fs, 201);
    }
    if (path.match(/^\/finance\/fee-schedules\/[^\/]+\/approve$/) && method === 'POST') {
      const id = path.split('/')[3];
      const fs = feeSchedules.find(fs => fs.id === id);
      if (!fs) return notFound();
      fs.status = 'APPROVED';
      fs.version += 1;
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'FeeSchedule', id, 'APPROVE', `Approved fee schedule for ${fs.levelId}`);
      return jsonResponse(fs);
    }

    // FINANCE — invoices & payments
    if (path === '/finance/invoices' && method === 'GET') {
      const page = parseInt(params.get('page') || '0');
      const size = parseInt(params.get('size') || '20');
      const studentId = params.get('studentId');
      let filtered = invoices;
      if (studentId) filtered = filtered.filter(i => i.studentId === studentId);
      const withNames = filtered.map(inv => {
        const s = students.find(s => s.id === inv.studentId);
        return { ...inv, studentName: s ? `${s.firstName} ${s.lastName}` : 'Unknown', studentNumber: s?.studentNumber };
      });
      return jsonResponse(paginate(withNames, page, size));
    }
    if (path === '/finance/payments' && method === 'GET') {
      const page = parseInt(params.get('page') || '0');
      const size = parseInt(params.get('size') || '20');
      const withNames = payments.map(p => {
        const s = students.find(s => s.id === p.studentId);
        return { ...p, studentName: s ? `${s.firstName} ${s.lastName}` : 'Unknown', studentNumber: s?.studentNumber };
      }).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
      return jsonResponse(paginate(withNames, page, size));
    }
    if (path === '/finance/payments' && method === 'POST') {
      const invoice = invoices.find(i => i.id === body.invoiceId);
      if (!invoice) return notFound();
      const id = `pay${payments.length + 1}`;
      const payment: Payment = {
        id, studentId: invoice.studentId, invoiceId: invoice.id, amount: Number(body.amount).toFixed(2),
        method: body.method, reference: body.reference, receiptNumber: `RCT-${receiptSeq++}`,
        createdAt: new Date().toISOString(), createdBy: currentUser().firstName + ' ' + currentUser().lastName,
      };
      payments.push(payment);
      const newPaid = parseFloat(invoice.paidAmount) + parseFloat(payment.amount);
      invoice.paidAmount = newPaid.toFixed(2);
      invoice.balance = (parseFloat(invoice.totalAmount) - newPaid).toFixed(2);
      invoice.status = parseFloat(invoice.balance) <= 0 ? 'PAID' : 'PARTIAL';
      logAudit(payment.createdBy, currentUser().id, 'Payment', id, 'CREATE', `Recorded payment of GHS ${payment.amount} (${payment.receiptNumber})`);
      return jsonResponse(payment, 201);
    }
    if (path.match(/^\/finance\/payments\/[^\/]+$/) && method === 'GET') {
      const id = path.split('/')[3];
      const p = payments.find(p => p.id === id);
      if (!p) return notFound();
      const s = students.find(s => s.id === p.studentId);
      return jsonResponse({ ...p, studentName: s ? `${s.firstName} ${s.lastName}` : 'Unknown', studentNumber: s?.studentNumber });
    }

    // FINANCE — adjustments
    if (path === '/finance/adjustments' && method === 'GET') {
      const withNames = adjustments.map(a => {
        const s = students.find(s => s.id === a.studentId);
        return { ...a, studentName: s ? `${s.firstName} ${s.lastName}` : 'Unknown', studentNumber: s?.studentNumber };
      });
      return jsonResponse({ content: withNames });
    }
    if (path === '/finance/adjustments' && method === 'POST') {
      const id = `adj${adjustments.length + 1}`;
      const adj: Adjustment = { id, studentId: body.studentId, amount: body.amount, reason: body.reason, status: 'PENDING', version: 1 };
      adjustments.push(adj);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Adjustment', id, 'CREATE', `Requested adjustment: ${adj.reason}`);
      return jsonResponse(adj, 201);
    }
    if (path.match(/^\/finance\/adjustments\/[^\/]+\/approve$/) && method === 'POST') {
      const id = path.split('/')[3];
      const adj = adjustments.find(a => a.id === id);
      if (!adj) return notFound();
      adj.status = 'APPROVED';
      adj.version += 1;
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Adjustment', id, 'APPROVE', `Approved adjustment: ${adj.reason}`);
      return jsonResponse(adj);
    }

    if (path === '/finance/billing/run' && method === 'POST') {
      const jobId = `job-${Date.now()}`;
      return jsonResponse({ jobId, status: 'PENDING' }, 202);
    }

    if (path.startsWith('/jobs/') && method === 'GET') {
      const jobId = path.split('/')[2];
      const age = Date.now() - parseInt(jobId.split('-')[1]);
      if (age < 2000) return jsonResponse({ id: jobId, status: 'RUNNING', progress: 30 });
      if (age < 4000) return jsonResponse({ id: jobId, status: 'RUNNING', progress: 75 });
      return jsonResponse({ id: jobId, status: 'COMPLETED', message: 'Job completed successfully' });
    }

    // COMMUNICATION
    if (path === '/communication/templates' && method === 'GET') {
      return jsonResponse({ content: messageTemplates });
    }
    if (path === '/communication/templates' && method === 'POST') {
      const id = `mt${messageTemplates.length + 1}`;
      const tpl: MessageTemplate = { id, name: body.name, subject: body.subject, body: body.body };
      messageTemplates.push(tpl);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'MessageTemplate', id, 'CREATE', `Created template ${tpl.name}`);
      return jsonResponse(tpl, 201);
    }
    if (path === '/communication/announcements' && method === 'GET') {
      const content = [...announcements].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
      return jsonResponse({ content });
    }
    if (path === '/communication/announcements' && method === 'POST') {
      const id = `an${announcements.length + 1}`;
      const ann: Announcement = {
        id, title: body.title, body: body.body, scope: body.scope, targetId: body.targetId,
        createdBy: currentUser().firstName + ' ' + currentUser().lastName, createdAt: new Date().toISOString(), readCount: 0,
      };
      announcements.push(ann);
      logAudit(ann.createdBy, currentUser().id, 'Announcement', id, 'CREATE', `Published announcement: ${ann.title}`);
      return jsonResponse(ann, 201);
    }

    // ADMIN — accounts
    if (path === '/admin/accounts' && method === 'GET') {
      return jsonResponse({ content: accounts });
    }
    if (path === '/admin/accounts' && method === 'POST') {
      const id = `u${accounts.length + 1}`;
      const account: Account = {
        id, staffNumber: `STF-${String(accounts.length + 1).padStart(3, '0')}`,
        firstName: body.firstName, lastName: body.lastName, email: body.email, phone: body.phone,
        roles: [body.role], status: 'ACTIVE', createdAt: new Date().toISOString(),
      };
      accounts.push(account);
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Account', id, 'CREATE', `Provisioned account for ${account.firstName} ${account.lastName} (${account.roles[0]})`);
      return jsonResponse({ ...account, temporaryPassword: `Temp${Math.floor(1000 + Math.random() * 9000)}!` }, 201);
    }
    if (path.match(/^\/admin\/accounts\/[^\/]+\/deactivate$/) && method === 'POST') {
      const id = path.split('/')[3];
      const account = accounts.find(a => a.id === id);
      if (!account) return notFound();
      account.status = 'INACTIVE';
      logAudit(currentUser().firstName + ' ' + currentUser().lastName, currentUser().id, 'Account', id, 'DEACTIVATE', `Deactivated account for ${account.firstName} ${account.lastName}`);
      return jsonResponse(account);
    }

    // ADMIN — audit log
    if (path === '/audit-log' && method === 'GET') {
      const page = parseInt(params.get('page') || '0');
      const size = parseInt(params.get('size') || '20');
      const from = params.get('from');
      const to = params.get('to');
      const actor = params.get('actor')?.toLowerCase();
      const entity = params.get('entity');
      let filtered = auditLog;
      if (from) filtered = filtered.filter(a => a.timestamp >= from);
      if (to) filtered = filtered.filter(a => a.timestamp <= to);
      if (actor) filtered = filtered.filter(a => a.actor.toLowerCase().includes(actor));
      if (entity) filtered = filtered.filter(a => a.entity === entity);
      return jsonResponse(paginate(filtered, page, size));
    }

    // FALLBACK
    return notFound();
  };
}
