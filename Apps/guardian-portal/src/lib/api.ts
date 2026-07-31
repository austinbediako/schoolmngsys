import { Announcement, AttendanceSummary, FeeInvoice, PaymentReceipt, ReportCard, Ward } from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1';

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('ubs_guardian_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

async function requestApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers: {
      ...getAuthHeaders(),
      ...(options?.headers || {})
    }
  });

  const contentType = res.headers.get('content-type') || '';

  if (!res.ok) {
    const errorData = contentType.includes('application/json') ? await res.json().catch(() => null) : null;
    throw new Error(errorData?.detail || `API error ${res.status}: ${res.statusText}`);
  }

  if (res.status === 204) {
    return [] as unknown as T;
  }

  if (contentType.includes('application/json')) {
    return res.json();
  }

  return [] as unknown as T;
}

export async function loginGuardian(identifier: string, secret: string) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginIdentifier: identifier, password: secret })
  });

  const contentType = res.headers.get('content-type') || '';
  if (!res.ok || !contentType.includes('application/json')) {
    const errorData = contentType.includes('application/json') ? await res.json().catch(() => ({})) : {};
    throw new Error(errorData.detail || 'Login failed. Invalid phone number, email or password.');
  }

  return res.json();
}

export async function fetchWards(): Promise<Ward[]> {
  try {
    const data = await requestApi<Ward[]>('/students/me/wards');
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.warn('Wards API request failed:', err);
    return [];
  }
}

export async function fetchAttendance(studentId: string): Promise<AttendanceSummary> {
  try {
    return await requestApi<AttendanceSummary>(`/attendance/student/${studentId}`);
  } catch (err) {
    console.warn('Attendance API request failed:', err);
    return {
      totalDays: 0,
      presentDays: 0,
      absentDays: 0,
      lateDays: 0,
      excusedDays: 0,
      ratePercentage: 0,
      records: []
    };
  }
}

export async function fetchReportCards(studentId: string): Promise<ReportCard[]> {
  try {
    const data = await requestApi<ReportCard[]>(`/assessment/student/${studentId}/report-cards`);
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.warn('Report cards API request failed:', err);
    return [];
  }
}

export async function fetchInvoices(studentId: string): Promise<FeeInvoice[]> {
  try {
    const data = await requestApi<FeeInvoice[]>(`/finance/student/${studentId}/invoices`);
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.warn('Invoices API request failed:', err);
    return [];
  }
}

export async function fetchReceipts(studentId: string): Promise<PaymentReceipt[]> {
  try {
    const data = await requestApi<PaymentReceipt[]>(`/finance/student/${studentId}/payments`);
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.warn('Receipts API request failed:', err);
    return [];
  }
}

export async function fetchAnnouncements(): Promise<Announcement[]> {
  try {
    const data = await requestApi<Announcement[]>('/announcements');
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.warn('Announcements API request failed:', err);
    return [];
  }
}
