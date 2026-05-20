import { Link, useLocation } from 'react-router';
import { Dumbbell, Menu, User } from 'lucide-react';
import { Button } from './ui/button';

export function Header() {
  const location = useLocation();
  const isHomePage = location.pathname === '/';

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-sm border-b border-gray-200">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
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