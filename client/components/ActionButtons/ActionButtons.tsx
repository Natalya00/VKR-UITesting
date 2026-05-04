import React from 'react';
import './ActionButtons.css';

interface ActionButtonsProps {
  onShowReference: () => void;
  onShowHint: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({
  onShowReference,
  onShowHint
}) => {
  return (
    <div className="action-buttons">
      <button className="action-button" onClick={onShowReference}>
        Справочная информация
      </button>
      <button className="action-button" onClick={onShowHint}>
        Подсказка
      </button>
    </div>
  );
};

export default ActionButtons;
