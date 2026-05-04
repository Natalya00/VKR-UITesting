import React from 'react';
import './ResultCard.css';

interface ResultCardProps {
  resultText: string;
  infoBadge?: string;
  stabilityText?: string;
}

const ResultCard: React.FC<ResultCardProps> = ({ resultText, infoBadge, stabilityText }) => {
  return (
    <div className="card results-card">
      <h3 className="card-title">Результат</h3>
      <p className="result-text">{resultText}</p>
      {stabilityText && <p className="stability-text">{stabilityText}</p>}
      {infoBadge && <span className="info-badge">{infoBadge}</span>}
    </div>
  );
};

export default ResultCard;
