const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

describe('Database Integration Tests', () => {
  let testUsers = [];
  let authCookies = [];

  beforeAll(async () => {
    try {
      await axios.get(`${API_BASE_URL}/actuator/health`);
    } catch (error) {
      console.warn('Health check failed, proceeding anyway:', error.message);
    }
  });

  afterAll(async () => {
  });

  describe('User Repository Integration', () => {
    it('должен создавать и находить пользователя', async () => {
      const testUser = {
        email: `dbtest${Date.now()}@example.com`,
        password: 'TestPassword123!'
      };

      const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(registerResponse.status).toBe(200);
      expect(registerResponse.data.user).toHaveProperty('id');
      expect(registerResponse.data.user.email).toBe(testUser.email);

      testUsers.push(registerResponse.data.user);

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(loginResponse.status).toBe(200);
      expect(loginResponse.data.user.id).toBe(registerResponse.data.user.id);
      expect(loginResponse.data.user.email).toBe(testUser.email);
    });

    it('должен предотвращать дублирование email', async () => {
      const testUser = {
        email: `duplicate${Date.now()}@example.com`,
        password: 'TestPassword123!'
      };

      const firstResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(firstResponse.status).toBe(200);

      const secondResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(secondResponse.status).toBe(400);
      expect(secondResponse.data.message).toContain('уже существует');
    });

    it('должен обрабатывать множественных пользователей', async () => {
    const users = [];
    const userCount = 5;

    for (let i = 0; i < userCount; i++) {
      const testUser = {
        email: `multiuser${Date.now()}_${i}@example.com`,
        password: 'TestPassword123!'
      };

      const response = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: false, 
        validateStatus: () => true
      });

      expect(response.status).toBe(200);
      users.push({
        credentials: testUser,
        userData: response.data.user
      });
    }

    for (const user of users) {
      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, user.credentials, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(loginResponse.status).toBe(200);
      expect(loginResponse.data.user.id).toBe(user.userData.id);
    }
  });
  });

  describe('UserProgress Repository Integration', () => {
    let testUser;
    let userAuthCookies;

    beforeEach(async () => {
      testUser = {
        email: `progress${Date.now()}@example.com`,
        password: 'TestPassword123!'
      };

      const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      userAuthCookies = loginResponse.headers['set-cookie'];
      testUsers.push(registerResponse.data.user);
    });

    it('должен сохранять и получать прогресс упражнений', async () => {
      const codeRequest = {
        code: 'System.out.println("Progress test");',
        exercise: 1,
        exerciseId: 'db-test-exercise-1',
        moduleId: 'module-2'
      };

      const executeResponse = await axios.post(`${API_BASE_URL}/api/code/run`, codeRequest, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(executeResponse.status).toBe(200);

      if (executeResponse.data.success) {
        const markResponse = await axios.post(
          `${API_BASE_URL}/api/progress/module-2/db-test-exercise-1/complete`,
          {
            codeSnapshot: codeRequest.code,
            isSuccess: true,
            errorMessage: null
          },
          {
            headers: {
              Cookie: userAuthCookies?.join('; ')
            },
            withCredentials: true,
            validateStatus: () => true
          }
        );
        expect(markResponse.status).toBe(200);
      }

      const progressResponse = await axios.get(`${API_BASE_URL}/api/progress`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(progressResponse.status).toBe(200);
      expect(progressResponse.data).toHaveProperty('userId');
      expect(progressResponse.data).toHaveProperty('modules');

      if (executeResponse.data.success) {
        expect(progressResponse.data.totalCompleted).toBeGreaterThan(0);
      }
    });

    it('должен обновлять существующий прогресс', async () => {
      const codeRequest = {
        code: 'System.out.println("Update test");',
        exercise: 1,
        exerciseId: 'db-test-exercise-update',
        moduleId: 'module-2'
      };

      const requestConfig = {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      };

      const markData = {
        codeSnapshot: codeRequest.code,
        isSuccess: true,
        errorMessage: null
      };

      await axios.post(`${API_BASE_URL}/api/code/run`, codeRequest, requestConfig);

      await axios.post(
        `${API_BASE_URL}/api/progress/module-2/db-test-exercise-update/complete`,
        markData,
        requestConfig
      );

      await axios.post(`${API_BASE_URL}/api/code/run`, codeRequest, requestConfig);

      await axios.post(
        `${API_BASE_URL}/api/progress/module-2/db-test-exercise-update/complete`,
        markData,
        requestConfig
      );

      const progressResponse = await axios.get(`${API_BASE_URL}/api/progress/module/module-2`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(progressResponse.status).toBe(200);

      const exercise = progressResponse.data.exercises.find(
        e => e.exerciseId === 'db-test-exercise-update'
      );

      expect(exercise).toBeDefined();
      expect(exercise.attemptsCount).toBeGreaterThan(1);
    });

    it('должен изолировать прогресс разных пользователей', async () => {
      const secondUser = {
        email: `progress2${Date.now()}@example.com`,
        password: 'TestPassword123!'
      };

      await axios.post(`${API_BASE_URL}/api/auth/register`, secondUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const secondLoginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, secondUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const secondUserCookies = secondLoginResponse.headers['set-cookie'];

      const codeRequest1 = {
        code: 'System.out.println("User 1");',
        exercise: 1,
        exerciseId: 'db-test-isolation-1',
        moduleId: 'module-2'
      };

      await axios.post(`${API_BASE_URL}/api/code/run`, codeRequest1, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      await axios.post(
        `${API_BASE_URL}/api/progress/module-2/db-test-isolation-1/complete`,
        {
          codeSnapshot: codeRequest1.code,
          isSuccess: true,
          errorMessage: null
        },
        {
          headers: {
            Cookie: userAuthCookies?.join('; ')
          },
          withCredentials: true,
          validateStatus: () => true
        }
      );

      const codeRequest2 = {
        code: 'System.out.println("User 2");',
        exercise: 2,
        exerciseId: 'db-test-isolation-2',
        moduleId: 'module-2'
      };

      await axios.post(`${API_BASE_URL}/api/code/run`, codeRequest2, {
        headers: {
          Cookie: secondUserCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      await axios.post(
        `${API_BASE_URL}/api/progress/module-2/db-test-isolation-2/complete`,
        {
          codeSnapshot: codeRequest2.code,
          isSuccess: true,
          errorMessage: null
        },
        {
          headers: {
            Cookie: secondUserCookies?.join('; ')
          },
          withCredentials: true,
          validateStatus: () => true
        }
      );

      const progress1Response = await axios.get(`${API_BASE_URL}/api/progress`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      const progress2Response = await axios.get(`${API_BASE_URL}/api/progress`, {
        headers: {
          Cookie: secondUserCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(progress1Response.status).toBe(200);
      expect(progress2Response.status).toBe(200);

      expect(progress1Response.data.userId).not.toBe(progress2Response.data.userId);

      expect(progress1Response.data.totalCompleted).toBeGreaterThan(0);
      expect(progress2Response.data.totalCompleted).toBeGreaterThan(0);

      const module1 = progress1Response.data.modules.find(m => m.moduleId === 'module-2');
      const module2 = progress2Response.data.modules.find(m => m.moduleId === 'module-2');

      const exercise1 = module1?.exercises.find(e => e.exerciseId === 'db-test-isolation-1');
      const exercise2 = module2?.exercises.find(e => e.exerciseId === 'db-test-isolation-2');

      expect(exercise1).toBeDefined();
      expect(exercise2).toBeDefined();
      expect(exercise1.exerciseId).not.toBe(exercise2.exerciseId);
    });
  });

  describe('RevokedToken Repository Integration', () => {
    let testUser;
    let userAuthCookies;

    beforeEach(async () => {
      testUser = {
        email: `tokens${Date.now()}@example.com`,
        password: 'TestPassword123!'
      };

      const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      userAuthCookies = loginResponse.headers['set-cookie'];
      testUsers.push(registerResponse.data.user);
    });

    it('должен добавлять токены в черный список при выходе', async () => {
      const beforeLogoutResponse = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(beforeLogoutResponse.status).toBe(200);

      const logoutResponse = await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(logoutResponse.status).toBe(200);

      const afterLogoutResponse = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(afterLogoutResponse.status).toBe(401);
    });

    it('должен предотвращать повторное использование отозванных refresh токенов', async () => {
    const refreshTokenCookie = userAuthCookies?.find(c => c.includes('refreshToken'));
    const refreshToken = refreshTokenCookie?.split(';')[0]?.split('=')[1];

    await axios.post(`${API_BASE_URL}/api/auth/logout`, {}, {
      headers: { Cookie: userAuthCookies?.join('; ') },
      withCredentials: true
    });

    const refreshResponse = await axios.post(`${API_BASE_URL}/api/auth/refresh`, {}, {
      headers: { Cookie: `refreshToken=${refreshToken}` }, 
      withCredentials: true,
      validateStatus: () => true
    });

    expect(refreshResponse.status).toBe(400);
    expect(refreshResponse.data.message).toContain('отозван');
  });
  });

  describe('Транзакционность и целостность данных', () => {
    let testUser;
    let userAuthCookies;

    beforeEach(async () => {
      testUser = {
        email: `integrity${Date.now()}@example.com`,
        password: 'TestPassword123!'
      };

      const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
        withCredentials: true,
        validateStatus: () => true
      });

      userAuthCookies = loginResponse.headers['set-cookie'];
      testUsers.push(registerResponse.data.user);
    });

    it('должен корректно обрабатывать связи между таблицами', async () => {
      const codeRequest = {
        code: 'System.out.println("Relations test");',
        exercise: 1,
        exerciseId: 'db-test-relations',
        moduleId: 'module-2'
      };

      const executeResponse = await axios.post(`${API_BASE_URL}/api/code/run`, codeRequest, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(executeResponse.status).toBe(200);

      if (executeResponse.data.success) {
        await axios.post(
          `${API_BASE_URL}/api/progress/module-2/db-test-relations/complete`,
          {
            codeSnapshot: codeRequest.code,
            isSuccess: true,
            errorMessage: null
          },
          {
            headers: {
              Cookie: userAuthCookies?.join('; ')
            },
            withCredentials: true,
            validateStatus: () => true
          }
        );
      }

      const progressResponse = await axios.get(`${API_BASE_URL}/api/progress`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(progressResponse.status).toBe(200);
      expect(progressResponse.data).toHaveProperty('userId');

      if (executeResponse.data.success) {
        expect(progressResponse.data.totalCompleted).toBeGreaterThan(0);
      }

      const userResponse = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: {
          Cookie: userAuthCookies?.join('; ')
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect(userResponse.data.id).toBe(progressResponse.data.userId);
    });
  });
});
