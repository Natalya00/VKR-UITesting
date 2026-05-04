const CHARS = 'abcdefghijklmnopqrstuvwxyz0123456789';

const generateHash = (length: number = 4): string => {
  let result = '';
  for (let i = 0; i < length; i++) {
    result += CHARS.charAt(Math.floor(Math.random() * CHARS.length));
  }
  return result;
};


export const generateDynamicAttribute = (): string => {
  const part1 = generateHash(4);
  const part2 = generateHash(4);
  return `${part1}-${part2}`;
};


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


export const generateRandomString = (length: number = 8): string => {
  return generateHash(length);
};


export const getDynamicValue = (originalValue?: string): string => {
  if (originalValue && /^[a-z0-9]{4}-[a-z0-9]{4}$/.test(originalValue)) {
    return originalValue;
  }
  return generateDynamicAttribute();
};


export const generateId = (length?: number, preserveSuffix?: string, preservePrefix?: string): string => {
  return generateDynamicAttribute();
};


export const generateName = (wordCount?: number): string => {
  return generateDynamicAttribute();
};


export const generateClass = (wordCount?: number): string => {
  return generateDynamicAttribute();
};


export const generateDataId = (): string => {
  return generateDynamicAttribute();
};


export const generateSimpleHash = (length?: number): string => {
  return generateRandomString(length || 6);
};


export const replaceAttribute = (originalValue?: string): string => {
  return getDynamicValue(originalValue);
};


export const generateNewAttribute = (): string => {
  return generateDynamicAttribute();
};


export const generateRandomAttributeValue = (type?: 'class' | 'id' | 'name' | 'data'): string => {
  return generateDynamicAttribute();
};

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