import React, { useState, useEffect, useMemo } from 'react';
import './Training.css';
import { generateId, generateName, generateClass } from '../../utils/attributeGenerator';

/** Простой элемент формы (поле ввода или кнопка) */
interface SimpleElement {
  /** Тип HTML элемента: input или button */
  tag: 'input' | 'button';
  /** Уникальный идентификатор элемента */
  id?: string;
  /** Атрибут name элемента */
  name?: string;
  /** CSS класс элемента */
  className?: string;
  /** Тип input элемента (text, email, password и т.д.) */
  type?: string;
  /** Плейсхолдер для input */
  placeholder?: string;
  /** Значение элемента */
  value?: string;
  /** Текст кнопки (для button) */
  text?: string;
}

/** Конфигурация тренажера для простого элемента формы */
interface SimpleElementTrainerConfig {
  /** Основной элемент для отработки взаимодействия */
  element: SimpleElement;
  /** Селектор целевого элемента для взаимодействия */
  targetSelector: string;
  /** Стили страницы тренажера */
  pageStyle?: {
    /** Цвет фона страницы */
    backgroundColor?: string;
  };
  /** Заголовок формы */
  formTitle?: string;
  /** Дополнительные поля для отображения в форме */
  additionalFields?: SimpleElement[];
  /** ID упражнения */
  exerciseId?: string;
}

/** Пропсы компонента SimpleElementTrainer */
interface SimpleElementTrainerProps {
  /** Конфигурация тренажера */
  config: SimpleElementTrainerConfig;
}

/**
 * Компонент тренажера для отработки взаимодействия с простыми элементами формы
 * Создает форму с основным элементом (input/button) и дополнительными полями
 * 
 * @param props - Пропсы компонента
 * @param props.config - Объект конфигурации тренажера
 * @returns JSX элемент тренажера с формой и элементами управления
 */
const SimpleElementTrainer: React.FC<SimpleElementTrainerProps> = ({ config }) => {
  const { element, pageStyle, formTitle = 'Форма', additionalFields = [] } = config;

  const [generatedAttrs, setGeneratedAttrs] = useState<Record<string, any>>({});

  useEffect(() => {
    const attrs: Record<string, any> = {};

    if (config.element.id) attrs.elementId = generateId();
    if (config.element.name) attrs.elementName = generateName();
    if (config.element.className) attrs.elementClass = generateClass();

    (config.additionalFields || []).forEach((field, idx) => {
      if (field.id) attrs[`field${idx}Id`] = generateId();
      if (field.name) attrs[`field${idx}Name`] = generateName();
      if (field.className) attrs[`field${idx}Class`] = generateClass();
    });

    setGeneratedAttrs(attrs);
  }, [config.element.id, config.element.name, config.element.className]);

  /**
   * Рендерит отдельный элемент формы на основе конфигурации
   * Применяет сгенерированные уникальные атрибуты к целевому элементу
   * 
   * @param el - Конфигурация элемента для рендеринга
   * @param index - Индекс элемента в массиве дополнительных полей
   * @param isTarget - Является ли элемент целевым для взаимодействия
   * @returns JSX элемент (input или button) или null
   */
  const renderElement = (el: SimpleElement, index?: number, isTarget = false) => {
    const { tag, text, id, name, className, ...props } = el;

    const elementProps: Record<string, unknown> = { ...props };

    if (isTarget) {
      if (id) elementProps.id = generatedAttrs.elementId;
      if (name) elementProps.name = generatedAttrs.elementName;
      if (className) (elementProps as Record<string, unknown>).className = generatedAttrs.elementClass;
      (elementProps as Record<string, unknown>)['data-target'] = 'true';
    } else if (index !== undefined) {
      if (id) elementProps.id = generatedAttrs[`field${index}Id`];
      if (name) elementProps.name = generatedAttrs[`field${index}Name`];
      if (className) (elementProps as Record<string, unknown>).className = generatedAttrs[`field${index}Class`];
    }

    if (tag === 'input') {
      return <input key={index} {...elementProps} />;
    }

    if (tag === 'button') {
      return (
        <button key={index} {...elementProps}>
          {text || 'Кнопка'}
        </button>
      );
    }

    return null;
  };

  const allFields = useMemo(() => {
    const fields: Array<{ element: SimpleElement; index?: number; isTarget: boolean }> = [];

    additionalFields.forEach((field, idx) => {
      if (field.tag === 'button') {
        fields.push({ element, index: undefined, isTarget: true });
      }
      fields.push({ element: field, index: idx, isTarget: false });
    });

    if (!additionalFields.some(f => f.tag === 'button')) {
      fields.push({ element, index: undefined, isTarget: true });
    }

    return fields;
  }, [element, additionalFields]);

  return (
    <div
      key={config.exerciseId}
      className="training-container simple-element-trainer"
      style={pageStyle}
    >
      <div className="form-wrapper">
        <h3 className="form-title">{formTitle}</h3>
        <form className="real-form" onSubmit={(e) => e.preventDefault()}>
          {allFields.map((field, idx) => (
            <div key={idx} className="form-group">
              {renderElement(field.element, field.index, field.isTarget)}
            </div>
          ))}
        </form>
      </div>
    </div>
  );
};

export default SimpleElementTrainer;
