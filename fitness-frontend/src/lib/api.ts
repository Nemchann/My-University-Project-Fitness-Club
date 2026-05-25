import axios from "axios";

// 1. Создаем инстанс Axios с базовыми настройками
export const api = axios.create({
  // Укажи здесь URL своего Go-прокси или Java-бэкенда
  baseURL: "http://localhost:9000/api", 
  timeout: 5000, // Если бэкенд не ответит за 5 секунд, запрос прервется
  headers: {
    "Content-Type": "application/json",
  },
});

// 2. Перехватчик (Interceptor) для автоматического добавления токена авторизации
api.interceptors.request.use(
  (config) => {
    // Берём JWT-токен из localStorage (куда мы его сохраним при логине)
    const token = localStorage.getItem("token");
    
    // Если токен есть, автоматически добавляем его в каждый запрос в заголовок Authorization
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);