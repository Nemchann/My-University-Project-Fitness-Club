import { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import { User, Mail, Phone, Calendar, Clock, Award, ChevronLeft, ChevronRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { api } from '../../lib/api';
import { format, parseISO } from 'date-fns';
import { ru } from 'date-fns/locale';

// 1. Интерфейс, соответствующий твоему BookingShortResponseDto.java
interface BookingShortResponseDto {
  scheduleName: string;
  scheduleDate: string; // LocalDate 'YYYY-MM-DD'
  status: string;
  startTime: string;    // LocalDateTime
  trainerFullName: string;
}

export function ProfilePage() {
  const [bookings, setBookings] = useState<BookingShortResponseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Состояние для пагинации Spring Data Page
  const [currentPage, setCurrentPage] = useState(0); // В Spring страницы начинаются с 0
  const [totalPages, setTotalPages] = useState(1);

  // Данные пользователя (пока оставляем статичными, кроме UUID)
  const userMetadata = {
    fullName: 'Анна Коваленко',
    email: 'anna.kovalenko@example.com',
    phone: '+7 (999) 123-45-67',
    memberSince: 'Май 2026',
  };

  // 2. Получение бронирований с учетом текущей страницы
  useEffect(() => {
    const fetchUserBookings = async () => {
      const clientId = localStorage.getItem("userId");
      if (!clientId) return;

      try {
        setIsLoading(true);
        // Передаем страницу через параметры: page и size (как настроено в @PageableDefault на бэке)
        const response = await api.get(
          `/fitness-club/bookings/get_clients_bookings/${clientId}?page=${currentPage}&size=10`
        );
        
        // Важно: берем response.data.content, так как бэк возвращает Page!
        setBookings(response.data.content || []);
        setTotalPages(response.data.totalPages || 1);
      } catch (error) {
        console.error("Ошибка при получении бронирований клиента:", error);
        setBookings([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserBookings();
  }, [currentPage]); // Перезапускаем при клике на другую страницу пагинации

  const formatBookingTime = (dateTimeIso: string) => {
    try {
      const date = parseISO(dateTimeIso);
      return format(date, 'HH:mm');
    } catch {
      return '--:--';
    }
  };

  const getStatusStyle = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
      case 'Активна':
        return 'bg-green-100 text-green-700';
      case 'CANCELED':
      case 'Отменена':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-blue-100 text-blue-700';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pt-28 pb-12">
      <div className="container mx-auto px-4 max-w-6xl">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-8"
        >
          {/* Шапка профиля */}
          <div className="flex flex-col md:flex-row gap-6 items-start md:items-center justify-between bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 bg-pink-100 text-pink-600 rounded-full flex items-center justify-center font-bold text-2xl">
                {userMetadata.fullName.charAt(0)}
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">{userMetadata.fullName}</h1>
                <p className="text-gray-500">Клубная карта активна • {userMetadata.memberSince}</p>
              </div>
            </div>
            <div className="flex gap-4 bg-pink-50/50 p-4 rounded-xl border border-pink-50">
              <div className="text-center px-4">
                <div className="text-2xl font-bold text-pink-600">{bookings.length}</div>
                <div className="text-xs text-gray-500 uppercase tracking-wider">Записей на странице</div>
              </div>
            </div>
          </div>

          <div className="grid md:grid-cols-[350px_1fr] gap-8 items-start">
            {/* Левая колонка: Личные данные */}
            <Card className="shadow-sm border-gray-100 bg-white">
              <CardHeader>
                <CardTitle className="text-lg font-bold">Личные данные</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">ФИО</Label>
                  <div className="relative">
                    <User className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                    <Input id="name" value={userMetadata.fullName} readOnly className="pl-9 bg-gray-50 cursor-not-allowed" />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                    <Input id="email" value={userMetadata.email} readOnly className="pl-9 bg-gray-50 cursor-not-allowed" />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Телефон</Label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                    <Input id="phone" value={userMetadata.phone} readOnly className="pl-9 bg-gray-50 cursor-not-allowed" />
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Правая колонка: Список бронирований из Page.content */}
            <div className="space-y-4">
              <Card className="shadow-sm border-gray-100 bg-white">
                <CardHeader>
                  <CardTitle className="text-lg font-bold">Мои тренировки</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {isLoading ? (
                    <div className="text-center py-12 text-gray-500">Загрузка ваших записей...</div>
                  ) : bookings.length > 0 ? (
                    <div className="space-y-4">
                      {bookings.map((booking, index) => (
                        <div
                          key={index}
                          className="flex flex-col sm:flex-row justify-between sm:items-center p-4 rounded-xl border border-gray-100 hover:border-pink-100 transition-all gap-4"
                        >
                          <div className="space-y-1">
                            <div className="flex items-center gap-2">
                              <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${getStatusStyle(booking.status)}`}>
                                {booking.status === 'CONFIRMED' ? 'Подтверждена' : booking.status}
                              </span>
                            </div>
                            <h4 className="font-bold text-gray-900 text-lg">{booking.scheduleName}</h4>
                            
                            <div className="flex flex-wrap gap-4 text-sm text-gray-500 pt-1">
                              <div className="flex items-center gap-1.5">
                                <Calendar className="w-4 h-4 text-pink-500" />
                                <span>{format(parseISO(booking.scheduleDate), 'd MMMM (EEEE)', { locale: ru })}</span>
                              </div>
                              <div className="flex items-center gap-1.5">
                                <Clock className="w-4 h-4 text-pink-500" />
                                <span>{formatBookingTime(booking.startTime)}</span>
                              </div>
                            </div>
                          </div>

                          <div className="flex sm:flex-col items-start sm:items-end justify-between gap-2 border-t sm:border-t-0 pt-2 sm:pt-0">
                            <span className="text-sm text-gray-600 font-medium">Инструктор:</span>
                            <span className="text-sm font-semibold text-gray-900">{booking.trainerFullName || 'Не указан'}</span>
                          </div>
                        </div>
                      ))}

                      {/* Твоя Пагинация под бэкенд Pageable */}
                      {totalPages > 1 && (
                        <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-100">
                          <Button
                            variant="outline"
                            size="icon"
                            onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                            disabled={currentPage === 0}
                            className="w-8 h-8 cursor-pointer"
                          >
                            <ChevronLeft className="w-4 h-4" />
                          </Button>
                          <span className="text-sm text-gray-600">
                            Страница {currentPage + 1} из {totalPages}
                          </span>
                          <Button
                            variant="outline"
                            size="icon"
                            onClick={() => setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))}
                            disabled={currentPage === totalPages - 1}
                            className="w-8 h-8 cursor-pointer"
                          >
                            <ChevronRight className="w-4 h-4" />
                          </Button>
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="text-center py-12">
                      <Calendar className="w-16 h-16 text-pink-300 mx-auto mb-4" />
                      <h3 className="text-xl font-semibold text-gray-700 mb-2">Нет записей</h3>
                      <p className="text-gray-500">Вы еще не записались ни на одну тренировку.</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}