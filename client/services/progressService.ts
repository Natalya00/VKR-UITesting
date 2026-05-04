import api from './api';

export interface ExerciseAttempt {
  id: number;
  exerciseId: string;
  moduleId: string;
  codeSnapshot: string;
  isSuccess: boolean;
  errorMessage?: string;
  createdAt: string;
  exerciseNumber: number;
}

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

export interface ExerciseProgress {
  exerciseId: string;
  moduleId: string;
  isCompleted: boolean;
  completedAt: string | null;
  attemptsCount: number;
}

export interface ModuleProgress {
  moduleId: string;
  moduleTitle: string;
  totalExercises: number;
  completedExercises: number;
  percentage: number;
  exercises: ExerciseProgress[];
}

export interface UserProgress {
  userId: number;
  totalCompleted: number;
  totalExercises: number;
  totalPercentage: number;
  modules: ModuleProgress[];
}

export interface CompleteExerciseData {
  codeSnapshot: string;
  isSuccess: boolean;
  errorMessage?: string | null;
}

export const progressService = {
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

  async getUserProgress(): Promise<UserProgress> {
    const response = await api.get<UserProgress>('/api/progress');
    return response.data;
  },

  async getModuleAttempts(moduleId: string): Promise<ModuleAttempts> {
    const response = await api.get<ModuleAttempts>(`/api/progress/module/${moduleId}/attempts`);
    return response.data;
  },
};
