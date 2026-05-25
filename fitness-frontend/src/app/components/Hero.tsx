import { Button } from './ui/button';
import { ImageWithFallback } from './figma/ImageWithFallback';
import heroGirl from '../../assets/heroGirl.png';

export function Hero() {
  // Функция для плавного перехода к секции по её ID
  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };
  return (
    <section id="home" className="pt-20 min-h-screen flex items-center relative overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-br from-pink-50 via-purple-50 to-white z-0" />
      
      <div className="container mx-auto px-4 py-20 relative z-10">
        <div className="grid md:grid-cols-2 gap-12 items-center">
          <div>
            <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
              Женский фитнес клуб для<br />
              <span className="text-pink-500">Вашего здоровья</span>
            </h1>
            <p className="text-xl text-gray-600 mb-8">
              Профессиональные тренировки в комфортной атмосфере. 
              Йога, пилатес, танцы и силовые тренировки для женщин любого уровня подготовки.
            </p>
            <div className="flex gap-4">
              <Button size="lg" className="bg-pink-500 hover:bg-pink-600"
                onClick={() => scrollToSection('trial')}>
                Пробное занятие
              </Button>
              <Button size="lg" variant="outline"
                onClick={() => scrollToSection('pricing')}>
                Узнать больше
              </Button>
            </div>
          </div>
          
          <div className="relative">
            <div className="rounded-2xl overflow-hidden shadow-2xl">
              <ImageWithFallback
                src={heroGirl}
                alt="Спортивная девушка пока показывает большие пальцы вверх"
                className="w-full h-[500px] object-cover"
              />
            </div>
            <div className="absolute -bottom-6 -right-6 bg-white p-6 rounded-xl shadow-lg">
              <div className="text-4xl font-bold text-pink-500">500+</div>
              <div className="text-gray-600">Довольных клиентов</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
