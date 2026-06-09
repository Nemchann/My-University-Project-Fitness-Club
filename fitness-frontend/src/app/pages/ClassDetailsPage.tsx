import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router';
import { motion } from 'motion/react';
import {
  Calendar,
  Clock,
  User,
  MapPin,
  Users,
  ArrowLeft,
} from 'lucide-react';
import { Card, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Progress } from '../components/ui/progress';
import { format, differenceInMinutes, parseISO } from 'date-fns';
import { ru } from 'date-fns/locale';
import { api } from "../../lib/api"; 
import { AdminCancelModal } from './AdminCancelModal';

// Интерфейс, совпадающий с Java ScheduleResponseDto
interface ScheduleResponseDto {
  id: number;
  workoutName: string;
  trainerFullName: string;
  workoutType: string;
  description: string;
  room: string;
  scheduleDate: string; // YYYY-MM-DD
  startTime: string;    // ISO string
  endTime: string;      // ISO string
  maxParticipants: number;
  currentParticipants: number;
}

export function ClassDetailsPage() {
  const { id } = useParams<{ id: string }>(); // Получаем id из URL
  const navigate = useNavigate();
  
  const [workout, setWorkout] = useState<ScheduleResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isBooking, setIsBooking] = useState(false);
  const [bookingMessage, setBookingMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);
  const [isAdminCancelOpen, setIsAdminCancelOpen] = useState(false);

  // Получаем детали тренировки при загрузке страницы
  useEffect(() => {
    const fetchWorkoutDetails = async () => {
      try {
        setIsLoading(true);
        // Запрос к Java через Go-прокси
        const response = await api.get(`/fitness-club/schedules/schedule/${id}`);
        setWorkout(response.data);
      } catch (error) {
        console.error("Ошибка при загрузке деталей тренировки:", error);
      } finally {
        setIsLoading(false);
      }
    };

    if (id) fetchWorkoutDetails();
  }, [id]);

  // Логика бронирования
  const handleBooking = async () => {
    const userId = localStorage.getItem("userId");
    
    if (!userId) {
      setBookingMessage("Ошибка: не найден UUID пользователя в localStorage.");
      setIsSuccess(false);
      return;
    }

    setIsBooking(true);
    setBookingMessage('');

    try {
      // Формируем JSON-тело в соответствии с BookingCreateDto
      const bookingPayload = {
        userId: userId,                   // UUID клиента
        scheduleId: Number(id),           // Integer ID тренировки
        createdAt: new Date().toISOString() // Текущее время бронирования (OffsetDateTime)
      };

      // Отправляем POST запрос на прокси
      await api.post('/fitness-club/bookings/book', bookingPayload);
      
      setIsSuccess(true);
      setBookingMessage("Вы успешно записались на занятие!");
      
      // Обновляем количество участников локально на экране, чтобы кнопка сразу заблокировалась
      if (workout) {
        setWorkout({
          ...workout,
          currentParticipants: workout.currentParticipants + 1
        });
      }

      navigate('/profile');

    } catch (error: any) {
      console.error("Ошибка при бронировании:", error);
      setIsSuccess(false);
      // Проверяем, вернул ли бэкенд текстовый ответ с ошибкой
      if (error.response && typeof error.response.data === 'string') {
        setBookingMessage(error.response.data);
      } 
      // Если бэкенд вернул JSON-ошибку с полем message 
      else if (error.response?.data?.message) {
        setBookingMessage(error.response.data.message);
      } 
      // Запасной вариант на случай, если бэкенд «упал» 
      else {
        setBookingMessage("Не удалось связаться с сервером. Попробуйте позже.");
      }
    } finally {
      setIsBooking(false);
    }
  };

  if (isLoading) {
    return <div className="p-20 text-center text-gray-500">Загрузка информации о занятии...</div>;
  }

  if (!workout) {
    return (
      <div className="p-20 text-center">
        <p className="text-red-500 font-semibold mb-4">Занятие не найдено</p>
        <Link to="/" className="text-pink-600 hover:underline">Вернуться на главную</Link>
      </div>
    );
  }

  // Вычисляем время и день недели
  const start = parseISO(workout.startTime);
  const end = parseISO(workout.endTime);
  const timeString = format(start, 'HH:mm');
  const durationMin = differenceInMinutes(end, start);
  
  // Получаем день недели с заглавной буквы
  const dayOfWeek = format(start, 'EEEE', { locale: ru });
  const capitalizedDay = dayOfWeek.charAt(0).toUpperCase() + dayOfWeek.slice(1);

  const spotsLeft = workout.maxParticipants - workout.currentParticipants;
  const fillPercentage = (workout.currentParticipants / workout.maxParticipants) * 100;

  const executeSoftDelete = async () => {
  if (!workout?.id) return;

  try {

    await api.delete(`/fitness-club/schedules/cancel/${workout.id}`);
    
    alert("Тренировка успешно переведена в статус удаленной (Soft Delete)!");
    
    // Перенаправляем пользователя обратно на главную страницу, 
    // чтобы увидеть обновленное расписание
    navigate('/');
  } catch (error: any) {
    console.error("Ошибка при мягком удалении тренировки:", error);
    const serverMessage = typeof error.response?.data === 'string' 
      ? error.response.data 
      : error.response?.data?.message;
    alert(`Не удалось отменить тренировку: ${serverMessage || 'Ошибка сервера'}`);
  }
};

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="container mx-auto px-4 max-w-4xl">
        
        {/* Кнопка назад */}
        <Link to="/">
          <Button variant="ghost" className="mb-6 gap-2 text-gray-600 hover:text-pink-600 transition-colors cursor-pointer">
            <ArrowLeft className="w-4 h-4" /> Назад к расписанию
          </Button>
        </Link>

        <div className="grid md:grid-cols-[1fr_320px] gap-8 items-start">
          
          {/* Левая колонка: Основная информация */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="space-y-6"
          >
            <div className="bg-white rounded-2xl p-8 shadow-sm border border-gray-100">
              <span className="px-3 py-1 bg-pink-100 text-pink-700 rounded-full text-sm font-medium">
                {workout.workoutType}
              </span>
              
              <h1 className="text-3xl font-bold text-gray-900 mt-3 mb-4">
                {workout.workoutName}
              </h1>

              <p className="text-gray-600 leading-relaxed">
                {workout.description || "Описание для данной тренировки пока не добавлено."}
              </p>
            </div>

            {/* Блок деталей */}
            <div className="bg-white rounded-2xl p-8 shadow-sm border border-gray-100 grid sm:grid-cols-2 gap-6">
              <div className="flex items-start gap-3">
                <Calendar className="w-5 h-5 text-pink-500 mt-0.5" />
                <div>
                  <div className="text-sm text-gray-400">День проведения</div>
                  <div className="font-semibold text-gray-800">{capitalizedDay}</div>
                  <div className="text-xs text-gray-500">{format(start, 'd MMMM yyyy', { locale: ru })}</div>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Clock className="w-5 h-5 text-pink-500 mt-0.5" />
                <div>
                  <div className="text-sm text-gray-400">Время и длительность</div>
                  <div className="font-semibold text-gray-800">{timeString}</div>
                  <div className="text-sm text-gray-500">{durationMin} мин</div>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-pink-500 mt-0.5" />
                <div>
                  <div className="text-sm text-gray-400">Локация</div>
                  <div className="font-semibold text-gray-800">{workout.room}</div>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <User className="w-5 h-5 text-pink-500 mt-0.5" />
                <div>
                  <div className="text-sm text-gray-400">Инструктор</div>
                  <div className="font-semibold text-gray-800">{workout.trainerFullName}</div>
                </div>
              </div>
            </div>
          </motion.div>

          {/* Правая колонка: Карточка записи */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 }}
          >
            <Card className="shadow-md border-gray-100 bg-white sticky top-6">
              <CardContent className="p-6">
                <div className="space-y-4">
                  <div className="flex items-center gap-2 text-gray-700 font-semibold">
                    <Users className="w-5 h-5 text-pink-500" />
                    <span>Доступность мест</span>
                  </div>

                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Занято мест:</span>
                      <span className="font-semibold text-gray-800">
                        {workout.currentParticipants} / {workout.maxParticipants}
                      </span>
                    </div>
                    <Progress value={fillPercentage} className="h-2" />
                  </div>

                  <div className="text-center py-2">
                    <div className="text-2xl font-bold text-gray-900">
                      {spotsLeft > 0 ? spotsLeft : "0"}
                    </div>
                    <div className="text-xs text-gray-400 uppercase tracking-wider">
                      осталось свободных мест
                    </div>
                  </div>

                  {/* Кнопка записи */}
                  <Button
                    onClick={handleBooking}
                    disabled={isBooking || spotsLeft === 0 || isSuccess}
                    className="w-full bg-pink-500 hover:bg-pink-600 text-white py-6 text-lg font-semibold shadow-md disabled:bg-gray-200 cursor-pointer transition-all"
                  >
                    {isSuccess ? "Вы записаны!" : spotsLeft === 0 ? "Мест нет" : isBooking ? "Запись..." : "Записаться"}
                  </Button>

                  {/* Сообщение об успехе или ошибке */}
                  {bookingMessage && (
                    <p className={`text-sm text-center font-medium mt-2 ${isSuccess ? 'text-green-600' : 'text-red-500'}`}>
                      {bookingMessage}
                    </p>
                  )}
                </div>

                {/* ================= НОВЫЙ БЛОК: УПРАВЛЕНИЕ ДЛЯ АДМИНИСТРАТОРА ================= */}
                <div className="mt-6 pt-6 border-t border-dashed border-gray-200">
                  <div className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                    <div className="flex items-center gap-2 mb-3 text-xs font-bold text-gray-400 uppercase tracking-wider">
                      <span className="w-2 h-2 rounded-full bg-red-400 animate-pulse" />
                        Панель администратора
                    </div>
    
                    <Button
                      onClick={() => setIsAdminCancelOpen(true)} // ОТКРЫВАЕМ ОКНО АВТОРИЗАЦИИ
                      variant="outline"
                      className="w-full border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700 gap-2 cursor-pointer text-sm py-4 h-auto font-semibold"
                    >
                    Отменить (мягкое удаление)
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          </motion.div>
          <AdminCancelModal 
            isOpen={isAdminCancelOpen}
            onClose={() => setIsAdminCancelOpen(false)}
            onConfirm={executeSoftDelete} // Передаем функцию удаления
          />
        </div>
      </div>
    </div>
  );
}