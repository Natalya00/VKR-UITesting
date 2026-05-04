export interface StabilityResult {
  score: number;
  level: string;
  reasons: string[];
  text: string;
}

export const evaluateStability = (xpath: string, foundElements: Element[]): StabilityResult => {
  if (foundElements.length === 0) {
    return { score: 0, level: '', reasons: [], text: '' };
  }

  let score = 50;
  const reasons: string[] = [];

  if (/@id\s*=/.test(xpath)) {
    score += 40;
    reasons.push('ID');
  }
  if (/@data-testid/.test(xpath) || /@data-test/.test(xpath)) {
    score += 35;
    reasons.push('test-атрибут');
  }
  if (/@name\s*=/.test(xpath) && xpath.includes('input')) {
    score += 25;
    reasons.push('name');
  }

  if (/@class\s*=/.test(xpath)) {
    score += 10;
    reasons.push('class');
  }
  if (/contains\(@class/.test(xpath)) {
    score += 5;
    reasons.push('частичный class');
  }
  if (/@type\s*=/.test(xpath)) {
    score += 15;
    reasons.push('type');
  }
  if (/@role\s*=/.test(xpath)) {
    score += 20;
    reasons.push('role');
  }

  if (/text\(\)\s*=/.test(xpath)) {
    score -= 20;
    reasons.push('⚠ точный текст');
  }
  if (/contains\(text\(\)/.test(xpath)) {
    score -= 10;
    reasons.push('⚠ частичный текст');
  }
  if (/\[\d+\]/.test(xpath)) {
    score -= 30;
    reasons.push('⚠ индекс');
  }
  if (/position\(\)/.test(xpath) && !/mod/.test(xpath)) {
    score -= 25;
    reasons.push('⚠ позиция');
  }

  score = Math.max(0, Math.min(100, score));

  let level = 'средняя';
  if (score >= 80) level = 'очень высокая';
  else if (score >= 60) level = 'высокая';
  else if (score >= 40) level = 'средняя';
  else if (score >= 20) level = 'низкая';
  else level = 'очень низкая';

  const reasonText = reasons.length > 0 ? ` (${reasons.join(', ')})` : '';
  const text = `Устойчивость: ${level}${reasonText}`;

  return { score, level, reasons, text };
};
