/** Базовый URL API сервера */
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

/** Флаг процесса обновления токена */
let isRefreshing = false;

/**
 * Тип для отложенного запроса в очереди
 */
type QueuedRequest = {
  resolve: (value: Response) => void;
  reject: (reason?: unknown) => void;
  url: string;
  init?: RequestInit;
};

/** Очередь отложенных запросов на время обновления токена */
let failedQueue: QueuedRequest[] = [];

/**
 * Обрабатывает очередь отложенных запросов
 * @param error - Ошибка, если обновление токена не удалось
 */
const processQueue = (error: Error | null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(null as unknown as Response);
    }
  });
  failedQueue = [];
};

/**
 * Обновляет access token через refresh token с помощью fetch API
 * @throws {Error} Когда обновление не удалось
 */
const refreshAccessToken = async (): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  if (!response.ok) {
    throw new Error('Refresh failed');
  }
};

/**
 * Выполняет HTTP-запрос с автоматическим обновлением токенов
 * @param url - URL для запроса
 * @param init - Настройки запроса (RequestInit)
 * @returns Promise с Response объектом
 * @throws {Error} При ошибках авторизации или обновления токена
 */
export const fetchWithAuth = async (
  url: string,
  init?: RequestInit
): Promise<Response> => {
  const makeRequest = async (): Promise<Response> => {
    const response = await fetch(url, {
      ...init,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers || {}),
      },
    });

    return response;
  };

  let response = await makeRequest();

  if (response.status === 401) {
    if (url.includes('/api/auth/refresh')) {
      return response;
    }

    if (url.includes('/api/auth/login') || url.includes('/api/auth/register')) {
      return response;
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, url, init });
      }).then(() => fetchWithAuth(url, init));
    }

    isRefreshing = true;

    try {
      await refreshAccessToken();
      processQueue(null);
      response = await makeRequest();
      return response;
    } catch (refreshError) {
      processQueue(refreshError as Error);
      window.dispatchEvent(new CustomEvent('auth:expired'));
      throw refreshError;
    } finally {
      isRefreshing = false;
    }
  }

  return response;
};