import { renderHook } from '@testing-library/react';
import { useCodeValidator, ValidationRules } from '../../../../client/hooks/useCodeValidator';

describe('useCodeValidator', () => {
  describe('validateSyntax', () => {
    it('должен возвращать ошибку для пустого кода', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const validationResult = validator.validateSyntax('');
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Введите код');
    });

    it('должен возвращать ошибку для кода только с пробелами', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const validationResult = validator.validateSyntax('   \n  \t  ');
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Введите код');
    });

    it('должен возвращать ошибку для несбалансированных скобок', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$("element").shouldBe(visible';
      const validationResult = validator.validateSyntax(code);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Несбалансированные скобки');
    });

    it('должен возвращать ошибку для несбалансированных кавычек', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$("element).shouldBe(visible)';
      const validationResult = validator.validateSyntax(code);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Несбалансированные кавычки');
    });

    it('должен возвращать ошибку если нет вызовов Selenide', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = 'System.out.println("Hello World");';
      const validationResult = validator.validateSyntax(code);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Код должен содержать вызовы Selenide ($, $$, byText, и т.д.)');
    });

    it('должен принимать валидный Selenide код с $', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$("element").shouldBe(visible);';
      const validationResult = validator.validateSyntax(code);
      expect(validationResult.isValid).toBe(true);
      expect(validationResult.error).toBe('');
    });

    it('должен принимать валидный Selenide код с $$', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$$("elements").shouldHave(size(3));';
      const validationResult = validator.validateSyntax(code);
      expect(validationResult.isValid).toBe(true);
      expect(validationResult.error).toBe('');
    });

    it('должен принимать валидный Selenide код с byText', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$(byText("Click me")).click();';
      const validationResult = validator.validateSyntax(code);
      expect(validationResult.isValid).toBe(true);
      expect(validationResult.error).toBe('');
    });
  });

  describe('validateCode', () => {
    it('должен возвращать true если правила не заданы', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$("element").shouldBe(visible);';
      const validationResult = validator.validateCode(code);
      expect(validationResult.isValid).toBe(true);
      expect(validationResult.error).toBe('');
    });

    it('должен проверять обязательные импорты', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { 
        requiredImports: ['import static com.codeborne.selenide.Selenide.*'] 
      };
      const code = '$("element").shouldBe(visible);';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Отсутствует импорт: import static com.codeborne.selenide.Selenide.*');
    });

    it('должен проверять обязательные методы', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { requiredMethods: ['click'] };
      const code = '$("element").shouldBe(visible);';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Используйте метод .click()');
    });

    it('должен проверять запрещенные методы', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { forbiddenMethods: ['click'] };
      const code = '$("element").click();';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Запрещено использовать метод .click()');
    });

    it('должен проверять обязательные селекторы', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { requiredSelectors: ['#submit-button'] };
      const code = '$("element").click();';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Используйте селектор: #submit-button');
    });

    it('должен проверять запрещенные селекторы', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { forbiddenSelectors: ['#forbidden'] };
      const code = '$("#forbidden").click();';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Запрещено использовать селектор: #forbidden');
    });

    it('должен проверять обязательные условия', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { requiredConditions: ['visible'] };
      const code = '$("element").click();';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Используйте условие visible');
    });

    it('должен проверять обязательные методы поиска', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const rules: ValidationRules = { requiredFinder: 'byText' };
      const code = '$("element").click();';
      const validationResult = validator.validateCode(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Используйте метод поиска: byText');
    });
  });

  describe('validateFull', () => {
    it('должен сначала проверять синтаксис', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = ''; 
      const rules: ValidationRules = { requiredMethods: ['click'] };
      const validationResult = validator.validateFull(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Введите код');
    });

    it('должен проверять правила после успешной проверки синтаксиса', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$("element").shouldBe(visible);';
      const rules: ValidationRules = { requiredMethods: ['click'] };
      const validationResult = validator.validateFull(code, rules);
      expect(validationResult.isValid).toBe(false);
      expect(validationResult.error).toBe('Используйте метод .click()'); 
    });

    it('должен возвращать успех для валидного кода с правилами', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = '$("element").click().shouldBe(visible);';
      const rules: ValidationRules = { 
        requiredMethods: ['click'],
        requiredConditions: ['visible']
      };
      const validationResult = validator.validateFull(code, rules);
      expect(validationResult.isValid).toBe(true);
      expect(validationResult.error).toBe('');
    });
  });

  describe('сложные сценарии валидации', () => {
    it('должен корректно обрабатывать код с множественными методами', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const code = `
        $("input").setValue("test").pressEnter();
        $("button").click();
        $("result").shouldBe(visible);
      `;
      const rules: ValidationRules = { 
        requiredMethods: ['setValue', 'click'],
        requiredConditions: ['visible']
      };
      const validationResult = validator.validateFull(code, rules);
      expect(validationResult.isValid).toBe(true);
    });

    it('должен корректно обрабатывать различные методы поиска', () => {
      const { result } = renderHook(() => useCodeValidator());
      const validator = result.current;
      
      const testCases = [
        { finder: '$', code: '$("element")' },
        { finder: '$$', code: '$$("elements")' },
        { finder: 'byText', code: '$(byText("text"))' },
        { finder: 'byId', code: '$(byId("id"))' },
        { finder: 'byClassName', code: '$(byClassName("class"))' },
        { finder: 'byCssSelector', code: '$(byCssSelector(".class"))' },
        { finder: 'byXpath', code: '$(byXpath("//div"))' },
        { finder: 'byName', code: '$(byName("name"))' }
      ];

      testCases.forEach(({ finder, code }) => {
        const rules: ValidationRules = { requiredFinder: finder };
        const fullCode = `${code}.click();`;
        const validationResult = validator.validateCode(fullCode, rules);
        expect(validationResult.isValid).toBe(true);
      });
    });
  });
});