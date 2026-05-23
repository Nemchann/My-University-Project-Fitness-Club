import { useState } from 'react';
import { motion } from 'motion/react';
import { UserPlus, Mail, Phone, Lock, User } from 'lucide-react';
import { Card, CardContent } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';

export function RegistrationPage() {
  const [formData, setFormData] = useState({
    fullName: '',
    phone: '',
    email: '',
    login: '',
    password: '',
    confirmPassword: ''
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Registration data:', formData);
    // Handle registration logic here
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
                  <div className="space-y-2">
                    <Label htmlFor="fullName" className="text-gray-700 font-medium">
                      Полное имя
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
