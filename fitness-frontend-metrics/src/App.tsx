import { useState, useEffect } from 'react';
import { MetricCard } from './components/MetricCard';
import { IPChecker } from './components/IPChecker';
import { DashboardCharts } from './components/DashboardCharts';
import { TopClients } from './components/TopClients';
import { SystemHealth } from './components/SystemHealth'; // <-- Импорт здоровья
import { SystemStats } from './components/SystemStats';

//Запуск: npm run dev

interface MetricsData {
  current_rps: number;
  latency_ms: number;
  active_connections: number;
  total_traffic_bytes: number;
  rps_history: number[];      // <-- Добавили историю RPS
  traffic_history: number[];  // <-- Добавили историю Трафика
}

interface ClientStats {
  ip: string;
  total_requests: number;
  blocked_requests: number;
  bytes_transferred: number;
}

interface HealthData {
  status: string;
  details: { mongodb: string; java_backend: string };
}

interface StatsData {
  uptime: string;
  goroutines: number;
  memory_usage_kb: number;
  cache: { total_keys: number; hit_rate: number };
}

function App() {
  const [metrics, setMetrics] = useState<MetricsData>({
    current_rps: 0,
    latency_ms: 0,
    active_connections: 0,
    total_traffic_bytes: 0,
    rps_history: new Array(60).fill(0),
    traffic_history: new Array(60).fill(0),
  });

  // Состояние для списка топ-клиентов
  const [topClients, setTopClients] = useState<ClientStats[]>([]);

  
  const [health, setHealth] = useState<HealthData>({
    status: 'Loading...',
    details: { mongodb: 'checking', java_backend: 'checking' }
  });

  const [stats, setStats] = useState<StatsData>({
    uptime: '0s',
    goroutines: 0,
    memory_usage_kb: 0,
    cache: { total_keys: 0, hit_rate: 0 }
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch('http://localhost:9000/api/proxy/management/metrics');
        const data = await response.json();
        setMetrics(data);

        // 2. Загружаем топ клиентов из твоего ClientsHandler
        // Измени URL, если у тебя другой префикс роутера (например, /management/clients)
        const clientsRes = await fetch('http://localhost:9000/api/proxy/management/clients'); 
        const clientsData = await clientsRes.json();

        const healthRes = await fetch('http://localhost:9000/api/proxy/management/health')
        const healthData = await healthRes.json()

        const statsRes = await fetch('http://localhost:9000/api/proxy/management/stats')
        const statsData = await statsRes.json()

        setHealth(healthData);
        setStats(statsData);

        // В Go-хендлере ключ называется "top_clients"
        setTopClients(clientsData.top_clients || []);

      } catch (err) {
        console.error("Ошибка обновления метрик:", err);
      }
    };

    fetchData();
    const interval = setInterval(fetchData, 2000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100 p-8">
      <header className="mb-8 border-b border-gray-800 pb-4">
        <h1 className="text-3xl font-bold text-emerald-400">Fitness Proxy Admin Panel 📊</h1>
        <p className="text-gray-400 text-sm mt-1">Панель управления шлюзом безопасности фитнес-клуба</p>
      </header>

      {/* Сетка наших переиспользуемых карточек */}
      <section className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-10">
        <MetricCard title="Текущий RPS" value={`${metrics.current_rps} req/m`} textColor="text-blue-400" />
        <MetricCard title="Средняя задержка" value={`${metrics.latency_ms} ms`} textColor="text-amber-400" />
        <MetricCard title="Активные соединения" value={metrics.active_connections} textColor="text-emerald-400" />
        <MetricCard title="Всего трафика" value={`${(metrics.total_traffic_bytes / 1024 / 1024).toFixed(2)} MB`} textColor="text-purple-400" />
      </section>

      {/* 2. Блок системных метрик и здоровья */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
        <SystemHealth status={health.status} details={health.details} />
        <SystemStats uptime={stats.uptime} goroutines={stats.goroutines} memory_usage_kb={stats.memory_usage_kb} cache={stats.cache} />
      </section>

      {/* 3. Блок инструментов и клиентов */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <IPChecker />
        <TopClients clients={topClients} />
      </section>

      {/* Блок интерактивных графиков */}
      <DashboardCharts rpsHistory={metrics.rps_history} trafficHistory={metrics.traffic_history} />
    </div>
  );
}

export default App;