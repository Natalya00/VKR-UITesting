import React, { useState, useRef, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProgress } from '../../hooks/useProgress';
import { fetchWithAuth } from '../../services/fetchWithAuth';
import './XPathSimulatorPage.css';
import PageHeader from '../../components/PageHeader/PageHeader';
import ExerciseHeader from '../../components/ExerciseHeader/ExerciseHeader';
import ResultCard from '../../components/ResultCard/ResultCard';
import ReferenceModal from '../../components/ReferenceModal/ReferenceModal';
import DifficultyFilter from '../../components/DifficultyFilter/DifficultyFilter';
import SimpleElementTrainer from '../../components/Training/SimpleElementTrainer';
import PageTrainer from '../../components/Training/PageTrainer';
import RadioGroupTrainer from '../../components/Training/RadioGroupTrainer';
import TableTrainer from '../../components/Training/TableTrainer';
import NavigationTrainer from '../../components/Training/NavigationTrainer';
import { useXPathValidator } from '../../hooks/useXPathValidator';
import { useExerciseValidator, ExerciseSyntaxRules } from '../../hooks/useExerciseValidator';
import { useXPathExecution } from '../../hooks/useXPathExecution';
import { evaluateStability } from '../../utils/xpathStability';

/** Упражнение модуля XPath */
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
  componentConfig: Record<string, unknown>;
  /** Начальный код упражнения */
  initialCode: string;
  /** Порядковый номер упражнения */
  order: number;
  /** Сложность упражнения */
  difficulty: 'easy' | 'medium' | 'hard';
  /** Селектор целевого элемента */
  targetSelector: string;
  /** Ожидаемые синтаксические правила */
  expectedSyntax?: ExerciseSyntaxRules;
}

/** Блок упражнений */
interface Block {
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
  blocks: Block[];
}

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

/**
 * Страница симулятора XPath
 * @returns JSX элемент страницы
 */
