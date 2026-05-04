import { test, expect } from '@playwright/test';

test.describe('XPath Exercises E2E Tests', () => {
  const TEST_USER = {
    email: `xpath_test_${Math.random().toString(36).substring(2, 15)}@example.com`,
    password: 'TestPassword123!'
  };

  let isUserCreated = false;

  async function registerAndLogin(page: any) {
    if (isUserCreated) {
      await page.goto('/login');
      await page.fill('input[name="email"]', TEST_USER.email);
      await page.fill('input[name="password"]', TEST_USER.password);
      await page.click('button[type="submit"]');
      await page.waitForURL('**/xpath-simulator', { timeout: 15000 });
    } else {
      await page.goto('/register');
      await page.fill('input[name="email"]', TEST_USER.email);
      await page.fill('input[name="password"]', TEST_USER.password);
      await page.fill('input[name="confirmPassword"]', TEST_USER.password);
      await page.click('button[type="submit"]');
      await page.waitForURL('**/xpath-simulator', { timeout: 15000 });
      isUserCreated = true;
    }
  }

  test.beforeEach(async ({ page }) => {
    await registerAndLogin(page);
    await page.goto('/xpath-simulator');
    await expect(page).toHaveURL(/xpath-simulator/);
    await page.waitForSelector('.top-section', { timeout: 15000 });
  });

  test.describe('Интерфейс XPath симулятора', () => {
    test('должен отображать основные элементы интерфейса', async ({ page }) => {
      await page.waitForLoadState('networkidle');

      await expect(page.locator('textarea.xpath-input')).toBeVisible();
      await expect(page.locator('button.check-button')).toBeVisible();
      await expect(page.locator('.results-card')).toBeVisible();
      await expect(page.locator('.test-container')).toBeVisible();
      await expect(page.locator('.elements-found')).toBeVisible();
    });

    test('должен показывать справочную информацию', async ({ page }) => {
      const helpButton = page.locator('button:has-text("Справочная")');
      await helpButton.click();

      await expect(page.locator('.reference-modal')).toBeVisible();
      
      await expect(page.locator('.reference-header h2')).toContainText('Справочная информация');
      
      await expect(page.locator('.tab-button:has-text("Основы")')).toBeVisible();
      await expect(page.locator('.tab-button:has-text("Примеры")')).toBeVisible();

      await page.locator('.close-button').click();
      await expect(page.locator('.reference-modal')).not.toBeVisible();
    });
  });

  test.describe('Выполнение XPath выражений', () => {
    test('должен выполнять простые XPath выражения', async ({ page }) => {
      const simpleXPaths = ['//h1', '//div', '//button'];

      for (const xpath of simpleXPaths) {
        const xpathInput = page.locator('textarea.xpath-input');
        await xpathInput.clear();
        await xpathInput.fill(xpath);
        await page.click('button.check-button');
        await page.waitForTimeout(2000);

        await expect(page.locator('.results-card')).toBeVisible();

        const elementsText = await page.locator('.elements-found').textContent();
        expect(elementsText).toMatch(/Найдено элементов: \d+/);
      }
    });

    test('должен показывать количество найденных элементов', async ({ page }) => {
      const xpathInput = page.locator('textarea.xpath-input');
      await xpathInput.fill('//div');
      await page.click('button.check-button');
      await page.waitForTimeout(2000);

      await expect(page.locator('.elements-status')).toBeVisible();
      const statusText = await page.locator('.elements-found').textContent();
      expect(statusText).toMatch(/Найдено элементов: \d+/);

      const match = statusText?.match(/Найдено элементов: (\d+)/);
      if (match) {
        expect(parseInt(match[1])).toBeGreaterThan(0);
      }
    });

    test('должен обрабатывать некорректные XPath выражения', async ({ page }) => {
      const invalidXPaths = [
        '//div[@class=unclosed',
        '//div[position()=',
        'invalid xpath'
      ];

      for (const xpath of invalidXPaths) {
        const xpathInput = page.locator('textarea.xpath-input');
        await xpathInput.clear();
        await xpathInput.fill(xpath);
        await page.click('button.check-button');
        await page.waitForTimeout(1000);

        const resultCard = page.locator('.results-card');
        await expect(resultCard).toBeVisible();
        const resultText = await resultCard.textContent();
        expect(resultText).toMatch(/ошибка|некорректный|неверный|error|Ошибка/i);
      }
    });
  });

  test.describe('Интерактивные функции', () => {
    test('должен показывать подсказки для упражнений', async ({ page }) => {
      const hintButton = page.locator('button:has-text("Подсказка")');
      if (await hintButton.isVisible()) {
        await hintButton.click();

        const hintToast = page.locator('.hint-toast');
        await expect(hintToast).toBeVisible();

        const hintText = await hintToast.locator('.hint-text').textContent();
        expect(hintText?.length).toBeGreaterThan(10);

        await page.locator('.hint-close').click();
        await expect(hintToast).not.toBeVisible();
      }
    });

    test('должен блокировать кнопку проверки при пустом поле', async ({ page }) => {
      const xpathInput = page.locator('textarea.xpath-input');
      const checkButton = page.locator('button.check-button');

      await xpathInput.clear();
      await expect(checkButton).toBeDisabled();

      await xpathInput.fill('//div');
      await expect(checkButton).toBeEnabled();
    });
  });
});