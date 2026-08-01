import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types';
import { apiClient, setTokens, getTokens } from '@/lib/api-client';

interface AuthContextType {
  user: User | null;
  permissions: string[];
  isLoading: boolean;
  login: (credentials: any) => Promise<void>;
  logout: () => void;
  hasPermission: (perm: string | null) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchMe = async () => {
    try {
      const data = await apiClient('/auth/me');
      const userData = data?.user || null;
      setUser(userData);
      setPermissions(userData?.permissions || []);
    } catch {
      setUser(null);
      setPermissions([]);
      setTokens(null);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (getTokens()) {
      fetchMe();
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (credentials: any) => {
    const data = await apiClient('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken });
    await fetchMe();
  };

  const logout = () => {
    setTokens(null);
    setUser(null);
    setPermissions([]);
    window.location.href = '/login';
  };

  const hasPermission = (perm: string | null): boolean => {
    if (!perm) return true;
    if (!user) return false;

    // Check if user has admin/head of school roles
    const userRoles = user.roles || (user.role ? [user.role] : []);
    const isSuperUser =
      userRoles.includes('SYSTEM_ADMIN') ||
      userRoles.includes('HEAD_OF_SCHOOL') ||
      permissions.includes('*') ||
      permissions.includes('SYSTEM_ADMIN');

    if (isSuperUser) return true;

    // Direct permission match
    if (permissions.includes(perm)) return true;

    // Alias mapping for navigation sections
    switch (perm) {
      case 'STUDENT_VIEW':
        return permissions.includes('STUDENT_VIEW') || permissions.includes('STUDENT_CREATE');
      case 'ATTENDANCE_VIEW':
        return permissions.includes('ATTENDANCE_VIEW') || permissions.includes('ATTENDANCE_CORRECT');
      case 'RESULT_VIEW':
        return permissions.includes('RESULT_VIEW') || permissions.includes('RESULT_PUBLISH');
      case 'PROMOTION_VIEW':
        return permissions.includes('PROMOTION_RUN_EXECUTE') || permissions.includes('PROMOTION_APPROVE');
      case 'FINANCE_VIEW':
        return permissions.includes('FINANCE_REPORT_VIEW') || permissions.includes('INVOICE_VIEW') || permissions.includes('PAYMENT_VIEW');
      case 'ACADEMIC_MANAGE':
        return permissions.includes('ACADEMIC_YEAR_VIEW') || permissions.includes('CLASS_VIEW') || permissions.includes('SUBJECT_VIEW');
      case 'COMMS_SEND':
        return permissions.includes('ANNOUNCEMENT_VIEW') || permissions.includes('ANNOUNCEMENT_CREATE');
      case 'ADMIN_ACCOUNTS':
        return permissions.includes('ACCOUNT_VIEW') || permissions.includes('ACCOUNT_CREATE') || permissions.includes('SCHOOL_SETTINGS_VIEW') || permissions.includes('ROLE_ASSIGN');
      default:
        return false;
    }
  };

  return (
    <AuthContext.Provider value={{ user, permissions, isLoading, login, logout, hasPermission }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};

export const usePermission = (perm: string) => {
  const { hasPermission } = useAuth();
  return hasPermission(perm);
};
