import { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import { useLocation } from 'react-router';
import { Calendar, Clock, ChevronLeft, ChevronRight, XCircle, CreditCard, Hourglass, Award } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { api } from '../../lib/api';
import { format, parseISO } from 'date-fns';
import { ru } from 'date-fns/locale';
import { useSelector } from 'react-redux';
import type { RootState } from '../../store';


// Интерфейс, соответствующий BookingShortResponseDto.java
interface BookingShortResponseDto {
  bookingId: string;    // UUID бронирования с бэкенда
  scheduleName: string;
  scheduleDate: string; // LocalDate 'YYYY-MM-DD'
  status: string;
  startTime: string;    // LocalDateTime
  trainerFullName: string;
}

interface ClientSubscriptionResponseDto {
  subscriptionName?: string; // имя абонемента (если добавила на бэке, либо выведем дефолтное)
  startDate: string;         // LocalDate приходит строкой 'YYYY-MM-DD'
  endDate: string;           // LocalDate
  remainingVisits: number | null; // может быть null для абонементов с неограниченными посещениями
  subscriptionStatus: 'ACTIVE' | 'PENDING' | 'LAPSED' | string;
  unlimited: boolean;
}


interface ClientActiveAndFutureSubscriptionsDto {
  activeSubscription: ClientSubscriptionResponseDto | null;
  pendingSubscriptions: ClientSubscriptionResponseDto[];
}

export function ProfilePage() {
  const location = useLocation();

  // Проверяем, передал ли нам RegistrationPage готовые данные пользователя
  const inheritedUser = location.state?.user;

  // Вытаскиваем ID пользователя из глобального стейта Redux Toolkit
  const reduxUserId = useSelector((state: RootState) => state.auth.userId);
  
  // Для проверки в консоли, что Redux работает
  console.log("ID пользователя из Redux Toolkit:", reduxUserId);

  // --- Стейты для тренировок ---
  const [bookings, setBookings] = useState<BookingShortResponseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0); // В Spring страницы начинаются с 0
  const [totalPages, setTotalPages] = useState(1);
  const [activeTab, setActiveTab] = useState<'future' | 'past'>('future');

  // --- СТЕЙТЫ ДЛЯ АБОНЕМЕНТОВ ---
  const [activeAndFutureSubs, setActiveAndFutureSubs] = useState<ClientActiveAndFutureSubscriptionsDto | null>(null);
  const [lapsedSubs, setLapsedSubs] = useState<ClientSubscriptionResponseDto[]>([]);
  const [isLoadingSubs, setIsLoadingSubs] = useState(true);
  const [subsTab, setSubsTab] = useState<'current' | 'history'>('current');
  const [subsPage, setSubsPage] = useState(0); // страница для архивных абонементов
  const [subsTotalPages, setSubsTotalPages] = useState(1);


  // --- Данные пользователя и формы ---
  const [user, setUser] = useState<{ fullName: string; email: string; phone: string } | null>(
    inheritedUser ? {
      fullName: inheritedUser.fullName,
      email: inheritedUser.email,
      phone: '+7 (999) 000-00-00' // временная заглушка для телефона, если его нет в UserResponseDto
    } : null
  );

  // Стейты для формы редактирования профиля (UserEditingDto)
  const [profileForm, setProfileForm] = useState({
    surname: '',
    selfname: '',
    patronymic: '',
    phone: '',
    email: '',
    birthday: '' 
  });

  // Стейты для формы смены пароля (PasswordChangeDto)
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmNewPassword: ''
  });

  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // --- ФУНКЦИЯ ЗАГРУЗКИ АБОНЕМЕНТОВ ---
  const fetchClientSubscriptions = async () => {
    const clientId = localStorage.getItem("userId");
    if (!clientId) return;

    try {
      setIsLoadingSubs(true);
      if (subsTab === 'current') {
        // Первый эндпоинт: возвращает объект с активным и очередью будущих
        const response = await api.get<ClientActiveAndFutureSubscriptionsDto>(
          `/fitness-club/bookings/subscriptions/upcoming/${clientId}`
        );
        setActiveAndFutureSubs(response.data);
      } else {
        // Второй эндпоинт: возвращает Page просроченных (LAPSED)
        const response = await api.get(`/fitness-club/bookings/subscriptions/past/${clientId}`, {
          params: { page: subsPage, size: 3 } // берем по 3 штуки в историю для компактности
        });
        setLapsedSubs(response.data.content || []);
        setSubsTotalPages(response.data.totalPages || 1);
      }
    } catch (error) {
      console.error("Ошибка при загрузке абонементов клиента:", error);
    } {
      setIsLoadingSubs(false);
    }
  };

  // Вызываем загрузку абонементов при смене вкладки или страницы архива
  useEffect(() => {
    fetchClientSubscriptions();
  }, [subsTab, subsPage]);

  // Сброс страницы архива при переключении вкладок абонементов
  useEffect(() => {
    setSubsPage(0);
  }, [subsTab]);

  useEffect(() => {
    if (user) {
    }
  }, [user]);

  // ФУНКЦИЯ ОБНОВЛЕНИЯ ПРОФИЛЯ 
  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();

    const userId = localStorage.getItem("userId");
    if (!userId) return;

    try {
      setIsEditingProfile(true);

      // Собираем объект строго по UserEditingDto.java
      const editingPayload = {
        id: userId,
        surname: profileForm.surname,
        selfname: profileForm.selfname,
        patronymic: profileForm.patronymic || null,
        phone: profileForm.phone,
        email: profileForm.email,
        birthday: profileForm.birthday // Возвращаем сохраненную дату рождения без изменений
      };

      const response = await api.put('/fitness-club/users/edit_profile', editingPayload);
    
      // Обновляем отображение в профиле
      setUser({
        fullName: `${profileForm.surname} ${profileForm.selfname}`,
        email: profileForm.email,
        phone: profileForm.phone
      });

      alert("Личные данные успешно обновлены!");
    } catch (error: any) {
      console.error("Ошибка при обновлении профиля:", error);
      alert(error.response?.data?.message || "Не удалось обновить данные.");
    } finally {
      setIsEditingProfile(false);
    }
  };

  // --- ФУНКЦИЯ СМЕНЫ ПАРОЛЯ ---
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    const userId = localStorage.getItem("userId");
    if (!userId) return;

    if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
      alert("Новые пароли не совпадают!");
      return;
    }

    try {
      setIsChangingPassword(true);

      // Собираем PasswordChangeDto
      const passwordPayload = {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      };

      // Передаем id в URL
      await api.put(`/fitness-club/users/change_password/${userId}`, passwordPayload);

      alert("Пароль успешно изменен!");
      setPasswordForm({ oldPassword: '', newPassword: '', confirmNewPassword: '' });
    } catch (error: any) {
      console.error("Ошибка при смене пароля:", error);
      alert(error.response?.data?.message || "Не удалось изменить пароль. Проверьте старый пароль.");
    } finally {
      setIsChangingPassword(false);
    }
  };

  // Функция загрузки профиля и бронирований
  const fetchProfileAndBookings = async () => {
    const clientId = localStorage.getItem("userId");
    if (!clientId) return;

    try {
      setIsLoading(true);

      if (!user) {
        try {
          const userResponse = await api.get(`/fitness-club/users/${clientId}`);
          const userData = userResponse.data;
          setUser({
            fullName: `${userData.surname} ${userData.selfname}`,
            email: userData.email || 'Не указан',
            phone: userData.phone || '+7 (999) 000-00-00'
          });
          // Инициализируем форму актуальными данными с бэкенда
          setProfileForm({
            surname: userData.surname || '',
            selfname: userData.selfname || '',
            patronymic: userData.patronymic || '',
            phone: userData.phone || '',
            email: userData.email || '',
            birthday: userData.birthday || '2000-01-01' 
          });
        } catch (err) {
          console.error("Не удалось загрузить личные данные:", err);
          setUser({
            fullName: 'Спортивный Клиент',
            email: 'user@example.com',
            phone: '+7 (999) 000-00-00'
          });
        }
      }

      const endpoint = activeTab === 'future' 
      ? `/fitness-club/bookings/upcoming/${clientId}` 
      : `/fitness-club/bookings/past/${clientId}`;
      const response = await api.get(endpoint, { params: { page: currentPage, size: 5 } });

      setBookings(response.data.content || []);
      console.log("Данные, которые пришли с бэка:", response.data); 
      setTotalPages(response.data.totalPages || 1);
    } catch (error) {
      console.error("Ошибка при получении бронирований:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileAndBookings();
  }, [activeTab, currentPage]);

  useEffect(() => {
    setCurrentPage(0);
  }, [activeTab]);

  // Функция отмены бронирования по BookingCancelDto
  const handleCancelBooking = async (bookingId: string) => {
    const userId = localStorage.getItem("userId");
    if (!userId || !bookingId) return;

    if (!window.confirm("Вы уверены, что хотите отменить запись на это занятие?")) {
      return;
    }

    try {
      // Формируем тело запроса 
      const cancelPayload = {
        bookingId: bookingId,
        userId: userId
      };

      await api.delete('/fitness-club/bookings/cancel', { data: cancelPayload });

      alert("Запись успешно отменена");
      
      // Перезагружаем текущую страницу с бэкенда, чтобы увидеть обновленный статус CANCELLED
      fetchProfileAndBookings();
    } catch (error: any) {
      console.error("Ошибка при отмене бронирования:", error);
      alert(error.response?.data?.message || "Не удалось отменить запись. Возможно, до тренировки осталось меньше 2 часов.");
    }
  };

  const currentUserData = user || {
    fullName: 'Загрузка...',
    email: '...',
    phone: '...',
  };

  const formatBookingTime = (dateTimeIso: string) => {
    try {
      const date = parseISO(dateTimeIso);
      return format(date, 'HH:mm');
    } catch {
      return '--:--';
    }
  };

  // Обновленный маппинг стилей под статусы
  const getStatusStyle = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return 'bg-green-100 text-green-700 border-green-200';
      case 'PROCESSING':
        return 'bg-yellow-100 text-yellow-700 border-yellow-200';
      case 'CANCELLED':
        return 'bg-red-100 text-red-700 border-red-200';
      case 'COMPLETED':
        return 'bg-grey-100 text-grey-700 border-grey-200';
      default:
        return 'bg-blue-100 text-blue-700 border-blue-200';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'ACCEPTED': return 'Подтверждена';
      case 'PROCESSING': return 'В обработке';
      case 'CANCELLED': return 'Отменена';
      case 'COMPLETED': return 'Посещено';
      default: return status;
    }
  };

  // Вспомогательная функция для форматирования дат абонементов
  const formatDateString = (dateStr: string) => {
    try {
      return format(parseISO(dateStr), 'd MMMM yyyy', { locale: ru });
    } catch {
      return dateStr;
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
                {currentUserData.fullName.charAt(0)}
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">{currentUserData.fullName}</h1>
                <p className="text-gray-500">Клубная карта активна • Май 2026</p>
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
            <div className="space-y-6">
              <Card className="shadow-sm border-gray-100 bg-white">
                <CardHeader>
                  <CardTitle className="text-lg font-bold">Личные данные</CardTitle>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleUpdateProfile} className="space-y-4">
                    <div className="space-y-1.5">
                      <Label htmlFor="surname">Фамилия</Label>
                      <Input 
                        id="surname" 
                        value={profileForm.surname} 
                        onChange={(e) => setProfileForm({...profileForm, surname: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="selfname">Имя</Label>
                      <Input 
                        id="selfname" 
                        value={profileForm.selfname} 
                        onChange={(e) => setProfileForm({...profileForm, selfname: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="patronymic">Отчество</Label>
                      <Input 
                        id="patronymic" 
                        value={profileForm.patronymic} 
                        onChange={(e) => setProfileForm({...profileForm, patronymic: e.target.value})}
                        placeholder="Если есть"
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="phone">Телефон</Label>
                      <Input 
                        id="phone" 
                        value={profileForm.phone} 
                        onChange={(e) => setProfileForm({...profileForm, phone: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="email">Email</Label>
                      <Input 
                        id="email" 
                        type="email"
                        value={profileForm.email} 
                        onChange={(e) => setProfileForm({...profileForm, email: e.target.value})}
                        required
                      />
                    </div>
        
                    <Button 
                      type="submit" 
                      disabled={isEditingProfile}
                      className="w-full bg-pink-500 hover:bg-pink-600 text-white mt-2 cursor-pointer"
                    >
                      {isEditingProfile ? 'Сохранение...' : 'Обновить профиль'}
                    </Button>
                  </form>
                </CardContent>
              </Card>
            
          
            {/* Форма 2: Смена пароля */}
              <Card className="shadow-sm border-gray-100 bg-white">
                <CardHeader>
                  <CardTitle className="text-lg font-bold">Безопасность</CardTitle>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleChangePassword} className="space-y-4">
                    <div className="space-y-1.5">
                      <Label htmlFor="oldPassword">Текущий пароль</Label>
                      <Input 
                        id="oldPassword" 
                        type="password"
                        value={passwordForm.oldPassword}
                        onChange={(e) => setPasswordForm({...passwordForm, oldPassword: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="newPassword">Новый пароль</Label>
                      <Input 
                        id="newPassword" 
                        type="password"
                        value={passwordForm.newPassword}
                        onChange={(e) => setPasswordForm({...passwordForm, newPassword: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="confirmNewPassword">Подтвердите пароль</Label>
                      <Input 
                        id="confirmNewPassword" 
                        type="password"
                        value={passwordForm.confirmNewPassword}
                        onChange={(e) => setPasswordForm({...passwordForm, confirmNewPassword: e.target.value})}
                        required
                      />
                    </div>

                  <Button 
                    type="submit" 
                    disabled={isChangingPassword}
                    variant="outline"
                    className="w-full border-pink-200 text-pink-600 hover:bg-pink-50 mt-2 cursor-pointer"
                  >
                    {isChangingPassword ? 'Изменение...' : 'Сменить пароль'}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div> 
          {/* Конец левой колонки */}


            {/* Правая колонка: Список бронирований из Page.content */}
            <div className="space-y-8">

              {/* БЛОК 1: МОИ ТРЕНИРОВКИ */}
              <Card className="shadow-sm border-gray-100 bg-white">
                <CardHeader className="pb-2">
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <CardTitle className="text-lg font-bold">Мои тренировки</CardTitle>
        
                    {/* Переключатель вкладок (Tabs) */}
                    <div className="flex bg-gray-100 p-1 rounded-xl w-full sm:w-auto">
                      <button
                        onClick={() => setActiveTab('future')}
                        className={`flex-1 sm:flex-none px-4 py-2 text-sm font-medium rounded-lg transition-all cursor-pointer ${
                          activeTab === 'future'
                            ? 'bg-white text-pink-600 shadow-sm'
                            : 'text-gray-500 hover:text-gray-700'
                          }`}
                      >
                        Предстоящие
                      </button>
                      <button
                        onClick={() => setActiveTab('past')}
                        className={`flex-1 sm:flex-none px-4 py-2 text-sm font-medium rounded-lg transition-all cursor-pointer ${
                          activeTab === 'past'
                            ? 'bg-white text-pink-600 shadow-sm'
                          : 'text-gray-500 hover:text-gray-700'
                        }`}
                      >
                        История занятий
                      </button>
                    </div>
                  </div>  
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
                              <span className={`text-xs px-2 py-0.5 rounded-full font-medium border ${getStatusStyle(booking.status)}`}>
                                {getStatusLabel(booking.status)}
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

                          {/* Инструктор и Кнопка отмены */}
                          <div className="flex sm:flex-col items-start sm:items-end justify-between gap-3 border-t sm:border-t-0 pt-2 sm:pt-0">
                            <div className="sm:text-right">
                              <span className="text-xs text-gray-400 block">Инструктор:</span>
                              <span className="text-sm font-semibold text-gray-900">{booking.trainerFullName || 'Не указан'}</span>
                            </div>

                            {/* Кнопка отмены активна только для статуса ACCEPTED */}
                            {booking.status === 'ACCEPTED' && (
                              <Button
                                onClick={() => handleCancelBooking(booking.bookingId)}
                                variant="outline"
                                size="sm"
                                className="border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors gap-1.5 cursor-pointer text-xs h-8"
                              >
                                <XCircle className="w-3.5 h-3.5" />
                                Отменить запись
                              </Button>
                            )}
                          </div>
                        </div>
                      ))}

                      {/* Пагинация */}
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

              {/* НОВЫЙ БЛОК 2: МОИ АБОНЕМЕНТЫ */}
              <Card className="shadow-sm border-gray-100 bg-white">
                <CardHeader className="pb-2">
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <CardTitle className="text-lg font-bold">Мои абонементы</CardTitle>
                    
                    {/* Переключатель вкладок абонементов */}
                    <div className="flex bg-gray-100 p-1 rounded-xl w-full sm:w-auto">
                      <button 
                        onClick={() => setSubsTab('current')} 
                        className={`flex-1 sm:flex-none px-4 py-2 text-sm font-medium rounded-lg transition-all cursor-pointer ${
                          subsTab === 'current' ? 'bg-white text-pink-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'
                        }`}
                      >
                        Текущие
                      </button>
                      <button 
                        onClick={() => setSubsTab('history')} 
                        className={`flex-1 sm:flex-none px-4 py-2 text-sm font-medium rounded-lg transition-all cursor-pointer ${
                          subsTab === 'history' ? 'bg-white text-pink-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'
                        }`}
                      >
                        История
                      </button>
                    </div>
                  </div>
                </CardHeader>
                
                <CardContent className="space-y-4">
                  {isLoadingSubs ? (
                    <div className="text-center py-12 text-gray-500">Загрузка данных об абонементах...</div>
                  ) : subsTab === 'current' ? (
                    // === ВКЛАДКА: ТЕКУЩИЕ И БУДУЩИЕ АБОНЕМЕНТЫ ===
                    <div className="space-y-6">
                      
                      {/* А. Активный абонемент */}
                      <div>
                        <h4 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">Активный абонемент</h4>
                        {activeAndFutureSubs?.activeSubscription ? (
                          <div className="p-5 rounded-2xl bg-gradient-to-r from-pink-500 to-purple-600 text-white shadow-md relative overflow-hidden">
                            <div className="absolute right-4 bottom-2 opacity-10">
                              <CreditCard className="w-36 h-36" />
                            </div>
                            <div className="flex justify-between items-start mb-4">
                              <div>
                                <h5 className="text-xl font-bold">
                                  {activeAndFutureSubs.activeSubscription.subscriptionName || 'Клубный абонемент'}
                                </h5>
                                <p className="text-pink-100 text-xs mt-1">
                                  Действует до: {formatDateString(activeAndFutureSubs.activeSubscription.endDate)}
                                </p>
                              </div>
                              <span className="bg-white/20 text-white text-xs px-2.5 py-1 rounded-full font-semibold uppercase tracking-wider border border-white/20">
                                Активен
                              </span>
                            </div>
                            <div className="grid grid-cols-2 gap-4 pt-2 border-t border-white/20">
                              <div>
                                <span className="text-xs text-pink-200 block">Осталось визитов</span>
                                <span className="text-2xl font-black uppercase tracking-wide">
                                  {activeAndFutureSubs.activeSubscription.unlimited 
                                    ? 'Безлимит' 
                                    : activeAndFutureSubs.activeSubscription.remainingVisits}
                                </span>
                              </div>
                              <div className="text-right">
                                <span className="text-xs text-pink-200 block">Дата старта</span>
                                <span className="text-lg font-bold">{formatDateString(activeAndFutureSubs.activeSubscription.startDate)}</span>
                              </div>
                            </div>
                          </div>
                        ) : (
                          <div className="p-4 rounded-xl border border-dashed border-gray-200 text-center text-gray-500 text-sm">
                            У вас сейчас нет активных абонементов.
                          </div>
                        )}
                      </div>

                      {/* Б. Будущие абонементы в очереди */}
                      <div>
                        <h4 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">Очередь активации (Будущие)</h4>
                        {activeAndFutureSubs?.pendingSubscriptions && activeAndFutureSubs.pendingSubscriptions.length > 0 ? (
                          <div className="space-y-3">
                            {activeAndFutureSubs.pendingSubscriptions.map((sub, idx) => (
                              <div key={idx} className="flex items-center justify-between p-4 rounded-xl border border-gray-100 bg-gray-50/50 hover:border-purple-100 transition-all">
                                <div className="flex items-center gap-3">
                                  <div className="p-2 bg-purple-50 rounded-lg text-purple-500">
                                    <Hourglass className="w-5 h-5" />
                                  </div>
                                  <div>
                                    <h5 className="font-bold text-gray-900">{sub.subscriptionName || 'Следующий абонемент'}</h5>
                                    <p className="text-xs text-gray-500">
                                      Плановый период: с {formatDateString(sub.startDate)} по {formatDateString(sub.endDate)}
                                    </p>
                                  </div>
                                </div>
                                <div className="text-right">
                                  <span className="text-xs font-bold text-purple-600 bg-purple-50 border border-purple-100 px-2 py-0.5 rounded-full">
                                    В очереди
                                  </span>
                                  <span className="text-xs text-gray-400 block mt-1">
                                    {sub.unlimited ? 'Безлимитное посещение' : `${sub.remainingVisits} визитов`}
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <div className="text-xs text-gray-400 italic">Очередь пуста. Купленные заранее абонементы появятся здесь.</div>
                        )}
                      </div>

                    </div>
                  ) : (
                    // === ВКЛАДКА: ИСТОРsecondary (ПРОСРОЧЕННЫЕ) ===
                    <div className="space-y-4">
                      {lapsedSubs.length > 0 ? (
                        <div className="space-y-3">
                          {lapsedSubs.map((sub, idx) => (
                            <div key={idx} className="flex flex-col sm:flex-row justify-between sm:items-center p-4 rounded-xl border border-gray-100 bg-white opacity-75">
                              <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                  <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-gray-100 text-gray-500 border border-gray-200">
                                    Истёк
                                  </span>
                                </div>
                                <h5 className="font-bold text-gray-700 text-base">{sub.subscriptionName || 'Архивный абонемент'}</h5>
                                <p className="text-xs text-gray-400">
                                  Действовал: {formatDateString(sub.startDate)} — {formatDateString(sub.endDate)}
                                </p>
                              </div>
                              <div className="text-left sm:text-right pt-2 sm:pt-0 border-t sm:border-t-0 border-gray-50">
                                <span className="text-xs text-gray-400 block">
                                  {sub.unlimited ? 'Тип карты:' : 'Осталось визитов:'}
                                </span>
                                <span className="text-sm font-bold text-gray-600">
                                  {sub.unlimited ? 'Безлимит' : sub.remainingVisits}
                                </span>
                              </div>
                            </div>
                          ))}

                          {/* Пагинация для архива абонементов */}
                          {subsTotalPages > 1 && (
                            <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-100">
                              <Button variant="outline" size="icon" onClick={() => setSubsPage((prev) => Math.max(0, prev - 1))} disabled={subsPage === 0} className="w-8 h-8 cursor-pointer"><ChevronLeft className="w-4 h-4" /></Button>
                              <span className="text-sm text-gray-600">Страница {subsPage + 1} из {subsTotalPages}</span>
                              <Button variant="outline" size="icon" onClick={() => setSubsPage((prev) => Math.min(subsTotalPages - 1, prev + 1))} disabled={subsPage === subsTotalPages - 1} className="w-8 h-8 cursor-pointer"><ChevronRight className="w-4 h-4" /></Button>
                            </div>
                          )}
                        </div>
                      ) : (
                        <div className="text-center py-8 text-gray-500">
                          <Award className="w-12 h-12 text-gray-300 mx-auto mb-2" />
                          <p className="text-sm">У вас еще нет архивных или просроченных абонементов.</p>
                        </div>
                      )}
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