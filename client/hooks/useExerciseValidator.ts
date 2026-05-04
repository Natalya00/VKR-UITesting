import { useCallback } from 'react';

export interface ExerciseSyntaxRules {
  requiredPatterns?: string[];
  forbiddenPatterns?: string[];
  allowedAxes?: string[];
  maxComplexity?: number;
  requireAllTargets?: boolean;
  exactOnly?: {
    attributes?: string[];
    functions?: string[];
    axes?: string[];
  };
  requireAttrValue?: { [attr: string]: string };
}

export interface ExerciseValidationResult {
  isValid: boolean;
  error: string;
}

export const useExerciseValidator = () => {
  const validateExerciseSyntax = useCallback(
    (xpath: string, rules?: ExerciseSyntaxRules): ExerciseValidationResult => {
      if (!rules) return { isValid: true, error: '' };

      if (rules.requiredPatterns) {
        for (const pattern of rules.requiredPatterns) {
          if (!xpath.includes(pattern)) {
            return { isValid: false, error: `Используйте ${pattern} в вашем XPath` };
          }
        }
      }

      if (rules.forbiddenPatterns) {
        for (const pattern of rules.forbiddenPatterns) {
          if (xpath.includes(pattern)) {
            return { isValid: false, error: `Не используйте ${pattern} - найдите более устойчивый способ` };
          }
        }
      }

      if (rules.allowedAxes) {
        const axisPattern = /([a-z-]+)::/gi;
        const matches = xpath.matchAll(axisPattern);
        for (const match of matches) {
          if (!rules.allowedAxes.includes(match[1])) {
            return { isValid: false, error: `Ось ${match[1]}:: не подходит для этого задания` };
          }
        }
      }

      if (rules.maxComplexity !== undefined) {
        const predicates = (xpath.match(/\[/g) || []).length;
        if (predicates > rules.maxComplexity) {
          return { isValid: false, error: `Упростите XPath (слишком много условий)` };
        }
      }

      if (rules.exactOnly) {
        if (rules.exactOnly.attributes) {
          const usedAttrs = xpath.match(/@([\w-]+)/g)?.map(a => a.substring(1)) || [];
          const allowedAttrs = rules.exactOnly.attributes;
          const forbiddenAttrs = usedAttrs.filter(attr => !allowedAttrs.includes(attr));
          if (forbiddenAttrs.length > 0) {
            return {
              isValid: false,
              error: `Используйте только атрибут(ы): ${allowedAttrs.join(', ')}. Найден(ы): ${forbiddenAttrs.join(', ')}`
            };
          }
          const missingAttrs = allowedAttrs.filter(attr => !usedAttrs.includes(attr));
          if (missingAttrs.length > 0) {
            return {
              isValid: false,
              error: `Необходимо использовать атрибут: ${missingAttrs.join(', ')}`
            };
          }
        }

        if (rules.exactOnly.functions) {
          const usedFunctions = xpath.match(/\b([a-z-]+)\s*\(/gi)?.map(f => f.replace('(', '').toLowerCase()) || [];
          const allowedFunctions = rules.exactOnly.functions.map(f => f.toLowerCase());
          const allowedFunctionsSet = new Set(allowedFunctions);
          const forbiddenFunctions = usedFunctions.filter(fn => !allowedFunctions.includes(fn));

          if (forbiddenFunctions.length > 0) {
            return {
              isValid: false,
              error: `Используйте только функцию(и): ${allowedFunctions.join(', ')}. Найдено(ы): ${forbiddenFunctions.join(', ')}`
            };
          }
        }

        if (rules.exactOnly.axes) {
          const usedAxes = [...(xpath.matchAll(/([a-z-]+)::/gi))].map(m => m[1]) || [];
          const allowedAxes = rules.exactOnly.axes;
          const forbiddenAxes = usedAxes.filter(axis => !allowedAxes.includes(axis));
          if (forbiddenAxes.length > 0) {
            return {
              isValid: false,
              error: `Используйте только ось(и): ${allowedAxes.join(', ')}. Найдено(ы): ${forbiddenAxes.join(', ')}`
            };
          }
        }
      }

      if (rules.requireAttrValue) {
        for (const [attr, expectedValue] of Object.entries(rules.requireAttrValue)) {
          const attrPattern = new RegExp(`@${attr}\\s*=\\s*['"]([^'"]+)['"]`, 'g');
          const matches = [...xpath.matchAll(attrPattern)];

          if (matches.length === 0) {
            return {
              isValid: false,
              error: `Используйте атрибут @${attr} со значением "${expectedValue}"`
            };
          }

          const hasCorrectValue = matches.some(m => m[1] === expectedValue);
          if (!hasCorrectValue) {
            const foundValues = matches.map(m => m[1]).join(', ');
            return {
              isValid: false,
              error: `Неверное значение @${attr}. Ожидается "${expectedValue}", найдено: ${foundValues}`
            };
          }
        }
      }

      return { isValid: true, error: '' };
    },
    []
  );

  return { validateExerciseSyntax };
};
