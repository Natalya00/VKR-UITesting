import React from 'react';
import './ExerciseHeader.css';
import ExerciseNavigation from '../ExerciseNavigation/ExerciseNavigation';
import ActionButtons from '../ActionButtons/ActionButtons';
import TaskDescription from '../TaskDescription/TaskDescription';
import BlockTabs from '../BlockTabs/BlockTabs';
import DifficultyFilter from '../DifficultyFilter/DifficultyFilter';

interface Block {
  blockId: string;
  title: string;
}

interface ExerciseHeaderProps {
  currentExercise: number;
  totalExercises: number;
  completedExercises: number;
  blockTitle: string;
  taskTitle: string;
  taskDescription: string;
  onPrevious: () => void;
  onNext: () => void;
  onShowReference: () => void;
  onShowHint: () => void;
  blocks?: Block[];
  currentBlockIndex?: number;
  onBlockChange?: (index: number) => void;
  isCurrentExerciseCompleted?: boolean;
  selectedDifficulty?: 'easy' | 'medium' | 'hard' | null;
  onDifficultyChange?: (difficulty: 'easy' | 'medium' | 'hard' | null) => void;
  originalBlocks?: Block[];
}

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
      <div className="left-column">
        <div className="block-title">{blockTitle}</div>
        <ExerciseNavigation
          currentExercise={currentExercise}
          totalExercises={totalExercises}
          completedExercises={completedExercises}
          taskTitle={taskTitle}
          onPrevious={onPrevious}
          onNext={onNext}
          isCompleted={isCurrentExerciseCompleted}
        />
        {selectedDifficulty !== undefined && onDifficultyChange && (
          <DifficultyFilter
            selectedDifficulty={selectedDifficulty}
            onDifficultyChange={onDifficultyChange}
          />
        )}
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
