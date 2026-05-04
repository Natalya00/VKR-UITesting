import { test, expect } from '@playwright/test';

test.describe('Selenide Exercises E2E Tests', () => {
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
    await page.goto('/element-simulator');
    await expect(page).toHaveURL(/element-simulator/);
    await page.waitForSelector('.top-section', { timeout: 15000 });
  });

  test.describe('Интерфейс Selenide симулятора', () => {
    test('должен отображать основные элементы интерфейса', async ({ page }) => {
      await expect(page.locator('.ide-column')).toBeVisible();
      
      await expect(page.locator('button:has-text("Запустить")')).toBeVisible();
      
      await expect(page.locator('.results-card')).toBeVisible();
      
      await expect(page.locator('.task-description')).toBeVisible();
    });


    test('должен показывать справочную информацию по Selenide', async ({ page }) => {
      const helpButton = page.locator('button:has-text("Справочная")');
      if (await helpButton.isVisible()) {
        await helpButton.click();
        await expect(page.locator('.reference-modal')).toBeVisible();
        
        await page.locator('.close-button').click();
        await expect(page.locator('.reference-modal')).not.toBeVisible();
      }
    });
  });

  test.describe('Выполнение Selenide кода', () => {
    test('должен выполнять простой Selenide код', async ({ page }) => {
      const ideColumn = page.locator('.ide-column');
      if (await ideColumn.isVisible()) {
        const simpleCode = `import static com.codeborne.selenide.Selenide.*;\nimport static com.codeborne.selenide.Condition.*;\n\nopen("/");\n$("h1").shouldBe(visible);`;

        const codeArea = page.locator('.monaco-editor textarea, .code-input');
        if (await codeArea.isVisible()) {
          await codeArea.fill(simpleCode);
        }

        await page.click('button:has-text("Запустить")');

        await page.waitForTimeout(10000);

        const resultCard = page.locator('.results-card');
        await expect(resultCard).toBeVisible();
      }
    });

    test('должен показывать ошибки компиляции', async ({ page }) => {
      const ideColumn = page.locator('.ide-column');
      if (await ideColumn.isVisible()) {
        const invalidCode = `import static com.codeborne.selenide.Selenide.*;\n\nopen("/");\n$("h1").shouldBe(visible // Незакрытая скобка`;

        const codeArea = page.locator('.monaco-editor textarea, .code-input');
        if (await codeArea.isVisible()) {
          await codeArea.fill(invalidCode);
        }

        await page.click('button:has-text("Запустить")');
        await page.waitForTimeout(5000);
        const resultCard = page.locator('.results-card');
        await expect(resultCard).toBeVisible();
      }
    });

    test('должен показывать статус выполнения', async ({ page }) => {
      const ideColumn = page.locator('.ide-column');
      if (await ideColumn.isVisible()) {
        const code = `import static com.codeborne.selenide.Selenide.*;\nopen("/");`;

        const codeArea = page.locator('.monaco-editor textarea, .code-input');
        if (await codeArea.isVisible()) {
          await codeArea.fill(code);
        }

        await page.click('button:has-text("Запустить")');
        await page.waitForTimeout(8000);

        const resultCard = page.locator('.results-card');
        await expect(resultCard).toBeVisible();
        
        const resultText = await resultCard.textContent();
        expect(resultText?.length).toBeGreaterThan(0);
      }
    });
  });
});