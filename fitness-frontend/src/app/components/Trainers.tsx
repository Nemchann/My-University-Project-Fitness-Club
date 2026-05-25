import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { ImageWithFallback } from './figma/ImageWithFallback';
import trainerCardImg from '../../assets/trainer.png';

interface Trainer {
  name: string;
  specialization: string;
  experience: string;
  photo: string;
  specialties: string[];
  description: string;
}

const trainers: Trainer[] = [
  {
    name: 'Елена Смирнова',
    specialization: 'Инструктор по йоге',
    experience: '8 лет опыта',
    photo: trainerCardImg,
    specialties: ['Хатха йога', 'Виньяса', 'Медитация'],
    description: 'Сертифицированный инструктор с международной лицензией. Помогу найти гармонию тела и духа.'
  },
  {
    name: 'Анна Петрова',
    specialization: 'Инструктор по пилатесу',
    experience: '6 лет опыта',
    photo: trainerCardImg,
    specialties: ['Классический пилатес', 'Реформер', 'Растяжка'],
    description: 'Специалист по реабилитации и укреплению мышц. Работаю с клиентами любого уровня подготовки.'
  },
  {
    name: 'Мария Иванова',
    specialization: 'Персональный тренер',
    experience: '10 лет опыта',
    photo: trainerCardImg,
    specialties: ['Силовые тренировки', 'HIIT', 'Функциональный тренинг'],
    description: 'Мастер спорта по фитнесу. Помогу достичь ваших целей и создать тело мечты.'
  },
  {
    name: 'София Новикова',
    specialization: 'Хореограф',
    experience: '7 лет опыта',
    photo: trainerCardImg,
    specialties: ['Зумба', 'Латина', 'Танцевальная аэробика'],
    description: 'Профессиональный танцор и хореограф. Сделаю ваши тренировки яркими и энергичными!'
  },
];

export function Trainers() {
  return (
    <section id="trainers" className="py-20 bg-white">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Наши тренеры
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Команда профессионалов с международными сертификатами и огромным опытом
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
          {trainers.map((trainer, index) => (
            <Card key={index} className="overflow-hidden hover:shadow-xl transition-shadow group">
              <div className="relative overflow-hidden">
                <ImageWithFallback
                  src={trainer.photo}
                  alt={trainer.name}
                  className="w-full h-80 object-cover group-hover:scale-105 transition-transform duration-300"
                />
                <div className="absolute top-4 right-4">
                  <Badge className="bg-pink-500 hover:bg-pink-600 text-white">
                    {trainer.experience}
                  </Badge>
                </div>
              </div>
              <CardContent className="p-6">
                <h3 className="text-xl font-bold text-gray-900 mb-1">
                  {trainer.name}
                </h3>
                <p className="text-pink-500 font-medium mb-3">
                  {trainer.specialization}
                </p>
                <p className="text-gray-600 text-sm mb-4">
                  {trainer.description}
                </p>
                <div className="flex flex-wrap gap-2">
                  {trainer.specialties.map((specialty, idx) => (
                    <Badge key={idx} variant="outline" className="text-xs">
                      {specialty}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
