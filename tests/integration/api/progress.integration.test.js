const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

describe('Progress Integration Tests', () => {
  let testUser;
  let authCookies;
  let userId;

  beforeAll(async () => {
    try {
      await axios.get(`${API_BASE_URL}/actuator/health`);
    } catch (error) {
      console.warn('Health check failed, proceeding anyway:', error.message);
    }
  });

  beforeEach(async () => {
    testUser = {
      email: `test${Date.now()}@example.com`,
      password: 'TestPassword123!'
    };

    const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
      withCredentials: true,
      validateStatus: () => true
    });

    expect([200, 201]).toContain(registerResponse.status);
    expect(registerResponse.data?.user?.id).toBeDefined();
    userId = registerResponse.data.user.id;

    const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, testUser, {
      withCredentials: true,
      validateStatus: () => true
    });

    expect(loginResponse.status).toBe(200);
    authCookies = loginResponse.headers['set-cookie'];
    expect(authCookies).toBeDefined();
  });

  afterEach(async () => {
    try {
      await axios.delete(`${API_BASE_URL}/api/auth/account`, {
        headers: { Cookie: authCookies?.join('; ') },
        withCredentials: true,
        validateStatus: () => true
      });
    } catch {
    }
  });

  const getProgress = () =>
    axios.get(`${API_BASE_URL}/api/progress`, {
      headers: { Cookie: authCookies?.join('; ') },
      withCredentials: true,
      validateStatus: () => true
    });

  const getModuleProgress = (moduleId) =>
    axios.get(`${API_BASE_URL}/api/progress/module/${moduleId}`, {
      headers: { Cookie: authCookies?.join('; ') },
      withCredentials: true,
      validateStatus: () => true
    });

  const runCode = (body) =>
    axios.post(`${API_BASE_URL}/api/code/run`, body, {
      headers: { Cookie: authCookies?.join('; ') },
      withCredentials: true,
      validateStatus: () => true,
      timeout: 60000
    });

  const runCodeAndExpectSuccess = async (body) => {
    const response = await runCode(body);
    expect(response.status).toBe(200);
    expect(response.data.success).toBe(true);
    return response;
  };

  const markComplete = (moduleId, exerciseId, data) =>
    axios.post(`${API_BASE_URL}/api/progress/${moduleId}/${exerciseId}/complete`, data, {
      headers: { Cookie: authCookies?.join('; ') },
      withCredentials: true,
      validateStatus: () => true
    });

  describe('GET /api/progress', () => {
    it('должен возвращать пустой прогресс для нового пользователя', async () => {
      const response = await getProgress();

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('userId', userId);
      expect(response.data).toHaveProperty('totalCompleted', 0);
      expect(response.data.totalExercises).toBeGreaterThan(0);
      expect(response.data).toHaveProperty('totalPercentage', 0);
      expect(Array.isArray(response.data.modules)).toBe(true);
      expect(response.data.modules.length).toBe(3);
    });

    it('должен содержать информацию о всех модулях', async () => {
      const response = await getProgress();

      expect(response.status).toBe(200);

      const modules = response.data.modules;
      const moduleIds = modules.map(m => m.moduleId);

      expect(moduleIds).toContain('module-1');
      expect(moduleIds).toContain('module-2');
      expect(moduleIds).toContain('module-3');

      modules.forEach(module => {
        expect(module).toHaveProperty('moduleId');
        expect(module).toHaveProperty('moduleTitle');
        expect(module.totalExercises).toBeGreaterThan(0);
        expect(module).toHaveProperty('completedExercises', 0);
        expect(module).toHaveProperty('percentage', 0);
        expect(Array.isArray(module.exercises)).toBe(true);
      });
    });

    it('должен возвращать правильные названия модулей', async () => {
      const response = await getProgress();

      expect(response.status).toBe(200);

      const moduleMap = {};
      response.data.modules.forEach(m => { moduleMap[m.moduleId] = m.moduleTitle; });

      expect(moduleMap['module-1']).toBe('XPath');
      expect(moduleMap['module-2']).toBe('Взаимодействие с элементами');
      expect(moduleMap['module-3']).toBe('Page Object Model (POM)');
    });

    it('должен требовать аутентификации', async () => {
      const response = await axios.get(`${API_BASE_URL}/api/progress`, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect([401, 403]).toContain(response.status);
    });
  });

  describe('GET /api/progress/module/:moduleId', () => {
    it('должен возвращать прогресс конкретного модуля', async () => {
      const response = await getModuleProgress('module-2');

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('moduleId', 'module-2');
      expect(response.data).toHaveProperty('moduleTitle', 'Взаимодействие с элементами');
      expect(response.data.totalExercises).toBeGreaterThan(0);
      expect(response.data).toHaveProperty('completedExercises', 0);
      expect(response.data).toHaveProperty('percentage', 0);
      expect(Array.isArray(response.data.exercises)).toBe(true);
    });

    it('должен обрабатывать несуществующий модуль', async () => {
      const response = await getModuleProgress('nonexistent-module');

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('moduleId', 'nonexistent-module');
      expect(response.data).toHaveProperty('totalExercises', 0);
      expect(response.data).toHaveProperty('completedExercises', 0);
      expect(response.data).toHaveProperty('percentage', 0);
    });

    it('должен требовать аутентификации для модуля', async () => {
      const response = await axios.get(`${API_BASE_URL}/api/progress/module/module-1`, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect([401, 403]).toContain(response.status);
    });
  });

  describe('Интеграция с выполнением упражнений', () => {
    it('должен отслеживать количество попыток', async () => {
      const ATTEMPTS = 3;
      const codeRequest = {
        code: `invalid code here`,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      };

      for (let i = 0; i < ATTEMPTS; i++) {
        await runCode(codeRequest);
        await markComplete('module-2', 'module-2-block-1-exercise-1', {
          codeSnapshot: codeRequest.code,
          isSuccess: false,
          errorMessage: 'Compilation error'
        });
      }

      const progressResponse = await getModuleProgress('module-2');
      expect(progressResponse.status).toBe(200);

      const exerciseProgress = progressResponse.data.exercises.find(
        e => e.exerciseId === 'module-2-block-1-exercise-1'
      );

      expect(exerciseProgress).toBeDefined();
      expect(exerciseProgress.attemptsCount).toBe(ATTEMPTS);
    });

    it('должен правильно вычислять проценты выполнения', async () => {
      const initialResponse = await getModuleProgress('module-2');
      expect(initialResponse.status).toBe(200);

      const totalExercises = initialResponse.data.totalExercises;
      expect(totalExercises).toBeGreaterThan(0);

      const code = `System.out.println("Test");`;
      await runCodeAndExpectSuccess({
        code,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      await markComplete('module-2', 'module-2-block-1-exercise-1', {
        codeSnapshot: code,
        isSuccess: true,
        errorMessage: null
      });

      const updatedResponse = await getModuleProgress('module-2');
      expect(updatedResponse.status).toBe(200);

      const expectedPercentage = (1 / totalExercises) * 100;
      expect(updatedResponse.data.percentage).toBeCloseTo(expectedPercentage, 1);
      expect(updatedResponse.data.completedExercises).toBe(1);
    });
  });

  describe('Множественные упражнения', () => {
    it('должен обрабатывать выполнение нескольких упражнений', async () => {
      const exercises = [
        {
          code: 'System.out.println("Exercise 1");',
          exercise: 1,
          exerciseId: 'module-2-block-1-exercise-1',
          moduleId: 'module-2'
        },
        {
          code: 'System.out.println("Exercise 2");',
          exercise: 2,
          exerciseId: 'module-2-block-1-exercise-2',
          moduleId: 'module-2'
        },
        {
          code: 'System.out.println("Exercise 3");',
          exercise: 3,
          exerciseId: 'module-2-block-1-exercise-3',
          moduleId: 'module-2'
        }
      ];

      let successfulExercises = 0;
      for (const exercise of exercises) {
        const response = await runCode(exercise);
        expect(response.status).toBe(200);
        if (response.data.success) {
          successfulExercises++;
          await markComplete(exercise.moduleId, exercise.exerciseId, {
            codeSnapshot: exercise.code,
            isSuccess: true,
            errorMessage: null
          });
        }
      }

      expect(successfulExercises).toBeGreaterThan(0);

      const progressResponse = await getProgress();
      expect(progressResponse.status).toBe(200);
      expect(progressResponse.data.totalCompleted).toBe(successfulExercises);
      expect(progressResponse.data.totalPercentage).toBeGreaterThan(0);
    });

    it('должен обрабатывать упражнения из разных модулей', async () => {
      const module2Code = 'System.out.println("Module 2");';
      await runCodeAndExpectSuccess({
        code: module2Code,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      await markComplete('module-2', 'module-2-block-1-exercise-1', {
        codeSnapshot: module2Code,
        isSuccess: true,
        errorMessage: null
      });

      const module3ExecResponse = await runCode({
        files: { 'TestPage.java': 'public class TestPage { }' },
        exercise: 101,
        exerciseId: 'module-3-block-1-exercise-1',
        moduleId: 'module-3'
      });
      expect(module3ExecResponse.status).toBe(200);

      if (module3ExecResponse.data.success) {
        await markComplete('module-3', 'module-3-block-1-exercise-1', {
          codeSnapshot: 'public class TestPage { }',
          isSuccess: true,
          errorMessage: null
        });
      }

      const module2Response = await getModuleProgress('module-2');
      expect(module2Response.status).toBe(200);
      expect(module2Response.data.completedExercises).toBeGreaterThan(0);

      const module3Response = await getModuleProgress('module-3');
      expect(module3Response.status).toBe(200);
      if (module3ExecResponse.data.success) {
        expect(module3Response.data.completedExercises).toBeGreaterThan(0);
      } else {
        expect(module3Response.data.completedExercises).toBe(0);
      }
    });
  });
});
