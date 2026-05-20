import { useState } from 'react';
import { Link } from 'react-router';
import { motion, AnimatePresence } from 'motion/react';
import { Card, CardContent } from './ui/card';
import { Calendar } from './ui/calendar';
import { Clock, User, TrendingUp, CalendarDays } from 'lucide-react';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';

interface ClassItem {
  time: string;
  name: string;
  trainer: string;
  type: string;
  duration: string;
}

const scheduleData: Record<string, ClassItem[]> = {
  monday: [
    { time: '08:00', name: 'Утренняя йога', trainer: 'Елена Смирнова', type: 'Йога', duration: '60 мин' },
    { time: '10:00', name: 'Пилатес', trainer: 'Анна Петрова', type: 'Пилатес', duration: '55 мин' },
    { time: '12:00', name: 'Силовая тренировка', trainer: 'Мария Иванова', type: 'Силовая', duration: '50 мин' },
    { time: '18:00', name: 'Зумба', trainer: 'София Новикова', type: 'Танцевальная', duration: '60 мин' },
    { time: '19:30', name: 'Растяжка', trainer: 'Елена Смирнова', type: 'Растяжка', duration: '45 мин' },
  ],
  tuesday: [
    { time: '09:00', name: 'Хатха йога', trainer: 'Елена Смирнова', type: 'Йога', duration: '75 мин' },
    { time: '11:00', name: 'Функциональный тренинг', trainer: 'Мария Иванова', type: 'Силовая', duration: '50 мин' },
    { time: '17:00', name: 'Стретчинг', trainer: 'Анна Петрова', type: 'Растяжка', duration: '45 мин' },
    { time: '19:00', name: 'Танцевальная аэробика', trainer: 'София Новикова', type: 'Танцевальная', duration: '60 мин' },
  ],
  wednesday: [
    { time: '08:00', name: 'Утренняя йога', trainer: 'Елена Смирнова', type: 'Йога', duration: '60 мин' },
    { time: '10:00', name: 'Пилатес Реформер', trainer: 'Анна Петрова', type: 'Пилатес', duration: '55 мин' },
    { time: '12:00', name: 'Силовая тренировка', trainer: 'Мария Иванова', type: 'Силовая', duration: '50 мин' },
    { time: '18:00', name: 'Латина', trainer: 'София Новикова', type: 'Танцевальная', duration: '60 мин' },
    { time: '20:00', name: 'Йога для спины', trainer: 'Елена Смирнова', type: 'Йога', duration: '60 мин' },
  ],
  thursday: [
    { time: '09:00', name: 'Виньяса йога', trainer: 'Елена Смирнова', type: 'Йога', duration: '75 мин' },
    { time: '11:00', name: 'Круговая тренировка', trainer: 'Мария Иванова', type: 'Кардио', duration: '50 мин' },
    { time: '17:00', name: 'Стретчинг', trainer: 'Анна Петрова', type: 'Растяжка', duration: '45 мин' },
    { time: '19:00', name: 'Зумба', trainer: 'София Новикова', type: 'Танцевальная', duration: '60 мин' },
  ],
  friday: [
    { time: '08:00', name: 'Утренняя йога', trainer: 'Елена Смирнова', type: 'Йога', duration: '60 мин' },
    { time: '10:00', name: 'Пилатес', trainer: 'Анна Петрова', type: 'Пилатес', duration: '55 мин' },
    { time: '12:00', name: 'Функциональный тренинг', trainer: 'Мария Иванова', type: 'Силовая', duration: '50 мин' },
    { time: '18:00', name: 'Танцевальный микс', trainer: 'София Новикова', type: 'Танцевальная', duration: '60 мин' },
    { time: '19:30', name: 'Восстановительная йога', trainer: 'Елена Смирнова', type: 'Йога', duration: '60 мин' },
  ],
  saturday: [
    { time: '10:00', name: 'Йога + медитация', trainer: 'Елена Смирнова', type: 'Йога', duration: '90 мин' },
    { time: '12:00', name: 'Пилатес для начинающих', trainer: 'Анна Петрова', type: 'Пилатес', duration: '60 мин' },
    { time: '14:00', name: 'HIIT тренировка', trainer: 'Мария Иванова', type: 'Кардио', duration: '45 мин' },
  ],
  sunday: [
    { time: '11:00', name: 'Йога выходного дня', trainer: 'Елена Смирнова', type: 'Йога', duration: '75 мин' },
    { time: '13:00', name: 'Растяжка и релакс', trainer: 'Анна Петрова', type: 'Растяжка', duration: '60 мин' },
  ],
};

const dayMap: Record<number, string> = {
  1: 'monday',
  2: 'tuesday',
  3: 'wednesday',
  4: 'thursday',
  5: 'friday',
  6: 'saturday',
  0: 'sunday',
};

