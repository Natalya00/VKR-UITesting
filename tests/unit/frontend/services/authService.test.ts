import { authService, LoginData, RegisterData } from '../../../../client/services/authService';
import api from '../../../../client/services/api';

jest.mock('../../../../client/services/api');
const mockedApi = api as jest.Mocked<typeof api>;

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('register', () => {
    it('должен успешно регистрировать пользователя', async () => {
      const email = 'test@example.com';
      const password = 'password123';
      const mockResponse = {
        data: {
          user: { id: 1, email },
          accessToken: 'access-token',
          refreshToken: 'refresh-token'
        }
      };

      mockedApi.post.mockResolvedValue(mockResponse);

      const result = await authService.register(email, password);

      expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/register', { email, password });
      expect(result).toEqual(mockResponse.data);
    });

    it('должен обрабатывать ошибки регистрации', async () => {
      const email = 'test@example.com';
      const password = 'password123';
      const error = new Error('Email already exists');

      mockedApi.post.mockRejectedValue(error);

      await expect(authService.register(email, password)).rejects.toThrow('Email already exists');
      expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/register', { email, password });
    });
  });

  describe('login', () => {
    it('должен успешно выполнять вход', async () => {
      const loginData: LoginData = {
        email: 'test@example.com',
        password: 'password123'
      };
      const mockResponse = {
        data: {
          user: { id: 1, email: loginData.email },
          accessToken: 'access-token',
          refreshToken: 'refresh-token'
        }
      };

      mockedApi.post.mockResolvedValue(mockResponse);

      const result = await authService.login(loginData);

      expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/login', loginData);
      expect(result).toEqual(mockResponse.data);
    });

    it('должен обрабатывать ошибки входа', async () => {
      const loginData: LoginData = {
        email: 'test@example.com',
        password: 'wrong-password'
      };
      const error = new Error('Invalid credentials');

      mockedApi.post.mockRejectedValue(error);

      await expect(authService.login(loginData)).rejects.toThrow('Invalid credentials');
      expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/login', loginData);
    });

    it('должен обрабатывать различные типы ошибок входа', async () => {
      const loginData: LoginData = {
        email: 'test@example.com',
        password: 'password123'
      };

      const unauthorizedError = {
        response: { status: 401, data: { message: 'Unauthorized' } },
        message: 'Request failed with status code 401'
      };
      mockedApi.post.mockRejectedValue(unauthorizedError);

      await expect(authService.login(loginData)).rejects.toEqual(unauthorizedError);

      const serverError = {
        response: { status: 500, data: { message: 'Internal Server Error' } },
        message: 'Request failed with status code 500'
      };
      mockedApi.post.mockRejectedValue(serverError);

      await expect(authService.login(loginData)).rejects.toEqual(serverError);
    });
  });

  describe('logout', () => {
    it('должен успешно выполнять выход', async () => {
      mockedApi.post.mockResolvedValue({ data: null });

      await authService.logout();

      expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/logout');
    });

    it('должен обрабатывать ошибки выхода', async () => {
      const error = new Error('Network error');
      mockedApi.post.mockRejectedValue(error);

      await expect(authService.logout()).rejects.toThrow('Network error');
      expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/logout');
    });
  });

  describe('getCurrentUser', () => {
    it('должен успешно получать текущего пользователя', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      const mockResponse = { data: mockUser };

      mockedApi.get.mockResolvedValue(mockResponse);

      const result = await authService.getCurrentUser();

      expect(mockedApi.get).toHaveBeenCalledWith('/api/auth/me');
      expect(result).toEqual(mockUser);
    });

    it('должен обрабатывать ошибки получения пользователя', async () => {
      const error = new Error('Unauthorized');
      mockedApi.get.mockRejectedValue(error);

      await expect(authService.getCurrentUser()).rejects.toThrow('Unauthorized');
      expect(mockedApi.get).toHaveBeenCalledWith('/api/auth/me');
    });
  });

  describe('checkAuth', () => {
    it('должен возвращать пользователя при успешной проверке', async () => {
      const mockUser = { id: 1, email: 'test@example.com' };
      const mockResponse = { data: mockUser };

      mockedApi.get.mockResolvedValue(mockResponse);

      const result = await authService.checkAuth();

      expect(mockedApi.get).toHaveBeenCalledWith('/api/auth/me');
      expect(result).toEqual(mockUser);
    });

    it('должен возвращать null при ошибке проверки', async () => {
      const error = new Error('Unauthorized');
      mockedApi.get.mockRejectedValue(error);

      const result = await authService.checkAuth();

      expect(mockedApi.get).toHaveBeenCalledWith('/api/auth/me');
      expect(result).toBeNull();
    });

    it('должен возвращать null при различных типах ошибок', async () => {
      mockedApi.get.mockRejectedValue({ response: { status: 401 } });
      let result = await authService.checkAuth();
      expect(result).toBeNull();

      mockedApi.get.mockRejectedValue(new Error('Network Error'));
      result = await authService.checkAuth();
      expect(result).toBeNull();

      mockedApi.get.mockRejectedValue({ response: { status: 500 } });
      result = await authService.checkAuth();
      expect(result).toBeNull();
    });
  });

  describe('интеграционные сценарии', () => {
    it('должен корректно обрабатывать полный цикл аутентификации', async () => {
      const email = 'test@example.com';
      const password = 'password123';
      const mockUser = { id: 1, email };
      const mockAuthResponse = {
        data: {
          user: mockUser,
          accessToken: 'access-token',
          refreshToken: 'refresh-token'
        }
      };

      mockedApi.post.mockResolvedValueOnce(mockAuthResponse);
      const registerResult = await authService.register(email, password);
      expect(registerResult).toEqual(mockAuthResponse.data);

      mockedApi.get.mockResolvedValueOnce({ data: mockUser });
      const checkResult = await authService.checkAuth();
      expect(checkResult).toEqual(mockUser);

      mockedApi.get.mockResolvedValueOnce({ data: mockUser });
      const currentUserResult = await authService.getCurrentUser();
      expect(currentUserResult).toEqual(mockUser);

      mockedApi.post.mockResolvedValueOnce({ data: null });
      await expect(authService.logout()).resolves.not.toThrow();

      expect(mockedApi.post).toHaveBeenCalledTimes(2); 
      expect(mockedApi.get).toHaveBeenCalledTimes(2); 
    });
  });
}); 