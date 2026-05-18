import React from 'react';

interface HealthDetails {
  mongodb: string;
  java_backend: string;
}

interface SystemHealthProps {
  status: string;
  details: HealthDetails;
}

export const SystemHealth: React.FC<SystemHealthProps> = ({ status, details }) => {
  const isOK = status === 'OK';
  
  return (
    <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-200">Статус Upstream & БД</h3>
        <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
          isOK ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-amber-950 text-amber-400 border border-amber-800'
        }`}>
          {status}
        </span>
      </div>

      <div className="space-y-3 text-sm">
        <div className="flex justify-between items-center p-2.5 bg-gray-900 rounded-lg">
          <span className="text-gray-400">Java Backend (Upstream):</span>
          <span className={`font-medium ${details.java_backend === 'reachable' ? 'text-emerald-400' : 'text-rose-400'}`}>
            {details.java_backend === 'reachable' ? '● Доступен' : '○ Недоступен'}
          </span>
        </div>

        <div className="flex justify-between items-center p-2.5 bg-gray-900 rounded-lg">
          <span className="text-gray-400">База данных MongoDB:</span>
          <span className={`font-medium ${details.mongodb === 'connected' ? 'text-emerald-400' : 'text-rose-400'}`}>
            {details.mongodb === 'connected' ? '● Подключена' : '○ Отключена'}
          </span>
        </div>
      </div>
    </div>
  );
};