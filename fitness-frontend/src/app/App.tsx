import { RouterProvider } from 'react-router';
import { router } from './routes';
import React, { useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import '../styles/index.css';

export default function App() {
  useEffect(() => {
    if (!localStorage.getItem("userId")) {
      localStorage.setItem("userId", "922ddbc4-bef0-453a-8b1f-27fcfb5f3ab7");
    }
  }, []);
  return <RouterProvider router={router} />;
}

const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('Не удалось найти корневой элемент id="root". Проверь index.html!');
}

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);