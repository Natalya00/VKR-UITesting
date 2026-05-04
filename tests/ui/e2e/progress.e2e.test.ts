import { test, expect } from '@playwright/test';

test.describe('Progress Page E2E Tests', () => {
    const TEST_USER = {
      email: `progress_fixed_${Math.random().toString(36).substring(2, 15)}@example.com`,
      password: 'TestPassword123!'
    };

    let isUserCreated = false;

    async function registerAndLogin(page: any) {
      if (isUserCreated) {
        await page.goto('/login');
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
      await registerAndLogin(page);
      
      await page.goto('/progress');
      await expect(page).toHaveURL('/progress');
    });

  test.describe('Отображение страницы прогресса', () => {
    test('должен отображать основные элементы страницы', async ({ page }) => {
      await page.waitForLoadState('networkidle');
      
      await expect(page.locator('.page-title')).toContainText('Ваш прогресс');
      
      await expect(page.locator('.stats-cards')).toBeVisible();
      await expect(page.locator('.stat-card')).toHaveCount(3);
      
      await expect(page.locator('.modules-container')).toBeVisible();
      
      await expect(page.locator('.module-card')).toHaveCount(3, { timeout: 10000 });
      const moduleCards = page.locator('.module-card');
      await expect(moduleCards.first()).toBeVisible();
    });

    test('должен показывать информацию о всех модулях', async ({ page }) => {
      await expect(page.locator('.module-card')).toHaveCount(3, { timeout: 10000 });
      const moduleCards = page.locator('.module-card');
      
      const moduleTitle1 = await page.locator('.module-title').nth(0).textContent();
      const moduleTitle2 = await page.locator('.module-title').nth(1).textContent();
      const moduleTitle3 = await page.locator('.module-title').nth(2).textContent();
      
      const titles = [moduleTitle1, moduleTitle2, moduleTitle3];
      expect(titles).toContain('Page Object Model (POM)');
      expect(titles).toContain('Взаимодействие с элементами');
      expect(titles).toContain('XPath');
      
      for (let i = 0; i < 3; i++) {
        const moduleCard = moduleCards.nth(i);
        await expect(moduleCard.locator('.module-percentage')).toBeVisible();
        await expect(moduleCard.locator('.module-stats')).toBeVisible();
        await expect(moduleCard.locator('.module-stats')).toContainText(/из/);
      }
    });

    test('должен отображать процент выполнения для каждого модуля', async ({ page }) => {
      const moduleCards = page.locator('.module-card');
      
      for (let i = 0; i < await moduleCards.count(); i++) {
        const moduleCard = moduleCards.nth(i);
        
        const percentage = moduleCard.locator('.module-percentage');
        await expect(percentage).toBeVisible();
        const percentText = await percentage.textContent();
        expect(percentText).toMatch(/\d+\.\d+%/);
        
        const stats = moduleCard.locator('.module-stats');
        await expect(stats).toContainText(/\d+ из \d+ упражнений/);
      }
    });

    test('должен показывать общую статистику пользователя', async ({ page }) => {
      const statCards = page.locator('.stat-card');
      
      await expect(statCards.nth(0).locator('.stat-label')).toContainText('Выполнено упражнений');
      const completedValue = statCards.nth(0).locator('.stat-value');
      await expect(completedValue).toBeVisible();
      expect(await completedValue.textContent()).toMatch(/\d+/);
      
      await expect(statCards.nth(1).locator('.stat-label')).toContainText('Всего упражнений');
      const totalValue = statCards.nth(1).locator('.stat-value');
      await expect(totalValue).toBeVisible();
      expect(await totalValue.textContent()).toMatch(/\d+/);
      
      await expect(statCards.nth(2).locator('.stat-label')).toContainText('Общий прогресс');
      const progressValue = statCards.nth(2).locator('.stat-value');
      await expect(progressValue).toBeVisible();
      expect(await progressValue.textContent()).toMatch(/\d+\.\d+%/);
    });

    test('должен показывать подсказку о кликабельности модулей', async ({ page }) => {
      await expect(page.locator('.modules-hint')).toBeVisible();
      await expect(page.locator('.modules-hint')).toContainText('Нажмите на модуль для просмотра детальной информации');
      
      const moduleCards = page.locator('.module-card.clickable');
      expect(await moduleCards.count()).toBe(3);
      
      await expect(page.locator('.click-hint').first()).toContainText('Подробнее');
      
      const clickHints = page.locator('.click-hint');
      const hintCount = await clickHints.count();
      expect(hintCount).toBe(3);
      
      for (let i = 0; i < hintCount; i++) {
        await expect(clickHints.nth(i)).toContainText('Подробнее');
      }
    });
  });

  test.describe('Интерактивность страницы прогресса', () => {
    test('должен открывать детальную информацию при клике на модуль', async ({ page }) => {
      const firstModule = page.locator('.module-card.clickable').first();
      await firstModule.click();
      
      await expect(page.locator('.module-details-overlay')).toBeVisible();
      await expect(page.locator('.module-details-modal')).toBeVisible();
      
      await expect(page.locator('.module-details-header h2')).toBeVisible();
      
      await expect(page.locator('.close-button')).toBeVisible();
    });

    test('должен закрывать модальное окно при клике на крестик', async ({ page }) => {
      await page.locator('.module-card.clickable').first().click();
      await expect(page.locator('.module-details-overlay')).toBeVisible();
      
      await page.locator('.close-button').click();
      await expect(page.locator('.module-details-overlay')).not.toBeVisible();
    });

    test('должен закрывать модальное окно при клике на оверлей', async ({ page }) => {
      await page.locator('.module-card.clickable').first().click();
      await expect(page.locator('.module-details-overlay')).toBeVisible();
      
      await page.locator('.module-details-overlay').click({ position: { x: 10, y: 10 } });
      await expect(page.locator('.module-details-overlay')).not.toBeVisible();
    });

    test('должен показывать детальную статистику модуля', async ({ page }) => {
      await page.locator('.module-card.clickable').first().click();
      
      await expect(page.locator('.module-summary')).toBeVisible();
      await expect(page.locator('.summary-stats')).toBeVisible();
      
      const statItems = page.locator('.stat-item');
      expect(await statItems.count()).toBeGreaterThanOrEqual(5);
      
      await expect(page.locator('.stat-item')).toContainText(['Выполнено упражнений', 'Прогресс', 'Всего попыток']);
    });

    test('должен обновлять прогресс при обновлении страницы', async ({ page }) => {
      const initialProgress = await page.locator('.stat-card').nth(2).locator('.stat-value').textContent();
      
      await page.reload();
      await page.waitForLoadState('networkidle');
      
      const updatedProgress = await page.locator('.stat-card').nth(2).locator('.stat-value').textContent();
      expect(updatedProgress).toBe(initialProgress);
    });
  });

  test.describe('Визуализация прогресса', () => {
    test('должен отображать прогресс-бары для модулей', async ({ page }) => {
      await expect(page.locator('.module-card')).toHaveCount(3, { timeout: 10000 });
      
      const progressBarContainers = page.locator('.module-card .progress-bar-container');
      await expect(progressBarContainers).toHaveCount(3, { timeout: 5000 });
      
      const progressBars = page.locator('.module-card .progress-bar');
      await expect(progressBars).toHaveCount(3, { timeout: 5000 });
      
      const barCount = await progressBars.count();
      
      for (let i = 0; i < barCount; i++) {
        const progressBar = progressBars.nth(i);
        
        await expect(progressBar).toBeAttached();
        
        const style = await progressBar.getAttribute('style');
        expect(style).toContain('width:');
        expect(style).toMatch(/width:\s*\d+(\.\d+)?%/);
        
        const container = progressBarContainers.nth(i);
        await expect(container).toBeVisible();
      }
    });

    test('должен показывать круговую диаграмму прогресса', async ({ page }) => {
      await expect(page.locator('.chart-container')).toBeVisible();
      await expect(page.locator('.chart-container .section-title')).toContainText('Общий прогресс');
      
      await expect(page.locator('.recharts-wrapper')).toBeVisible();
      
      const pieElements = page.locator('.recharts-pie');
      if (await pieElements.count() > 0) {
        await expect(pieElements.first()).toBeVisible();
      }
    });
  });

  test.describe('Детальная информация о модуле', () => {
    test('должен показывать список упражнений в детальном виде', async ({ page }) => {
      await page.locator('.module-card.clickable').first().click();
      
      await expect(page.locator('.exercises-list')).toBeVisible();
      await expect(page.locator('.exercises-list h3')).toContainText('Детальная информация по упражнениям');
      
      const exerciseItems = page.locator('.exercise-item');
      const exerciseCount = await exerciseItems.count();
      
      if (exerciseCount > 0) {
        const firstExercise = exerciseItems.first();
        await expect(firstExercise.locator('.exercise-header')).toBeVisible();
        await expect(firstExercise.locator('.exercise-info')).toBeVisible();
        await expect(firstExercise.locator('.exercise-stats')).toBeVisible();
      } else {
        await expect(page.locator('.no-exercises')).toContainText('Упражнения еще не начаты');
      }
    });

    test('должен разворачивать детали упражнения при клике', async ({ page }) => {
      await page.locator('.module-card.clickable').first().click();
      
      const exerciseItems = page.locator('.exercise-item');
      const exerciseCount = await exerciseItems.count();
      
      if (exerciseCount > 0) {
        const firstExercise = exerciseItems.first();
        
        await firstExercise.locator('.exercise-header').click();
        
        await expect(firstExercise.locator('.exercise-details')).toBeVisible();
        await expect(firstExercise.locator('.exercise-summary')).toBeVisible();
        
        const expandIcon = firstExercise.locator('.expand-icon');
        expect(await expandIcon.textContent()).toBe('▼');
      }
    });
  });
});