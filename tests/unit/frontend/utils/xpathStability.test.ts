import { evaluateStability, StabilityResult } from '../../../../client/utils/xpathStability';

describe('xpathStability', () => {
  const createMockElement = (tagName: string = 'div'): Element => {
    return {
      tagName: tagName.toUpperCase(),
      getAttribute: jest.fn(),
      setAttribute: jest.fn(),
      removeAttribute: jest.fn(),
      hasAttribute: jest.fn(),
    } as unknown as Element;
  };

  const mockElements = [createMockElement()];

  describe('evaluateStability', () => {
    describe('базовые случаи', () => {
      it('должен возвращать нулевой результат для пустого массива элементов', () => {
        const result = evaluateStability('//div', []);
        expect(result.score).toBe(0);
        expect(result.level).toBe('');
        expect(result.reasons).toEqual([]);
        expect(result.text).toBe('');
      });

      it('должен начинать с базового счета 50', () => {
        const result = evaluateStability('//div', mockElements);
        expect(result.score).toBe(50);
        expect(result.level).toBe('средняя');
      });
    });

    describe('положительные факторы стабильности', () => {
      it('должен добавлять 40 баллов за ID атрибут', () => {
        const xpath = '//div[@id="unique-id"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(90); 
        expect(result.reasons).toContain('ID');
        expect(result.level).toBe('очень высокая');
      });

      it('должен добавлять 35 баллов за data-testid', () => {
        const xpath = '//div[@data-testid="login-button"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(85);
        expect(result.reasons).toContain('test-атрибут');
        expect(result.level).toBe('очень высокая');
      });

      it('должен добавлять 35 баллов за data-test', () => {
        const xpath = '//div[@data-test="submit-form"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(85); 
        expect(result.reasons).toContain('test-атрибут');
      });

      it('должен добавлять 25 баллов за name атрибут в input', () => {
        const xpath = '//input[@name="username"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(75); 
        expect(result.reasons).toContain('name');
        expect(result.level).toBe('высокая');
      });

      it('должен добавлять 20 баллов за role атрибут', () => {
        const xpath = '//div[@role="button"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(70);
        expect(result.reasons).toContain('role');
        expect(result.level).toBe('высокая');
      });

      it('должен добавлять 15 баллов за type атрибут', () => {
        const xpath = '//input[@type="submit"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(65); 
        expect(result.reasons).toContain('type');
        expect(result.level).toBe('высокая');
      });

      it('должен добавлять 10 баллов за class атрибут', () => {
        const xpath = '//div[@class="btn-primary"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(60); 
        expect(result.reasons).toContain('class');
        expect(result.level).toBe('высокая');
      });

      it('должен добавлять 5 баллов за частичный class', () => {
        const xpath = '//div[contains(@class, "btn")]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(55); 
        expect(result.reasons).toContain('частичный class');
        expect(result.level).toBe('средняя');
      });
    });

    describe('отрицательные факторы стабильности', () => {
      it('должен вычитать 20 баллов за точный текст', () => {
        const xpath = '//div[text()="Нажмите здесь"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(30);
        expect(result.reasons).toContain('⚠ точный текст');
        expect(result.level).toBe('низкая');
      });

      it('должен вычитать 10 баллов за частичный текст', () => {
        const xpath = '//div[contains(text(), "Нажмите")]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(40);
        expect(result.reasons).toContain('⚠ частичный текст');
        expect(result.level).toBe('средняя');
      });

      it('должен вычитать 30 баллов за использование индекса', () => {
        const xpath = '//div[1]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(20);
        expect(result.reasons).toContain('⚠ индекс');
        expect(result.level).toBe('низкая');
      });

      it('должен вычитать 30 баллов за индекс в середине пути', () => {
        const xpath = '//div/span[2]/button';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(20); 
        expect(result.reasons).toContain('⚠ индекс');
      });

      it('должен вычитать 25 баллов за position()', () => {
        const xpath = '//div[position()=1]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(25); 
        expect(result.reasons).toContain('⚠ позиция');
        expect(result.level).toBe('низкая');
      });

      it('не должен вычитать баллы за position() с mod', () => {
        const xpath = '//div[position() mod 2 = 0]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(50); 
        expect(result.reasons).not.toContain('⚠ позиция');
      });
    });

    describe('уровни стабильности', () => {
      it('должен определять "очень высокую" стабильность (80+)', () => {
        const xpath = '//div[@id="test"][@data-testid="button"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBeGreaterThanOrEqual(80);
        expect(result.level).toBe('очень высокая');
      });

      it('должен определять "высокую" стабильность (60-79)', () => {
          const xpath = '//div[@class="btn"][@type="button"]';
          const result = evaluateStability(xpath, mockElements);
          expect(result.score).toBeGreaterThanOrEqual(60);
          expect(result.score).toBeLessThan(80);
          expect(result.level).toBe('высокая');
      });

      it('должен определять "среднюю" стабильность (40-59)', () => {
          const xpath = '//div[contains(text(), "Click")]';
          const result = evaluateStability(xpath, mockElements);
          expect(result.score).toBeGreaterThanOrEqual(40);
          expect(result.score).toBeLessThan(60);
          expect(result.level).toBe('средняя');
      });

      it('должен определять "низкую" стабильность (20-39)', () => {
          const xpath = '//div[text()="Click me"]';
          const result = evaluateStability(xpath, mockElements);
          expect(result.score).toBeGreaterThanOrEqual(20);
          expect(result.score).toBeLessThan(40);
          expect(result.level).toBe('низкая');
      });

      it('должен определять "очень низкую" стабильность (0-19)', () => {
        const xpath = '//div[1][text()="Exact text"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBeLessThan(20);
        expect(result.level).toBe('очень низкая');
      });
    });

    describe('форматирование текста результата', () => {
      it('должен формировать текст с причинами', () => {
        const xpath = '//div[@id="test"][@class="btn"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.text).toContain('Устойчивость: очень высокая');
        expect(result.text).toContain('(ID, class)');
      });

      it('должен формировать текст без причин для базового случая', () => {
        const xpath = '//div';
        const result = evaluateStability(xpath, mockElements);
        expect(result.text).toBe('Устойчивость: средняя');
        expect(result.text).not.toContain('()');
      });

      it('должен включать предупреждающие символы в текст', () => {
        const xpath = '//div[1][text()="Click"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.text).toContain('⚠ индекс');
        expect(result.text).toContain('⚠ точный текст');
      });
    });

    describe('граничные случаи', () => {
      it('должен обрабатывать пустой xpath', () => {
        const result = evaluateStability('', mockElements);
        expect(result.score).toBe(50);
        expect(result.level).toBe('средняя');
      });

      it('должен обрабатывать сложные xpath выражения', () => {
        const xpath = '//div[@id="main"]//form[@class="login-form"]//input[@type="password" and @name="password"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBeGreaterThan(50);
        expect(result.reasons).toContain('ID');
        expect(result.reasons).toContain('class');
        expect(result.reasons).toContain('type');
        expect(result.reasons).toContain('name');
      });

      it('должен обрабатывать xpath с пробелами и переносами строк', () => {
        const xpath = `//div[@id = "test"]
                      [@class = "btn primary"]`;
        const result = evaluateStability(xpath, mockElements);
        expect(result.reasons).toContain('ID');
        expect(result.reasons).toContain('class');
      });

      it('должен ограничивать максимальный счет до 100', () => {
        const xpath = '//input[@id="test"][@data-testid="test"][@name="test"][@type="text"][@role="textbox"][@class="input"]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(100);
      });

      it('должен ограничивать минимальный счет до 0', () => {
        const xpath = '//div[1][2][3][text()="exact"][position()=1][position()=2]';
        const result = evaluateStability(xpath, mockElements);
        expect(result.score).toBe(0);
      });
    });
  });
});