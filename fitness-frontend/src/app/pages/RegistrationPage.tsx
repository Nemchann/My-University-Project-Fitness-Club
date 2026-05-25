import { useState } from 'react';
import { motion } from 'motion/react';
import { UserPlus, Mail, Phone, Lock, User, CalendarDays } from 'lucide-react';
import { Card, CardContent } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { api } from '../../lib/api';
import { useNavigate } from 'react-router';

export function RegistrationPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: '',       // Вводится как "Иванов Иван Иванович" или "Петров Петр"
    phone: '',
    email: '',
    birthday: '',       // Сюда запишется "YYYY-MM-DD" из календарика
    login: '',
    password: '',
    confirmPassword: ''
  });

  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Обработчик отправки формы
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    // 1. Простейшая валидация на фронтенде
    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Пароли не совпадают!');
      return;
    }

    if (!formData.fullName.trim()) {
      setErrorMessage('Пожалуйста, введите ФИО');
      return;
    }

    setIsLoading(false);

    try {
      setIsLoading(true);

      // 2. Логика разделения ФИО на Фамилию, Имя и Отчество
      // Убираем лишние пробелы по краям и делим строку по пробелам
      const nameParts = formData.fullName.trim().split(/\s+/); 
      
      let surname = '';
      let selfname = '';
      let patronymic = '';

      if (nameParts.length === 1) {
        // Если ввели только одно слово, запишем его в Имя
        selfname = nameParts[0];
      } else if (nameParts.length === 2) {
        // Если ввели два слова (Фамилия Имя)
        surname = nameParts[0];
        selfname = nameParts[1];
      } else {
        // Если ввели три слова и более (Фамилия Имя Отчество)
        surname = nameParts[0];
        selfname = nameParts[1];
        // Все остальные слова склеиваем в отчество (на случай двойных отчеств)
        patronymic = nameParts.slice(2).join(' ');
      }

      // 3. Формируем тело запроса в строгом соответствии с UserRegistrationDto.java
      const registrationPayload = {
        login: formData.login,
        password: formData.password,
        surname: surname || "Не указана", // Проверка на @NotBlank бэкенда
        selfname: selfname,
        patronymic: patronymic || null,  // Отчество может быть null
        phone: formData.phone,
        email: formData.email,
        birthday: formData.birthday,      // Строка "YYYY-MM-DD" автоматически распарсится в LocalDate
        createdAt: new Date().toISOString() // OffsetDateTime для бэкенда
      };

      // 4. Отправляем POST-запрос на регистрацию через прокси
      const response = await api.post('/fitness-club/users/register', registrationPayload); 
      // Замени путь '/fitness-club/users/register' на твой реальный @PostMapping эндпоинт контроллера

      // Твой бэкенд возвращает UserResponseDto, у которого есть поле id (UUID)
      if (response.data && response.data.id) {
         // Сохраняем реальный UUID пользователя в браузере
        localStorage.setItem("userId", response.data.id);
      }

      // Перенаправляем на главную (к расписанию) или сразу в профиль
     navigate('/profile', { 
      state: { 
        user: {
          fullName: `${response.data.surname} ${response.data.selfname}`,
          email: response.data.email
        }
      } 
    });

    } catch (error: any) {
      console.error("Ошибка при регистрации:", error);
      setErrorMessage(
        error.response?.data?.message || 'Не удалось зарегистрироваться. Проверьте введенные данные.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  return (
    <div className="min-h-screen pt-24 pb-20 bg-gradient-to-br from-pink-50 via-white to-pink-50">
      <div className="container mx-auto px-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="max-w-2xl mx-auto"
        >
          <div className="text-center mb-10">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2, type: "spring", stiffness: 200 }}
              className="inline-block bg-gradient-to-br from-pink-500 to-pink-600 p-4 rounded-2xl mb-6 shadow-lg shadow-pink-200"
            >
              <UserPlus className="w-12 h-12 text-white" />
            </motion.div>
            <h1 className="text-5xl font-bold text-gray-900 mb-4">
              Присоединяйтесь к FitLady
            </h1>
            <p className="text-xl text-gray-600">
              Начните свой путь к здоровью и красоте уже сегодня
            </p>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, duration: 0.6 }}
          >
            <Card className="border-2 border-pink-100 shadow-xl shadow-pink-100/50">
              <CardContent className="p-8">
                <form onSubmit={handleSubmit} className="space-y-6">
                  {/* Поле ФИО */}
                  <div className="space-y-2">
                    <Label htmlFor="fullName" className="text-gray-700 font-medium">
                      ФИО (через пробел)
                    </Label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-pink-500" />
                      <Input
                        id="fullName"
                        name="fullName"
                        type="text"
                        placeholder="Введите ваше ФИО"
                        value={formData.fullName}
                        onChange={handleChange}
                        className="pl-11 border-pink-200 focus:border-pink-500"
                        required
                      />
                    </div>
                  </div>

                  {/* Поле День Рождения (Добавленное) */}
                  <div className="space-y-2">
                    <Label htmlFor="birthday">Дата рождения</Label>
                    <div className="relative">
                      <CalendarDays className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                      <Input
                        id="birthday"
                        name="birthday"
                        type="date"
                        value={formData.birthday}
                        onChange={handleChange}
                        className="pl-9 border-pink-100 focus:border-pink-500 text-gray-700"
                        required
                      />
                    </div>
                  </div>

                  {/* Поле Номер телефона */}
                  <div className="space-y-2">
                    <Label htmlFor="phone" className="text-gray-700 font-medium">
                      Номер телефона
                    </Label>
                    <div className="relative">
                      <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-pink-500" />
                      <Input
                        id="phone"
                        name="phone"
                        type="tel"
                        placeholder="+7 (___) ___-__-__"
                        value={formData.phone}
                        onChange={handleChange}
                        className="pl-11 border-pink-200 focus:border-pink-500"
                        required
                      />
                    </div>
                  </div>

                  {/* Поле Email */}
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-gray-700 font-medium">
                      Электронная почта
                    </Label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-pink-500" />
                      <Input
                        id="email"
                        name="email"
                        type="email"
                        placeholder="your@email.com"
                        value={formData.email}
                        onChange={handleChange}
                        className="pl-11 border-pink-200 focus:border-pink-500"
                        required
                      />
                    </div>
                  </div>

                  {/* Поле Логин */}
                  <div className="space-y-2">
                    <Label htmlFor="login" className="text-gray-700 font-medium">
                      Логин
                    </Label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-pink-500" />
                      <Input
                        id="login"
                        name="login"
                        type="text"
                        placeholder="Придумайте логин"
                        value={formData.login}
                        onChange={handleChange}
                        className="pl-11 border-pink-200 focus:border-pink-500"
                        required
                      />
                    </div>
                  </div>

                  {/* Поле Пароль */}
                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-gray-700 font-medium">
                      Пароль
                    </Label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-pink-500" />
                      <Input
                        id="password"
                        name="password"
                        type="password"
                        placeholder="Минимум 8 символов"
                        value={formData.password}
                        onChange={handleChange}
                        className="pl-11 border-pink-200 focus:border-pink-500"
                        required
                        minLength={8}
                      />
                    </div>
                  </div>

                  {/* Подтверждение Пароля */}
                  <div className="space-y-2">
                    <Label htmlFor="confirmPassword" className="text-gray-700 font-medium">
                      Подтвердите пароль
                    </Label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-pink-500" />
                      <Input
                        id="confirmPassword"
                        name="confirmPassword"
                        type="password"
                        placeholder="Повторите пароль"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        className="pl-11 border-pink-200 focus:border-pink-500"
                        required
                        minLength={8}
                      />
                    </div>
                  </div>

                  {/* Ошибки валидации */}
                  {errorMessage && (
                    <p className="text-sm text-red-500 text-center font-medium bg-red-50 py-2 rounded-lg border border-red-100">
                      {errorMessage}
                    </p>
                  )}
                  
                  {/* Кнопка отправки */}
                  <motion.div
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    <Button
                      type="submit"
                      className="w-full bg-gradient-to-r from-pink-500 to-pink-600 hover:from-pink-600 hover:to-pink-700 text-white py-6 text-lg font-semibold shadow-lg shadow-pink-300/50"
                    >
                      Зарегистрироваться
                    </Button>
                  </motion.div>

                  <p className="text-center text-gray-600 text-sm">
                    Уже есть аккаунт?{' '}
                    <a href="/" className="text-pink-600 hover:text-pink-700 font-semibold">
                      Войти
                    </a>
                  </p>
                </form>
              </CardContent>
            </Card>

            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.6 }}
              className="mt-8 p-6 bg-gradient-to-r from-pink-100 to-pink-50 rounded-2xl border border-pink-200"
            >
              <p className="text-center text-gray-700">
                <span className="font-semibold text-pink-600">Первое занятие бесплатно!</span>
                <br />
                Зарегистрируйтесь и получите возможность попробовать любую тренировку
              </p>
            </motion.div>
          </motion.div>
        </motion.div>
      </div>
    </div>
  );
}
