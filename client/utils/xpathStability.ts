/**
 * Результат оценки устойчивости XPath выражения
 */
export interface StabilityResult {
  /** Числовая оценка устойчивости (0-100) */
  score: number;
  /** Уровень устойчивости (текстовое описание) */
  level: string;
  /** Массив причин, влияющих на оценку */
  reasons: string[];
  /** Полное текстовое описание результата */
  text: string;
}

/**
 * Оценивает устойчивость XPath выражения
 * Анализирует используемые атрибуты, функции и конструкции для определения
 * вероятности сохранения работоспособности при изменениях в DOM
 * @param xpath - XPath выражение для анализа
 * @param foundElements - Массив найденных элементов (для проверки работоспособности)
 * @returns Объект с оценкой устойчивости и пояснениями
 */
export const evaluateStability = (xpath: string, foundElements: Element[]): StabilityResult => {
  // Если элементы не найдены, возвращаем пустой результат
  if (foundElements.length === 0) {
    return { score: 0, level: '', reasons: [], text: '' };
  }

  // Начальная оценка - средняя
  let score = 50;
  const reasons: string[] = [];

  // Положительные факторы (увеличивают устойчивость)

  // ID - самый устойчивый атрибут
  if (/@id\s*=/.test(xpath)) {
    score += 40;
    reasons.push('ID');
  }
  // Тестовые атрибуты - очень хорошо
  if (/@data-testid/.test(xpath) || /@data-test/.test(xpath)) {
    score += 35;
    reasons.push('test-атрибут');
  }
  // Name для input элементов - хорошо
  if (/@name\s*=/.test(xpath) && xpath.includes('input')) {
    score += 25;
    reasons.push('name');
  }

  // Классы - умеренно устойчивы
  if (/@class\s*=/.test(xpath)) {
    score += 10;
    reasons.push('class');
  }
  // Частичное совпадение классов - лучше чем точное
  if (/contains\(@class/.test(xpath)) {
    score += 5;
    reasons.push('частичный class');
  }
  // Тип элемента - хорошо
  if (/@type\s*=/.test(xpath)) {
    score += 15;
    reasons.push('type');
  }
  // Role атрибут - очень хорошо для доступности
  if (/@role\s*=/.test(xpath)) {
    score += 20;
    reasons.push('role');
  }

  // Отрицательные факторы (уменьшают устойчивость)
  
  // Точный текст - может измениться
  if (/text\(\)\s*=/.test(xpath)) {
    score -= 20;
    reasons.push('⚠ точный текст');
  }
  // Частичный текст - лучше, но все еще ненадежно
  if (/contains\(text\(\)/.test(xpath)) {
    score -= 10;
    reasons.push('⚠ частичный текст');
  }
  // Индексы - очень ненадежно
  if (/\[\d+\]/.test(xpath)) {
    score -= 30;
    reasons.push('⚠ индекс');
  }
  // Позиция - ненадежно
  if (/position\(\)/.test(xpath) && !/mod/.test(xpath)) {
    score -= 25;
    reasons.push('⚠ позиция');
  }

  // Ограничиваем оценку в диапазоне 0-100
  score = Math.max(0, Math.min(100, score));

  // Определяем уровень устойчивости
  let level = 'средняя';
  if (score >= 80) level = 'очень высокая';
  else if (score >= 60) level = 'высокая';
  else if (score >= 40) level = 'средняя';
  else if (score >= 20) level = 'низкая';
  else level = 'очень низкая';

  // Формируем итоговое сообщение
  const reasonText = reasons.length > 0 ? ` (${reasons.join(', ')})` : '';
  const text = `Устойчивость: ${level}${reasonText}`;

  return { score, level, reasons, text };
};
