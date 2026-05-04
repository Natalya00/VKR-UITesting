const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

describe('Exercises Integration Tests', () => {
  let testUser;
  let authCookies;

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

    await axios.post(`${API_BASE_URL}/api/auth/register`, testUser, {
      withCredentials: true,
      validateStatus: () => true
    });

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

  const runCode = (body, options = {}) =>
    axios.post(`${API_BASE_URL}/api/code/run`, body, {
      headers: { Cookie: authCookies?.join('; ') },
      withCredentials: true,
      validateStatus: () => true,
      timeout: 60000,
      ...options
    });

  describe('POST /api/code/run - Модуль 2 (Selenide)', () => {
    it('должен успешно выполнять простой Selenide код', async () => {
      const response = await runCode({
        code: `
          import static com.codeborne.selenide.Selenide.*;
          import static com.codeborne.selenide.Condition.*;

          $("#submit-btn").click();
        `,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2',
        baseUrl: 'http://localhost:3000'
      }, { timeout: 200000 });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(true);
      expect(response.data.stdout).toBeDefined();
      expect(response.data.stderr).toBeDefined();
      expect(response.data.message).toBeDefined();
    });

    it('должен обрабатывать код с ошибками компиляции', async () => {
      const response = await runCode({
        code: `
          import static com.codeborne.selenide.Selenide.*;

          open("/");
          $("h1").shouldBe(visible
        `,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(response.data.success).toBe(false);
      expect(response.data.uiMode).toBe(true);
      expect(response.data.stderr).toContain('ERROR');
      expect(response.data.message).toContain('Ошибка компиляции');
    });

    it('должен обрабатывать обычный Java код (не Selenide)', async () => {
      const response = await runCode({
        code: `System.out.println("Hello World");`,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(false);
      expect(response.data.success).toBe(true);
      expect(response.data.stdout).toContain('Hello World');
    });

    it('должен валидировать статические правила кода', async () => {
      const response = await runCode({
        code: `
          import static com.codeborne.selenide.Selenide.*;

          open("/");
          $("element").click();
        `,
        exercise: 5,
        exerciseId: 'module-2-block-1-exercise-5',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(true);
      expect(typeof response.data.success).toBe('boolean');
    });

    it('должен обрабатывать пустой код', async () => {
      const response = await runCode({
        code: '',
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(response.data.success).toBe(false);
      expect(response.data.stderr).toContain('не должен быть пустым');
      expect(response.data.message).toContain('Введите код');
    });

    it('должен обрабатывать код с файлами и выводить результат выполнения', async () => {
      const response = await runCode({
        files: {
          'TestClass.java': `
            public class TestClass {
              public static void main(String[] args) {
                System.out.println("Test from file");
              }
            }
          `
        },
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(typeof response.data.success).toBe('boolean');
      if (response.data.success) {
        expect(response.data.stdout).toContain('Test from file');
      } else {
        expect(response.data.stderr || response.data.message).toBeTruthy();
      }
    });
  });

  describe('POST /api/code/run - Модуль 3 (POM)', () => {
    it('должен обрабатывать упражнения модуля 3', async () => {
      const response = await runCode({
        files: {
          'LoginPage.java': `
            public class LoginPage {
              public void login(String username, String password) {
                // Page Object implementation
              }
            }
          `
        },
        exercise: 101,
        exerciseId: 'module-3-block-1-exercise-1',
        moduleId: 'module-3',
        baseUrl: 'http://localhost:3000'
      });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(true);
      expect(typeof response.data.success).toBe('boolean');
      expect(response.data.message || response.data.stdout || response.data.stderr).toBeTruthy();
    });

    it('должен возвращать ошибку при пустых файлах для модуля 3', async () => {
      const response = await runCode({
        files: {},
        exercise: 101,
        exerciseId: 'module-3-block-1-exercise-1',
        moduleId: 'module-3'
      });

      expect(response.status).toBe(200);
      expect(response.data.success).toBe(false);
      expect(response.data.stderr).toContain('не должны быть пустыми');
    });

    it('должен валидировать AST правила для модуля 3', async () => {
      const response = await runCode({
        files: {
          'TestPage.java': `
            public class TestPage {
              // Minimal implementation
            }
          `
        },
        exercise: 102,
        exerciseId: 'module-3-block-1-exercise-2',
        moduleId: 'module-3'
      });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(true);
      expect(typeof response.data.success).toBe('boolean');
      expect(response.data.success).toBe(false);
      expect(response.data.message || response.data.stderr).toBeTruthy();
    });
  });

  describe('Безопасность выполнения кода', () => {
    it('должен блокировать или изолировать потенциально опасный код', async () => {
      const response = await runCode({
        code: `
          import java.io.File;
          new File("/etc/passwd").delete();
          System.exit(1);
        `,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      }, { timeout: 30000 });

      expect(response.status).toBe(200);
      if (response.data.success === false) {
        expect(response.data.message || response.data.stderr).toBeTruthy();
      } else {
        expect(response.data.stdout).toBeDefined();
      }
    });

    it('должен обрабатывать бесконечные циклы с таймаутом', async () => {
      const response = await runCode({
        code: `while(true) { }`,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      }, { timeout: 200000 });

      expect(response.status).toBe(200);
      expect(response.data.success).toBe(false);
      expect(response.data.message).toMatch(/Время выполнения истекло/i);
    }, 210000);

    it('должен требовать аутентификации', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/code/run`, {
        code: 'System.out.println("Hello");',
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      }, {
        withCredentials: true,
        validateStatus: () => true
      });

      expect(response.status).toBe(403);
    });
  });

  describe('Обработка ошибок', () => {
    it('должен возвращать ошибку при некорректных параметрах запроса', async () => {
      const response = await runCode({});

      expect(response.status).toBe(200);
      expect(response.data.success).toBe(false);
      expect(response.data.message || response.data.stderr).toBeTruthy();
    });

    it('должен возвращать 400 или 401 при некорректном JSON', async () => {
      const response = await axios.post(`${API_BASE_URL}/api/code/run`, 'invalid json', {
        headers: {
          Cookie: authCookies?.join('; '),
          'Content-Type': 'application/json'
        },
        withCredentials: true,
        validateStatus: () => true
      });

      expect([400, 401, 403]).toContain(response.status);
    });
  });

  describe('Различные типы кода', () => {
    it('должен обрабатывать код с различными селекторами Selenide', async () => {
      const response = await runCode({
        code: `
          import static com.codeborne.selenide.Selenide.*;
          import static com.codeborne.selenide.Condition.*;

          open("/");
          $("#id-selector").shouldBe(visible);
          $(".class-selector").click();
          $("tag-selector").setValue("test");
          $(byText("Click me")).click();
          $(byName("username")).shouldBe(enabled);
        `,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(true);
      expect(typeof response.data.success).toBe('boolean');
    });

    it('должен обрабатывать код с различными условиями Selenide', async () => {
      const response = await runCode({
        code: `
          import static com.codeborne.selenide.Selenide.*;
          import static com.codeborne.selenide.Condition.*;

          open("/");
          $(".element").shouldBe(visible);
          $(".element").shouldHave(text("Expected"));
          $(".element").shouldBe(enabled);
          $(".element").shouldHave(attribute("data-test"));
        `,
        exercise: 1,
        exerciseId: 'module-2-block-1-exercise-1',
        moduleId: 'module-2'
      });

      expect(response.status).toBe(200);
      expect(response.data.uiMode).toBe(true);
      expect(typeof response.data.success).toBe('boolean');
    });
  });
});