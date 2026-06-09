import { useState } from 'react';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { Lock } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "../components/ui/dialog";

interface AdminCancelModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void; // Функция, которая вызовется при успешном вводе пароля
}

export function AdminCancelModal({ isOpen, onClose, onConfirm }: AdminCancelModalProps) {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [authError, setAuthError] = useState('');

  const handleModalClose = () => {
    setLogin('');
    setPassword('');
    setAuthError('');
    onClose();
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (login === 'admin' && password === '12345678') {
      onConfirm(); // Вызываем удаление
      handleModalClose();
    } else {
      setAuthError('Неверный логин или пароль администратора!');
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleModalClose}>
      <DialogContent className="fixed left-[50%] top-[50%] z-[10000] sm:max-w-md translate-x-[-50%] translate-y-[-50%] bg-white rounded-2xl p-6 text-gray-900 shadow-2xl border border-gray-100">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold flex items-center gap-2">
            <Lock className="w-5 h-5 text-red-500" />
            Подтверждение прав администратора
          </DialogTitle>
          <DialogDescription>
            Отмена тренировки затронет всех записанных пользователей. Пожалуйста, введите данные администратора.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 pt-2">
          <div className="space-y-1.5">
            <Label htmlFor="cancelLogin">Логин</Label>
            <Input 
              id="cancelLogin" 
              value={login} 
              onChange={(e) => setLogin(e.target.value)} 
              placeholder="admin"
              required 
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cancelPassword">Пароль</Label>
            <Input 
              id="cancelPassword" 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              placeholder="••••••••"
              required 
            />
          </div>
          {authError && <p className="text-xs text-red-500 font-medium">{authError}</p>}
          <Button type="submit" className="w-full bg-red-500 hover:bg-red-600 text-white cursor-pointer mt-2">
            Подтвердить отмену занятия
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}