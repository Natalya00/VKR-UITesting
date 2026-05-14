import React from 'react';
import './DifficultyFilter.css';

/**
 * Пропсы компонента DifficultyFilter
 */
interface DifficultyFilterProps {
  /** Текущая выбранная сложность (null = все) */
  selectedDifficulty: 'easy' | 'medium' | 'hard' | null;
  /** Обработчик смены фильтра сложности */
  onDifficultyChange: (difficulty: 'easy' | 'medium' | 'hard' | null) => void;
}

/**
 * Компонент фильтра по сложности упражнений
 * Позволяет фильтровать упражнения по уровню сложности
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент фильтра сложности
 */
const DifficultyFilter: React.FC<DifficultyFilterProps> = ({ selectedDifficulty, onDifficultyChange }) => {
  const difficulties = [
    { value: null, label: 'Все', color: '#666' },
    { value: 'easy', label: 'Легкая', color: '#4CAF50' },     
    { value: 'medium', label: 'Средняя', color: '#FF9800' },
    { value: 'hard', label: 'Сложная', color: '#F44336' },    
  ];

  return (
    <div className="difficulty-filter">
      <span className="filter-label">Сложность:</span>
      <div className="filter-buttons">
        {difficulties.map(diff => (
          <button
            key={diff.value || 'all'}
            className={`difficulty-btn ${selectedDifficulty === diff.value ? 'active' : ''}`}
            onClick={() => onDifficultyChange(diff.value as any)}
            style={selectedDifficulty === diff.value ? { backgroundColor: diff.color, color: 'white' } : {}}
          >
            {diff.label}
          </button>
        ))}
      </div>
    </div>
  );
};

export default DifficultyFilter;
