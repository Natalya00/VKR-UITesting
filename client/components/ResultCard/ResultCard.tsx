import React from 'react';
import './ResultCard.css';

/**
 * Пропсы компонента ResultCard
 */
interface ResultCardProps {
  /** Основной текст результата */
  resultText: string;
  /** Дополнительная информация в виде значка */
  infoBadge?: string;
  /** Текст об устойчивости XPath */
  stabilityText?: string;
}

/**
 * Компонент карточки результата
 * Отображает результаты выполнения XPath запросов или проверки кода
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент карточки результата
 */
const ResultCard: React.FC<ResultCardProps> = ({ resultText, infoBadge, stabilityText }) => {
  return (
    <div className="card results-card">
      <h3 className="card-title">Результат</h3>
      {/* Основной текст результата */}
      <p className="result-text">{resultText}</p>
      {/* Информация об устойчивости */}
      {stabilityText && <p className="stability-text">{stabilityText}</p>}
      {/* Дополнительный значок */}
      {infoBadge && <span className="info-badge">{infoBadge}</span>}
    </div>
  );
};

export default ResultCard;
