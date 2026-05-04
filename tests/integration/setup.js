const axios = require('axios');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';
const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3000';

jest.setTimeout(120000);

axios.defaults.timeout = 60000;
axios.defaults.validateStatus = (status) => status < 500;

global.testUtils = {
  createTestUser: () => ({
    email: `test${Date.now()}_${Math.random().toString(36).substring(7)}@example.com`,
    password: 'TestPassword123!'
  }),

  registerAndLogin: async (user) => {
    const registerResponse = await axios.post(`${API_BASE_URL}/api/auth/register`, user, {
      withCredentials: true,
      validateStatus: () => true
    });

    if (registerResponse.status !== 200) {
      throw new Error(`Registration failed: ${registerResponse.data?.message || 'Unknown error'}`);
    }

    const loginResponse = await axios.post(`${API_BASE_URL}/api/auth/login`, user, {
      withCredentials: true,
      validateStatus: () => true
    });

    if (loginResponse.status !== 200) {
      throw new Error(`Login failed: ${loginResponse.data?.message || 'Unknown error'}`);
    }

    return {
      user: loginResponse.data.user,
      cookies: loginResponse.headers['set-cookie']
    };
  },

  waitForServer: async (maxAttempts = 30, interval = 2000) => {
    for (let i = 0; i < maxAttempts; i++) {
      try {
        const response = await axios.get(`${API_BASE_URL}/actuator/health`, {
          timeout: 5000,
          validateStatus: () => true
        });
        
        if (response.status === 200) {
          return true;
        }
      } catch (error) {
      }
      
      await new Promise(resolve => setTimeout(resolve, interval));
    }
    
    return false;
  },

  cleanup: async () => {
  },

  checkFrontend: async () => {
    try {
      const response = await axios.get(FRONTEND_URL, {
        timeout: 5000,
        validateStatus: () => true
      });
      return response.status === 200;
    } catch (error) {
      return false;
    }
  },

  generateTestCode: {
    selenide: (action = 'click') => `
      import static com.codeborne.selenide.Selenide.*;
      import static com.codeborne.selenide.Condition.*;
      
      open("/");
      $("button").${action}();
      $("h1").shouldBe(visible);
    `,
    
    java: (message = 'Hello World') => `
      public class TestClass {
        public static void main(String[] args) {
          System.out.println("${message}");
        }
      }
    `,
    
    pageObject: (className = 'TestPage') => `
      public class ${className} {
        public void performAction() {
          
        }
      }
    `,
    
    invalid: () => `
      public class Invalid {
        public void method( {
          
        }
      }
    `
  },

  constants: {
    API_BASE_URL,
    FRONTEND_URL,
    MODULES: {
      MODULE_1: 'module-1',
      MODULE_2: 'module-2', 
      MODULE_3: 'module-3'
    },
    EXERCISE_COUNTS: {
      'module-1': 49,
      'module-2': 90,
      'module-3': 57
    }
  }
};

beforeAll(async () => {
  await global.testUtils.waitForServer();
  
  const frontendAvailable = await global.testUtils.checkFrontend();
  if (!frontendAvailable) {
  }
});

afterAll(async () => {
  await global.testUtils.cleanup();
});

process.on('unhandledRejection', (reason, promise) => {
});

process.on('uncaughtException', (error) => {
});

const originalConsoleLog = console.log;
console.log = (...args) => {
  const timestamp = new Date().toISOString();
  originalConsoleLog(`[${timestamp}]`, ...args);
};

module.exports = {
  API_BASE_URL,
  FRONTEND_URL,
  testUtils: global.testUtils
};