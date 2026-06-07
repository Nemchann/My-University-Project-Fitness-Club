import { Link, useLocation, useNavigate } from 'react-router';
import { useState, useEffect } from "react";
import { Dumbbell, Menu, User, LogOut } from 'lucide-react';
import { Button } from './ui/button';
import { api } from "../../lib/api";
import { useDispatch } from 'react-redux';
import { setUserId } from '../../store';

import { 
  Dialog, 
  DialogContent, 
  DialogHeader, 
  DialogTitle, 
  DialogTrigger,
  DialogFooter
} from "./ui/dialog";
import { Input } from "./ui/input";
import { Label } from "./ui/label";

export function Header() {
  
  const location = useLocation();
  const navigate = useNavigate();
  const isHomePage = location.pathname === '/';
  
  const [login, setLogin] = useState(""); 
  const [password, setPassword] = useState("");
  const [errorAuth, setErrorAuth] = useState("");
  
  // Состояние для отслеживания: вошел пользователь или нет
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const dispatch = useDispatch();

  // Проверяем статус авторизации при монтировании компонента
  useEffect(() => {
    const userId = localStorage.getItem("userId");
    setIsAuthenticated(!!userId); // превратит строку или null в true/false
  }, []);

  // Функция входа
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorAuth("");
    try {
      // Отправляем login
      const response = await api.post("/fitness-club/users/authentification", { 
        login, 
        password 
      });
      
      if (response.data && response.data.id) {
        localStorage.setItem("userId", response.data.id);
        dispatch(setUserId(response.data.id));
        setIsAuthenticated(true);
      } else if (response.data && response.data.token) {
        localStorage.setItem("token", response.data.token);
        setIsAuthenticated(true);
      }

      setIsDialogOpen(false); // Закрываем модальное окно
      setLogin("");
      setPassword("");
      
      // Перенаправляем в профиль после успешного входа
      navigate('/profile');
    } catch (error: any) {
      console.error("Ошибка авторизации:", error);
      setErrorAuth(error.response?.data?.message || "Неверный логин или пароль");
    }
  };

  // Функция выхода из аккаунта
  const handleLogout = () => {
    localStorage.removeItem("userId");
    dispatch(setUserId(null));
    localStorage.removeItem("token");
    setIsAuthenticated(false);
    navigate('/'); // Возвращаем на главную
  };

  return (
    <header className="fixed top-0 left-0 right-0 bg-white/80 backdrop-blur-md border-b border-pink-100 z-50">
      <div className="container mx-auto px-4 h-20 flex items-center justify-between">
        
        {/* Логотип */}
        <Link to="/" className="flex items-center gap-2 text-pink-600 font-bold text-xl">
          <Dumbbell className="w-6 h-6" />
          <span>FitLady</span>
        </Link>

        {/* Навигация (показываем только на главной) */}
        {isHomePage && (
          <nav className="hidden md:flex items-center gap-8">
            <a href="#schedule" className="text-gray-700 hover:text-pink-500 transition-colors">
              Расписание
            </a>
            <a href="#trainers" className="text-gray-700 hover:text-pink-500 transition-colors">
              Тренеры
            </a>
            <a href="#pricing" className="text-gray-700 hover:text-pink-500 transition-colors">
              Цены
            </a>
            <a href="#contact" className="text-gray-700 hover:text-pink-500 transition-colors">
              Контакты
            </a>
          </nav>
        )}

        {/* Блок кнопок с условным рендерингом */}
        <div className="flex items-center gap-4">
          {isAuthenticated ? (
            // Пользователь зашел -> Показываем только Профиль и Выход
            <>
              <Link to="/profile">
                <Button variant="ghost" className="text-pink-600 hover:text-pink-700 hover:bg-pink-50 gap-2 font-medium cursor-pointer">
                  <User className="w-5 h-5" />
                  Личный кабинет
                </Button>
              </Link>
              
              <Button 
                onClick={handleLogout}
                variant="outline" 
                className="border-gray-200 text-gray-500 hover:text-red-600 hover:bg-red-50 gap-2 cursor-pointer"
              >
                <LogOut className="w-4 h-4" />
                Выйти
              </Button>
            </>
          ) : (
            //  Пользователь НЕ зашел -> Показываем только Вход и Регистрацию
            <>
              {/* Модальное окно Входа */}
              <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="ghost" className="text-pink-600 hover:text-pink-700 hover:bg-pink-50 cursor-pointer">
                    Вход
                  </Button>
                </DialogTrigger>
                <DialogContent className="sm:max-w-[425px] bg-white">
                  <DialogHeader>
                    <DialogTitle className="text-2xl font-bold text-gray-900 text-center">Войти в аккаунт</DialogTitle>
                  </DialogHeader>
                  <form onSubmit={handleLogin} className="space-y-4 pt-4">
                    <div className="space-y-2">
                      <Label htmlFor="login">Логин</Label>
                      <Input 
                        id="login" 
                        placeholder="Ваш логин" 
                        value={login}
                        onChange={(e) => setLogin(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="password">Пароль</Label>
                      <Input 
                        id="password" 
                        type="password" 
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required 
                      />
                    </div>

                    {errorAuth && (
                      <p className="text-sm text-red-500 font-medium text-center bg-red-50 py-1.5 rounded border border-red-100">
                        {errorAuth}
                      </p>
                    )}

                    <Button type="submit" className="w-full bg-pink-500 hover:bg-pink-600 text-white py-5 mt-2 cursor-pointer">
                      Войти
                    </Button>
                  </form>
                </DialogContent>
              </Dialog>

              {/* Кнопка Регистрации */}
              <Link to="/registration">
                <Button className="bg-pink-500 hover:bg-pink-600 text-white cursor-pointer">
                  Регистрация
                </Button>
              </Link>
            </>
          )}

          {/* Мобильное меню */}
          <Button variant="ghost" size="icon" className="md:hidden text-gray-700">
            <Menu className="w-6 h-6" />
          </Button>
        </div>

      </div>
    </header>
  );
}