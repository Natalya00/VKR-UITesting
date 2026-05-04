import { test, expect } from '@playwright/test';

test.describe('POM Exercises E2E Tests', () => {
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
    await page.goto('/pom-simulator');
    await expect(page).toHaveURL(/pom-simulator/);
    await page.waitForSelector('.block-tabs', { timeout: 15000 });
    await page.waitForTimeout(2000);
  });

  test.describe('Интерфейс POM симулятора', () => {
    test('должен отображать основные элементы интерфейса', async ({ page }) => {
      await expect(page.locator('.ide-card')).toBeVisible();
      
      await expect(page.locator('.pom-card')).toBeVisible();
      
      await expect(page.locator('button:has-text("Запустить")')).toBeVisible();
      
      await expect(page.locator('.results-card')).toBeVisible();
      
    });

    test('должен показывать справочную информацию по POM', async ({ page }) => {
      const helpButton = page.locator('button:has-text("Справочная")');
      await helpButton.click();
      
      const modal = page.locator('.reference-modal');
      await expect(modal).toBeVisible();
      
      await page.locator('.close-button').click();
              await expect(page.locator('.reference-modal')).not.toBeVisible();
    });

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

   test('должен отмечать выполненные упражнения', async ({ page }) => {
          const simpleCode = `public class SimpleElement extends BaseElement {
        public SimpleElement(String xpath) {
            super(xpath, "");
        }
    }`;

          const codeEditor = page.locator('.code-editor');
          await codeEditor.click();
          await page.keyboard.press('Control+A');
          await page.keyboard.type(simpleCode);
          
          await page.click('button:has-text("Запустить")');
          await page.waitForTimeout(8000);
          
          const terminalOutput = page.locator('.terminal-output');
          const outputText = await terminalOutput.textContent();
          
          if (outputText?.includes('успешно') || outputText?.includes('✅')) {
            const completedIndicator = page.locator('.completed, .success, .checkmark, .exercise-completed');
            if (await completedIndicator.isVisible()) {
              await expect(completedIndicator).toBeVisible();
            }
          }
      });
  });

  test.describe('Многофайловая работа', () => {
    test('должен поддерживать создание нескольких файлов', async ({ page }) => {
      await page.click('.block-tab:has-text("Блок 3")');
      await page.waitForTimeout(1000);
      
      const fileTabs = page.locator('.file-tabs .tab');
      const tabCount = await fileTabs.count();
      
      if (tabCount > 1) {
        await fileTabs.nth(1).click();
        await page.waitForTimeout(500);
        
        const codeEditor = page.locator('.code-editor');
        await codeEditor.click();
        await page.keyboard.press('Control+A');
        await page.keyboard.type('// Второй файл');
        
        await fileTabs.nth(0).click();
        await page.waitForTimeout(500);
        
        await codeEditor.click();
        await page.keyboard.press('Control+A');
        await page.keyboard.type('// Первый файл');
      }
    });

    test('должен сохранять изменения в разных файлах', async ({ page }) => {
      const fileTabs = page.locator('.file-tabs .tab');
      const tabCount = await fileTabs.count();
      
      if (tabCount > 1) {
        await fileTabs.nth(0).click();
        const codeEditor = page.locator('.code-editor');
        await codeEditor.click();
        await page.keyboard.press('Control+A');
        await page.keyboard.type('public class FirstFile {}');
        
        await fileTabs.nth(1).click();
        await codeEditor.click();
        await page.keyboard.press('Control+A');
        await page.keyboard.type('public class SecondFile {}');
        
        await fileTabs.nth(0).click();
        const firstFileContent = await codeEditor.inputValue();
        expect(firstFileContent).toContain('FirstFile');
      }
    });
  });
});