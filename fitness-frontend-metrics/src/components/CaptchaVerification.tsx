import React, { useState } from 'react';

interface CaptchaVerificationProps {
  clientIP: string;
  onSuccess: () => void;
}

export const CaptchaVerification: React.FC<CaptchaVerificationProps> = ({ clientIP, onSuccess }) => {
  const [isChecked, setIsChecked] = useState(false);
  const [isVerifying, setIsVerifying] = useState(false);

  const handleCheckboxChange = async () => {
    if (isVerifying || isChecked) return;

    setIsVerifying(true);

    try {
      // Имитируем сетевую задержку проверки капчи (1.5 секунды)
      await new Promise((resolve) => setTimeout(resolve, 1500));

      // Отправляем запрос на Go-бэкенд, чтобы подтвердить прохождение капчи
      const response = await fetch('http://127.0.0.1:9000/api/proxy/management/ip_access/verify-captcha', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ip: clientIP }),
      });

      if (response.ok) {
        setIsChecked(true);
        // Вызываем колбэк успешного прохождения, чтобы обновить интерфейс/пропустить пользователя
        setTimeout(onSuccess, 800); 
      } else {
        alert('Ошибка проверки капчи. Попробуйте еще раз.');
        setIsVerifying(false);
      }
    } catch (err) {
      console.error('Ошибка при отправке капчи:', err);
      setIsVerifying(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-950 p-4">
      <div className="w-full max-max-w-md bg-gray-900 border border-gray-800 p-6 rounded-2xl shadow-xl text-center">
        
        {/* Иконка щита / безопасности */}
        <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-amber-950 border border-amber-800 text-amber-400 mb-4">
          🛡️
        </div>

        <h2 className="text-xl font-bold text-gray-100">Проверка безопасности</h2>
        <p className="text-gray-400 text-sm mt-2 mb-6">
          Ваш IP-адрес <span className="font-mono text-amber-400 bg-gray-800 px-1.5 py-0.5 rounded">{clientIP}</span> находится в сером списке. Пожалуйста, подтвердите, что вы не робот.
        </p>

        {/* Виджет капчи а-ля Cloudflare */}
        <div className="flex items-center justify-between bg-gray-800 border border-gray-750 rounded-xl p-4 max-w-sm mx-auto shadow-inner">
          <div className="flex items-center space-x-4">
            <button
              type="button"
              onClick={handleCheckboxChange}
              disabled={isVerifying || isChecked}
              className={`w-7 h-7 rounded border-2 flex items-center justify-center transition-all ${
                isChecked 
                  ? 'bg-emerald-500 border-emerald-500 text-gray-950' 
                  : isVerifying 
                    ? 'border-amber-500 bg-gray-800 animate-spin' 
                    : 'border-gray-600 bg-gray-700 hover:border-emerald-500'
              }`}
            >
              {isChecked && (
                <svg className="w-5 h-5 font-bold" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
                </svg>
              )}
              {isVerifying && !isChecked && (
                <div className="w-3 h-3 border-2 border-amber-400 border-t-transparent rounded-full" />
              )}
            </button>
            <span className="text-sm font-medium text-gray-300 select-none">
              {isVerifying ? 'Проверка...' : isChecked ? 'Проверено' : 'Я не робот'}
            </span>
          </div>

          {/* Фирменный логотип системы безопасности */}
          <div className="text-right">
            <p className="text-[10px] font-bold text-emerald-400 font-mono tracking-wider">FITNESS</p>
            <p className="text-[9px] text-gray-500 uppercase">ProxyShield</p>
          </div>
        </div>

      </div>
    </div>
  );
};