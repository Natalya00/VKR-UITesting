import React from 'react';
import { useProgress } from '../../hooks/useProgress';
import './ProgressDashboard.css';
import { getExerciseNumber } from '../../utils/getExerciseNumber';

/**
 * Компонент панели прогресса пользователя
 * Отображает общую статистику прогресса и детальную информацию по каждому модулю:
 * - Общий процент выполнения
 * - Количество выполненных упражнений
 * - Прогресс-бары по модулям
 * - Список упражнений с статусом
 * 
 * @returns JSX элемент панели прогресса
 */
const ProgressDashboard: React.FC = () => {
  const { progress, isLoading, error } = useProgress();

  // Отображение состояния загрузки
  if (isLoading) {
    return (
      <div className="progress-dashboard">
        <div className="progress-loading">Загрузка прогресса...</div>
      </div>
    );
  }

  // Отображение ошибки
  if (error) {
    return (
      <div className="progress-dashboard">
        <div className="progress-error">{error}</div>
      </div>
    );
  }

  // Отображение пустого состояния
  if (!progress || progress.modules.length === 0) {
    return (
      <div className="progress-dashboard">
        <div className="progress-empty">
          <h3>Ваш прогресс</h3>
          <p>Вы ещё не выполнили ни одного упражнения</p>
        </div>
      </div>
    );
  }

  return (
    <div className="progress-dashboard">
      <h3 className="progress-title">Ваш прогресс</h3>

      {/* Общая статистика */}
      <div className="progress-summary">
        <div className="progress-stat">
          <span className="progress-stat-value">{progress.totalCompleted}</span>
          <span className="progress-stat-label">из {progress.totalExercises} упражнений</span>
        </div>
        <div className="progress-stat">
          <span className="progress-stat-value">{progress.totalPercentage.toFixed(1)}%</span>
          <span className="progress-stat-label">выполнено</span>
        </div>
      </div>

      {/* Карточки модулей */}
      <div className="progress-modules">
        {progress.modules.map((module) => (
          <div key={module.moduleId} className="progress-module-card">
            {/* Заголовок модуля */}
            <div className="progress-module-header">
              <h4 className="progress-module-title">{module.moduleTitle}</h4>
              <span className="progress-module-percentage">
                {module.percentage.toFixed(1)}%
              </span>
            </div>

            {/* Прогресс-бар */}
            <div className="progress-bar-container">
              <div
                className="progress-bar"
                style={{ width: `${module.percentage}%` }}
              />
            </div>

            {/* Статистика модуля */}
            <div className="progress-module-stats">
              <span className="progress-stat-text">
                {module.completedExercises} из {module.totalExercises} упражнений
              </span>
            </div>

            {/* Разворачиваемый список упражнений */}
            {module.exercises && module.exercises.length > 0 && (
              <details className="progress-exercises-details">
                <summary className="progress-exercises-summary">
                  Показать упражнения ({module.exercises.length})
                </summary>
                <ul className="progress-exercises-list">
                  {module.exercises.map((exercise) => (
                    <li
                      key={exercise.exerciseId}
                      className={`progress-exercise-item ${
                        exercise.isCompleted ? 'completed' : ''
                      }`}
                    >
                      <span className="exercise-id">Упражнение {getExerciseNumber(exercise.exerciseId, module.moduleId)}</span>
                      <span className="exercise-status">
                        {exercise.isCompleted ? '✅' : '⏳'}
                      </span>
                      {exercise.attemptsCount > 1 && (
                        <span className="exercise-attempts">
                          ({exercise.attemptsCount} попыток)
                        </span>
                      )}
                    </li>
                  ))}
                </ul>
              </details>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProgressDashboard;
