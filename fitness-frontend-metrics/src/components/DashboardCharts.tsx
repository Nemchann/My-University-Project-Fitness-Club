import React from 'react';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';

interface DashboardChartsProps {
  rpsHistory: number[];
  trafficHistory: number[];
}

export const DashboardCharts: React.FC<DashboardChartsProps> = ({ rpsHistory, trafficHistory }) => {
  // Трансформируем два массива из Go в один массив объектов для Recharts
  const chartData = rpsHistory.map((rps, index) => {
    const bytes = trafficHistory[index] || 0;
    return {
      second: index + 1,
      rps: rps,
      // Переводим байты в мегабайты для наглядности графика трафика
      trafficMB: parseFloat((bytes / 1024 / 1024).toFixed(3)), 
    };
  });

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-10">
      
      {/* График 1: Нагрузка RPS */}
      <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md">
        <h3 className="text-lg font-semibold text-gray-200 mb-4">📈 График нагрузки (RPS за минуту)</h3>
        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="colorRps" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
              <XAxis dataKey="second" stroke="#9ca3af" fontSize={12} tickLine={false} />
              <YAxis stroke="#9ca3af" fontSize={12} tickLine={false} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#1f2937', borderColor: '#4b5563', borderRadius: '8px' }}
                labelFormatter={(label) => `Секунда: ${label}`}
              />
              <Area type="monotone" dataKey="rps" name="Запросы/сек" stroke="#3b82f6" strokeWidth={2} fillOpacity={1} fill="url(#colorRps)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* График 2: Динамика сетевого трафика */}
      <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md">
        <h3 className="text-lg font-semibold text-gray-200 mb-4">🌐 Потребление трафика (МБ/сек)</h3>
        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="colorTraffic" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#a855f7" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#a855f7" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
              <XAxis dataKey="second" stroke="#9ca3af" fontSize={12} tickLine={false} />
              <YAxis stroke="#9ca3af" fontSize={12} tickLine={false} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#1f2937', borderColor: '#4b5563', borderRadius: '8px' }}
                labelFormatter={(label) => `Секунда: ${label}`}
              />
              <Area type="monotone" dataKey="trafficMB" name="Трафик (МБ)" stroke="#a855f7" strokeWidth={2} fillOpacity={1} fill="url(#colorTraffic)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

    </div>
  );
};