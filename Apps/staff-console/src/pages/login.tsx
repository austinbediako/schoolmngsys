import { useState } from 'react';
import { useAuth } from '@/contexts/auth-context';
import { useForm } from 'react-hook-form';
import { handleApiError } from '@/lib/error-handler';
import { useLocation } from 'wouter';
import { Button } from '@/components/ui/button';
import { ArrowRight, KeyRound, X, CheckCircle2 } from 'lucide-react';
import { toast } from 'sonner';

export default function Login() {
  const { login } = useAuth();
  const [, setLocation] = useLocation();
  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm({
    defaultValues: { identifier: '', password: '' },
  });

  // Forgot Password Modal State
  const [forgotOpen, setForgotOpen] = useState(false);
  const [resetIdentifier, setResetIdentifier] = useState('');
  const [resetSubmitted, setResetSubmitted] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);

  const onSubmit = async (data: any) => {
    try {
      await login({
        identifier: data.identifier,
        password: data.password
      });
      setLocation('/dashboard');
    } catch (e: any) {
      handleApiError(e);
    }
  };

  const handleForgotSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!resetIdentifier) return;
    setResetLoading(true);
    setTimeout(() => {
      setResetLoading(false);
      setResetSubmitted(true);
      toast.success('Password reset instructions requested');
    }, 600);
  };

  return (
    <div className="min-h-screen w-screen bg-[#f8fafc] flex font-sans overflow-hidden relative">
      {/* Left Column - Full Campus Image Background (home1.jpg) */}
      <div className="hidden lg:flex lg:w-1/2 text-white relative flex-col justify-between p-12 overflow-hidden border-r border-slate-800 bg-slate-950">
        {/* Edited Campus Background Image */}
        <img
          src="/home1.jpg"
          alt="University Basic School"
          className="absolute inset-0 w-full h-full object-cover object-center z-0"
        />

        {/* Subtle Gradient Overlay for Readability */}
        <div className="absolute inset-0 bg-gradient-to-t from-slate-950/90 via-slate-950/40 to-slate-950/30 z-0" />

        {/* Single Header Logo & Title */}
        <div className="relative z-10 flex items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-white/10 backdrop-blur-md border border-white/20 p-2 shadow-lg flex items-center justify-center shrink-0">
            <img src="/logo.png" alt="UBS Logo" className="w-full h-full object-contain" />
          </div>
          <div>
            <h1 className="font-display font-bold text-lg text-white tracking-tight leading-tight">University Basic School</h1>
            <p className="text-xs text-slate-300 font-medium">Legon • Staff Console</p>
          </div>
        </div>

        {/* Bottom Title Overlay */}
        <div className="relative z-10 space-y-2 max-w-lg mt-auto">
          <h2 className="font-display text-3xl font-extrabold text-white leading-tight tracking-tight drop-shadow-md">
            Integrated Management Information System
          </h2>
          <p className="text-xs text-slate-300 font-medium leading-relaxed drop-shadow-sm">
            Empowering school management, academic records, and staff operations.
          </p>
        </div>

        {/* Footer */}
        <div className="relative z-10 pt-6 border-t border-white/10 text-xs text-slate-400 flex justify-between items-center mt-6">
          <span>© 2026 University Basic School, Legon</span>
          <span>UBS-LMIS v2.4</span>
        </div>
      </div>

      {/* Right Column - Form Input Fields */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center items-center p-6 sm:p-12 bg-[#f8fafc] overflow-y-auto">
        <div className="w-full max-w-md space-y-8 animate-fade-in">
          {/* Mobile Logo Branding */}
          <div className="lg:hidden text-center space-y-2">
            <img src="/logo.png" alt="UBS Logo" className="mx-auto h-16 w-16 object-contain mb-2" />
            <h2 className="font-display text-2xl font-bold text-slate-900">University Basic School</h2>
            <p className="text-xs text-slate-500 font-medium">Staff Portal Sign In</p>
          </div>

          <div className="space-y-2 text-center lg:text-left">
            <h2 className="font-display text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Sign In to Staff Console
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 font-medium">
              Enter your staff credentials to access class registers, results, and fee administration.
            </p>
          </div>

          {/* Main Form Inputs */}
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-700">Email / Staff ID / Phone</label>
              <input
                id="identifier"
                type="text"
                required
                placeholder="e.g. admin@ubs.edu.gh"
                {...register('identifier', { required: true })}
                className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900 transition-all"
              />
            </div>

            <div className="space-y-1">
              <div className="flex justify-between items-center">
                <label className="text-xs font-semibold text-slate-700">Password</label>
                <button
                  type="button"
                  onClick={() => {
                    setForgotOpen(true);
                    setResetSubmitted(false);
                  }}
                  className="text-[11px] font-semibold text-slate-600 hover:text-slate-900 hover:underline"
                >
                  Forgot Password?
                </button>
              </div>
              <input
                id="password"
                type="password"
                required
                placeholder="••••••••"
                {...register('password', { required: true })}
                className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900 transition-all"
              />
            </div>

            <div className="flex items-center">
              <input
                id="remember-me"
                type="checkbox"
                className="h-4 w-4 text-slate-900 focus:ring-slate-900 border-slate-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 text-xs font-medium text-slate-600">
                Remember this device for 30 days
              </label>
            </div>

            <Button
              type="submit"
              disabled={isSubmitting}
              variant="default"
              size="lg"
              className="w-full"
            >
              {isSubmitting ? (
                <span>Signing in...</span>
              ) : (
                <>
                  <span>Sign In to Portal</span>
                  <ArrowRight className="w-4 h-4 text-amber-400" />
                </>
              )}
            </Button>
          </form>
        </div>
      </div>

      {/* Forgot Password Reset Modal Dialog */}
      {forgotOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl p-6 w-full max-w-md space-y-4 relative z-10">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-xl bg-slate-100 text-slate-800 border border-slate-200">
                  <KeyRound className="w-4 h-4" />
                </div>
                <h3 className="font-display font-bold text-slate-900 text-base">Reset Staff Password</h3>
              </div>
              <button
                onClick={() => setForgotOpen(false)}
                className="p-1 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-100 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {resetSubmitted ? (
              <div className="space-y-4 text-center py-4">
                <div className="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 border border-emerald-200 flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <div className="space-y-1">
                  <h4 className="font-bold text-slate-900 text-sm">Reset Request Dispatched</h4>
                  <p className="text-xs text-slate-500 font-medium leading-relaxed">
                    A temporary password reset code has been sent via SMS to the phone number registered for{' '}
                    <strong className="text-slate-800 font-mono">{resetIdentifier}</strong>.
                  </p>
                </div>
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 text-[11px] text-slate-500 font-medium">
                  Need immediate access? Contact System Admin at <strong className="text-slate-800">admin@ubs.edu.gh</strong>.
                </div>
                <Button onClick={() => setForgotOpen(false)} variant="default" className="w-full">
                  Return to Sign In
                </Button>
              </div>
            ) : (
              <form onSubmit={handleForgotSubmit} className="space-y-4">
                <p className="text-xs text-slate-500 font-medium leading-relaxed">
                  Enter your registered Staff ID, phone number, or username. We will dispatch a temporary reset code to your phone.
                </p>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-slate-700">Staff ID / Phone / Username</label>
                  <input
                    required
                    value={resetIdentifier}
                    onChange={(e) => setResetIdentifier(e.target.value)}
                    placeholder="e.g. +233201234567 or STF-2024-001"
                    className="w-full px-3.5 py-2.5 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-900"
                  />
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <Button type="button" onClick={() => setForgotOpen(false)} variant="outline">
                    Cancel
                  </Button>
                  <Button type="submit" disabled={resetLoading} variant="default">
                    {resetLoading ? 'Requesting...' : 'Request Reset Code'}
                  </Button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
