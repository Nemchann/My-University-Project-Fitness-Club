import { Link, useLocation } from 'react-router';
import { useState } from "react";
import { Dumbbell, Menu, User } from 'lucide-react';
import { Button } from './ui/button';
import { api } from "../../lib/api";

import { 
  Dialog, 
  DialogContent, 
  DialogHeader, 
  DialogTitle, 
  DialogTrigger 
} from "./ui/dialog";
import { Input } from "./ui/input";
import { Label } from "./ui/label";

export function Header() {
  const location = useLocation();
  const isHomePage = location.pathname === '/';
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // Отправляем POST-запрос на Go/Java бэкенд
      const response = await api.post("/auth/login", { email, password });
      
      // Сохраняем токен, если бэкенд его возвращает
      localStorage.setItem("token", response.data.token);
      alert("Успешный вход!");
      
      // Здесь можно перезагрузить страницу или закрыть окно
      window.location.reload(); 
    } catch (error) {
      console.error("Ошибка авторизации:", error);
      alert("Неверный логин или пароль");
    }
  }

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-sm border-b border-gray-200">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">

        <Dialog>
        <DialogTrigger asChild>
          <Button variant="outline" className="cursor-pointer">Войти</Button>
        </DialogTrigger>
        
        <DialogContent className="sm:max-w-[420px]">
          <DialogHeader>
            <DialogTitle className="text-center text-xl font-bold">Вход в личный кабинет</DialogTitle>
          </DialogHeader>
          
          <form onSubmit={handleLogin} className="space-y-4 pt-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input 
                id="email" 
                type="email" 
                placeholder="example@mail.com" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)}
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
            
            <Button type="submit" className="w-full bg-pink-500 hover:bg-pink-600 text-white cursor-pointer">
              Войти
            </Button>
          </form>
        </DialogContent>
      </Dialog>

        <Link to="/" className="flex items-center gap-2">
          <div className="bg-pink-500 p-2 rounded-lg">
            <Dumbbell className="w-6 h-6 text-white" />
          </div>
          <span className="text-2xl font-bold text-gray-900">FitLady</span>
        </Link>

        {isHomePage && (
          <nav className="hidden md:flex items-center gap-8">
            <a href="#home" className="text-gray-700 hover:text-pink-500 transition-colors">
              Главная
            </a>
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

        <div className="flex items-center gap-4">
          {isHomePage ? (
            <>
              <Link to="/profile">
                <Button variant="ghost" size="icon" className="hidden md:inline-flex text-pink-600 hover:text-pink-700 hover:bg-pink-50">
                  <User className="w-5 h-5" />
                </Button>
              </Link>
              <Link to="/registration">
                <Button className="hidden md:inline-flex bg-pink-500 hover:bg-pink-600">
                  Регистрация
                </Button>
              </Link>
            </>
          ) : (
            <Button className="hidden md:inline-flex bg-pink-500 hover:bg-pink-600">
              Позвонить
            </Button>
          )}
          <Button variant="ghost" size="icon" className="md:hidden">
            <Menu className="w-6 h-6" />
          </Button>
        </div>
      </div>
    </header>
  );
}