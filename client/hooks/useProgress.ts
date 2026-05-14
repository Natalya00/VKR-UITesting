import { useState, useEffect, useCallback } from 'react';
import { progressService, UserProgress, ExerciseProgress, CompleteExerciseData } from '../services/progressService';

/**
 * Результат работы хука useProgress
 */
interface UseProgressResult {
  /** Прогресс пользователя по всем модулям */
  progress: UserProgress | null;
  /** Флаг загрузки данных */
  isLoading: boolean;
  /** Сообщение об ошибке */
  error: string | null;
  /** Метод для обновления прогресса */
  refresh: () => Promise<void>;
  /** Метод для отметки упражнения как выполненного */
  markComplete: (moduleId: string, exerciseId: string, data: CompleteExerciseData) => Promise<ExerciseProgress>;
}

/**
 * Хук для управления прогрессом пользователя
 * Обеспечивает загрузку, обновление и отслеживание прогресса по всем модулям
 * @returns Объект с данными о прогрессе и методами для его управления
 */
export const useProgress = (): UseProgressResult => {
  const [progress, setProgress] = useState<UserProgress | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /**
   * Загружает прогресс пользователя с сервера
   */
  const loadProgress = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await progressService.getUserProgress();
      setProgress(data);
    } catch (err) {
      setError('Не удалось загрузить прогресс');
      setProgress(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProgress();
  }, [loadProgress]);

  /**
   * Обновляет прогресс пользователя
   */
  const refresh = useCallback(async () => {
    await loadProgress();
  }, [loadProgress]);

  /**
   * Отмечает упражнение как выполненное и обновляет прогресс
   * @param moduleId - ID модуля
   * @param exerciseId - ID упражнения
   * @param data - Данные о выполнении упражнения
   * @returns Обновленный прогресс по упражнению
   */
  const markComplete = useCallback(
    async (
      moduleId: string,
      exerciseId: string,
      data: CompleteExerciseData
    ): Promise<ExerciseProgress> => {
      try {
        const result = await progressService.markExerciseComplete(moduleId, exerciseId, data);
        await loadProgress();
        return result;
      } catch (err) {
        throw err;
      }
    },
    [loadProgress]
  );

  return {
    progress,
    isLoading,
    error,
    refresh,
    markComplete,
  };
};
