import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { authService, UserInfo } from '../services/authService';

/**
 * Тип контекста авторизации
 * Определяет все доступные методы и свойства для управления авторизацией
 */
interface AuthContextType {
  /** Информация о текущем пользователе (или null, если не авторизован) */
  user: UserInfo | null;
  /** Флаг загрузки (проверка авторизации при запуске) */
  isLoading: boolean;
  /** Флаг авторизации (вычисляется на основе наличия user) */
  isAuthenticated: boolean;
  /** Метод для входа в систему */
  login: (email: string, password: string) => Promise<UserInfo>;
  /** Метод для регистрации нового пользователя */
  register: (email: string, password: string) => Promise<UserInfo>;
  /** Метод для выхода из системы */
  logout: () => Promise<void>;
  /** Метод для обновления информации о пользователе */
  refreshUser: () => Promise<void>;
}

/**
 * Контекст для управления состоянием авторизации
 * Предоставляет глобальный доступ к состоянию авторизации во всем приложении
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/** Пропсы для компонента AuthProvider */
interface AuthProviderProps {
  /** Дочерние компоненты, которые будут иметь доступ к контексту */
  children: ReactNode;
}

/**
 * Провайдер контекста авторизации
 * Обеспечивает управление состоянием авторизации, автоматическую проверку
 * при запуске и обработку событий истечения токенов
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент провайдера
 */
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * Обновляет информацию о текущем пользователе
   * Используется для синхронизации данных пользователя с сервером
   */
  const refreshUser = useCallback(async (): Promise<void> => {
    try {
      const userInfo = await authService.getCurrentUser();
      setUser(userInfo);
    } catch {
      setUser(null);
    }
  }, []);

  // Проверка авторизации при запуске приложения
  useEffect(() => {
    const checkAuth = async () => {
      setIsLoading(true);
      try {
        const userInfo = await authService.checkAuth();
        setUser(userInfo);
      } catch (error) {
        console.error('Auth check failed:', error);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  // Обработка события истечения токена
  useEffect(() => {
    const handleAuthExpired = () => {
      console.log('Auth expired event received, clearing user');
      setUser(null);
    };

    window.addEventListener('auth:expired', handleAuthExpired);
    return () => {
      window.removeEventListener('auth:expired', handleAuthExpired);
    };
  }, []);

  /**
   * Выполняет вход пользователя в систему
   * @param email - Электронная почта
   * @param password - Пароль
   * @returns Информация о пользователе
   */
  const login = useCallback(async (email: string, password: string): Promise<UserInfo> => {
    const response = await authService.login({ email, password });
    setUser(response.user);
    return response.user;
  }, []);

  /**
   * Выполняет регистрацию нового пользователя
   * @param email - Электронная почта
   * @param password - Пароль
   * @returns Информация о пользователе
   */
  const register = useCallback(async (email: string, password: string): Promise<UserInfo> => {
    const response = await authService.register(email, password);
    setUser(response.user);
    return response.user;
  }, []);

  /**
   * Выполняет выход пользователя из системы
   * Очищает состояние пользователя независимо от результата запроса к серверу
   */
  const logout = useCallback(async (): Promise<void> => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
    }
  }, []);

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Хук для использования контекста авторизации
 * Предоставляет доступ к состоянию авторизации и методам управления
 * 
 * @returns Объект контекста с данными о пользователе и методами авторизации
 * @throws {Error} Когда хук используется вне AuthProvider
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};