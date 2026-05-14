import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useProgress } from '../../hooks/useProgress';
import { fetchWithAuth } from '../../services/fetchWithAuth';
import './ElementSimulatorPage.css';
import PageHeader from '../../components/PageHeader/PageHeader';
import ExerciseHeader from '../../components/ExerciseHeader/ExerciseHeader';
import IDE from '../../components/IDE/IDE';
import ResultCard from '../../components/ResultCard/ResultCard';
import ReferenceModal from '../../components/ReferenceModal/ReferenceModal';
import DifficultyFilter from '../../components/DifficultyFilter/DifficultyFilter';
import ElementsButtonsTrainer, { ElementsButtonsTrainerConfig } from '../../components/Training/ElementsButtonsTrainer';
import SimpleElementTrainer from '../../components/Training/SimpleElementTrainer';
import NavigationTrainer from '../../components/Training/NavigationTrainer';
import RadioGroupTrainer from '../../components/Training/RadioGroupTrainer';
import TableTrainer from '../../components/Training/TableTrainer';
import PageTrainer from '../../components/Training/PageTrainer';
import QuizTrainer from '../../components/Training/QuizTrainer';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

/** Упражнение модуля */
interface Exercise {
  /** ID упражнения */
  id: string;
  /** Название упражнения */
  title: string;
  /** Описание упражнения */
  description: string;
  /** Подсказка к упражнению */
  hint: string;
  /** Название компонента тренажера */
  trainingComponent: string;
  /** Конфигурация компонента тренажера */
  componentConfig?: Record<string, any>;
  /** Начальный код для упражнения */
  initialCode: string;
  /** Порядковый номер упражнения */
  order: number;
  /** Сложность упражнения */
  difficulty: 'easy' | 'medium' | 'hard';
}

/** Данные блока упражнений */
interface BlockData {
  /** ID блока */
  blockId: string;
  /** Название блока */
  title: string;
  /** Описание блока */
  description: string;
  /** Сложность блока */
  difficulty: string;
  /** Количество упражнений в блоке */
  exerciseCount?: number;
  /** Массив упражнений */
  exercises: Exercise[];
}

/** Данные модуля */
interface ModuleData {
  /** ID модуля */
  moduleId: string;
  /** Название модуля */
  title: string;
  /** Массив блоков упражнений */
  blocks: BlockData[];
}

/** Ответ от сервера при выполнении кода */
interface RunResponse {
  /** Успешность выполнения */
  success: boolean;
  /** Режим UI */
  uiMode: boolean;
  /** Вывод stdout */
  stdout: string;
  /** Вывод stderr */
  stderr: string;
  /** Сообщение о результате */
  message: string;
}

/**
 * Рендерит компонент тренажера на основе его названия и конфигурации
 * 
 * @param trainingComponent - Название компонента тренажера
 * @param config - Конфигурация для компонента
 * @param exerciseId - ID упражнения
 * @param onQuizAnswer - Колбэк для ответа в викторине
 * @returns JSX элемент тренажера
 */
const renderTrainingComponent = (trainingComponent: string, config?: Record<string, any>, exerciseId?: string, onQuizAnswer?: (answer: string) => void) => {
  const configWithExerciseId = exerciseId && config ? { ...config, exerciseId } : config;

  switch (trainingComponent) {
    case 'QuizTrainer':
      return <QuizTrainer key={exerciseId} config={config as any} exerciseId={exerciseId} onAnswer={onQuizAnswer} />;
    case 'ElementsButtonsTrainer':
      return <ElementsButtonsTrainer key={exerciseId} config={configWithExerciseId as ElementsButtonsTrainerConfig} exerciseId={exerciseId} />;
    case 'SimpleElementTrainer':
      return <SimpleElementTrainer key={exerciseId} config={configWithExerciseId as any} />;
    case 'NavigationTrainer':
      return <NavigationTrainer key={exerciseId} config={configWithExerciseId as any} />;
    case 'RadioGroupTrainer':
      return <RadioGroupTrainer key={exerciseId} config={configWithExerciseId as any} />;
    case 'TableTrainer':
      return <TableTrainer key={exerciseId} config={configWithExerciseId as any} />;
    case 'PageTrainer':
      return <PageTrainer key={exerciseId} config={configWithExerciseId as any} />;
    default:
      return <ElementsButtonsTrainer key={exerciseId} config={configWithExerciseId as ElementsButtonsTrainerConfig} exerciseId={exerciseId} />;
  }
};

const ElementSimulatorPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const exerciseFromUrl = searchParams.get('exercise');
  const { isAuthenticated } = useAuth();

  const [moduleData, setModuleData] = useState<ModuleData | null>(null);
  const [blocksCache, setBlocksCache] = useState<Record<string, BlockData>>({});
  const [loading, setLoading] = useState(true);
  const [loadingBlockId, setLoadingBlockId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [currentBlockIndex, setCurrentBlockIndex] = useState(0);
  const [currentExerciseIndex, setCurrentExerciseIndex] = useState(0);
  const [selectedDifficulty, setSelectedDifficulty] = useState<'easy' | 'medium' | 'hard' | null>(null);
  const [resultText, setResultText] = useState('Выполните задание!');
  const [codeValue, setCodeValue] = useState('');
  const [terminalOutput, setTerminalOutput] = useState('Hello, world!');
  const [isRunning, setIsRunning] = useState(false);
  const [showReference, setShowReference] = useState(false);
  const [showHint, setShowHint] = useState(false);
  const [hintText, setHintText] = useState('');

  const { markComplete, refresh, progress } = useProgress();

  const currentBlockMeta = moduleData?.blocks[currentBlockIndex];
  const currentBlock = currentBlockMeta
    ? blocksCache[currentBlockMeta.blockId] || currentBlockMeta
    : undefined;

  /**
   * Фильтрация блоков по выбранной сложности
   * @returns Отфильтрованный массив блоков
   */
  const filteredBlocks = useMemo(() => {
    if (!moduleData?.blocks) return [];
    if (!selectedDifficulty) return moduleData.blocks;
    
    return moduleData.blocks.filter(block => {
      const blockData = blocksCache[block.blockId] || block;
      return blockData.exercises.some(ex => ex.difficulty === selectedDifficulty);
    });
  }, [moduleData?.blocks, selectedDifficulty, blocksCache]);

  /**
   * Фильтрация упражнений текущего блока по сложности
   * @returns Отфильтрованный массив упражнений
   */
  const filteredExercises = useMemo(() => {
    if (!currentBlock?.exercises) return [];
    if (!selectedDifficulty) return currentBlock.exercises;
    const filtered = currentBlock.exercises.filter(ex => ex.difficulty === selectedDifficulty);
    return filtered;
  }, [currentBlock?.exercises, selectedDifficulty]);

  const currentExercise = filteredExercises[currentExerciseIndex] || null;

  /**
   * Глобальный индекс текущего упражнения (учитывает фильтрацию)
   * @returns Номер упражнения от 1 до общего количества
   */
  const globalExerciseIndex = useMemo(() => {
    if (!moduleData) return 1;
    
    if (selectedDifficulty) {
      const currentFilteredIndex = filteredBlocks.findIndex(b => b.blockId === currentBlockMeta?.blockId);
      if (currentFilteredIndex === -1) return 1;
      
      const exercisesBefore = filteredBlocks
        .slice(0, currentFilteredIndex)
        .reduce((sum, block) => {
          const blockData = blocksCache[block.blockId] || block;
          return sum + blockData.exercises.filter(ex => ex.difficulty === selectedDifficulty).length;
        }, 0);
      
      return exercisesBefore + currentExerciseIndex + 1;
    } else {
      const exercisesBefore = moduleData.blocks
        .slice(0, currentBlockIndex)
        .reduce((sum, block) => sum + (block.exerciseCount ?? block.exercises.length), 0);
      
      return exercisesBefore + currentExerciseIndex + 1;
    }
  }, [moduleData, selectedDifficulty, filteredBlocks, currentBlockMeta, blocksCache, currentBlockIndex, currentExerciseIndex]);

  /**
   * Загрузка данных модуля
   */
  useEffect(() => {
    setLoading(true);
    setBlocksCache({});

    fetchWithAuth(`${API_BASE_URL}/api/exercises/module-2`)
      .then(res => {
        if (res.status === 401) {
          throw new Error('AUTH_REQUIRED');
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data: ModuleData) => {
        setModuleData(data);
        if (data.blocks && data.blocks.length > 0) {
          const firstBlock = data.blocks[0];
          setBlocksCache(prev => ({
            ...prev,
            [firstBlock.blockId]: firstBlock,
          }));

          if (exerciseFromUrl) {
            const exerciseNum = parseInt(exerciseFromUrl, 10);
            if (!isNaN(exerciseNum) && exerciseNum > 0) {
              let globalIndex = 0;
              let foundBlockIndex = 0;
              let foundExerciseIndex = 0;

              for (let blockIdx = 0; blockIdx < data.blocks.length; blockIdx++) {
                const block = data.blocks[blockIdx];
                const exerciseCount = block.exerciseCount ?? block.exercises.length;

                if (globalIndex + exerciseCount >= exerciseNum) {
                  foundBlockIndex = blockIdx;
                  foundExerciseIndex = exerciseNum - globalIndex - 1;
                  break;
                }
                globalIndex += exerciseCount;
              }

              setCurrentBlockIndex(foundBlockIndex);
              setCurrentExerciseIndex(foundExerciseIndex);
            }
          }
        }
        setLoading(false);
      })
      .catch(err => {
        if (err.message === 'AUTH_REQUIRED') {
          setError(' Требуется авторизация\n\nВы не вошли в систему. Пожалуйста, войдите под своим аккаунтом.');
        } else {
          setError('Ошибка загрузки заданий для модуля взаимодействия с элементами.');
        }
        setLoading(false);
      });
  }, [exerciseFromUrl]);

  /**
   * Загрузка данных текущего блока при смене блока
   */
  useEffect(() => {
    if (!moduleData || !moduleData.blocks[currentBlockIndex]) {
      return;
    }

    const blockMeta = moduleData.blocks[currentBlockIndex];
    const blockId = blockMeta.blockId;

    setLoadingBlockId(blockId);
    fetchWithAuth(`${API_BASE_URL}/api/exercises/module-2/blocks/${blockId}`)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((block: BlockData) => {
        setBlocksCache(prev => ({
          ...prev,
          [blockId]: block,
        }));
      })
      .catch(err => {
        setError('Ошибка загрузки блока. Попробуйте обновить страницу.');
      })
      .finally(() => {
        setLoadingBlockId(null);
      });
  }, [currentBlockIndex, moduleData, exerciseFromUrl]);

  /**
   * Обработчик смены блока
   * @param filteredIndex - Индекс блока в отфильтрованном списке
   */
  const handleBlockChange = (filteredIndex: number) => {
    if (!moduleData || !filteredBlocks[filteredIndex]) return;

    const targetBlock = filteredBlocks[filteredIndex];
    const realBlockIndex = moduleData.blocks.findIndex(block => block.blockId === targetBlock.blockId);
    
    if (realBlockIndex === -1) return;

    const { blockId } = targetBlock;

    if (blocksCache[blockId]) {
      setCurrentBlockIndex(realBlockIndex);
      setCurrentExerciseIndex(0);
      return;
    }

    setLoadingBlockId(blockId);
    fetchWithAuth(`${API_BASE_URL}/api/exercises/module-2/blocks/${blockId}`)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((block: BlockData) => {
        setBlocksCache(prev => ({
          ...prev,
          [block.blockId]: block,
        }));
        setCurrentBlockIndex(realBlockIndex);
        setCurrentExerciseIndex(0);
      })
      .catch(err => {
        setError('Ошибка загрузки блока. Попробуйте обновить страницу.');
      })
      .finally(() => {
        setLoadingBlockId(null);
      });
  };

  /**
   * Обработчик изменения фильтра по сложности
   * @param difficulty - Выбранная сложность или null для сброса
   */
  const handleDifficultyChange = async (difficulty: 'easy' | 'medium' | 'hard' | null) => {
    setSelectedDifficulty(difficulty);
    setCurrentExerciseIndex(0);
    
    if (difficulty && moduleData) {
      setLoading(true);
      try {
        const newBlocksCache = { ...blocksCache };
        
        for (const block of moduleData.blocks) {
          if (!newBlocksCache[block.blockId]) {
            const response = await fetchWithAuth(`${API_BASE_URL}/api/exercises/module-2/blocks/${block.blockId}`);
            if (response.ok) {
              const blockData = await response.json();
              newBlocksCache[block.blockId] = blockData;
            }
          }
        }
        
        setBlocksCache(newBlocksCache);
        
        for (let i = 0; i < moduleData.blocks.length; i++) {
          const block = moduleData.blocks[i];
          const blockData = newBlocksCache[block.blockId] || block;
          
          if (blockData.exercises.some(ex => ex.difficulty === difficulty)) {
            setCurrentBlockIndex(i);
            break;
          }
        }
      } catch (error) {
        console.error('Ошибка при загрузке блоков для фильтрации:', error);
        setError('Ошибка при загрузке упражнений для фильтрации');
      } finally {
        setLoading(false);
      }
    }
  };

  /**
   * Обработчик открытия справочного окна
   */
  const handleShowReference = () => {
    setShowReference(true);
    setResultText('Справка открыта. Нажмите на вкладки для просмотра.');
  };

  /**
   * Обработчик показа подсказки
   */
  const handleShowHint = () => {
    if (!showHint && currentExercise) {
      setShowHint(true);
      setHintText(currentExercise.hint);
      setTimeout(() => setShowHint(false), 8000);
    } else {
      setShowHint(false);
      setHintText('');
    }
  };

  const module2 = progress?.modules.find(m => m.moduleId === 'module-2');
  const completedCount = module2?.completedExercises || 0;

  const isCurrentExerciseCompleted = currentExercise
    ? progress?.modules
        .find(m => m.moduleId === 'module-2')?.exercises
        .some(ex => ex.exerciseId === currentExercise?.id && ex.isCompleted) || false
    : false;

  useEffect(() => {
    if (currentExercise) {      
      setCodeValue(currentExercise.initialCode || '');
      setTerminalOutput('Hello, world!');
      setResultText('Выполните задание!');
    }
  }, [currentBlockIndex, currentExerciseIndex, currentExercise, globalExerciseIndex]);

  /**
   * Обработчик выполнения кода
   * Отправляет код на сервер для валидации и обновляет прогресс
   */
  const handleRunCode = async () => {
    const trimmed = codeValue.trim();
    if (!trimmed) {
      setTerminalOutput('Ошибка: введите код перед запуском.');
      return;
    }

    const exerciseOrder = currentExercise?.order || 1;
    const exerciseId = currentExercise?.id;
    const exerciseTitle = currentExercise?.title;

    setIsRunning(true);
    try {
      const requestBody = {
        code: codeValue,
        baseUrl: 'http://localhost:5173',
        exercise: exerciseOrder,
        moduleId: 'module-2',
        exerciseId: exerciseId
      };
      
      const response = await fetchWithAuth(`${API_BASE_URL}/api/code/run`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(requestBody),
        credentials: 'include'
      });

      if (!response.ok) {
        const text = await response.text();
        setTerminalOutput(`=== Динамическая проверка ===\n${text}`);
        setResultText('✗ Ошибка выполнения кода.');
        return;
      }

      const data: RunResponse = await response.json();
      
      const combinedOutput = [data.stdout, data.stderr].filter(Boolean).join('\n').trim();

      const lines = combinedOutput.split('\n');
      const dynamicLines: string[] = [];
      const staticLines: string[] = [];
      let inStaticSection = false;

      for (const line of lines) {
        if (line.includes('[CodeValidator]')) {
          inStaticSection = true;
        }
        if (inStaticSection) {
          staticLines.push(line);
        } else {
          dynamicLines.push(line);
        }
      }

      const formattedOutput = [
        '=== Динамическая проверка ===',
        ...dynamicLines,
        '',
        '=== Статическая проверка ===',
        ...staticLines
      ].join('\n');

      setTerminalOutput(formattedOutput || 'Вывода нет.');

      if (data.success) {
        setResultText('✓ Задание выполнено! Динамическая и статическая проверки пройдены.');
        try {
          await markComplete('module-2', currentExercise!.id, {
            codeSnapshot: codeValue,
            isSuccess: true,
            errorMessage: null
          });
          await refresh();
        } catch (error) {
          console.error('Ошибка при сохранении прогресса:', error);
        }
      } else {
        setResultText('✗ Проверка не пройдена. Проверьте код и попробуйте снова.');
        
        try {
          await markComplete('module-2', currentExercise!.id, {
            codeSnapshot: codeValue,
            isSuccess: false,
            errorMessage: data.message || 'Проверка не пройдена'
          });
          await refresh();
        } catch (error) {
          console.error('Ошибка при сохранении прогресса:', error);
        }
      }
    } catch (error) {
      console.error('[ElementSimulator] Ошибка выполнения:', error);
      setTerminalOutput(`=== Ошибка ===\n${String(error)}`);
      setResultText('Ошибка выполнения кода.');
      
      try {
        await markComplete('module-2', currentExercise!.id, {
          codeSnapshot: codeValue,
          isSuccess: false,
          errorMessage: String(error)
        });
        await refresh();
      } catch (saveError) {
        console.error('Ошибка при сохранении:', saveError);
      }
    } finally {
      setIsRunning(false);
    }
  };

  /**
   * Обработчик перехода к предыдущему упражнению
   */
  const handlePrevious = () => {
    if (currentExerciseIndex > 0) {
      setCurrentExerciseIndex(currentExerciseIndex - 1);
    } else {
      const currentFilteredIndex = filteredBlocks.findIndex(b => b.blockId === currentBlockMeta?.blockId);
      if (currentFilteredIndex > 0) {
        const prevBlock = filteredBlocks[currentFilteredIndex - 1];
        const prevBlockRealIndex = moduleData?.blocks.findIndex(b => b.blockId === prevBlock.blockId) ?? -1;
        
        if (prevBlockRealIndex !== -1) {
          setCurrentBlockIndex(prevBlockRealIndex);
          
          const prevBlockData = blocksCache[prevBlock.blockId] || prevBlock;
          const prevFilteredExercises = selectedDifficulty 
            ? prevBlockData.exercises.filter(ex => ex.difficulty === selectedDifficulty)
            : prevBlockData.exercises;
          setCurrentExerciseIndex(Math.max(0, prevFilteredExercises.length - 1));
        }
      }
    }
  };

  /**
   * Обработчик перехода к следующему упражнению
   */
  const handleNext = () => {
    if (filteredExercises && currentExerciseIndex < filteredExercises.length - 1) {
      setCurrentExerciseIndex(currentExerciseIndex + 1);
    } else {
      const currentFilteredIndex = filteredBlocks.findIndex(b => b.blockId === currentBlockMeta?.blockId);
      if (currentFilteredIndex < filteredBlocks.length - 1) {
        const nextBlock = filteredBlocks[currentFilteredIndex + 1];
        const nextBlockRealIndex = moduleData?.blocks.findIndex(b => b.blockId === nextBlock.blockId) ?? -1;
        
        if (nextBlockRealIndex !== -1) {
          setCurrentBlockIndex(nextBlockRealIndex);
          setCurrentExerciseIndex(0);
        }
      }
    }
  };

  /**
   * Обработчик ответа в викторине
   * @param answer - Выбранный ответ
   */
  const handleQuizAnswer = async (answer: string) => {
    if (!currentExercise) return;
    
    const isCorrect = answer === currentExercise.componentConfig?.correctAnswer;
    
    if (isCorrect) {
      setResultText('✓ Правильный ответ! Задание выполнено.');
      
      try {
        await markComplete('module-2', currentExercise!.id, {
          codeSnapshot: `Quiz answer: ${answer}`,
          isSuccess: true,
          errorMessage: null
        });
        await refresh();
      } catch (error) {
        console.error('Ошибка при сохранении прогресса:', error);
      }
    } else {
      setResultText('✗ Неправильный ответ. Попробуйте ещё раз.');
      
      try {
        await markComplete('module-2', currentExercise!.id, {
          codeSnapshot: `Quiz answer: ${answer}`,
          isSuccess: false,
          errorMessage: 'Неправильный ответ'
        });
        await refresh();
      } catch (error) {
        console.error('Ошибка при сохранении прогресса:', error);
      }
    }
  };

  /**
   * Информационный бейдж для ResultCard
   * @returns Текст информационного сообщения
   */
  const infoBadge = useMemo(() => {
    if (showReference) return 'Показана справочная информация.';
    if (showHint) return 'Активна подсказка по заданию.';
    return '';
  }, [showReference, showHint]);

  /**
   * Общее количество упражнений в модуле (с учетом фильтрации)
   * @returns Количество упражнений
   */
  const totalExercisesInModule = selectedDifficulty
    ? filteredBlocks.reduce(
        (sum, block) => {
          const blockData = blocksCache[block.blockId] || block;
          return sum + blockData.exercises.filter(ex => ex.difficulty === selectedDifficulty).length;
        },
        0
      )
    : moduleData?.blocks.reduce(
        (sum, block) => sum + (block.exerciseCount ?? block.exercises.length),
        0
      ) || 0;

  if (loading) {
    return (
      <div className="element-simulator-page">
        <PageHeader activeNav="element" />
        <main className="element-main-content">
          <div className="loading">Загрузка заданий...</div>
        </main>
      </div>
    );
  }

  if (!isAuthenticated && !exerciseFromUrl) {
    return (
      <div className="element-simulator-page">
        <PageHeader activeNav="element" />
        <main className="element-main-content">
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <h2>Требуется авторизация</h2>
            <p>Пожалуйста, войдите в систему для доступа к упражнениям.</p>
            <a href="/login" style={{ color: '#667eea' }}>Перейти на страницу входа</a>
          </div>
        </main>
      </div>
    );
  }

  if (error || !moduleData) {
    return (
      <div className="element-simulator-page">
        <PageHeader activeNav="element" />
        <main className="element-main-content">
          <div className="error-message" style={{ color: 'red', padding: '20px' }}>
            {error || 'Модуль не найден'}
          </div>
        </main>
      </div>
    );
  }

  if (!currentExercise) {
    return (
      <div className="element-simulator-page">
        <PageHeader activeNav="element" />
        <main className="element-main-content">
          <div className="error-message">Задание не найдено</div>
        </main>
      </div>
    );
  }

  return (
    <div className="element-simulator-page">
      <PageHeader activeNav="element" />

      <ReferenceModal
        isOpen={showReference}
        onClose={() => setShowReference(false)}
        module="elements"
      />

      <main className="element-main-content">
        {showHint && (
          <div className="hint-toast">
            <div className="hint-toast-content">
              <span className="hint-text">{hintText}</span>
              <button className="hint-close" onClick={() => setShowHint(false)}>×</button>
            </div>
          </div>
        )}

        <ExerciseHeader
          currentExercise={globalExerciseIndex}
          totalExercises={totalExercisesInModule}
          completedExercises={completedCount}
          blockTitle={`Блок ${currentBlockIndex + 1}. ${currentBlock?.title || 'Загрузка...'}`}
          taskTitle={currentExercise.title}
          taskDescription={currentExercise.description}
          onPrevious={handlePrevious}
          onNext={handleNext}
          onShowReference={handleShowReference}
          onShowHint={handleShowHint}
          blocks={filteredBlocks}
          currentBlockIndex={filteredBlocks.findIndex(b => b.blockId === currentBlockMeta?.blockId)}
          onBlockChange={handleBlockChange}
          isCurrentExerciseCompleted={isCurrentExerciseCompleted}
          selectedDifficulty={selectedDifficulty}
          onDifficultyChange={handleDifficultyChange}
          originalBlocks={moduleData?.blocks}
        />

        <section className="element-middle-section">
          <div className="element-card ui-card">
            {currentExercise ? (
              renderTrainingComponent(
                currentExercise.trainingComponent || 'ElementsButtonsTrainer',
                currentExercise.componentConfig,
                currentExercise.id,
                handleQuizAnswer
              )
            ) : (
              <div style={{ padding: '20px', color: '#666' }}>
                Загрузка упражнения...
              </div>
            )}
          </div>

          {(!currentExercise?.componentConfig?.disableIDE && currentExercise?.trainingComponent !== 'QuizTrainer') && (
            <div className="ide-column">
              <IDE
                codeValue={codeValue}
                terminalOutput={terminalOutput}
                isRunning={isRunning}
                onCodeChange={setCodeValue}
                onRunCode={handleRunCode}
                fileName="UserScript.java"
              />
            </div>
          )}
        </section>

        <ResultCard
          resultText={resultText}
          infoBadge={infoBadge}
        />
      </main>
    </div>
  );
};

export default ElementSimulatorPage;