const XPathSimulatorPage: React.FC = () => {
  const [moduleData, setModuleData] = useState<ModuleData | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingBlockId, setLoadingBlockId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [currentBlockIndex, setCurrentBlockIndex] = useState(0);
  const [currentExerciseIndex, setCurrentExerciseIndex] = useState(0);
  const [selectedDifficulty, setSelectedDifficulty] = useState<'easy' | 'medium' | 'hard' | null>(null);
  const [xpathQuery, setXpathQuery] = useState('');
  const [result, setResult] = useState('Выполните задание!');
  const [showHint, setShowHint] = useState(false);
  const [hintText, setHintText] = useState('');
  const [exerciseCompleted, setExerciseCompleted] = useState(false);
  const [stabilityText, setStabilityText] = useState('');
  const [showReference, setShowReference] = useState(false);
  const [blocksCache, setBlocksCache] = useState<Record<string, Block>>({});
  const testContainerRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const { validateSyntax } = useXPathValidator();
  const { validateExerciseSyntax } = useExerciseValidator();
  const {
    elementsFound,
    highlightedElements,
    executeXPath,
    highlightElements,
    clearHighlight,
    setElementsFound,
  } = useXPathExecution(testContainerRef);

  const { markComplete, progress, refresh } = useProgress();

  const module1 = progress?.modules.find(m => m.moduleId === 'module-1');
  const completedCount = module1?.completedExercises || 0;

  /**
   * Загрузка данных модуля
   */
  useEffect(() => {
    setLoading(true);
    fetchWithAuth(`${API_BASE_URL}/api/exercises/module-1`)
      .then(res => {
        if (res.status === 401) {
          throw new Error('AUTH_REQUIRED');
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then(data => {
        setModuleData(data);
        if (data.blocks && data.blocks.length > 0) {
          const firstBlock = data.blocks[0];
          setBlocksCache(prev => ({
            ...prev,
            [firstBlock.blockId]: firstBlock,
          }));
        }
        setLoading(false);
      })
      .catch(err => {
        if (err.message === 'AUTH_REQUIRED') {
          setError(' Требуется авторизация\n\nВы не вошли в систему. Пожалуйста, войдите под своим аккаунтом.');
        } else {
          setError('Ошибка загрузки заданий. Убедитесь, что сервер запущен.');
        }
        setLoading(false);
      });
  }, []);

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
    return currentBlock.exercises.filter(ex => ex.difficulty === selectedDifficulty);
  }, [currentBlock?.exercises, selectedDifficulty]);

  const currentExercise = filteredExercises[currentExerciseIndex];

  /**
   * Глобальный индекс текущего упражнения (учитывает фильтрацию)
   * @returns Номер упражнения от 1 до общего количества
   */
  const globalExerciseIndex = selectedDifficulty
    ? (filteredBlocks
        .slice(0, filteredBlocks.findIndex(b => b.blockId === currentBlockMeta?.blockId))
        .reduce((sum, block) => {
          const blockData = blocksCache[block.blockId] || block;
          return sum + blockData.exercises.filter(ex => ex.difficulty === selectedDifficulty).length;
        }, 0) || 0) +
      currentExerciseIndex +
      1
    : (moduleData?.blocks
        .slice(0, currentBlockIndex)
        .reduce((sum, block) => sum + (block.exerciseCount ?? block.exercises.length), 0) || 0) +
      currentExerciseIndex +
      1;

  /**
   * Проверка, выполнено ли текущее упражнение
   * @returns true если упражнение выполнено
   */
  const isCurrentExerciseCompleted = currentExercise
    ? progress?.modules
        .find(m => m.moduleId === 'module-1')?.exercises
        .some(ex => ex.exerciseId === currentExercise.id && ex.isCompleted) || false
    : false;

  /**
   * Сброс состояния при смене упражнения
   */
  useEffect(() => {
    if (!currentExercise) return;

    clearHighlight();
    setXpathQuery('');
    setResult('Выполните задание!');
    setElementsFound(0);
    setShowHint(false);
    setHintText('');
    setExerciseCompleted(false);
    setStabilityText('');
  }, [currentBlockIndex, currentExerciseIndex, currentExercise, clearHighlight, setElementsFound]);

  /**
   * Обработчик изменения XPath запроса
   * Валидирует синтаксис и подсвечивает найденные элементы 
   * @param xpath - XPath выражение
   */
  const handleXPathChange = (xpath: string) => {
    setXpathQuery(xpath);
    clearHighlight();

    if (!xpath.trim()) {
      return;
    }

    const validation = validateSyntax(xpath);
    if (!validation.isValid) {
      return;
    }

    const foundElements = executeXPath(xpath);
    setElementsFound(foundElements.length);

    if (foundElements.length > 0) {
      highlightElements(foundElements);
    }
  };

  /**
   * Обработчик проверки решения
   * Выполняет валидацию XPath запроса и проверяет соответствие целевым элементам
   */
  const handleCheckSolution = async () => {
    if (!xpathQuery.trim() || !currentExercise) {
      setResult('Введите XPath запрос');
      return;
    }

    if (xpathQuery.includes('data-target')) {
      setResult('❌ Использование атрибута data-target запрещено');
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: 'Использование атрибута data-target запрещено'
        });
        await refresh();
      } catch {
      }
      return;
    }

    const syntaxValidation = validateSyntax(xpathQuery);
    if (!syntaxValidation.isValid) {
      setResult(`Ошибка в XPath запросе: ${syntaxValidation.error}`);
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: `Ошибка в XPath запросе: ${syntaxValidation.error}`
        });
        await refresh();
      } catch {
      }
      return;
    }

    const exerciseValidation = validateExerciseSyntax(xpathQuery, currentExercise.expectedSyntax);
    if (!exerciseValidation.isValid) {
      setResult(`Ошибка: ${exerciseValidation.error}`);
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: exerciseValidation.error
        });
        await refresh();
      } catch {
      }
      return;
    }

    clearHighlight();
    const foundElements = executeXPath(xpathQuery);
    setElementsFound(foundElements.length);

    if (foundElements.length === 0) {
      setResult('Элементы не найдены');
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: 'Элементы не найдены'
        });
        await refresh();
      } catch {
      }
      return;
    }

    const targetElements = testContainerRef.current?.querySelectorAll('[data-target="true"]');

    if (!targetElements || targetElements.length === 0) {
      setResult('Ошибка: целевой элемент не определен');
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: 'Целевой элемент не определен'
        });
        await refresh();
      } catch {
      }
      return;
    }

    const targetArray = Array.from(targetElements);
    const foundTargets = foundElements.filter(el => targetArray.includes(el));
    const foundAllTargets = foundTargets.length === targetElements.length;
    const foundExtraElements = foundElements.length > foundTargets.length;

    const requireAllTargets = currentExercise.expectedSyntax?.requireAllTargets ?? false;

    if (foundAllTargets && !foundExtraElements) {
      setResult('✓ Отлично! Найден точно нужный элемент');
      setStabilityText(evaluateStability(xpathQuery, foundElements).text);
      setExerciseCompleted(true);
      highlightElements(foundTargets);

      await saveProgress();
    } else if (requireAllTargets && !foundAllTargets) {
      const missingCount = targetElements.length - foundTargets.length;
      setResult(`✗ Найдено ${foundTargets.length} из ${targetElements.length} элементов. Не найдено ещё ${missingCount} элемент${missingCount > 1 ? 'ов' : ''}.`);
      setStabilityText('');
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: `Найдено ${foundTargets.length} из ${targetElements.length} элементов`
        });
        await refresh();
      } catch {
      }
    } else if (foundExtraElements) {
      const extraCount = foundElements.length - foundTargets.length;
      if (foundAllTargets) {
        setResult(`✓ Все целевые элементы найдены, но XPath также находит еще ${extraCount} элемент${extraCount > 1 ? 'ов' : ''}. Попробуйте сделать селектор более точным.`);
      } else {
        setResult(`✓ Элемент найден, но XPath также находит еще ${extraCount} элемент${extraCount > 1 ? 'ов' : ''}. Попробуйте сделать селектор более точным.`);
      }
      setStabilityText(evaluateStability(xpathQuery, foundElements).text);
      setExerciseCompleted(true);
      highlightElements(foundTargets);

      await saveProgress();
    } else {
      setResult('✗ Найден не тот элемент. Проверьте XPath запрос.');
      setStabilityText('');
      try {
        await markComplete('module-1', currentExercise.id, {
          codeSnapshot: xpathQuery,
          isSuccess: false,
          errorMessage: 'Найден не тот элемент'
        });
        await refresh();
      } catch {
      }
    }
  };

  /**
   * Сохранение прогресса выполнения упражнения
   */
  const saveProgress = async () => {
    if (!currentExercise) return;

    try {
      await markComplete('module-1', currentExercise.id, {
        codeSnapshot: xpathQuery,
        isSuccess: true,
        errorMessage: null
      });
      await refresh();
    } catch {
    }
  };

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
    fetchWithAuth(`${API_BASE_URL}/api/exercises/module-1/blocks/${blockId}`)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((block: Block) => {
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
   * Обработчик перехода к предыдущему упражнению
   */
  const handlePreviousExercise = () => {
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
  const handleNextExercise = () => {
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
   * Обработчик изменения фильтра по сложности
   * @param difficulty - Выбранная сложность или null для сброса
   */
  const handleDifficultyChange = async (difficulty: 'easy' | 'medium' | 'hard' | null) => {
    setSelectedDifficulty(difficulty);
    setCurrentExerciseIndex(0);
    
    if (difficulty && moduleData) {
      try {
        const newBlocksCache = { ...blocksCache };
        
        for (const block of moduleData.blocks) {
          if (!newBlocksCache[block.blockId]) {
            const response = await fetchWithAuth(`${API_BASE_URL}/api/exercises/module-1/blocks/${block.blockId}`);
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
      }
    }
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

  /**
   * Обработчик открытия справочного окна
   */
  const handleShowReference = () => {
    setShowReference(!showReference);
  };

  /**
   * Рендеринг компонента тренажера
   * @returns JSX элемент тренажера
   */
  const renderTrainer = useMemo(() => {
    if (!currentExercise) return null;

    const key = `${currentBlockIndex}-${currentExerciseIndex}`;

    switch (currentExercise.trainingComponent) {
      case 'SimpleElementTrainer':
        return <SimpleElementTrainer key={key} config={currentExercise.componentConfig as any} />;
      case 'PageTrainer':
        return <PageTrainer key={key} config={currentExercise.componentConfig as any} />;
      case 'RadioGroupTrainer':
        return <RadioGroupTrainer key={key} config={currentExercise.componentConfig as any} />;
      case 'TableTrainer':
        return <TableTrainer key={key} config={currentExercise.componentConfig as any} />;
      case 'NavigationTrainer':
        return <NavigationTrainer key={key} config={currentExercise.componentConfig as any} />;
      default:
        return <div>Неизвестный компонент: {currentExercise.trainingComponent}</div>;
    }
  }, [currentExercise, currentBlockIndex, currentExerciseIndex]);

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
  const totalExercises = selectedDifficulty
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
      <div className="xpath-simulator-page">
        <PageHeader activeNav="xpath" />
        <main className="main-content">
          <div className="loading">Загрузка заданий...</div>
        </main>
      </div>
    );
  }

  if (error || !moduleData) {
    return (
      <div className="xpath-simulator-page">
        <PageHeader activeNav="xpath" />
        <main className="main-content">
          <div className="error-message" style={{ color: 'red', padding: '20px' }}>
            {error || 'Модуль не найден'}
          </div>
        </main>
      </div>
    );
  }

  if (!currentExercise) {
    return (
      <div className="xpath-simulator-page">
        <PageHeader activeNav="xpath" />
        <main className="main-content">
          <div className="error-message">Задание не найдено</div>
        </main>
      </div>
    );
  }

  return (
    <div className="xpath-simulator-page">
      <PageHeader activeNav="xpath" />

      <ReferenceModal
        isOpen={showReference}
        onClose={handleShowReference}
        module="xpath"
      />

      <main className="main-content">
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
          totalExercises={totalExercises}
          completedExercises={completedCount}
          blockTitle={`Блок ${currentBlockIndex + 1}. ${currentBlock?.title}`}
          taskTitle={currentExercise.title}
          taskDescription={currentExercise.description}
          onPrevious={handlePreviousExercise}
          onNext={handleNextExercise}
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

        <section className="middle-section">
          <div className="card training-card">
            <div ref={testContainerRef} className="test-container">
              {renderTrainer}
            </div>
          </div>

          <div className="card xpath-form-card">
            <h3 className="card-title">Введите XPath</h3>

            <div className="xpath-input-container">
              <textarea
                className="xpath-input"
                placeholder="Введите XPath, например: //input[@id='email']"
                value={xpathQuery}
                onChange={(e) => handleXPathChange(e.target.value)}
                rows={6}
              />
            </div>

            <div className="elements-status">
              <span className="elements-found">Найдено элементов: {elementsFound}</span>
            </div>

            <button
              className="check-button"
              onClick={handleCheckSolution}
              disabled={!xpathQuery.trim()}
            >
              Проверить решение
            </button>
          </div>
        </section>

        <ResultCard
          resultText={result}
          infoBadge={infoBadge}
          stabilityText={stabilityText}
        />
      </main>
    </div>
  );
};

export default XPathSimulatorPage;