import { useState, useCallback, RefObject, useRef, useEffect } from 'react';

/**
 * Хук для выполнения XPath запросов и подсветки элементов
 * Обеспечивает поиск элементов по XPath и их визуальную подсветку
 * @param containerRef - Ссылка на контейнер, в котором выполняется поиск
 * @returns Объект с методами для выполнения XPath и управления подсветкой
 */
export const useXPathExecution = (containerRef: RefObject<HTMLElement>) => {
  const [elementsFound, setElementsFound] = useState(0);
  const [highlightedElements, setHighlightedElements] = useState<Element[]>([]);
  const highlightedElementsRef = useRef<Element[]>([]);

  useEffect(() => {
    highlightedElementsRef.current = highlightedElements;
  }, [highlightedElements]);

  /**
   * Выполняет XPath запрос в указанном контейнере
   * @param xpath - XPath выражение для выполнения
   * @returns Массив найденных элементов
   */
  const executeXPath = useCallback((xpath: string): Element[] => {
    if (!containerRef.current) return [];

    try {
      const result = document.evaluate(
        xpath,
        containerRef.current,
        null,
        XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,
        null
      );

      const elements: Element[] = [];
      for (let i = 0; i < result.snapshotLength; i++) {
        const node = result.snapshotItem(i);
        if (node && node.nodeType === Node.ELEMENT_NODE) {
          const element = node as Element;
          if (containerRef.current!.contains(element)) {
            elements.push(element);
          }
        }
      }

      return elements;
    } catch {
      return [];
    }
  }, [containerRef]);

  /**
   * Подсвечивает указанные элементы на странице
   * @param elements - Массив элементов для подсветки
   */
  const highlightElements = useCallback((elements: Element[]) => {
    highlightedElementsRef.current.forEach(el => el.classList.remove('xpath-highlight'));
    elements.forEach(el => el.classList.add('xpath-highlight'));
    setHighlightedElements(elements);
  }, []);

  /**
   * Очищает всю подсветку элементов
   */
  const clearHighlight = useCallback(() => {
    highlightedElementsRef.current.forEach(el => el.classList.remove('xpath-highlight'));
    setHighlightedElements([]);
  }, []);

  return {
    elementsFound,
    setElementsFound,
    highlightedElements,
    executeXPath,
    highlightElements,
    clearHighlight,
  };
};
