const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';
const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3000';

describe('Authentication Integration Tests', () => {
  let testUser;
  let authCookies;

  beforeAll(async () => {
    try {
      await axios.get(`${API_BASE_URL}/actuator/health`);
    } catch (error) {
    }
  });

  beforeEach(() => {
    testUser = {
      email: `test${Date.now()}@example.com`,
      password: 'TestPassword123!'
    };
    authCookies = null;
  });

  describe('POST /api/auth/register', () => {
    it('должен успешно регистрировать нового пользователя', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('user');
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');
      expect(response.data.user.email).toBe(testUser.email);
      expect(response.data.user).toHaveProperty('id');

      const cookies = response.headers['set-cookie'];
      expect(cookies).toBeDefined();
      expect(cookies.some(cookie => cookie.includes('accessToken'))).toBe(true);
      expect(cookies.some(cookie => cookie.includes('refreshToken'))).toBe(true);
    });

    it('должен возвращать ошибку при регистрации с существующим email', async () => {
      await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
      expect(response.data).toHaveProperty('message');
      expect(response.data.message).toContain('уже существует');
    });

    it('должен обрабатывать пустые поля', async () => {
      const emptyUser = {
        email: '',
        password: ''
      };

      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, emptyUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
    });
  });

  describe('POST /api/auth/login', () => {
    beforeEach(async () => {
      await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });
    });

    it('должен успешно выполнять вход с корректными данными', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('user');
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');
      expect(response.data.user.email).toBe(testUser.email);

      authCookies = response.headers['set-cookie'];
      expect(authCookies).toBeDefined();
    });

    it('должен возвращать ошибку при неверном пароле', async () => {
      const wrongPasswordUser = {
        email: testUser.email,
        password: 'WrongPassword123!'
      };

      const response = await axios.post(`${API_BASE_URL}/api/auth/login`, wrongPasswordUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
      expect(response.data).toHaveProperty('message');
      expect(response.data.message).toContain('Неверный email или пароль');
    });

    it('должен возвращать ошибку при несуществующем email', async () => {
      const nonExistentUser = {
        email: 'nonexistent@example.com',
        password: 'TestPassword123!'
      };

      const response = await axios.post(`${API_BASE_URL}/api/auth/login`, nonExistentUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
      expect(response.data).toHaveProperty('message');
      expect(response.data.message).toContain('Неверный email или пароль');
    });

    it('должен обрабатывать пустые поля при входе', async () => {
      const emptyLogin = {
        email: '',
        password: ''
      };

      const response = await axios.post(`${API_BASE_URL}/api/auth/login`, emptyLogin, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
    });
  });

  describe('GET /api/auth/me', () => {
    beforeEach(async () => {
      await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      authCookies = loginResponse.headers['set-cookie'];
    });

    it('должен возвращать информацию о текущем пользователе', async () => {
      const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('id');
      expect(response.data).toHaveProperty('email');
      expect(response.data.email).toBe(testUser.email);
    });

    it('должен возвращать 401 без аутентификации', async () => {
      const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(401);
    });

    it('должен возвращать 401 с невалидным токеном', async () => {
      const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: 'accessToken=invalid-token'
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(401);
    });
  });

  describe('POST /api/auth/refresh', () => {
    let refreshToken;

    beforeEach(async () => {
      await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      authCookies = loginResponse.headers['set-cookie'];
      refreshToken = loginResponse.data.refreshToken;
    });

    it('должен обновлять токены с валидным refresh токеном', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/refresh`, {
        refreshToken: refreshToken
      }, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('user');
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');
      expect(response.data.user.email).toBe(testUser.email);

      expect(response.data.refreshToken).not.toBe(refreshToken);
    });

    it('должен возвращать ошибку с невалидным refresh токеном', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/refresh`, {
        refreshToken: 'invalid-refresh-token'
      }, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
      expect(response.data).toHaveProperty('message');
    });

    it('должен возвращать ошибку без refresh токена', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/refresh`, {}, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(400);
    });
  });

  describe('POST /api/auth/logout', () => {
    beforeEach(async () => {
      await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      authCookies = loginResponse.headers['set-cookie'];
    });

    it('должен успешно выполнять выход', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(200);

      const cookies = response.headers['set-cookie'];
      expect(cookies).toBeDefined();
      expect(cookies.some(cookie => cookie.includes('accessToken=;'))).toBe(true);
      expect(cookies.some(cookie => cookie.includes('refreshToken=;'))).toBe(true);
    });

    it('должен обрабатывать выход без аутентификации', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect([200, 401]).toContain(response.status);
    });

    it('должен делать токены недействительными после выхода', async () => {
      await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(401);
    });
  });

  describe('Полный цикл аутентификации', () => {
    it('должен обрабатывать полный цикл: регистрация -> вход -> получение профиля -> выход', async () => {
      const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });
      expect(registerResponse.status).toBe(200);

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });
      expect(loginResponse.status).toBe(200);
      authCookies = loginResponse.headers['set-cookie'];

      const profileResponse = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });
      expect(profileResponse.status).toBe(200);
      expect(profileResponse.data.email).toBe(testUser.email);

      const logoutResponse = await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });
      expect(logoutResponse.status).toBe(200);

      const finalCheckResponse = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });
      expect(finalCheckResponse.status).toBe(401);
    });

    it('должен обрабатывать обновление токенов в процессе работы', async () => {
      await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const refreshToken = loginResponse.data.refreshToken;
      authCookies = loginResponse.headers['set-cookie'];

      const refreshResponse = await axios.post(`${API_BASE_URL}/api/auth/refresh`, {
        refreshToken: refreshToken
      }, {
        headers: {
          Cookie: authCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(refreshResponse.status).toBe(200);

      const newCookies = refreshResponse.headers['set-cookie'];
      const profileResponse = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: newCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(profileResponse.status).toBe(200);
      expect(profileResponse.data.email).toBe(testUser.email);
    });
  });

  describe('Безопасность', () => {
    it('должен устанавливать безопасные HTTP-only cookies', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const cookies = response.headers['set-cookie'];
      expect(cookies).toBeDefined();

      const accessTokenCookie = cookies.find(cookie => cookie.includes('accessToken'));
      const refreshTokenCookie = cookies.find(cookie => cookie.includes('refreshToken'));

      expect(accessTokenCookie).toContain('HttpOnly');
      expect(refreshTokenCookie).toContain('HttpOnly');
      expect(accessTokenCookie).toContain('SameSite=Strict');
      expect(refreshTokenCookie).toContain('SameSite=Strict');
    });

    it('должен обрабатывать CORS правильно', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        headers: {
          'Origin': FRONTEND_URL
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(200);
    });

    it('должен защищать от SQL инъекций в email', async () => {
      const maliciousUser = {
        email: "test'; DROP TABLE users; --@example.com",
        password: 'TestPassword123!'
      };

      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, maliciousUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect([400, 500]).toContain(response.status);
    });
  });
});