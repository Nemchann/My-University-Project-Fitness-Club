import React from 'react';

interface MetricCardProps {
  title: string;
  value: string | number;
  textColor: string; // например: 'text-blue-400' или 'text-amber-400'
}

export const MetricCard: React.FC<MetricCardProps> = ({ title, value, textColor }) => {
  return (
    <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-md">
      <p className="text-gray-400 text-sm font-medium uppercase tracking-wider">{title}</p>
      <p className={`text-3xl font-bold mt-2 ${textColor}`}>{value}</p>
    </div>
  );
};