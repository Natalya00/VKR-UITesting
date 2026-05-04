import { useCallback } from 'react';

export interface SyntaxValidationResult {
  isValid: boolean;
  error: string;
}

export const useXPathValidator = () => {
  const validateSyntax = useCallback((xpath: string): SyntaxValidationResult => {
    if (!xpath.trim()) {
      return { isValid: false, error: 'Введите XPath запрос' };
    }

    if (!xpath.startsWith('/') && !xpath.startsWith('//')) {
      return { isValid: false, error: 'XPath должен начинаться с / или //' };
    }

    if (xpath === '//' || xpath === '/') {
      return { isValid: false, error: `Неполное XPath выражение. Укажите элемент после ${xpath}` };
    }

    const openBrackets = (xpath.match(/\[/g) || []).length;
    const closeBrackets = (xpath.match(/\]/g) || []).length;
    if (openBrackets !== closeBrackets) {
      return { isValid: false, error: 'Несбалансированные квадратные скобки' };
    }

    const singleQuotes = (xpath.match(/'/g) || []).length;
    const doubleQuotes = (xpath.match(/"/g) || []).length;
    if (singleQuotes % 2 !== 0 || doubleQuotes % 2 !== 0) {
      return { isValid: false, error: 'Несбалансированные кавычки' };
    }

    return { isValid: true, error: '' };
  }, []);

  return { validateSyntax };
};
