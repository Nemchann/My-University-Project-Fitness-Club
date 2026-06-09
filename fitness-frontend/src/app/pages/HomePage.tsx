import { Hero } from '../components/Hero';
import { Schedule } from '../components/Schedule';
import { Trainers } from '../components/Trainers';
import { Pricing } from '../components/Pricing';
import { useState } from 'react';
import { Button } from '../components/ui/button';
import { ShieldAlert } from 'lucide-react';
import { AdminScheduleModal } from './AdminScheduleModal';

export function HomePage() {
  const [isAdminModalOpen, setIsAdminModalOpen] = useState(false);

  const handleAdminSuccess = () => {
    // Автоматически обновляем страницу, чтобы новые занятия точно подтянулись с бэка в <Schedule />
    window.location.reload();
  };
  return (
    <div className="relative min-h-screen bg-white">
      <Hero />
      {/* НЕБОЛЬШАЯ СЕКЦИЯ С КНОПКОЙ УПРАВЛЕНИЯ ПЕРЕД РАСПИСАНИЕМ */}
      <div className="bg-gray-50 border-y border-gray-100 py-3">
        <div className="container mx-auto px-4 flex justify-end">
          <Button 
            onClick={() => setIsAdminModalOpen(true)}
            variant="ghost" 
            className="relative z-50 text-gray-400 hover:text-pink-500 gap-1.5 text-xs border border-dashed border-gray-300 hover:border-pink-300 rounded-xl px-4 py-2 transition-all cursor-pointer"
          >
            <ShieldAlert className="w-3.5 h-3.5" />
            Панель администратора
          </Button>
        </div>
      </div>
      <Schedule />
      <Trainers />
      <Pricing />

      {/* МОДАЛЬНОЕ ОКНО */}
      <AdminScheduleModal 
        isOpen={isAdminModalOpen} 
        onClose={() => setIsAdminModalOpen(false)}
        onSuccess={handleAdminSuccess}
      />
    </div>
  );
}
