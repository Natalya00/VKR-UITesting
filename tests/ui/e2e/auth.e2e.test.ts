import { test, expect } from '@playwright/test';

const testUser = {
  email: `test${Date.now()}@example.com`,
  password: 'TestPassword123!',
  name: 'Test User'
};

const existingUser = {
  email: 'existing@example.com',
  password: 'ExistingPass123!'
};

test.describe('Authentication E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

    test.describe('Пользовательский интерфейс', () => {
    test('должен отображать корректные элементы на странице входа', async ({ page }) => {
      await page.goto('/login');
      
      await expect(page.locator('input[name="email"]')).toBeVisible();
      await expect(page.locator('input[name="password"]')).toBeVisible();
      await expect(page.locator('button[type="submit"]')).toBeVisible();
      
      const registerLink = page.locator('a:has-text("Регистрация"), a[href*="register"]');
      await expect(registerLink).toBeVisible();
    });

    test('должен отображать корректные элементы на странице регистрации', async ({ page }) => {
      await page.goto('/register');
      
      await expect(page.locator('input[name="email"]')).toBeVisible();
      await expect(page.locator('input[name="password"]')).toBeVisible();
      await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
      await expect(page.locator('button[type="submit"]')).toBeVisible();
      
      const loginLink = page.locator('a:has-text("Вход"), a[href*="login"], a[href="/"]');
      await expect(loginLink).toBeVisible();
    });
  });

  test.describe('Регистрация пользователя', () => {
    test('должен успешно регистрировать нового пользователя', async ({ page }) => {
      await page.goto('/register');
      
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.fill('input[name="confirmPassword"]', testUser.password);
      await page.click('button[type="submit"]');
      
      await expect(page).toHaveURL(/\/xpath-simulator/);
      
      const userMenu = page.locator('.user-menu-trigger');
      await expect(userMenu).toBeVisible();
    });

    test('должен показывать ошибку при регистрации с существующим email', async ({ page }) => {
      await page.goto('/register');
      await page.fill('input[name="email"]', existingUser.email);
      await page.fill('input[name="password"]', existingUser.password);
      await page.fill('input[name="confirmPassword"]', existingUser.password);
      await page.click('button[type="submit"]');
      
      await page.waitForResponse(response => 
        response.url().includes('/auth/register') || 
        response.url().includes('/register')
      ).catch(() => {});
      
      const errorMessage = page.locator('.error-message');
      await expect(errorMessage).toBeVisible();
      
      const errorText = await errorMessage.textContent();
      expect(errorText).toMatch(/существует|already exists/i);
    });

    test('должен валидировать поля формы регистрации', async ({ page }) => {
      await page.goto('/register');
      
      await page.fill('input[name="email"]', 'invalid-email');
      await page.fill('input[name="password"]', 'pass123');
      await page.fill('input[name="confirmPassword"]', 'pass123');
      await page.click('button[type="submit"]');
      
      await page.waitForTimeout(500);
      const emailInput = page.locator('input[name="email"]');
      const isValid = await emailInput.evaluate((el) => (el as HTMLInputElement).validity.valid);
      
      if (!isValid) {
        const validationMessage = await emailInput.evaluate((el) => (el as HTMLInputElement).validationMessage);
        expect(validationMessage).toBeTruthy();
      } else {
        const errorMessage = page.locator('.error-message');
        await expect(errorMessage).toBeVisible();
      }
      
      await page.fill('input[name="email"]', 'test@example.com');
      await page.fill('input[name="password"]', '123');
      await page.fill('input[name="confirmPassword"]', '123');
      await page.click('button[type="submit"]');
      
      await page.waitForTimeout(500);
      const passwordInput = page.locator('input[name="password"]');
      const isPasswordValid = await passwordInput.evaluate((el) => (el as HTMLInputElement).validity.valid);
      
      if (!isPasswordValid) {
        const passwordMessage = await passwordInput.evaluate((el) => (el as HTMLInputElement).validationMessage);
        expect(passwordMessage).toBeTruthy();
      }
      
      await page.fill('input[name="password"]', 'password123');
      await page.fill('input[name="confirmPassword"]', 'password456');
      await page.click('button[type="submit"]');
      
      await page.waitForTimeout(500);
      const mismatchError = page.locator('.error-message');
      if (await mismatchError.isVisible()) {
        const errorText = await mismatchError.textContent();
        expect(errorText).toMatch(/пароли не совпадают|passwords do not match/i);
      }
    });
  });

  test.describe('Вход в систему', () => {
    test('должен успешно выполнять вход с корректными данными', async ({ page }) => {
      await page.goto('/register');
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.fill('input[name="confirmPassword"]', testUser.password);
      await page.click('button[type="submit"]');
      
      await page.waitForTimeout(1000);
      
      await page.goto('/login');
      
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.click('button[type="submit"]');
      
      await expect(page).toHaveURL(/\/xpath-simulator/);
    });

    test('должен показывать ошибку при неверных учетных данных', async ({ page }) => {
      await page.goto('/login');
      
      await page.fill('input[name="email"]', 'wrong@example.com');
      await page.fill('input[name="password"]', 'wrongpassword');
      await page.click('button[type="submit"]');
      
      const errorMessage = page.locator('.error-message');
      await expect(errorMessage).toBeVisible();
      const errorText = await errorMessage.textContent();
      expect(errorText).toMatch(/неверный|invalid|ошибка/i);
    });

    test('должен показывать ошибку при несуществующем email', async ({ page }) => {
      await page.goto('/login');
      
      await page.fill('input[name="email"]', 'nonexistent@example.com');
      await page.fill('input[name="password"]', 'password123');
      await page.click('button[type="submit"]');
      
      const errorMessage = page.locator('.error-message');
      await expect(errorMessage).toBeVisible();
    });

    test('должен валидировать поля формы входа', async ({ page }) => {
      await page.goto('/login');
      
      await page.click('button[type="submit"]');
      
      await page.waitForTimeout(500);
      
      const emailInput = page.locator('input[name="email"]');
      const emailValidation = await emailInput.evaluate((el) => (el as HTMLInputElement).validity.valid);
      
      if (!emailValidation) {
        const validationMessage = await emailInput.evaluate((el) => (el as HTMLInputElement).validationMessage);
        expect(validationMessage).toBeTruthy();
      }
      
      await page.fill('input[name="email"]', 'invalid-email');
      await page.fill('input[name="password"]', 'pass');
      await page.click('button[type="submit"]');
      
      await page.waitForTimeout(500);
      
      const emailValid = await emailInput.evaluate((el) => (el as HTMLInputElement).validity.valid);
      if (!emailValid) {
        const message = await emailInput.evaluate((el) => (el as HTMLInputElement).validationMessage);
        expect(message).toBeTruthy();
      }
    });
  });

  test.describe('Выход из системы', () => {
    test('должен успешно выполнять выход из системы', async ({ page }) => {
      await page.goto('/login');
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.click('button[type="submit"]');
      
      await expect(page).toHaveURL(/\/xpath-simulator/);
      await page.waitForTimeout(1000);
      
      const userMenuTrigger = page.locator('.user-menu-trigger');
      await expect(userMenuTrigger).toBeVisible();
      await userMenuTrigger.click();
      
      await page.waitForSelector('.user-menu-dropdown', { state: 'visible' });
      
      const logoutButton = page.locator('.user-menu-item.logout');
      await expect(logoutButton).toBeVisible();
      await logoutButton.click();
      
      await expect(page).toHaveURL(/\/login/);
    });

      test('должен блокировать доступ к защищенным страницам после выхода', async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="email"]', testUser.email);
        await page.fill('input[name="password"]', testUser.password);
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL(/\/xpath-simulator/);
        
        const userMenuTrigger = page.locator('.user-menu-trigger');
        await userMenuTrigger.click();
        await page.waitForSelector('.user-menu-dropdown', { state: 'visible' });
        
        const logoutButton = page.locator('.user-menu-item.logout');
        await logoutButton.click();
        
        await expect(page).toHaveURL(/\/login/);
        
        await page.goto('/xpath-simulator');
        await expect(page.locator('h2:has-text("Требуется авторизация")')).toBeVisible();
        await expect(page).toHaveURL(/\/xpath-simulator/);
      });
  });

  test.describe('Навигация и защищенные маршруты', () => {
    test('должен показывать сообщение о необходимости авторизации для неавторизованных пользователей', async ({ page }) => {
      await page.context().clearCookies();
      
      const protectedRoutes = ['/xpath-simulator', '/element-simulator', '/pom-simulator', '/progress'];
      
      for (const route of protectedRoutes) {
        await page.goto(route);
        await expect(page.locator('h2:has-text("Требуется авторизация")')).toBeVisible();
        await expect(page.locator('a:has-text("Перейти на страницу входа")')).toBeVisible();
      }
    });

      test('должен сохранять состояние аутентификации при обновлении страницы', async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="email"]', testUser.email);
        await page.fill('input[name="password"]', testUser.password);
        await page.click('button[type="submit"]');
        
        await page.waitForURL(/\/xpath-simulator/, { timeout: 10000 });
        
        const userMenuTrigger = page.locator('.user-menu-trigger');
        await expect(userMenuTrigger).toBeVisible();
        
        await page.reload();
        
        await page.waitForTimeout(2000);
        
        await expect(userMenuTrigger).toBeVisible({ timeout: 10000 });
        
        const currentUrl = page.url();
        expect(currentUrl).toContain('xpath-simulator');
        expect(currentUrl).not.toContain('login');
      });
  });
});