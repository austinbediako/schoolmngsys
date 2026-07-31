import React, { useState } from 'react';
import { useWard } from '../contexts/WardContext';
import { Users, ChevronDown } from 'lucide-react';
import { Button } from './ui/button';

export const Header: React.FC = () => {
  const { wards, selectedWard, setSelectedWard } = useWard();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200/80 px-4 py-3 shadow-xs">
      <div className="max-w-4xl mx-auto flex items-center justify-between">
        {/* Brand with Staff Console Logo */}
        <div className="flex items-center gap-3">
          <img
            src="/logo.png"
            alt="UBS Legon Logo"
            className="w-10 h-10 object-contain rounded-xl shadow-xs border border-slate-100"
          />
          <div>
            <h1 className="text-sm font-extrabold text-slate-900 tracking-tight leading-tight">
              UBS Legon
            </h1>
          </div>
        </div>

        {/* Multi-Ward Switcher */}
        {wards.length > 0 && (
          <div className="relative">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="bg-white hover:bg-slate-50 text-slate-800 border-slate-200 rounded-xl px-3 py-1.5 shadow-2xs gap-2"
            >
              <Users className="w-4 h-4 text-amber-600" />
              <div className="text-left">
                <p className="text-[9px] uppercase tracking-wider text-slate-400 font-bold">Child</p>
                <p className="text-xs font-bold text-slate-900 truncate max-w-[120px]">
                  {selectedWard ? `${selectedWard.firstName} ${selectedWard.lastName}` : 'Select Ward'}
                </p>
              </div>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 ml-1" />
            </Button>

            {/* Ward Dropdown */}
            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-60 bg-white border border-slate-200 rounded-2xl shadow-xl p-1.5 z-50 animate-in fade-in duration-100">
                <div className="px-3 py-2 border-b border-slate-100 mb-1">
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                    Your Enrolled Children ({wards.length})
                  </p>
                </div>
                {wards.map((ward) => (
                  <button
                    key={ward.id}
                    onClick={() => {
                      setSelectedWard(ward);
                      setDropdownOpen(false);
                    }}
                    className={`w-full text-left px-3 py-2.5 rounded-xl transition-all flex items-center justify-between ${
                      selectedWard?.id === ward.id
                        ? 'bg-amber-50 text-amber-900 font-semibold border border-amber-200/60'
                        : 'hover:bg-slate-50 text-slate-700'
                    }`}
                  >
                    <div>
                      <p className="text-xs font-bold text-slate-900">{ward.firstName} {ward.lastName}</p>
                      <p className="text-[10px] text-slate-500">{ward.className || ward.classLevelName} · {ward.studentNumber}</p>
                    </div>
                    {selectedWard?.id === ward.id && (
                      <span className="w-2 h-2 rounded-full bg-amber-500"></span>
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
};
