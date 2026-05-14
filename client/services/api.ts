import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';

/** Базовый URL API сервера */
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

/**
 * Настроенный экземпляр Axios с автоматическим обновлением токенов
 * Обрабатывает 401 ошибки и автоматически обновляет access token
 */
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

/** Флаг процесса обновления токена */
let isRefreshing = false;

/** Тип для отложенных запросов */
type FailedRequest = {
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
};

/** Очередь отложенных запросов на время обновления токена */
let failedQueue: FailedRequest[] = [];

/**
 * Обрабатывает очередь отложенных запросов
 * @param error - Ошибка, если обновление токена не удалось
 */
const processQueue = (error: AxiosError | null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(null);
    }
  });
  failedQueue = [];
};

/**
 * Обновляет access token через refresh token
 * @throws {Error} Когда обновление не удалось
 */
const refreshAccessToken = async (): Promise<void> => {
  const response = await api.post(
    '/api/auth/refresh',
    {},
    {
      withCredentials: true,
    }
  );
  
  if (response.status !== 200) {
    throw new Error('Refresh failed');
  }
};

// Настройка интерцептора для автоматического обновления токенов
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (originalRequest?.url?.includes('/api/auth/refresh')) {
      return Promise.reject(error);
    }

    if (originalRequest?.url?.includes('/api/auth/login') || 
        originalRequest?.url?.includes('/api/auth/register')) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => api(originalRequest))
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        await refreshAccessToken();
        processQueue(null);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError);
        window.dispatchEvent(new CustomEvent('auth:expired'));
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;