import React from 'react';
import { renderHook, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../../../../client/context/AuthContext';
import { authService } from '../../../../client/services/authService';

jest.mock('../../../../client/services/authService');
const mockedAuthService = authService as jest.Mocked<typeof authService>;

const createWrapper = () => {
  return ({ children }: { children: React.ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
  );
};

describe('useAuth', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    mockedAuthService.checkAuth.mockResolvedValue(null);
  });

  describe('инициализация', () => {
    it('должен начинать с состояния загрузки', () => {
      mockedAuthService.checkAuth.mockImplementation(() => new Promise(() => {}));
      
      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      expect(result.current.isLoading).toBe(true);
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBe(null);
    });

    it('должен проверять аутентификацию при инициализации', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      mockedAuthService.checkAuth.mockResolvedValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUser);
      expect(mockedAuthService.checkAuth).toHaveBeenCalledTimes(1);
    });

    it('должен обрабатывать ошибку при проверке аутентификации', async () => {
      mockedAuthService.checkAuth.mockRejectedValue(new Error('Network error'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBe(null);
    });
  });

  describe('login', () => {
    it('должен успешно выполнять вход', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      const mockResponse = {
        user: mockUser,
        accessToken: 'access-token',
        refreshToken: 'refresh-token'
      };
      
      mockedAuthService.checkAuth.mockResolvedValue(null); 
      mockedAuthService.login.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      let loginResult: any;
      await act(async () => {
        loginResult = await result.current.login('test@example.com', 'password');
      });

      expect(loginResult).toEqual(mockUser);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUser);
      expect(mockedAuthService.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password'
      });
    });

    it('должен обрабатывать ошибки входа', async () => {
      mockedAuthService.checkAuth.mockResolvedValue(null);
      mockedAuthService.login.mockRejectedValue(new Error('Invalid credentials'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await expect(
        act(async () => {
          await result.current.login('test@example.com', 'wrong-password');
        })
      ).rejects.toThrow('Invalid credentials');

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBe(null);
    });
  });

  describe('register', () => {
    it('должен успешно выполнять регистрацию', async () => {
      const mockUser = { id: 1, email: 'new@example.com' };
      const mockResponse = {
        user: mockUser,
        accessToken: 'access-token',
        refreshToken: 'refresh-token'
      };
      
      mockedAuthService.checkAuth.mockResolvedValue(null);
      mockedAuthService.register.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      let registerResult: any;
      await act(async () => {
        registerResult = await result.current.register('new@example.com', 'password');
      });

      expect(registerResult).toEqual(mockUser);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUser);
      expect(mockedAuthService.register).toHaveBeenCalledWith('new@example.com', 'password');
    });

    it('должен обрабатывать ошибки регистрации', async () => {
      mockedAuthService.checkAuth.mockResolvedValue(null);
      mockedAuthService.register.mockRejectedValue(new Error('Email already exists'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await expect(
        act(async () => {
          await result.current.register('existing@example.com', 'password');
        })
      ).rejects.toThrow('Email already exists');

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBe(null);
    });
  });

  describe('logout', () => {
    it('должен успешно выполнять выход', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      
      mockedAuthService.checkAuth.mockResolvedValue(mockUser);
      mockedAuthService.logout.mockResolvedValue();

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(mockedAuthService.logout).toHaveBeenCalledTimes(1);
    });

    it('должен обрабатывать ошибки выхода', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      
      mockedAuthService.checkAuth.mockResolvedValue(mockUser);
      mockedAuthService.logout.mockRejectedValue(new Error('Network error'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.user).toBe(null);
    });
  });

  describe('refreshUser', () => {
    it('должен обновлять данные пользователя', async () => {
      const initialUser = { id: 1, email: 'test@example.com' };
      const updatedUser = { id: 1, email: 'updated@example.com' };
      
      mockedAuthService.checkAuth.mockResolvedValue(initialUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(initialUser);
      });

      mockedAuthService.getCurrentUser.mockResolvedValue(updatedUser);

      await act(async () => {
        await result.current.refreshUser();
      });

      expect(result.current.user).toEqual(updatedUser);
      expect(mockedAuthService.getCurrentUser).toHaveBeenCalledTimes(1);
    });

    it('должен обрабатывать ошибки при обновлении пользователя', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      
      mockedAuthService.checkAuth.mockResolvedValue(mockUser);
      mockedAuthService.getCurrentUser.mockRejectedValue(new Error('Unauthorized'));

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      await act(async () => {
        await result.current.refreshUser();
      });

      expect(result.current.user).toBe(null);
    });
  });

  describe('событие auth:expired', () => {
    it('должен сбрасывать пользователя при получении события auth:expired', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      mockedAuthService.checkAuth.mockResolvedValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      act(() => {
        window.dispatchEvent(new CustomEvent('auth:expired'));
      });

      expect(result.current.user).toBe(null);
      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe('ошибки использования', () => {
    it('должен выбрасывать ошибку при использовании вне AuthProvider', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      
      expect(() => {
        renderHook(() => useAuth());
      }).toThrow('useAuth must be used within an AuthProvider');
      
      consoleSpy.mockRestore();
    });
  });
});