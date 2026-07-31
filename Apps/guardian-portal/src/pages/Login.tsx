import React, { useState } from 'react';
import { loginGuardian } from '../lib/api';
import { Lock, Phone, AlertCircle, ArrowRight } from 'lucide-react';
import { Button } from '../components/ui/button';
import { motion } from 'framer-motion';

export const LoginPage: React.FC<{ onLoginSuccess: () => void }> = ({ onLoginSuccess }) => {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const data = await loginGuardian(identifier, password);
      localStorage.setItem('ubs_guardian_token', data.accessToken);
      onLoginSuccess();
    } catch (err: any) {
      setError(err.message || 'Login failed. Invalid phone number, email or password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4 sm:p-6 lg:p-10 font-sans relative overflow-hidden">
      {/* Subtle Background Decorative Shapes */}
      <div className="absolute top-[-10%] left-[-5%] w-[40rem] h-[40rem] bg-amber-200/30 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-[-10%] right-[-5%] w-[40rem] h-[40rem] bg-indigo-200/30 rounded-full blur-3xl pointer-events-none"></div>

      {/* Main Awwwards Card Container */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
        className="relative z-10 w-full max-w-5xl bg-white rounded-[2.5rem] shadow-2xl shadow-slate-300/60 border border-slate-200/80 overflow-hidden grid grid-cols-1 lg:grid-cols-12 min-h-[640px]"
      >
        {/* Left Side: Immersive Photography Frame (7 columns) */}
        <div className="lg:col-span-7 relative overflow-hidden p-8 lg:p-12 flex flex-col justify-between group">
          {/* Main Photo Background */}
          <img
            src="/home.jpg"
            alt="University Basic School Legon Campus"
            className="absolute inset-0 w-full h-full object-cover object-center transform group-hover:scale-105 transition-transform duration-1000 ease-out"
          />

          {/* Dark Architectural Gradient Overlay */}
          <div className="absolute inset-0 bg-gradient-to-t from-slate-950/90 via-slate-950/30 to-slate-900/10"></div>

          {/* Top Brand Header */}
          <div className="relative z-10 flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-white/95 backdrop-blur-md p-1.5 shadow-xl border border-white/40 flex items-center justify-center">
              <img src="/logo.png" alt="UBS Logo" className="w-full h-full object-contain" />
            </div>
            <div>
              <h1 className="text-white font-extrabold text-base tracking-tight leading-tight">
                UBS Legon
              </h1>
              <p className="text-amber-400 text-xs font-semibold tracking-wide">
                University of Ghana
              </p>
            </div>
          </div>

          {/* Bottom Clean Typography */}
          <div className="relative z-10 space-y-2">
            <h2 className="text-2xl sm:text-3xl font-extrabold text-white leading-tight tracking-tight">
              Empowering Parents & Shaping Future Leaders.
            </h2>
            <p className="text-xs text-slate-300 max-w-md font-light leading-relaxed">
              Track terminal academic performance, daily classroom attendance, fee receipts, and school announcements in one central location.
            </p>
          </div>
        </div>

        {/* Right Side: Ultra-Clean Form Area (5 columns) */}
        <div className="lg:col-span-5 p-8 lg:p-12 bg-white flex flex-col justify-between">
          <div>
            {/* Header */}
            <div className="space-y-1.5 mb-8">
              <p className="text-[11px] font-extrabold uppercase tracking-widest text-amber-600">
                Parent Sign In
              </p>
              <h3 className="text-2xl font-extrabold text-slate-900 tracking-tight">
                Portal Access
              </h3>
              <p className="text-xs text-slate-500 font-normal">
                Please enter your credentials to manage your child's profile.
              </p>
            </div>

            {error && (
              <motion.div
                initial={{ opacity: 0, y: -8 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-rose-50 border border-rose-200 rounded-2xl p-3.5 flex items-center gap-3 text-rose-700 text-xs mb-6 shadow-2xs"
              >
                <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-500" />
                <p className="font-medium">{error}</p>
              </motion.div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-5">
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-800 tracking-tight block">
                  Phone Number / Email
                </label>
                <div className="relative group">
                  <Phone className="w-4 h-4 text-slate-400 group-focus-within:text-slate-900 transition-colors absolute left-3.5 top-3.5" />
                  <input
                    type="text"
                    required
                    value={identifier}
                    onChange={(e) => setIdentifier(e.target.value)}
                    placeholder="e.g. 024XXXXXXX or parent@gmail.com"
                    className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-3 pl-10 pr-4 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-slate-900 focus:bg-white transition-all duration-200 font-medium"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-bold text-slate-800 tracking-tight block">
                    Password
                  </label>
                  <a href="#forgot" onClick={(e) => { e.preventDefault(); alert("Please contact the school admin office to reset your password."); }} className="text-[11px] font-bold text-amber-600 hover:underline">
                    Forgot?
                  </a>
                </div>
                <div className="relative group">
                  <Lock className="w-4 h-4 text-slate-400 group-focus-within:text-slate-900 transition-colors absolute left-3.5 top-3.5" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-3 pl-10 pr-4 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-slate-900 focus:bg-white transition-all duration-200 font-medium"
                  />
                </div>
              </div>

              <Button
                type="submit"
                disabled={loading}
                variant="default"
                size="lg"
                className="w-full h-11 text-xs font-bold rounded-2xl shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 group mt-4"
              >
                <span>{loading ? 'Authenticating...' : 'Sign In to Portal'}</span>
                <ArrowRight className="w-4 h-4 text-amber-400 group-hover:translate-x-1 transition-transform" />
              </Button>
            </form>
          </div>

          {/* Footer Assistance */}
          <div className="pt-6 border-t border-slate-100 text-xs text-slate-500">
            <p>
              Having trouble? Contact <span className="font-bold text-slate-900">UBS Administration</span>.
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
};
