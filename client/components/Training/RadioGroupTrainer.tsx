import React, { useState } from 'react';
import './Training.css';
import { generateId, generateName } from '../../utils/attributeGenerator';

/** Опция радио-кнопки для группы */
interface RadioOption {
  /** Значение опции */
  value: string;
  /** Отображаемый текст метки */
  label: string;
  /** Уникальный идентификатор опции */
  id?: string;
}

/** Конфигурация тренажера для группы радио-кнопок */
interface RadioGroupTrainerConfig {
  /** Имя группы радио-кнопок */
  groupName: string;
  /** Массив опций для отображения */
  options: RadioOption[];
  /** Селектор целевой опции */
  targetSelector: string;
  /** Стили страницы тренажера */
  pageStyle?: {
    /** Цвет фона страницы */
    backgroundColor?: string;
  };
  /** Заголовок формы */
  formTitle?: string;
  /** Описание формы */
  formDescription?: string;
  /** Показывать ли кнопку отправки */
  submitButton?: boolean;
  /** ID упражнения */
  exerciseId?: string;
}

/** Пропсы компонента RadioGroupTrainer */
interface RadioGroupTrainerProps {
  /** Конфигурация тренажера */
  config: RadioGroupTrainerConfig;
}

/**
 * Компонент тренажера для отработки взаимодействия с группой радио-кнопок
 * Создает интерактивную форму с радио-кнопками для обучения работе с формами
 * 
 * @param props - Пропсы компонента
 * @param props.config - Объект конфигурации тренажера
 * @returns JSX элемент тренажера с группой радио-кнопок
 */
const RadioGroupTrainer: React.FC<RadioGroupTrainerProps> = ({ config }) => {
  const {
    groupName,
    options,
    pageStyle,
    formTitle = 'Опрос',
    formDescription = '',
    submitButton = false
  } = config;

  /** 
   * Сгенерированные уникальные атрибуты для радио-кнопок
   */
  const [generatedAttrs] = useState(() => {
    return {
      groupName: generateName(),
      options: config.options.map((option) => ({
        id: generateId(),
        value: generateName(),
        originalValue: option.value
      }))
    };
  });

  return (
    <div
      key={config.exerciseId}
      className="training-container radio-group-trainer"
      style={pageStyle}
    >
      <div className="form-wrapper survey-form">
        <h3 className="form-title">{formTitle}</h3>
        {formDescription && <p className="form-description">{formDescription}</p>}
        <form className="real-form" onSubmit={(e) => e.preventDefault()}>
          <div className="radio-group">
            {options.map((option, index) => {
              const isTarget = config.targetSelector.includes(`value='${option.value}'`);

              return (
                <div key={index} className="radio-option">
                  <input
                    type="radio"
                    id={generatedAttrs.options[index].id}
                    name={generatedAttrs.groupName}
                    value={generatedAttrs.options[index].value}
                    data-target={isTarget ? 'true' : undefined}
                  />
                  <label htmlFor={generatedAttrs.options[index].id}>
                    {option.label}
                  </label>
                </div>
              );
            })}
          </div>
          {submitButton && (
            <div className="form-actions">
              <button type="submit" className="submit-button">
                Отправить
              </button>
            </div>
          )}
        </form>
      </div>
    </div>
  );
};

export default RadioGroupTrainer;
