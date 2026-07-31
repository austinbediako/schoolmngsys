import React from 'react';
import {
  LayoutDashboard,
  Users,
  CalendarCheck,
  BookOpen,
  TrendingUp,
  Wallet,
  Settings,
  Bell,
  ShieldCheck,
  Menu,
  LogOut,
  CheckCircle2,
  CreditCard,
  Clock,
  Plus,
  Pencil,
  Printer,
  Search,
  Filter,
  ArrowRight,
  ChevronRight,
  X,
  AlertCircle,
  UserCheck,
  UserX,
  FileText,
  DollarSign,
  GraduationCap,
  Sparkles,
  Layers,
  Award,
  Send,
  HelpCircle,
  LucideProps
} from 'lucide-react';

const iconMap: Record<string, React.FC<LucideProps>> = {
  // Navigation & Core Modules
  'dashboard-square-01': LayoutDashboard,
  'dashboard': LayoutDashboard,
  'user-multiple': Users,
  'students': Users,
  'calendar-check-in-01': CalendarCheck,
  'attendance': CalendarCheck,
  'book-open-01': BookOpen,
  'results': BookOpen,
  'chart-increase': TrendingUp,
  'promotion': TrendingUp,
  'wallet-01': Wallet,
  'finance': Wallet,
  'settings-01': Settings,
  'academic': GraduationCap,
  'notification-01': Bell,
  'communication': Send,
  'shield-01': ShieldCheck,
  'admin': ShieldCheck,

  // Action & Utility Icons
  'menu-01': Menu,
  'logout-01': LogOut,
  'checkmark-circle-01': CheckCircle2,
  'credit-card': CreditCard,
  'clock-01': Clock,
  'add-01': Plus,
  'pencil-edit-01': Pencil,
  'printer': Printer,
  'search-01': Search,
  'filter': Filter,
  'arrow-right-01': ArrowRight,
  'chevron-right': ChevronRight,
  'cancel-01': X,
  'alert-circle': AlertCircle,
  'user-check': UserCheck,
  'user-x': UserX,
  'file-text': FileText,
  'dollar-sign': DollarSign,
  'sparkles': Sparkles,
  'layers': Layers,
  'award': Award,
};

interface IconProps extends Omit<LucideProps, 'ref'> {
  name: string;
  className?: string;
  size?: number | string;
}

export function Icon({ name, className = '', size, ...props }: IconProps) {
  const IconComponent = iconMap[name.toLowerCase()] || HelpCircle;
  return <IconComponent className={className} size={size} {...props} />;
}
