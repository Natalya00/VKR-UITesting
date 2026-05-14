/** Набор символов для генерации случайных строк */
const CHARS = 'abcdefghijklmnopqrstuvwxyz0123456789';

/**
 * Генерирует случайную строку из букв и цифр
 * @param length - Длина генерируемой строки (по умолчанию 4)
 * @returns Случайная строка указанной длины
 */
const generateHash = (length: number = 4): string => {
  let result = '';
  for (let i = 0; i < length; i++) {
    result += CHARS.charAt(Math.floor(Math.random() * CHARS.length));
  }
  return result;
};


/**
 * Генерирует динамический атрибут в формате "xxxx-xxxx"
 * Используется для создания уникальных значений атрибутов в тестовых HTML-страницах
 * @returns Строка в формате "xxxx-xxxx" (например, "a1b2-c3d4")
 */
export const generateDynamicAttribute = (): string => {
  const part1 = generateHash(4);
  const part2 = generateHash(4);
  return `${part1}-${part2}`;
};


/**
 * Генерирует динамический атрибут заданной длины в формате "xxxx-xxxx"
 * @param length - Общая длина строки без учета дефиса (по умолчанию 8)
 * @returns Строка в формате "xxxx-xxxx" с указанной общей длиной
 */
export const generateDynamicAttributeWithLength = (length: number = 8): string => {
  if (length % 2 === 0) {
    const part1 = generateHash(length / 2);
    const part2 = generateHash(length / 2);
    return `${part1}-${part2}`;
  } else {
    const part1 = generateHash(Math.floor(length / 2));
    const part2 = generateHash(Math.ceil(length / 2));
    return `${part1}-${part2}`;
  }
};


/**
 * Генерирует случайную строку без дефисов
 * @param length - Длина строки (по умолчанию 8)
 * @returns Случайная строка из букв и цифр
 */
export const generateRandomString = (length: number = 8): string => {
  return generateHash(length);
};


/**
 * Возвращает существующее значение, если оно соответствует формату, иначе генерирует новое
 * @param originalValue - Исходное значение для проверки
 * @returns Исходное значение или новый динамический атрибут
 */
export const getDynamicValue = (originalValue?: string): string => {
  if (originalValue && /^[a-z0-9]{4}-[a-z0-9]{4}$/.test(originalValue)) {
    return originalValue;
  }
  return generateDynamicAttribute();
};


/**
 * Генерирует ID атрибут 
 * @param length - Длина
 * @param preserveSuffix - Суффикс для сохранения
 * @param preservePrefix - Префикс для сохранения
 * @returns Динамический атрибут
 */
export const generateId = (length?: number, preserveSuffix?: string, preservePrefix?: string): string => {
  return generateDynamicAttribute();
};


/**
 * Генерирует значение для атрибута name
 * @param wordCount - Количество слов
 * @returns Динамический атрибут
 */
export const generateName = (wordCount?: number): string => {
  return generateDynamicAttribute();
};


/**
 * Генерирует значение для атрибута class
 * @param wordCount - Количество слов
 * @returns Динамический атрибут
 */
export const generateClass = (wordCount?: number): string => {
  return generateDynamicAttribute();
};


/**
 * Генерирует значение для data-атрибута
 * @returns Динамический атрибут
 */
export const generateDataId = (): string => {
  return generateDynamicAttribute();
};


/**
 * Генерирует простой хеш заданной длины
 * @param length - Длина хеша (по умолчанию 6)
 * @returns Случайная строка указанной длины
 */
export const generateSimpleHash = (length?: number): string => {
  return generateRandomString(length || 6);
};


/**
 * Заменяет атрибут на новое значение
 * @param originalValue - Исходное значение
 * @returns Новое или существующее значение атрибута
 */
export const replaceAttribute = (originalValue?: string): string => {
  return getDynamicValue(originalValue);
};


/**
 * Генерирует новый атрибут
 * @returns Новый динамический атрибут
 */
export const generateNewAttribute = (): string => {
  return generateDynamicAttribute();
};


/**
 * Генерирует случайное значение атрибута в зависимости от типа
 * @param type - Тип атрибута ('class' | 'id' | 'name' | 'data')
 * @returns Динамический атрибут
 */
export const generateRandomAttributeValue = (type?: 'class' | 'id' | 'name' | 'data'): string => {
  return generateDynamicAttribute();
};

/**
 * Объект с методами для генерации динамических атрибутов
 * Предоставляет удобный API для создания случайных значений атрибутов в тестах
 */
export default {
  generate: generateDynamicAttribute,
  generateWithLength: generateDynamicAttributeWithLength,
  random: generateRandomString,
  getValue: getDynamicValue,
  generateId,
  generateName,
  generateClass,
  generateDataId,
  generateSimpleHash,
  replaceAttribute,
  generateNewAttribute,
  generateRandomAttributeValue
};