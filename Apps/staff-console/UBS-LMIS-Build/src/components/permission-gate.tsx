import React from 'react';
import { usePermission } from '@/contexts/auth-context';

export function PermissionGate({ children, perm }: { children: React.ReactNode; perm: string }) {
  const hasPerm = usePermission(perm);
  if (!hasPerm) return null;
  return <>{children}</>;
}
