import { useState, useEffect, useCallback } from 'react';
import { progressService, UserProgress, ExerciseProgress, CompleteExerciseData } from '../services/progressService';

interface UseProgressResult {
  progress: UserProgress | null;
  isLoading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  markComplete: (moduleId: string, exerciseId: string, data: CompleteExerciseData) => Promise<ExerciseProgress>;
}

export const useProgress = (): UseProgressResult => {
  const [progress, setProgress] = useState<UserProgress | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  const refresh = useCallback(async () => {
    await loadProgress();
  }, [loadProgress]);

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
