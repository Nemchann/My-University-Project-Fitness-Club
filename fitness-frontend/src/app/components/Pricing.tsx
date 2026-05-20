import { Card, CardContent, CardHeader } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Check, Gift } from 'lucide-react';

interface PricingPlan {
  name: string;
  price: string;
  description: string;
  features: string[];
  popular?: boolean;
  pricePerClass?: string;
}

const plans: PricingPlan[] = [
  {
    name: 'Разовое занятие',
    price: '600',
    description: 'Попробуйте наш клуб',
    features: [
      'Любое занятие на выбор',
      'Доступ в раздевалку',
      'Душ и полотенце',
      'Консультация тренера'
    ],
  },
  {
    name: '8 занятий',
    price: '4 000',
    pricePerClass: '500 ₽ за занятие',
    description: 'Для регулярных тренировок',
    features: [
      'Срок действия 30 дней',
      'Любые групповые занятия',
      'Заморозка абонемента 7 дней',
      'Скидка 800 ₽',
      'Бесплатное полотенце'
    ],
  },
  {
    name: '12 занятий',
    price: '5 100',
    pricePerClass: '425 ₽ за занятие',
    description: 'Оптимальный выбор',
    popular: true,
    features: [
      'Срок действия 45 дней',
      'Любые групповые занятия',
      'Заморозка абонемента 10 дней',
      'Скидка 900 ₽',
      'Бесплатное полотенце',
      'Консультация по питанию'
    ],
  },
  {
    name: 'Безлимит',
    price: '6 500',
    description: 'Неограниченные тренировки',
    features: [
      'Безлимитное посещение 30 дней',
      'Все групповые занятия',
      'Заморозка абонемента 14 дней',
      'Приоритетная запись',
      'Бесплатное полотенце',
      'Консультация по питанию',
      'Индивидуальная программа тренировок'
    ],
  },
];

export function Pricing() {
  return (
    <section id="pricing" className="py-20 bg-gradient-to-br from-pink-100 via-pink-50 to-purple-50">
      <div className="container mx-auto px-4">
        {/* Блок о бесплатном первом занятии */}
        <div className="max-w-4xl mx-auto mb-12">
          <div className="bg-gradient-to-r from-pink-500 to-pink-600 rounded-2xl p-8 text-white text-center shadow-xl">
            <div className="flex justify-center mb-4">
              <div className="bg-white/20 p-4 rounded-full">
                <Gift className="w-12 h-12 text-white" />
              </div>
            </div>
            <h3 className="text-3xl font-bold mb-3">
              Первое занятие — бесплатно!
            </h3>
            <p className="text-lg text-pink-100 mb-6">
              Приходите на пробную тренировку и убедитесь в качестве наших услуг. 
              Без обязательств и скрытых платежей.
            </p>
            <Button size="lg" className="bg-white text-pink-500 hover:bg-pink-50 font-semibold">
              Записаться на бесплатное занятие
            </Button>
          </div>
        </div>

        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Цены и абонементы
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Выберите подходящий вариант для комфортных и эффективных тренировок
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-7xl mx-auto">
          {plans.map((plan, index) => (
            <Card 
              key={index} 
              className={`relative overflow-hidden hover:shadow-2xl transition-all duration-300 ${
                plan.popular ? 'border-2 border-pink-500 scale-105 bg-pink-50/50' : ''
              }`}
            >
              {plan.popular && (
                <div className="absolute top-0 right-0">
                  <Badge className="bg-pink-500 hover:bg-pink-600 text-white rounded-tl-none rounded-br-none">
                    Популярный
                  </Badge>
                </div>
              )}
              
              <CardHeader className={`text-center pb-8 pt-8 ${plan.popular ? 'bg-gradient-to-b from-pink-100/50 to-transparent' : ''}`}>
                <h3 className="text-2xl font-bold text-gray-900 mb-2">
                  {plan.name}
                </h3>
                <p className="text-gray-600 text-sm mb-4">
                  {plan.description}
                </p>
                <div className="mb-2">
                  <span className={`text-4xl font-bold ${plan.popular ? 'text-pink-600' : 'text-gray-900'}`}>
                    {plan.price}
                  </span>
                  <span className="text-gray-600 ml-1">₽</span>
                </div>
                {plan.pricePerClass && (
                  <p className="text-sm text-pink-500 font-medium">
                    {plan.pricePerClass}
                  </p>
                )}
              </CardHeader>

              <CardContent className="pt-0">
                <ul className="space-y-3 mb-6">
                  {plan.features.map((feature, idx) => (
                    <li key={idx} className="flex items-start gap-2">
                      <Check className="w-5 h-5 text-pink-500 flex-shrink-0 mt-0.5" />
                      <span className="text-gray-700 text-sm">{feature}</span>
                    </li>
                  ))}
                </ul>

                <Button 
                  className={`w-full ${
                    plan.popular 
                      ? 'bg-pink-500 hover:bg-pink-600' 
                      : 'bg-pink-400 hover:bg-pink-500'
                  }`}
                >
                  Купить абонемент
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="mt-12 text-center">
          <p className="text-gray-600 mb-4">
            Не нашли подходящий вариант? Есть вопросы?
          </p>
          <Button variant="outline" size="lg" className="border-pink-500 text-pink-500 hover:bg-pink-50">
            Связаться с нами
          </Button>
        </div>
      </div>
    </section>
  );
}