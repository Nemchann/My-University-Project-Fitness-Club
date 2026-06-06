import { RouterProvider } from 'react-router';
import { router } from './routes';
import React, { useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import '../styles/index.css';
import { Provider } from 'react-redux';
import { store } from '../store';

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
  throw new Error('Не удалось найти корневой элемент id="root". Проверь index.html');
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/* Оборачиваем всё приложение в Redux */}
    <Provider store={store}>
      <App />
    </Provider>
  </React.StrictMode>
);