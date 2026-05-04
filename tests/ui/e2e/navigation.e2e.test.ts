import { test, expect } from '@playwright/test';

test.describe('Navigation E2E Tests', () => {
  const TEST_USER = {
    email: `navigation_fixed_${Math.random().toString(36).substring(2, 15)}@example.com`,
    password: 'TestPassword123!'
  };

  let isUserCreated = false;

  async function registerUser(page: any) {
    if (isUserCreated) {
      await page.goto('/login');
      await page.waitForSelector('input[name="email"]', { state: 'visible', timeout: 10000 });
      await page.fill('input[name="email"]', TEST_USER.email);
      await page.fill('input[name="password"]', TEST_USER.password);
      await page.click('button[type="submit"]');
      await page.waitForURL('/xpath-simulator', { timeout: 10000 });
    } else {
      await page.goto('/register');
      await page.fill('input[name="email"]', TEST_USER.email);
      await page.fill('input[name="password"]', TEST_USER.password);
      await page.fill('input[name="confirmPassword"]', TEST_USER.password);
      await page.click('button[type="submit"]');
      await page.waitForURL('/xpath-simulator', { timeout: 10000 });
      isUserCreated = true;
    }
  }

  test.beforeEach(async ({ page }) => {
    await registerUser(page);
    
    const userMenu = page.locator('.user-menu-trigger');
    await expect(userMenu).toBeVisible({ timeout: 5000 });
  });

  test.describe('Основная навигация', () => {
    test('должен отображать главное меню навигации', async ({ page }) => {
      const menuItems = [
        '.nav-link[href="/xpath-simulator"]',
        '.nav-link[href="/element-simulator"]',
        '.nav-link[href="/pom-simulator"]',
        '.nav-link[href="/progress"]'
      ];

      for (const selector of menuItems) {
        await expect(page.locator(selector)).toBeVisible();
      }
    });

    test('должен позволять переходить между основными разделами', async ({ page }) => {
      const sections = [
        { url: '/progress', selector: 'a[href="/progress"]' },
        { url: '/xpath-simulator', selector: 'a[href="/xpath-simulator"]' },
        { url: '/element-simulator', selector: 'a[href="/element-simulator"]' },
        { url: '/pom-simulator', selector: 'a[href="/pom-simulator"]' }
      ];

      for (const section of sections) {
        const link = page.locator(section.selector);
        await expect(link).toBeVisible();
        await link.click({ force: true });
        await page.waitForTimeout(1000);
        expect(page.url()).toContain(section.url);
      }
    });

    test('должен показывать активный пункт меню', async ({ page }) => {
      const sections = [
        { url: '/xpath-simulator', selector: '.nav-link[href="/xpath-simulator"]' },
        { url: '/element-simulator', selector: '.nav-link[href="/element-simulator"]' },
        { url: '/pom-simulator', selector: '.nav-link[href="/pom-simulator"]' }
      ];

      for (const section of sections) {
        await page.goto(section.url);
        await page.waitForTimeout(500);
        const activeLink = page.locator(`${section.selector}.active`);
        await expect(activeLink).toBeVisible();
      }
    });

    test('должен сохранять состояние при обновлении страницы', async ({ page }) => {
      await page.goto('/element-simulator');
      await page.waitForTimeout(1000);
      
      const currentUrl = page.url();
      await page.reload();
      await page.waitForTimeout(2000);
      
      expect(page.url()).toContain('element-simulator');
      
      const exerciseTitle = page.locator('.exercise-title');
      await expect(exerciseTitle).toBeVisible();
    });
  });

  test.describe('Пользовательское меню', () => {
    test('должен отображать информацию о пользователе', async ({ page }) => {
      const userMenuTrigger = page.locator('.user-menu-trigger');
      await expect(userMenuTrigger).toBeVisible();
      
      await userMenuTrigger.click();
      
      const userEmail = page.locator('.user-email');
      await expect(userEmail).toBeVisible();
      await expect(userEmail).toContainText(TEST_USER.email);
    });

    test('должен предоставлять доступ к выходу из системы', async ({ page }) => {
      const userMenuTrigger = page.locator('.user-menu-trigger');
      await expect(userMenuTrigger).toBeVisible();
      await userMenuTrigger.click();
      
      const dropdown = page.locator('.user-menu-dropdown');
      await expect(dropdown).toBeVisible();
      
      const logoutButton = dropdown.locator('.user-menu-item.logout');
      await expect(logoutButton).toBeVisible();
      await logoutButton.click();
      
      await expect(page).toHaveURL('/login');
      
      await page.goto('/login');
      await page.fill('input[name="email"]', TEST_USER.email);
      await page.fill('input[name="password"]', TEST_USER.password);
      await page.click('button[type="submit"]');
      await page.waitForURL('/xpath-simulator', { timeout: 10000 });
    });

    test('должен показывать выпадающее меню пользователя', async ({ page }) => {
      const userMenuTrigger = page.locator('.user-menu-trigger');
      await expect(userMenuTrigger).toBeVisible();
      
      await userMenuTrigger.click();
      
      const dropdown = page.locator('.user-menu-dropdown');
      await expect(dropdown).toBeVisible();
      
      const logoutButton = dropdown.locator('.user-menu-item.logout');
      await expect(logoutButton).toBeVisible();
      await expect(logoutButton).toHaveText('Выйти');
    });
  });

  test.describe('Навигация по упражнениям', () => {
    test('должен позволять переходить к следующему упражнению', async ({ page }) => {
      await page.goto('/element-simulator');
      await page.waitForTimeout(1000);
      
      const exerciseTitle = page.locator('.exercise-title');
      const currentTitle = await exerciseTitle.textContent();
      const currentNumber = parseInt(currentTitle?.match(/\d+/)?.pop() || '1');
      
      const nextButton = page.locator('.nav-button:has-text("Следующее")');
      await expect(nextButton).toBeEnabled();
      await nextButton.click();
      
      await page.waitForTimeout(1000);
      
      const newTitle = await exerciseTitle.textContent();
      const newNumber = parseInt(newTitle?.match(/\d+/)?.pop() || '1');
      expect(newNumber).toBeGreaterThan(currentNumber);
    });

    test('должен позволять переходить к предыдущему упражнению', async ({ page }) => {
      await page.goto('/element-simulator');
      await page.waitForTimeout(1000);
      
      const exerciseTitle = page.locator('.exercise-title');
      
      const nextButton = page.locator('.nav-button:has-text("Следующее")');
      const isNextEnabled = await nextButton.isEnabled();
      
      if (isNextEnabled) {
        await nextButton.click();
        await page.waitForTimeout(1000);
      }
      
      const currentTitle = await exerciseTitle.textContent();
      const currentNumber = parseInt(currentTitle?.match(/\d+/)?.pop() || '1');
      
      const prevButton = page.locator('.nav-button:has-text("Предыдущее")');
      await expect(prevButton).toBeEnabled();
      await prevButton.click();
      
      await page.waitForTimeout(1000);
      
      const newTitle = await exerciseTitle.textContent();
      const newNumber = parseInt(newTitle?.match(/\d+/)?.pop() || '1');
      expect(newNumber).toBeLessThan(currentNumber);
    });
  });
});