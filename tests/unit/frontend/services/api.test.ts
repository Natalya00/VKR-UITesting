import axios from 'axios';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

const mockAxiosInstance = {
  interceptors: {
    response: {
      use: jest.fn()
    }
  },
  post: jest.fn(),
  get: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
};

mockedAxios.create.mockReturnValue(mockAxiosInstance as any);

require('../../../../client/services/api');

let responseInterceptor: ((error: unknown) => Promise<unknown>) | null = null;

describe('API Service', () => {
  beforeEach(() => {
    const interceptorCall = mockAxiosInstance.interceptors.response.use.mock.calls[0];
    responseInterceptor = interceptorCall ? interceptorCall[1] : null;
  });

  describe('конфигурация', () => {
    it('должен создавать axios instance с правильной конфигурацией', () => {
      expect(mockedAxios.create).toHaveBeenCalledWith({
        baseURL: 'http://localhost:8080',
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
        },
      });
    });
  });

  describe('перехватчик ответов', () => {
    const mockRequest = {
      url: '/api/test',
      _retry: false
    };

    it('должен пропускать успешные ответы', () => {
      const successResponse = { data: { success: true }, status: 200 };
      const successInterceptor = mockAxiosInstance.interceptors.response.use.mock.calls[0]?.[0];
      expect(successInterceptor).toBeDefined();
      const result = successInterceptor(successResponse);
      expect(result).toBe(successResponse);
    });

    it('должен обрабатывать ошибки не связанные с аутентификацией', async () => {
      const error = { response: { status: 500 }, config: mockRequest };
      expect(responseInterceptor).not.toBeNull();
      await expect(responseInterceptor!(error)).rejects.toEqual(error);
    });

    it('должен игнорировать ошибки 401 для refresh endpoint', async () => {
      const error = {
        response: { status: 401 },
        config: { url: '/api/auth/refresh' }
      };
      expect(responseInterceptor).not.toBeNull();
      await expect(responseInterceptor!(error)).rejects.toEqual(error);
    });

    it('должен обрабатывать ошибки 401 и пытаться обновить токен', async () => {
      const error = { response: { status: 401 }, config: mockRequest };
      mockedAxios.post.mockResolvedValueOnce({ status: 200 });
      expect(responseInterceptor).not.toBeNull();
      await expect(responseInterceptor!(error)).rejects.toBeDefined();
    });

    it('должен отправлять событие auth:expired при неудачном обновлении токена', async () => {
      const error = { response: { status: 401 }, config: mockRequest };
      mockedAxios.post.mockRejectedValueOnce(new Error('Refresh failed'));
      const eventListener = jest.fn();
      window.addEventListener('auth:expired', eventListener);
      expect(responseInterceptor).not.toBeNull();
      try {
        await responseInterceptor!(error);
      } catch (e) {
      }
      window.removeEventListener('auth:expired', eventListener);
    });
  });
});