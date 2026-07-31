import React, { createContext, useContext, useState, useEffect } from 'react';
import { Ward } from '../lib/types';
import { fetchWards } from '../lib/api';

interface WardContextType {
  wards: Ward[];
  selectedWard: Ward | null;
  setSelectedWard: (ward: Ward) => void;
  loading: boolean;
  refreshWards: () => Promise<void>;
}

const WardContext = createContext<WardContextType>({
  wards: [],
  selectedWard: null,
  setSelectedWard: () => {},
  loading: false,
  refreshWards: async () => {}
});

export const WardProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [wards, setWards] = useState<Ward[]>([]);
  const [selectedWard, setSelectedWardState] = useState<Ward | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const loadWards = async () => {
    try {
      setLoading(true);
      const data = await fetchWards();
      setWards(data);
      if (data.length > 0) {
        const savedWardId = localStorage.getItem('ubs_selected_ward_id');
        const found = data.find((w) => w.id === savedWardId);
        setSelectedWardState(found || data[0]);
      }
    } catch (err) {
      console.error('Failed to load wards', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWards();
  }, []);

  const setSelectedWard = (ward: Ward) => {
    setSelectedWardState(ward);
    localStorage.setItem('ubs_selected_ward_id', ward.id);
  };

  return (
    <WardContext.Provider value={{ wards, selectedWard, setSelectedWard, loading, refreshWards: loadWards }}>
      {children}
    </WardContext.Provider>
  );
};

export const useWard = () => useContext(WardContext);
