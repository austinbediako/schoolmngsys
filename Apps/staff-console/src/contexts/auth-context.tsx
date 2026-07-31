import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types';
import { apiClient, setTokens, getTokens } from '@/lib/api-client';

interface AuthContextType {
  user: User | null;
  permissions: string[];
  isLoading: boolean;
  login: (credentials: any) => Promise<void>;
  logout: () => void;
  hasPermission: (perm: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchMe = async () => {
    try {
      const data = await apiClient('/auth/me');
      setUser(data.user);
      setPermissions(data.user.permissions || []);
    } catch (e) {
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

  const hasPermission = (perm: string) => permissions.includes(perm);

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
