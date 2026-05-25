import { useState, useEffect } from 'react';
import { Link } from 'react-router';
import { motion, AnimatePresence } from 'motion/react';
import { Card, CardContent } from './ui/card';
import { Calendar } from './ui/calendar';
import { Clock, User, TrendingUp, CalendarDays } from 'lucide-react';
import { format, differenceInMinutes, parseISO } from 'date-fns';
import { ru } from 'date-fns/locale';
import { api } from "../../lib/api";


interface ScheduleResponseDto {
  id: number;
  workoutName: string;
  trainerFullName: string;
  workoutType: string;
  description: string;
  room: string;
  scheduleDate: string; // LocalDate приходит в виде строки 'YYYY-MM-DD'
  startTime: string;    // LocalDateTime приходит в виде ISO строки 'YYYY-MM-DDTHH:mm:ss'
  endTime: string;      // LocalDateTime приходит в виде ISO строки 'YYYY-MM-DDTHH:mm:ss'
  maxParticipants: number;
  currentParticipants: number;
}



const getTypeColor = (type: string) => {
  switch (type) {
    case 'MIND_AND_BODY':
      return 'bg-purple-100 text-purple-700';
    case 'STRENGTH':
      return 'bg-orange-100 text-orange-700';
    case 'CARDIO':
      return 'bg-red-100 text-red-700';
    case 'DANCE':
      return 'bg-pink-100 text-pink-700';
    case 'WOMEN_HEALTH':
      return 'bg-green-100 text-green-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
};

export function Schedule() {

  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());
  // Стейт для хранения реальных тренировок с бэкенда
  const [classes, setClasses] = useState<ScheduleResponseDto[]>([]);
  // Стейт для анимации загрузки (скелетонов)
  const [isLoading, setIsLoading] = useState(false);

  // useEffect будет срабатывать каждый раз, когда пользователь выбирает новую дату
  useEffect(() => {
    if (!selectedDate) return;

    const fetchSchedule = async () => {
      setIsLoading(true);
      try {
        // Форматируем дату в строку (например, YYYY-MM-DD), которую ждет твой бэкенд
        const formattedDate = format(selectedDate, 'yyyy-MM-dd');
        
        // Делаем реальный GET-запрос к твоему Go/Java бэкенду
        const response = await api.get(`/fitness-club/schedules/get_schedules_by_date?date=${formattedDate}`);
        
        // Кладем ответ бэкенда в стейт
        setClasses(response.data); 
      } catch (error) {
        console.error("Ошибка при получении расписания:", error);
        setClasses([]); // В случае ошибки очищаем список
      } finally {
        setIsLoading(false);
      }
    };

    fetchSchedule();
  }, [selectedDate]); // Зависимость [selectedDate] запускает этот блок при каждом клике по календарю

  const getWorkoutTimeInfo = (startIso: string, endIso: string) => {
    const start = parseISO(startIso);
    const end = parseISO(endIso);
    
    // Форматируем время старта, например: "08:00" или "19:30"
    const startTimeFormatted = format(start, 'HH:mm');
    
    // Вычисляем разницу в минутах между endTime и startTime
    const durationMin = differenceInMinutes(end, start);
    
    return {
      time: startTimeFormatted,
      duration: `${durationMin} мин`
    };
  };

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
                    selected: "bg-pink-500 text-white hover:bg-pink-600 focus:bg-pink-600", 
                    today: "bg-pink-100 text-pink-900", 
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
              {isLoading ? (
                // Показываем простую надпись загрузки, пока данные не пришли
                <div className="text-center py-12 text-gray-500">Загрузка расписания...</div>
              ) : classes.length > 0 ? (
                <motion.div
                  key={selectedDate?.toISOString()}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.3 }}
                  className="space-y-4"
                >
                  {classes.map((classItem, index) =>{
                    const timeInfo = getWorkoutTimeInfo(classItem.startTime, classItem.endTime);
                  
                    return (
                    
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.1 }}
                    >
                      {/* Передаем реальный id из базы данных на страницу деталей */}
                      <Link to={`/class/${classItem.id}`}>
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
                                  <div className="font-semibold text-gray-900">{timeInfo.time}</div>
                                  <div className="text-sm text-gray-500">{timeInfo.duration}</div>
                                </div>
                              </div>

                              <div>
                                <div className="font-semibold text-lg text-gray-900">{classItem.workoutName}</div>
                              </div>

                              <div className="flex items-center gap-2 text-gray-600">
                                <User className="w-4 h-4 text-pink-500" />
                                <span>{classItem.trainerFullName}</span>
                              </div>

                              <div className="flex items-center justify-end gap-2">
                                <TrendingUp className="w-4 h-4 text-gray-500" />
                                <span className={`px-3 py-1 rounded-full text-sm font-medium ${getTypeColor(classItem.workoutType)}`}>
                                  {classItem.workoutType}
                                </span>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      </Link>
                    </motion.div>
                  );
                  })}
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
