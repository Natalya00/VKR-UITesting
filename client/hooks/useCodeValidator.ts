import { useCallback } from 'react';

/**
 * Правила валидации для Java/Selenide кода
 */
export interface ValidationRules {
  /** Обязательные методы, которые должны присутствовать в коде */
  requiredMethods?: string[];
  /** Запрещенные методы, которые не должны использоваться */
  forbiddenMethods?: string[];
  /** Обязательные селекторы */
  requiredSelectors?: string[];
  /** Запрещенные селекторы */
  forbiddenSelectors?: string[];
  /** Обязательные условия проверки */
  requiredConditions?: string[];
  /** Регулярные выражения для дополнительной валидации */
  regexPatterns?: Record<string, string>;
  /** Кастомное сообщение об ошибке */
  errorMessage?: string;
  /** Максимальная длина кода */
  maxCodeLength?: number;
  /** Минимальная длина кода */
  minCodeLength?: number;
  /** Обязательные импорты */
  requiredImports?: string[];
  /** Ожидаемое действие */
  expectedAction?: string;
  /** Ожидаемый тип элемента */
  expectedElementType?: string;
  /** Обязательный метод поиска элемента */
  requiredFinder?: string;
}

/**
 * Результат валидации кода
 */
export interface CodeValidationResult {
  /** Флаг валидности кода */
  isValid: boolean;
  /** Сообщение об ошибке (пустое, если валидно) */
  error: string;
}

/**
 * Хук для валидации Java/Selenide кода
 * Проверяет синтаксис, соответствие правилам и корректность использования Selenide API
 * @returns Объект с методами валидации
 */
export const useCodeValidator = () => {
  /**
   * Валидирует код по заданным правилам
   * @param code - Java/Selenide код для проверки
   * @param rules - Правила валидации
   * @returns Результат валидации с флагом и сообщением об ошибке
   */
  const validateCode = useCallback((code: string, rules?: ValidationRules): CodeValidationResult => {
    if (!rules) {
      return { isValid: true, error: '' };
    }

    if (rules.maxCodeLength !== undefined && code.length > rules.maxCodeLength) {
      return { isValid: false, error: `Код слишком длинный (максимум ${rules.maxCodeLength} символов)` };
    }

    if (rules.minCodeLength !== undefined && code.length < rules.minCodeLength) {
      return { isValid: false, error: `Код слишком короткий (минимум ${rules.minCodeLength} символов)` };
    }

    if (rules.requiredImports && rules.requiredImports.length > 0) {
      for (const importStr of rules.requiredImports) {
        if (!code.includes(importStr)) {
          return { isValid: false, error: `Отсутствует импорт: ${importStr}` };
        }
      }
    }

    if (rules.requiredMethods && rules.requiredMethods.length > 0) {
      for (const method of rules.requiredMethods) {
        const methodPattern = new RegExp(`\\.${method}\\s*[\(\{]`, 'g');
        if (!methodPattern.test(code)) {
          return { isValid: false, error: rules.errorMessage || `Используйте метод .${method}()` };
        }
      }
    }

    if (rules.forbiddenMethods && rules.forbiddenMethods.length > 0) {
      for (const method of rules.forbiddenMethods) {
        const methodPattern = new RegExp(`\\.${method}\\s*[\(\{]`, 'g');
        if (methodPattern.test(code)) {
          return { isValid: false, error: `Запрещено использовать метод .${method}()` };
        }
      }
    }

    if (rules.requiredSelectors && rules.requiredSelectors.length > 0) {
      for (const selector of rules.requiredSelectors) {
        if (!code.includes(selector)) {
          return { isValid: false, error: rules.errorMessage || `Используйте селектор: ${selector}` };
        }
      }
    }

    if (rules.forbiddenSelectors && rules.forbiddenSelectors.length > 0) {
      for (const selector of rules.forbiddenSelectors) {
        if (code.includes(selector)) {
          return { isValid: false, error: `Запрещено использовать селектор: ${selector}` };
        }
      }
    }

    if (rules.requiredConditions && rules.requiredConditions.length > 0) {
      for (const condition of rules.requiredConditions) {
        const conditionPattern = new RegExp(`\\.(shouldBe|shouldHave)\\s*\([^)]*${condition}[^)]*\)`, 'gi');
        if (!conditionPattern.test(code)) {
          return { isValid: false, error: rules.errorMessage || `Используйте условие ${condition}` };
        }
      }
    }

    if (rules.requiredFinder) {
      const finderPatterns: Record<string, RegExp> = {
        '$': /(?<!\$)\$\s*\(/,     
        '$$': /\$\$\s*\(/,
        'byText': /byText\s*\(/,
        'byId': /byId\s*\(/,
        'byClassName': /byClassName\s*\(/,
        'byCssSelector': /byCssSelector\s*\(/,
        'byXpath': /byXpath\s*\(/,
        'byName': /byName\s*\(/,
    };

      const pattern = finderPatterns[rules.requiredFinder];
      if (pattern && !pattern.test(code)) {
        return { isValid: false, error: `Используйте метод поиска: ${rules.requiredFinder}` };
      }
    }

    if (rules.regexPatterns) {
      for (const [name, pattern] of Object.entries(rules.regexPatterns)) {
        const regex = new RegExp(pattern);
        if (!regex.test(code)) {
          return { isValid: false, error: rules.errorMessage || `Код не соответствует паттерну: ${name}` };
        }
      }
    }

    return { isValid: true, error: '' };
  }, []);

  /**
   * Проверяет базовый синтаксис кода (скобки, кавычки, наличие Selenide вызовов)
   * @param code - Код для синтаксической проверки
   * @returns Результат проверки синтаксиса
   */
  const validateSyntax = useCallback((code: string): CodeValidationResult => {
    if (!code.trim()) {
      return { isValid: false, error: 'Введите код' };
    }

    const openBrackets = (code.match(/[\(\{]/g) || []).length;
    const closeBrackets = (code.match(/[\)\}]/g) || []).length;
    if (openBrackets !== closeBrackets) {
      return { isValid: false, error: 'Несбалансированные скобки' };
    }

    const singleQuotes = (code.match(/'/g) || []).length;
    const doubleQuotes = (code.match(/"/g) || []).length;
    if (singleQuotes % 2 !== 0 || doubleQuotes % 2 !== 0) {
      return { isValid: false, error: 'Несбалансированные кавычки' };
    }

    const hasSelenideCall = /\$(_|_\(|\(|s)/.test(code) || /by(Text|Id|ClassName|CssSelector|Xpath|Name)/.test(code);
    if (!hasSelenideCall) {
      return { isValid: false, error: 'Код должен содержать вызовы Selenide ($, $$, byText, и т.д.)' };
    }

    return { isValid: true, error: '' };
  }, []);

  /**
   * Выполняет полную валидацию: сначала синтаксис, затем правила
   * @param code - Код для полной проверки
   * @param rules - Правила валидации
   * @returns Результат полной валидации
   */
  const validateFull = useCallback((code: string, rules?: ValidationRules): CodeValidationResult => {
    const syntaxResult = validateSyntax(code);
    if (!syntaxResult.isValid) {
      return syntaxResult;
    }

    return validateCode(code, rules);
  }, [validateSyntax, validateCode]);

  return {
    validateCode,
    validateSyntax,
    validateFull,
  };
};
