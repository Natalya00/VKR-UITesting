import { chromium, FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  const { baseURL } = config.projects[0].use;
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  try {
    await page.goto(baseURL || 'http://localhost:3000', { 
      waitUntil: 'networkidle',
      timeout: 60000 
    });
    
    const response = await page.request.get('http://localhost:8080/actuator/health');
    if (response.ok()) {
      
    } else {
      
    }
    
    try {
      await page.goto(`${baseURL}/register`);
      await page.fill('input[name="email"]', 'e2e-test@example.com');
      await page.fill('input[name="password"]', 'TestPassword123!');
      await page.fill('input[name="confirmPassword"]', 'TestPassword123!');
      await page.click('button[type="submit"]');
      
      await page.waitForURL(/\/(progress|xpath-simulator|element-simulator)/, { timeout: 10000 });
      
      await page.click('text=Выйти');
      await page.waitForURL('/login');
      
    } catch (error) {
      
    }
    
  } catch (error) {
    throw error;
  } finally {
    await browser.close();
  }
}

export default globalSetup;