import React, { useState } from 'react';

interface IPStatusResponse {
  ip: string;
  status: string;
}

export const IPChecker: React.FC = () => {
  const [searchIP, setSearchIP] = useState('');
  const [ipStatus, setIpStatus] = useState<string | null>(null);
  const [ipError, setIpError] = useState<string | null>(null);

  const handleCheckIP = async (e: React.FormEvent) => {
    e.preventDefault();
    setIpStatus(null);
    setIpError(null);

    if (!searchIP) return;

    try {
      const response = await fetch(`http://127.0.0.1:9000/api/proxy/management/check_ip?ip=${searchIP}`);
      if (!response.ok) {
        throw new Error("Неверный формат IP или ошибка сервера");
      }
      const data: IPStatusResponse = await response.json();
      setIpStatus(data.status);
    } catch (err: any) {
      setIpError(err.message || "Ошибка соединения с прокси");
    }
  };

  return (
    <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md">
      <h2 className="text-xl font-semibold text-gray-200 mb-4">🛡️ Экспресс-проверка IP-адреса</h2>
      
      <form onSubmit={handleCheckIP} className="flex gap-3">
        <input 
          type="text" 
          placeholder="Например: 192.168.1.1" 
          value={searchIP}
          onChange={(e) => setSearchIP(e.target.value)}
          className="flex-1 bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white placeholder-gray-400 focus:outline-none focus:border-emerald-500 transition-colors"
        />
        <button 
          type="submit"
          className="bg-emerald-600 hover:bg-emerald-500 text-white font-medium px-6 py-2 rounded-lg transition-colors cursor-pointer"
        >
          Проверить
        </button>
      </form>

      {ipStatus && (
        <div className="mt-4 p-4 rounded-lg bg-gray-900 border border-gray-700 flex items-center justify-between">
          <span className="text-gray-300">Статус адреса:</span>
          <span className={`font-bold uppercase px-3 py-1 rounded text-sm ${
            ipStatus === 'whitelisted' ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' :
            ipStatus === 'blacklisted' ? 'bg-rose-950 text-rose-400 border border-rose-800' :
            ipStatus === 'grey' ? 'bg-amber-950 text-amber-400 border border-amber-800' :
            'bg-gray-700 text-gray-300'
          }`}>
            {ipStatus}
          </span>
        </div>
      )}

      {ipError && (
        <div className="mt-4 p-3 bg-rose-950 text-rose-400 text-sm rounded-lg border border-rose-900">
          ⚠️ {ipError}
        </div>
      )}
    </div>
  );
};