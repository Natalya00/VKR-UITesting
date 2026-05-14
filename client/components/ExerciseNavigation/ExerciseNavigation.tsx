import React from 'react';
import './ExerciseNavigation.css';

/**
 * Пропсы компонента ExerciseNavigation
 */
interface ExerciseNavigationProps {
  /** Номер текущего упражнения */
  currentExercise: number;
  /** Общее количество упражнений */
  totalExercises: number;
  /** Количество выполненных упражнений */
  completedExercises: number;
  /** Название текущего задания */
  taskTitle: string;
  /** Обработчик перехода к предыдущему упражнению */
  onPrevious: () => void;
  /** Обработчик перехода к следующему упражнению */
  onNext: () => void;
  /** Флаг выполнения текущего упражнения */
  isCompleted?: boolean;
}

/**
 * Компонент навигации по упражнениям
 * Отображает текущий прогресс, номер упражнения и кнопки навигации
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент навигации по упражнениям
 */
const ExerciseNavigation: React.FC<ExerciseNavigationProps> = ({
  currentExercise,
  totalExercises,
  completedExercises,
  taskTitle,
  onPrevious,
  onNext,
  isCompleted
}) => {
  return (
    <div className="exercise-navigation">
      {/* Информация о текущем упражнении и прогрессе */}
      <div className="exercise-info">
        <h2 className="exercise-title">
          Упражнение {currentExercise}/{totalExercises}
          {/* Значок выполнения */}
          {isCompleted && <span className="exercise-completed-badge"> ✅</span>}
        </h2>
        <p className="progress-text">Всего упражнений выполнено: {completedExercises}/{totalExercises}</p>
      </div>
      
      {/* Кнопки навигации */}
      <div className="navigation-buttons">
        <button 
          className="nav-button" 
          type="button" 
          onClick={onPrevious} 
          disabled={currentExercise === 1}
        >
          ← Предыдущее упражнение
        </button>
        <button 
          className="nav-button" 
          type="button" 
          onClick={onNext} 
          disabled={currentExercise === totalExercises}
        >
          Следующее упражнение →
        </button>
      </div>
    </div>
  );
};

export default ExerciseNavigation;
