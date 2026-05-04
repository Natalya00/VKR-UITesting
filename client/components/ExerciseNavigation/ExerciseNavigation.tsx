import React from 'react';
import './ExerciseNavigation.css';

interface ExerciseNavigationProps {
  currentExercise: number;
  totalExercises: number;
  completedExercises: number;
  taskTitle: string;
  onPrevious: () => void;
  onNext: () => void;
  isCompleted?: boolean;
}

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
      <div className="exercise-info">
        <h2 className="exercise-title">
          Упражнение {currentExercise}/{totalExercises}
          {isCompleted && <span className="exercise-completed-badge"> ✅</span>}
        </h2>
        <p className="progress-text">Всего упражнений выполнено: {completedExercises}/{totalExercises}</p>
      </div>
      
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
