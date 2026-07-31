import React, { useEffect, useState } from 'react';
import { fetchAnnouncements } from '../lib/api';
import { Announcement } from '../lib/types';
import { Megaphone, Calendar, Sparkles } from 'lucide-react';

export const AnnouncementsPage: React.FC = () => {
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnnouncements()
      .then(setAnnouncements)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-slate-900"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="bg-white border border-slate-200 rounded-3xl p-5 flex items-center justify-between shadow-xs">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Megaphone className="w-5 h-5 text-amber-600" />
            School Announcements & Notices
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Official communications from the Head of School and Administration
          </p>
        </div>
      </div>

      {/* Announcements Feed */}
      <div className="space-y-4">
        {announcements.map((anc) => (
          <div key={anc.id} className="bg-white border border-slate-200 rounded-3xl p-5 hover:border-slate-300 transition-all shadow-xs space-y-3">
            <div className="flex items-center justify-between">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-extrabold uppercase bg-amber-50 text-amber-800 border border-amber-200">
                <Sparkles className="w-3 h-3 text-amber-600" /> {anc.audienceType.replace('_', ' ')}
              </span>
              <span className="text-[11px] text-slate-400 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5" />
                {new Date(anc.createdAt).toLocaleDateString()}
              </span>
            </div>

            <h3 className="text-base font-bold text-slate-900">{anc.title}</h3>
            <p className="text-xs text-slate-600 leading-relaxed">{anc.content}</p>
          </div>
        ))}
      </div>
    </div>
  );
};
