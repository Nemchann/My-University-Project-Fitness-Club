import React from 'react';

interface CacheStats {
  total_keys: number;
  hit_rate: number;
}

interface SystemStatsProps {
  uptime: string;
  goroutines: number;
  memory_usage_kb: number;
  cache: CacheStats;
}

export const SystemStats: React.FC<SystemStatsProps> = ({ uptime, goroutines, memory_usage_kb, cache }) => {
  // Красиво форматируем аптайм, убирая лишние наносекунды
  const shortUptime = uptime.split('.')[0] + 's';

  return (
    <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md">
      <h3 className="text-lg font-semibold text-gray-200 mb-4">⚙️ Системные показатели Go и Кэша</h3>
      
      <div className="grid grid-cols-2 gap-4 text-sm">
        <div className="bg-gray-900 p-3 rounded-lg">
          <p className="text-gray-500 text-xs">Uptime</p>
          <p className="font-mono text-amber-400 font-semibold mt-1">{shortUptime}</p>
        </div>
        <div className="bg-gray-900 p-3 rounded-lg">
          <p className="text-gray-500 text-xs">Активные Горутины</p>
          <p className="font-mono text-blue-400 font-semibold mt-1">{goroutines}</p>
        </div>
        <div className="bg-gray-900 p-3 rounded-lg">
          <p className="text-gray-500 text-xs">Потребление памяти</p>
          <p className="font-mono text-purple-400 font-semibold mt-1">{(memory_usage_kb / 1024).toFixed(2)} МБ</p>
        </div>
        <div className="bg-gray-900 p-3 rounded-lg">
          <p className="text-gray-500 text-xs">Cache Hit Rate</p>
          <p className="font-mono text-emerald-400 font-semibold mt-1">{(cache.hit_rate * 100).toFixed(1)}%</p>
        </div>
      </div>
      <div className="mt-3 text-xs text-gray-500 text-right">
        Ключей в кэше: {cache.total_keys}
      </div>
    </div>
  );
};