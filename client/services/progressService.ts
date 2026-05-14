import api from './api';

/** Попытка выполнения упражнения */
export interface ExerciseAttempt {
  id: number;
  exerciseId: string;
  moduleId: string;
  /** Снимок кода на момент попытки */
  codeSnapshot: string;
  /** Успешность попытки */
  isSuccess: boolean;
  /** Сообщение об ошибке (если есть) */
  errorMessage?: string;
  createdAt: string;
  exerciseNumber: number;
}

/** Детальный прогресс по упражнению */
export interface DetailedExerciseProgress {
  exerciseId: string;
  moduleId: string;
  isCompleted: boolean;
  completedAt: string | null;
  attemptsCount: number;
  successfulAttempts: number;
  failedAttempts: number;
  firstAttemptAt: string | null;
  lastAttemptAt: string | null;
  attempts: ExerciseAttempt[];
}

/** Попытки по модулю */
export interface ModuleAttempts {
  moduleId: string;
  moduleTitle: string;
  totalExercises: number;
  completedExercises: number;
  percentage: number;
  totalAttempts: number;
  successfulAttempts: number;
  failedAttempts: number;
  exercises: DetailedExerciseProgress[];
}

/** Прогресс по упражнению */
export interface ExerciseProgress {
  exerciseId: string;
  moduleId: string;
  isCompleted: boolean;
  completedAt: string | null;
  attemptsCount: number;
}

/** Прогресс по модулю */
export interface ModuleProgress {
  moduleId: string;
  moduleTitle: string;
  totalExercises: number;
  completedExercises: number;
  percentage: number;
  exercises: ExerciseProgress[];
}

/** Общий прогресс пользователя */
export interface UserProgress {
  userId: number;
  totalCompleted: number;
  totalExercises: number;
  totalPercentage: number;
  modules: ModuleProgress[];
}

/** Данные для отметки упражнения как выполненного */
export interface CompleteExerciseData {
  /** Снимок кода */
  codeSnapshot: string;
  /** Успешность выполнения */
  isSuccess: boolean;
  /** Сообщение об ошибке (опционально) */
  errorMessage?: string | null;
}

/**
 * Сервис для работы с прогрессом пользователя
 * Обеспечивает отслеживание выполнения упражнений и получение статистики
 */
export const progressService = {
  /**
   * Отмечает упражнение как выполненное
   * @param moduleId - ID модуля
   * @param exerciseId - ID упражнения
   * @param data - Данные о выполнении
   * @returns Обновленный прогресс по упражнению
   */
  async markExerciseComplete(
    moduleId: string,
    exerciseId: string,
    data: CompleteExerciseData
  ): Promise<ExerciseProgress> {
    const response = await api.post<ExerciseProgress>(
      `/api/progress/${moduleId}/${exerciseId}/complete`,
      data
    );
    return response.data;
  },

  /**
   * Получает общий прогресс пользователя
   * @returns Прогресс по всем модулям
   */
  async getUserProgress(): Promise<UserProgress> {
    const response = await api.get<UserProgress>('/api/progress');
    return response.data;
  },

  /**
   * Получает детальную статистику попыток по модулю
   * @param moduleId - ID модуля
   * @returns Детальная статистика по модулю
   */
  async getModuleAttempts(moduleId: string): Promise<ModuleAttempts> {
    const response = await api.get<ModuleAttempts>(`/api/progress/module/${moduleId}/attempts`);
    return response.data;
  },
};
