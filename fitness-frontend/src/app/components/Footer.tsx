import { Dumbbell, Phone, Mail, MapPin} from 'lucide-react'; //Наверное не поддерживается на территории РФ

export function Footer() {
  return (
    <footer id="contact" className="bg-gray-900 text-white py-12">
      <div className="container mx-auto px-4">
        <div className="grid md:grid-cols-4 gap-8 mb-8">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="bg-pink-500 p-2 rounded-lg">
                <Dumbbell className="w-5 h-5 text-white" />
              </div>
              <span className="text-2xl font-bold">FitLady</span>
            </div>
            <p className="text-gray-400">
              Женский фитнес клуб для вашего здоровья и красоты
            </p>
          </div>

          <div>
            <h3 className="font-bold text-lg mb-4">Контакты</h3>
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-gray-400">
                <Phone className="w-4 h-4" />
                <span>+7 (999) 123-45-67</span>
              </div>
              <div className="flex items-center gap-2 text-gray-400">
                <Mail className="w-4 h-4" />
                <span>info@fitlady.ru</span>
              </div>
              <div className="flex items-center gap-2 text-gray-400">
                <MapPin className="w-4 h-4" />
                <span>Саратов, ул. Фитнес, д. 1</span>
              </div>
            </div>
          </div>

          <div>
            <h3 className="font-bold text-lg mb-4">Режим работы</h3>
            <div className="space-y-2 text-gray-400">
              <p>Понедельник - Пятница: 07:00 - 22:00</p>
              <p>Суббота - Воскресенье: 09:00 - 20:00</p>
            </div>
          </div>

          <div>
            <h3 className="font-bold text-lg mb-4">Мы в соцсетях</h3>
            <div className="flex gap-4">
              <a 
                href="#" 
                className="bg-gray-800 p-3 rounded-lg hover:bg-pink-500 transition-colors"
                aria-label="Instagram"
              >Instagram
              </a>
              <a 
                href="#" 
                className="bg-gray-800 p-3 rounded-lg hover:bg-pink-500 transition-colors"
                aria-label="Facebook"
              >Facebook
              </a>
              <a 
                href="#" 
                className="bg-gray-800 p-3 rounded-lg hover:bg-pink-500 transition-colors"
                aria-label="YouTube"
              >Youtube
              </a>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-800 pt-8 text-center text-gray-400">
          <p>&copy; 2026 FitLady. Все права защищены.</p>
        </div>
      </div>
    </footer>
  );
}
