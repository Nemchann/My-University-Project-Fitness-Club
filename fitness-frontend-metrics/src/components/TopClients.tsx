import React from 'react';

// Описываем структуру данных клиента на основе Go-структуры ClientStats
interface ClientStats {
  ip: string;
  total_requests: number;
  blocked_requests: number;
  bytes_transferred: number;
}

interface TopClientsProps {
  clients: ClientStats[];
}

export const TopClients: React.FC<TopClientsProps> = ({ clients }) => {
  return (
    <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md h-full">
      <h3 className="text-lg font-semibold text-gray-200 mb-4">👥 Топ активных клиентов (IP)</h3>
      
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-gray-300">
          <thead className="text-xs uppercase bg-gray-700 text-gray-400 border-b border-gray-600">
            <tr>
              <th className="px-4 py-3">IP Адрес</th>
              <th className="px-4 py-3 text-right">Всего запросов</th>
              <th className="px-4 py-3 text-right">Заблокировано</th>
              <th className="px-4 py-3 text-right">Трафик</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-700">
            {clients.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-gray-500">
                  Активных клиентов пока нет
                </td>
              </tr>
            ) : (
              clients.map((client) => (
                <tr key={client.ip} className="hover:bg-gray-750 transition-colors">
                  <td className="px-4 py-3 font-mono text-emerald-400 font-medium">
                    {client.ip}
                  </td>
                  <td className="px-4 py-3 text-right font-semibold text-blue-400">
                    {client.total_requests.toLocaleString()}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                      client.blocked_requests > 0 
                        ? 'bg-rose-950 text-rose-400 border border-rose-900' 
                        : 'text-gray-500'
                    }`}>
                      {client.blocked_requests}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right text-purple-400">
                    {(client.bytes_transferred / 1024 / 1024).toFixed(2)} МБ
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};