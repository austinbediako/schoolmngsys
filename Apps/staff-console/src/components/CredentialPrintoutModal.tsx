import React from 'react';
import { Button } from '@/components/ui/button';
import { Printer, X, ShieldCheck, Key, Lock, CheckCircle2 } from 'lucide-react';

interface CredentialPrintoutModalProps {
  open: boolean;
  onClose: () => void;
  accountType: 'STAFF' | 'GUARDIAN';
  name: string;
  identifier: string;
  temporaryPassword: string;
  portalUrl: string;
  roles?: string[];
  linkedWardName?: string;
}

export const CredentialPrintoutModal: React.FC<CredentialPrintoutModalProps> = ({
  open,
  onClose,
  accountType,
  name,
  identifier,
  temporaryPassword,
  portalUrl,
  roles = [],
  linkedWardName,
}) => {
  if (!open) return null;

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in">
      {/* Container */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-xl overflow-hidden print:shadow-none print:border-none print:w-full print:max-w-none print:p-0">
        {/* Modal Top Bar (Hidden on Print) */}
        <div className="bg-slate-900 text-white px-6 py-4 flex items-center justify-between print:hidden">
          <div className="flex items-center gap-2.5">
            <ShieldCheck className="w-5 h-5 text-amber-400" />
            <h3 className="font-display font-bold text-sm">
              Account Created — Official Credential Slip
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Printable Official Handout Slip */}
        <div className="p-6 sm:p-8 space-y-6 print:p-8 print:space-y-6 bg-white font-sans text-slate-900">
          {/* Header Branding */}
          <div className="flex items-center justify-between border-b-2 border-slate-900 pb-4">
            <div className="flex items-center gap-3">
              <img src="/logo.png" alt="UBS Logo" className="w-12 h-12 object-contain" />
              <div>
                <h1 className="font-display font-extrabold text-lg tracking-tight text-slate-900 leading-tight">
                  University Basic School
                </h1>
                <p className="text-xs text-slate-600 font-semibold">
                  Legon Campus • Integrated Management Information System
                </p>
              </div>
            </div>
            <div className="text-right">
              <span className="inline-block px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider bg-slate-100 text-slate-800 border border-slate-300">
                CONFIDENTIAL
              </span>
              <p className="text-[10px] text-slate-400 font-mono mt-1">
                Issued: {new Date().toLocaleDateString('en-GB')}
              </p>
            </div>
          </div>

          {/* Title Banner */}
          <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 text-center space-y-1">
            <h2 className="font-display text-base font-bold text-slate-900 uppercase tracking-wide">
              {accountType === 'GUARDIAN' ? 'Guardian Portal Access Slip' : 'Staff Console Account Access Slip'}
            </h2>
            <p className="text-xs text-slate-500 font-medium">
              Official One-Time Login Credentials & Security Instructions
            </p>
          </div>

          {/* User & Linked Profile Details */}
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div className="space-y-1 bg-slate-50/50 p-3 rounded-xl border border-slate-200/80">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Account Holder</span>
              <p className="font-bold text-slate-900 text-sm">{name}</p>
            </div>

            {accountType === 'GUARDIAN' && linkedWardName ? (
              <div className="space-y-1 bg-slate-50/50 p-3 rounded-xl border border-slate-200/80">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Enrolled Child (Ward)</span>
                <p className="font-bold text-slate-900 text-sm">{linkedWardName}</p>
              </div>
            ) : (
              <div className="space-y-1 bg-slate-50/50 p-3 rounded-xl border border-slate-200/80">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Assigned System Roles</span>
                <p className="font-bold text-slate-900 text-sm">
                  {roles.length > 0 ? roles.join(', ').replace(/_/g, ' ') : 'Staff Member'}
                </p>
              </div>
            )}
          </div>

          {/* Credentials Box */}
          <div className="bg-amber-50/80 border-2 border-amber-300/80 rounded-2xl p-5 space-y-4">
            <div className="flex items-center gap-2 text-amber-900 border-b border-amber-200/80 pb-2">
              <Key className="w-4 h-4 text-amber-700" />
              <span className="text-xs font-bold uppercase tracking-wider">Login Credentials</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-[10px] font-bold text-amber-800/70 uppercase tracking-wider block mb-1">
                  Portal Login URL
                </span>
                <code className="text-xs font-mono font-bold text-slate-900 bg-white px-2.5 py-1.5 rounded-lg border border-amber-200 block truncate">
                  {portalUrl}
                </code>
              </div>

              <div>
                <span className="text-[10px] font-bold text-amber-800/70 uppercase tracking-wider block mb-1">
                  Login Identifier (Username/Phone/Email)
                </span>
                <code className="text-xs font-mono font-bold text-slate-900 bg-white px-2.5 py-1.5 rounded-lg border border-amber-200 block truncate">
                  {identifier}
                </code>
              </div>
            </div>

            <div className="pt-2 border-t border-amber-200/80">
              <span className="text-[10px] font-bold text-amber-800/70 uppercase tracking-wider block mb-1">
                One-Time Temporary Password
              </span>
              <div className="bg-white border-2 border-amber-400 rounded-xl px-4 py-3 text-center">
                <span className="font-mono text-xl font-extrabold text-slate-900 tracking-wider">
                  {temporaryPassword}
                </span>
              </div>
            </div>
          </div>

          {/* Important Security Notice */}
          <div className="space-y-2 text-xs text-slate-600 bg-slate-50 p-4 rounded-xl border border-slate-200">
            <div className="flex items-center gap-2 font-bold text-slate-900">
              <Lock className="w-3.5 h-3.5 text-slate-700" />
              <span>Security Instructions:</span>
            </div>
            <ul className="list-disc list-inside space-y-1 text-[11px] text-slate-600 font-medium">
              <li>Navigate to <strong>{portalUrl}</strong> and log in with the temporary password above.</li>
              <li>You will be required to set a permanent, private password upon first sign in.</li>
              <li>Keep this credential slip in a secure place. Do not share your login details with anyone.</li>
            </ul>
          </div>

          {/* Official Signature Line for Handout */}
          <div className="pt-6 border-t border-slate-200 flex justify-between items-end text-[11px] text-slate-400">
            <div>
              <p className="font-semibold text-slate-600">Issued by: System Administrator</p>
              <p>University Basic School Administration</p>
            </div>
            <div className="text-right border-t border-slate-300 pt-1 w-48 text-slate-500">
              Authorized Signature & Stamp
            </div>
          </div>
        </div>

        {/* Modal Footer Actions (Hidden on Print) */}
        <div className="bg-slate-50 px-6 py-4 border-t border-slate-200 flex items-center justify-between print:hidden">
          <p className="text-xs text-slate-500 font-medium">
            Hand this printed slip directly to the account holder.
          </p>
          <div className="flex gap-3">
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
            <Button variant="default" onClick={handlePrint} className="gap-2">
              <Printer className="w-4 h-4" />
              <span>Print Credentials Slip</span>
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};
