import React, { useState, useEffect } from 'react';
import { ModuleAttempts, DetailedExerciseProgress, ExerciseAttempt } from '../../services/progressService';
import { progressService } from '../../services/progressService';
import './ModuleDetails.css';
import { getExerciseNumber } from '../../utils/getExerciseNumber';

interface ModuleDetailsProps {
  moduleId: string;
  onClose: () => void;
}

const ModuleDetails: React.FC<ModuleDetailsProps> = ({ moduleId, onClose }) => {
  const [moduleData, setModuleData] = useState<ModuleAttempts | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedExercise, setExpandedExercise] = useState<string | null>(null);

  useEffect(() => {
    const loadModuleDetails = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await progressService.getModuleAttempts(moduleId);
        setModuleData(data);
      } catch (err) {
        setError('Не удалось загрузить детальную информацию');
      } finally {
        setIsLoading(false);
      }
    };

    loadModuleDetails();
  }, [moduleId]);

  const formatDate = (dateString: string | null) => {
    if (!dateString) return 'Не выполнено';
    
    const utcDate = new Date(dateString);
    
    const moscowDate = new Date(utcDate.getTime() + (3 * 60 * 60 * 1000));
    
    return moscowDate.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }) + ' MSK';
  };

  const getStatusIcon = (isCompleted: boolean) => {
    return isCompleted ? '✓' : '•';
  };

  const getAttemptStatusIcon = (isSuccess: boolean) => {
    return isSuccess ? '✓' : '✗';
  };

  const toggleExerciseExpansion = (exerciseId: string) => {
    setExpandedExercise(expandedExercise === exerciseId ? null : exerciseId);
  };

  if (isLoading) {
    return (
      <div className="module-details-overlay">
        <div className="module-details-modal">
          <div className="loading">Загрузка детальной информации...</div>
        </div>
      </div>
    );
  }

  if (error || !moduleData) {
    return (
      <div className="module-details-overlay">
        <div className="module-details-modal">
          <div className="error">{error || 'Ошибка загрузки'}</div>
          <button className="close-button" onClick={onClose}>Закрыть</button>
        </div>
      </div>
    );
  }

  return (
    <div className="module-details-overlay" onClick={onClose}>
      <div className="module-details-modal" onClick={(e) => e.stopPropagation()}>
        <div className="module-details-header">
          <h2>{moduleData.moduleTitle}</h2>
          <button className="close-button" onClick={onClose}>×</button>
        </div>

        <div className="module-summary">
          <div className="summary-stats">
            <div className="stat-item">
              <span className="stat-label">Выполнено упражнений:</span>
              <span className="stat-value">{moduleData.completedExercises}/{moduleData.totalExercises}</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Прогресс:</span>
              <span className="stat-value">{moduleData.percentage.toFixed(1)}%</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Всего попыток:</span>
              <span className="stat-value">{moduleData.totalAttempts}</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Успешных:</span>
              <span className="stat-value success">{moduleData.successfulAttempts}</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">Неудачных:</span>
              <span className="stat-value failed">{moduleData.failedAttempts}</span>
            </div>
          </div>
        </div>

        <div className="exercises-list">
          <h3>Детальная информация по упражнениям</h3>
          {moduleData.exercises.length === 0 ? (
            <div className="no-exercises">Упражнения еще не начаты</div>
          ) : (
            moduleData.exercises.map((exercise) => (
              <div key={exercise.exerciseId} className="exercise-item">
                <div 
                  className="exercise-header"
                  onClick={() => toggleExerciseExpansion(exercise.exerciseId)}
                >
                  <div className="exercise-info">
                    <span className="exercise-status" style={{ color: exercise.isCompleted ? '#28a745' : '#ffc107' }}>
                      {getStatusIcon(exercise.isCompleted)}
                    </span>
                    <span className="exercise-id">Упражнение {getExerciseNumber(exercise.exerciseId, moduleData.moduleId)}</span>
                    <span className="exercise-attempts">
                      ({exercise.attemptsCount} попыток)
                    </span>
                  </div>
                  <div className="exercise-stats">
                    <span className="success-count">✓ {exercise.successfulAttempts}</span>
                    <span className="failed-count">✗ {exercise.failedAttempts}</span>
                    <span className="expand-icon">
                      {expandedExercise === exercise.exerciseId ? '▼' : '▶'}
                    </span>
                  </div>
                </div>

                {expandedExercise === exercise.exerciseId && (
                  <div className="exercise-details">
                    <div className="exercise-summary">
                      <div className="summary-row">
                        <span>Статус:</span>
                        <span className={exercise.isCompleted ? 'completed' : 'in-progress'}>
                          {exercise.isCompleted ? 'Выполнено' : 'В процессе'}
                        </span>
                      </div>
                      {exercise.completedAt && (
                        <div className="summary-row">
                          <span>Выполнено:</span>
                          <span>{formatDate(exercise.completedAt)}</span>
                        </div>
                      )}
                      <div className="summary-row">
                        <span>Первая попытка:</span>
                        <span>{formatDate(exercise.firstAttemptAt)}</span>
                      </div>
                      <div className="summary-row">
                        <span>Последняя попытка:</span>
                        <span>{formatDate(exercise.lastAttemptAt)}</span>
                      </div>
                    </div>

                    <div className="attempts-list">
                      <h4>История попыток</h4>
                      {exercise.attempts.map((attempt, index) => (
                        <div key={attempt.id} className={`attempt-item ${attempt.isSuccess ? 'success' : 'failed'}`}>
                          <div className="attempt-header">
                            <span className="attempt-number">#{exercise.attempts.length - index}</span>
                            <span className="attempt-status" style={{ color: attempt.isSuccess ? '#28a745' : '#dc3545' }}>
                              {getAttemptStatusIcon(attempt.isSuccess)}
                            </span>
                            <span className="attempt-date">
                              {formatDate(attempt.createdAt)}
                            </span>
                          </div>
                          {attempt.errorMessage && (
                            <div className="attempt-error">
                              <strong>Ошибка:</strong> {attempt.errorMessage}
                            </div>
                          )}
                          <details className="attempt-code">
                            <summary>Показать код</summary>
                            <pre className="code-snapshot">
                              {attempt.codeSnapshot}
                            </pre>
                          </details>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default ModuleDetails;