const getTypeColor = (type: string) => {
  switch (type) {
    case 'Йога':
      return 'bg-purple-100 text-purple-700';
    case 'Пилатес':
      return 'bg-blue-100 text-blue-700';
    case 'Силовая':
      return 'bg-orange-100 text-orange-700';
    case 'Кардио':
      return 'bg-red-100 text-red-700';
    case 'Танцевальная':
      return 'bg-pink-100 text-pink-700';
    case 'Растяжка':
      return 'bg-green-100 text-green-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
};

export function Schedule() {
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());

  const getDaySchedule = (date: Date | undefined): ClassItem[] => {
    if (!date) return [];
    const dayOfWeek = date.getDay();
    const dayKey = dayMap[dayOfWeek];
    return scheduleData[dayKey] || [];
  };

  const classes = getDaySchedule(selectedDate);

  return (
    <section id="schedule" className="py-20 bg-gradient-to-b from-white to-pink-50/30">
      <div className="container mx-auto px-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Расписание занятий
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Выберите дату в календаре и найдите идеальное время для тренировки
          </p>
        </motion.div>

        <div className="max-w-6xl mx-auto grid lg:grid-cols-[400px_1fr] gap-8">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <Card className="border-2 border-pink-100 shadow-lg sticky top-24">
              <CardContent className="p-6">
                <div className="flex items-center gap-2 mb-4">
                  <CalendarDays className="w-5 h-5 text-pink-600" />
                  <h3 className="font-semibold text-lg text-gray-900">Выберите дату</h3>
                </div>
                <Calendar
                  mode="single"
                  selected={selectedDate}
                  onSelect={setSelectedDate}
                  className="rounded-md border-none"
                  locale={ru}
                  classNames={{
                    day_selected: "bg-pink-500 text-white hover:bg-pink-600 focus:bg-pink-600",
                    day_today: "bg-pink-100 text-pink-900",
                  }}
                />
                {selectedDate && (
                  <div className="mt-4 p-3 bg-pink-50 rounded-lg">
                    <p className="text-sm text-gray-600 mb-1">Выбранная дата:</p>
                    <p className="font-semibold text-pink-600">
                      {format(selectedDate, 'EEEE, d MMMM yyyy', { locale: ru })}
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, x: 20 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="space-y-4"
          >
            <AnimatePresence mode="wait">
              {classes.length > 0 ? (
                <motion.div
                  key={selectedDate?.toISOString()}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.3 }}
                  className="space-y-4"
                >
                  {classes.map((classItem, index) => (
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.1 }}
                    >
                      <Link to={`/class/${index + 1}`}>
                        <Card className="hover:shadow-xl hover:border-pink-200 transition-all duration-300 border-pink-50 cursor-pointer">
                          <CardContent className="p-6">
                            <div className="grid md:grid-cols-4 gap-4 items-center">
                              <div className="flex items-center gap-3">
                                <motion.div
                                  whileHover={{ scale: 1.1, rotate: 5 }}
                                  className="bg-gradient-to-br from-pink-100 to-pink-200 p-3 rounded-lg"
                                >
                                  <Clock className="w-5 h-5 text-pink-600" />
                                </motion.div>
                                <div>
                                  <div className="font-semibold text-gray-900">{classItem.time}</div>
                                  <div className="text-sm text-gray-500">{classItem.duration}</div>
                                </div>
                              </div>

                              <div>
                                <div className="font-semibold text-lg text-gray-900">{classItem.name}</div>
                              </div>

                              <div className="flex items-center gap-2 text-gray-600">
                                <User className="w-4 h-4 text-pink-500" />
                                <span>{classItem.trainer}</span>
                              </div>

                              <div className="flex items-center justify-end gap-2">
                                <TrendingUp className="w-4 h-4 text-gray-500" />
                                <span className={`px-3 py-1 rounded-full text-sm font-medium ${getTypeColor(classItem.type)}`}>
                                  {classItem.type}
                                </span>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      </Link>
                    </motion.div>
                  ))}
                </motion.div>
              ) : (
                <motion.div
                  key="no-classes"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="text-center py-12"
                >
                  <Card className="border-2 border-dashed border-pink-200">
                    <CardContent className="p-12">
                      <CalendarDays className="w-16 h-16 text-pink-300 mx-auto mb-4" />
                      <h3 className="text-xl font-semibold text-gray-700 mb-2">
                        На эту дату занятий нет
                      </h3>
                      <p className="text-gray-500">
                        Попробуйте выбрать другой день
                      </p>
                    </CardContent>
                  </Card>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
        </div>
      </div>
    </section>
  );
}
