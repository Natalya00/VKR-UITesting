import React from 'react';
import './ExerciseHeader.css';
import ExerciseNavigation from '../ExerciseNavigation/ExerciseNavigation';
import ActionButtons from '../ActionButtons/ActionButtons';
import TaskDescription from '../TaskDescription/TaskDescription';
import BlockTabs from '../BlockTabs/BlockTabs';
import DifficultyFilter from '../DifficultyFilter/DifficultyFilter';

/**
 * Интерфейс блока упражнений
 */
interface Block {
  /** Уникальный идентификатор блока */
  blockId: string;
  /** Название блока */
  title: string;
}

/**
 * Пропсы компонента ExerciseHeader
 */
interface ExerciseHeaderProps {
  /** Номер текущего упражнения */
  currentExercise: number;
  /** Общее количество упражнений */
  totalExercises: number;
  /** Количество выполненных упражнений */
  completedExercises: number;
  /** Название текущего блока */
  blockTitle: string;
  /** Название задания */
  taskTitle: string;
  /** Описание задания */
  taskDescription: string;
  /** Обработчик перехода к предыдущему упражнению */
  onPrevious: () => void;
  /** Обработчик перехода к следующему упражнению */
  onNext: () => void;
  /** Обработчик открытия справочных материалов */
  onShowReference: () => void;
  /** Обработчик открытия подсказки */
  onShowHint: () => void;
  /** Массив блоков для отображения вкладок */
  blocks?: Block[];
  /** Индекс текущего блока */
  currentBlockIndex?: number;
  /** Обработчик смены блока */
  onBlockChange?: (index: number) => void;
  /** Флаг выполнения текущего упражнения */
  isCurrentExerciseCompleted?: boolean;
  /** Выбранная сложность фильтра */
  selectedDifficulty?: 'easy' | 'medium' | 'hard' | null;
  /** Обработчик смены фильтра сложности */
  onDifficultyChange?: (difficulty: 'easy' | 'medium' | 'hard' | null) => void;
  /** Оригинальные блоки до фильтрации */
  originalBlocks?: Block[];
}

/**
 * Компонент заголовка упражнения
 * Объединяет навигацию, описание задания, кнопки действий и фильтры
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент заголовка упражнения
 */
const ExerciseHeader: React.FC<ExerciseHeaderProps> = ({
  currentExercise,
  totalExercises,
  completedExercises,
  blockTitle,
  taskTitle,
  taskDescription,
  onPrevious,
  onNext,
  onShowReference,
  onShowHint,
  blocks,
  currentBlockIndex,
  onBlockChange,
  isCurrentExerciseCompleted,
  selectedDifficulty,
  onDifficultyChange,
  originalBlocks
}) => {
  return (
    <section className="top-section">
      {/* Левая колонка: навигация, фильтры и вкладки */}
      <div className="left-column">
        <div className="block-title">{blockTitle}</div>
        {/* Навигация по упражнениям */}
        <ExerciseNavigation
          currentExercise={currentExercise}
          totalExercises={totalExercises}
          completedExercises={completedExercises}
          taskTitle={taskTitle}
          onPrevious={onPrevious}
          onNext={onNext}
          isCompleted={isCurrentExerciseCompleted}
        />
        {/* Фильтр по сложности */}
        {selectedDifficulty !== undefined && onDifficultyChange && (
          <DifficultyFilter
            selectedDifficulty={selectedDifficulty}
            onDifficultyChange={onDifficultyChange}
          />
        )}
        {/* Вкладки блоков */}
        {blocks && currentBlockIndex !== undefined && onBlockChange && (
          <div className="block-tabs-wrapper">
            <BlockTabs
              blocks={blocks}
              currentBlockIndex={currentBlockIndex}
              onBlockChange={onBlockChange}
              originalBlocks={originalBlocks}
            />
          </div>
        )}
      </div>

      {/* Правая колонка: кнопки действий и описание */}
      <div className="right-column">
        <ActionButtons
          onShowReference={onShowReference}
          onShowHint={onShowHint}
        />
        <TaskDescription description={taskDescription} />
      </div>
    </section>
  );
};

export default ExerciseHeader;
