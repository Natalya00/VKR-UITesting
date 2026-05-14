import api from './api';

/** Данные для регистрации */
export interface RegisterData {
  email: string;
  password: string;
}

/** Данные для входа */
export interface LoginData {
  email: string;
  password: string;
}

/** Информация о пользователе */
export interface UserInfo {
  id: number;
  email: string;
}

/** Ответ сервера при авторизации */
export interface AuthResponse {
  user: UserInfo;
  accessToken: string;
  refreshToken: string;
}

/**
 * Сервис для работы с авторизацией
 * Обеспечивает регистрацию, вход, выход и сброс пароля
 */
export const authService = {
  /**
   * Регистрирует нового пользователя
   * @param email - Электронная почта
   * @param password - Пароль
   * @returns Данные авторизации
   */
  async register(email: string, password: string): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/register', { email, password });
    return response.data;
  },

  /**
   * Выполняет вход пользователя
   * @param data - Данные для входа
   * @returns Данные авторизации
   */
  async login(data: LoginData): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/login', data);
    return response.data;
  },

  /**
   * Выполняет выход пользователя
   */
  async logout(): Promise<void> {
    await api.post('/api/auth/logout');
  },

  /**
   * Отправляет запрос на сброс пароля
   * @param email - Электронная почта
   * @returns Токен и сообщение
   */
  async forgotPassword(email: string): Promise<{ token: string; message: string }> {
    const response = await api.post<{ token: string; message: string }>('/api/auth/forgot-password', { email });
    return response.data;
  },

  /**
   * Сбрасывает пароль по токену
   * @param token - Токен сброса
   * @param password - Новый пароль
   */
  async resetPassword(token: string, password: string): Promise<void> {
    await api.post('/api/auth/reset-password', { token, password, confirmPassword: password });
  },

  /**
   * Получает информацию о текущем пользователе
   * @returns Информация о пользователе
   */
  async getCurrentUser(): Promise<UserInfo> {
    const response = await api.get<UserInfo>('/api/auth/me');
    return response.data;
  },

  /**
   * Проверяет статус авторизации
   * @returns Информация о пользователе или null
   */
  async checkAuth(): Promise<UserInfo | null> {
    try {
      const response = await api.get<UserInfo>('/api/auth/me');
      return response.data;
    } catch {
      return null;
    }
  },
};
