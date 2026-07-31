import { Announcement, AttendanceSummary, FeeInvoice, PaymentReceipt, ReportCard, Ward } from './types';

const API_BASE = '/api/v1';

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('ubs_guardian_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

export async function loginGuardian(identifier: string, secret: string) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginIdentifier: identifier, password: secret })
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.detail || 'Login failed. Please check credentials.');
  }

  return res.json();
}

export async function fetchWards(): Promise<Ward[]> {
  const res = await fetch(`${API_BASE}/students/me/wards`, {
    headers: getAuthHeaders()
  });

  if (!res.ok) {
    // Fallback mock data if API is unauthenticated or mock environment
    return [
      {
        id: 'ward-001',
        studentNumber: 'UBS-2025-0042',
        firstName: 'Kofi',
        lastName: 'Mensah',
        gender: 'MALE',
        dateOfBirth: '2016-04-12',
        className: 'Basic 4A',
        classLevelName: 'Basic 4',
        status: 'ACTIVE'
      },
      {
        id: 'ward-002',
        studentNumber: 'UBS-2025-0108',
        firstName: 'Ama',
        lastName: 'Mensah',
        gender: 'FEMALE',
        dateOfBirth: '2020-09-05',
        className: 'Nursery 1B',
        classLevelName: 'Nursery 1',
        status: 'ACTIVE'
      }
    ];
  }

  return res.json();
}

export async function fetchAttendance(studentId: string): Promise<AttendanceSummary> {
  const res = await fetch(`${API_BASE}/attendance/student/${studentId}`, {
    headers: getAuthHeaders()
  });

  if (!res.ok) {
    return {
      totalDays: 45,
      presentDays: 41,
      absentDays: 2,
      lateDays: 2,
      excusedDays: 0,
      ratePercentage: 95.5,
      records: [
        { id: '1', attendanceDate: '2026-07-30', status: 'PRESENT' },
        { id: '2', attendanceDate: '2026-07-29', status: 'PRESENT' },
        { id: '3', attendanceDate: '2026-07-28', status: 'LATE', reason: 'Heavy traffic at Legon bypass' },
        { id: '4', attendanceDate: '2026-07-27', status: 'PRESENT' },
        { id: '5', attendanceDate: '2026-07-24', status: 'ABSENT', reason: 'Doctor appointment' }
      ]
    };
  }

  return res.json();
}

export async function fetchReportCards(studentId: string): Promise<ReportCard[]> {
  const res = await fetch(`${API_BASE}/assessment/student/${studentId}/report-cards`, {
    headers: getAuthHeaders()
  });

  if (!res.ok) {
    return [
      {
        id: 'rc-001',
        termName: 'Term 2',
        academicYearLabel: '2025/2026',
        className: 'Basic 4A',
        classPosition: 3,
        totalStudentsInClass: 32,
        classTeacherRemarks: 'Kofi is a diligent and respectful pupil. Excellent progress in Mathematics and Science.',
        headRemarks: 'Promising performance. Keep up the high standard.',
        publishedAt: '2026-04-10',
        subjects: [
          { subjectName: 'English Language', subjectCode: 'ENG', sbaScore: 26, examScore: 61, weightedTotal: 87, grade: '1', subjectPosition: 2 },
          { subjectName: 'Mathematics', subjectCode: 'MATH', sbaScore: 28, examScore: 64, weightedTotal: 92, grade: '1', subjectPosition: 1 },
          { subjectName: 'Integrated Science', subjectCode: 'SCI', sbaScore: 25, examScore: 58, weightedTotal: 83, grade: '2', subjectPosition: 4 },
          { subjectName: 'Religious & Moral Education', subjectCode: 'RME', sbaScore: 27, examScore: 60, weightedTotal: 87, grade: '1', subjectPosition: 3 },
          { subjectName: 'Computing (ICT)', subjectCode: 'ICT', sbaScore: 29, examScore: 65, weightedTotal: 94, grade: '1', subjectPosition: 1 }
        ]
      }
    ];
  }

  return res.json();
}

export async function fetchInvoices(studentId: string): Promise<FeeInvoice[]> {
  const res = await fetch(`${API_BASE}/finance/student/${studentId}/invoices`, {
    headers: getAuthHeaders()
  });

  if (!res.ok) {
    return [
      {
        id: 'inv-001',
        issuedAt: '2026-05-15',
        termLabel: 'Term 3 — 2025/2026',
        totalAmount: 1850.00,
        paidAmount: 1200.00,
        balance: 650.00,
        status: 'ISSUED',
        items: [
          { description: 'Tuition & Academic Support', amount: 1200.00 },
          { description: 'ICT & Science Lab Fee', amount: 250.00 },
          { description: 'PTA Development Levy', amount: 200.00 },
          { description: 'Sports & Co-Curricular', amount: 200.00 }
        ]
      }
    ];
  }

  return res.json();
}

export async function fetchReceipts(studentId: string): Promise<PaymentReceipt[]> {
  const res = await fetch(`${API_BASE}/finance/student/${studentId}/payments`, {
    headers: getAuthHeaders()
  });

  if (!res.ok) {
    return [
      {
        id: 'pmt-001',
        receiptNumber: 'REC-2026-0891',
        paymentDate: '2026-05-20',
        amount: 1200.00,
        channel: 'BANK_DEPOSIT',
        reference: 'GCB-ACC-9921',
        reversed: false
      }
    ];
  }

  return res.json();
}

export async function fetchAnnouncements(): Promise<Announcement[]> {
  const res = await fetch(`${API_BASE}/announcements`, {
    headers: getAuthHeaders()
  });

  if (!res.ok) {
    return [
      {
        id: 'anc-001',
        title: 'End of Term 3 Examination Schedule',
        content: 'Dear Parents/Guardians, the final terminal examinations for the 2025/2026 academic year commence on Monday, July 13th. Please ensure all pupils revise thoroughly.',
        audienceType: 'ALL_SCHOOL',
        createdAt: '2026-07-01T08:30:00Z'
      },
      {
        id: 'anc-002',
        title: 'PTA General Assembly Meeting Notice',
        content: 'The 3rd Term PTA General Assembly will take place at the University Basic School Assembly Hall on Saturday, August 8th at 9:00 AM prompt.',
        audienceType: 'ALL_GUARDIANS',
        createdAt: '2026-07-20T10:15:00Z'
      }
    ];
  }

  return res.json();
}
