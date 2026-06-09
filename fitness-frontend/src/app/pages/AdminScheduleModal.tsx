import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { api } from '../../lib/api'; // подправь путь к твоему axios
import { Lock, PlusCircle, Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "../components/ui/dialog";

interface WorkoutResponseDto {
  id: number;
  workoutName: string;
  workoutType: string;
  description: string;
}

interface TrainerResponseDto {
  id: string; // UUID
  surname: string;
  selfname: string;
  login: string;
  email: string;
}

// Структура для разбора Spring Data Page с тренерами
interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
}

interface AdminScheduleModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void; // колбэк для обновления расписания на главной
}

const ROOMS = ['ORANGE', 'PINK', 'BLUE', 'GREEN'];

export function AdminScheduleModal({ isOpen, onClose, onSuccess }: AdminScheduleModalProps) {
  // Стейты авторизации
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [authError, setAuthError] = useState('');

  // Стейты списков для формы
  const [workouts, setWorkouts] = useState<WorkoutResponseDto[]>([]);
  const [trainers, setTrainers] = useState<TrainerResponseDto[]>([]);
  const [isLoadingData, setIsLoadingData] = useState(false);

  // Стейты отправки формы
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    workoutId: '',
    scheduleDate: '',
    trainerId: '',
    startTime: '', // Будем собирать локально строку 'YYYY-MM-DDTHH:mm:ss'
    endTime: '',
    maxParticipants: '20',
    roomName: 'ORANGE'
  });

  // Загружаем тренеров и воркауты при успешном входе
  useEffect(() => {
    if (!isAuthenticated && isOpen) return;

    async function loadAdminData() {
      try {
        setIsLoadingData(true);
        // Получаем список воркаутов (передается списком)
        const workoutsRes = await api.get<WorkoutResponseDto[]>('/fitness-club/schedules/workouts');
        setWorkouts(workoutsRes.data);

        // Получаем тренеров (приходят в Page, берем content)
        const trainersRes = await api.get<PageResponse<TrainerResponseDto>>('/fitness-club/users/trainers');
        setTrainers(trainersRes.data.content || []);
      } catch (err) {
        console.error("Не удалось загрузить списки для админки:", err);
      } finally {
        setIsLoadingData(false);
      }
    }

    loadAdminData();
  }, [isAuthenticated, isOpen]);

  // Сброс при закрытии модалки
  const handleModalClose = () => {
    setIsAuthenticated(false);
    setLogin('');
    setPassword('');
    setAuthError('');
    onClose();
  };

  // Хэндлер простой авторизации
  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (login === 'admin' && password === '12345678') {
      setIsAuthenticated(true);
      setAuthError('');
    } else {
      setAuthError('Неверный логин или пароль администратора!');
    }
  };

  // Хэндлер отправки формы создания расписания
  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // Собираем LocalDateTime формат для бэкенда (дописываем секунды :00)
      const payload = {
        workoutId: parseInt(form.workoutId),
        scheduleDate: form.scheduleDate,
        trainerId: form.trainerId,
        startTime: `${form.scheduleDate}T${form.startTime}:00`,
        endTime: `${form.scheduleDate}T${form.endTime}:00`,
        maxParticipants: parseInt(form.maxParticipants),
        roomName: form.roomName
      };

      // Эндпоинт создания расписания (ScheduleCreateDto.java)
      await api.post('/fitness-club/schedules/schedule', payload);

      alert("Новое занятие успешно добавлено в расписание!");
      if (onSuccess) onSuccess(); // обновляем сетку расписания
      handleModalClose();
    } catch (error: any) {
      console.error(error);
      const serverMessage = typeof error.response?.data === 'string' 
        ? error.response.data 
        : error.response?.data?.message;
      alert(`Ошибка создания: ${serverMessage || 'Проверьте заполнение полей'}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
        alert("Модалка получила сигнал открыть окно! isOpen = true");
    }
    }, [isOpen]);

  return (
    <Dialog open={isOpen} onOpenChange={handleModalClose}>
      <DialogContent className="sm:max-w-lg bg-white rounded-2xl p-6 text-gray-900">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold flex items-center gap-2">
            <Lock className="w-5 h-5 text-pink-500" />
            {isAuthenticated ? 'Панель администратора: Добавление занятия' : 'Вход в панель администратора'}
          </DialogTitle>
          <DialogDescription>
            {isAuthenticated 
              ? 'Заполните параметры, чтобы сгенерировать новую тренировку в сетке расписания.' 
              : 'Для добавления тренировок, пожалуйста, подтвердите права доступа.'}
          </DialogDescription>
        </DialogHeader>

        {/* СТАДИЯ 1: ФОРМА АВТОРИЗАЦИИ */}
        {!isAuthenticated ? (
          <form onSubmit={handleLoginSubmit} className="space-y-4 pt-2">
            <div className="space-y-1.5">
              <Label htmlFor="adminLogin">Логин</Label>
              <Input 
                id="adminLogin" 
                value={login} 
                onChange={(e) => setLogin(e.target.value)} 
                placeholder="Например, admin"
                required 
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="adminPassword">Пароль</Label>
              <Input 
                id="adminPassword" 
                type="password" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                placeholder="••••••••"
                required 
              />
            </div>
            {authError && <p className="text-xs text-red-500 font-medium">{authError}</p>}
            <Button type="submit" className="w-full bg-pink-500 hover:bg-pink-600 text-white cursor-pointer mt-2">
              Войти
            </Button>
          </form>
        ) : (
          /* СТАДИЯ 2: ФОРМА СОЗДАНИЯ ТРЕНИРОВКИ */
          <form onSubmit={handleFormSubmit} className="space-y-4 max-h-[75vh] overflow-y-auto pr-1 pt-2">
            {isLoadingData ? (
              <div className="text-center py-8 text-gray-500 flex flex-col items-center gap-2">
                <Loader2 className="w-6 h-6 animate-spin text-pink-500" />
                <span>Загрузка данных с бэкенда...</span>
              </div>
            ) : (
              <>
                {/* 1. Выбор вида тренировки (Workout) */}
                <div className="space-y-1.5">
                  <Label htmlFor="workoutSelect">Вид тренировки (Workout)</Label>
                  <select
                    id="workoutSelect"
                    value={form.workoutId}
                    onChange={(e) => setForm({...form, workoutId: e.target.value})}
                    className="w-full h-10 px-3 rounded-md border border-gray-200 bg-white text-sm outline-none focus:border-pink-500"
                    required
                  >
                    <option value="">-- Выберите тренировку --</option>
                    {workouts.map((w) => (
                      <option key={w.id} value={w.id}>{w.workoutName} ({w.workoutType})</option>
                    ))}
                  </select>
                </div>

                {/* 2. Выбор Инструктора (Trainer) */}
                <div className="space-y-1.5">
                  <Label htmlFor="trainerSelect">Инструктор</Label>
                  <select
                    id="trainerSelect"
                    value={form.trainerId}
                    onChange={(e) => setForm({...form, trainerId: e.target.value})}
                    className="w-full h-10 px-3 rounded-md border border-gray-200 bg-white text-sm outline-none focus:border-pink-500"
                    required
                  >
                    <option value="">-- Выберите тренера --</option>
                    {trainers.map((t) => (
                      <option key={t.id} value={t.id}>{`${t.surname} ${t.selfname}`}</option>
                    ))}
                  </select>
                </div>

                {/* 3. Выбор комнаты (ROOMS) */}
                <div className="space-y-1.5">
                  <Label htmlFor="roomSelect">Зал / Комната</Label>
                  <select
                    id="roomSelect"
                    value={form.roomName}
                    onChange={(e) => setForm({...form, roomName: e.target.value})}
                    className="w-full h-10 px-3 rounded-md border border-gray-200 bg-white text-sm outline-none focus:border-pink-500"
                    required
                  >
                    {ROOMS.map((room) => (
                      <option key={room} value={room}>{room}</option>
                    ))}
                  </select>
                </div>

                {/* 4. Дата тренировки */}
                <div className="space-y-1.5">
                  <Label htmlFor="scheduleDate">Дата занятия</Label>
                  <Input
                    id="scheduleDate"
                    type="date"
                    value={form.scheduleDate}
                    onChange={(e) => setForm({...form, scheduleDate: e.target.value})}
                    required
                  />
                </div>

                {/* 5. Время начала и окончания */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <Label htmlFor="startTime">Время начала</Label>
                    <Input
                      id="startTime"
                      type="time"
                      value={form.startTime}
                      onChange={(e) => setForm({...form, startTime: e.target.value})}
                      required
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="endTime">Время окончания</Label>
                    <Input
                      id="endTime"
                      type="time"
                      value={form.endTime}
                      onChange={(e) => setForm({...form, endTime: e.target.value})}
                      required
                    />
                  </div>
                </div>

                {/* 6. Лимит мест */}
                <div className="space-y-1.5">
                  <Label htmlFor="maxParticipants">Макс. количество участников</Label>
                  <Input
                    id="maxParticipants"
                    type="number"
                    min="1"
                    max="100"
                    value={form.maxParticipants}
                    onChange={(e) => setForm({...form, maxParticipants: e.target.value})}
                    required
                  />
                </div>

                <Button 
                  type="submit" 
                  disabled={isSubmitting} 
                  className="w-full bg-pink-500 hover:bg-pink-600 text-white cursor-pointer mt-4 flex items-center justify-center gap-2"
                >
                  {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <PlusCircle className="w-4 h-4" />}
                  Создать занятие
                </Button>
              </>
            )}
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}