import React from 'react';
import './ActionButtons.css';

/**
 * Пропсы компонента ActionButtons
 */
interface ActionButtonsProps {
  /** Обработчик открытия справочных материалов */
  onShowReference: () => void;
  /** Обработчик открытия подсказки */
  onShowHint: () => void;
}

/**
 * Компонент кнопок действий
 * Предоставляет кнопки для открытия справочных материалов и подсказок
 * Используется в заголовке упражнений
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент с кнопками действий
 */
const ActionButtons: React.FC<ActionButtonsProps> = ({
  onShowReference,
  onShowHint
}) => {
  return (
    <div className="action-buttons">
      {/* Кнопка открытия справочных материалов */}
      <button className="action-button" onClick={onShowReference}>
        Справочная информация
      </button>
      {/* Кнопка открытия подсказки */}
      <button className="action-button" onClick={onShowHint}>
        Подсказка
      </button>
    </div>
  );
};

export default ActionButtons;
