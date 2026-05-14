import React, { useState, useMemo, useEffect } from 'react';
import './POMSimulatorPage.css';
import PageHeader from '../../components/PageHeader/PageHeader';
import ExerciseHeader from '../../components/ExerciseHeader/ExerciseHeader';
import ResultCard from '../../components/ResultCard/ResultCard';
import ReferenceModal from '../../components/ReferenceModal/ReferenceModal';
import DifficultyFilter from '../../components/DifficultyFilter/DifficultyFilter';
import IDE from '../../components/IDE/IDE';
import { useProgress } from '../../hooks/useProgress';
import { fetchWithAuth } from '../../services/fetchWithAuth';

/** Упражнение модуля POM */
interface Exercise {
  /** ID упражнения */
  id: string;
  /** Порядковый номер упражнения */
  order: number;
  /** Название упражнения */
  title: string;
  /** Описание упражнения */
  description: string;
  /** Подсказка к упражнению */
  hint: string;
  /** Начальный код для упражнения */
  initialCode: string;
  /** Тип страницы для отображения в iframe */
  harnessPageType: 'elements' | 'items' | 'login' | 'home' | 'products' | 'profile' | 'components' | 'cat-characters';
  /** ID блока упражнения */
  blockId: string;
  /** Только компиляция без динамической проверки */
  compileOnly?: boolean;
  /** Сложность упражнения */
  difficulty: 'easy' | 'medium' | 'hard';
}

/** Блок упражнений */
interface Block {
  /** ID блока */
  blockId: string;
  /** Название блока */
  title: string;
}

/** Режим отображения информации */
type InfoMode = 'none' | 'reference' | 'hint';

/** Массив блоков упражнений */
const BLOCKS: Block[] = [
  { blockId: 'block-1', title: 'Блок 1. Page Elements' },
  { blockId: 'block-2', title: 'Блок 2. Page Methods' },
  { blockId: 'block-3', title: 'Блок 3. Наследование и Base Page' },
  { blockId: 'block-4', title: 'Блок 4. Компоненты' },
  { blockId: 'block-5', title: 'Блок 5. Base Test и тесты' },
];

/**
 * Парсит начальный код на отдельные файлы
 * Разделяет код по маркеру "---" и извлекает имена классов
 * @param initialCode - Начальный код упражнения
 * @returns Объект с ключами-именами файлов и значениями-кодом
 */
const parseInitialCode = (initialCode: string | undefined): Record<string, string> => {
  if (!initialCode || initialCode.trim() === '') return { 'UserScript.java': '' };

  const parts = initialCode
    .split(/\n?---\n?/m)
    .map(p => p.trim())
    .filter(Boolean);

  if (parts.length === 0) return { 'UserScript.java': initialCode };

  const result: Record<string, string> = {};

  parts.forEach((part, index) => {
    const match = part.match(/public\s+(?:class|interface|enum|abstract\s+class)\s+(\w+)/);
    const className = match ? match[1] : `File${index + 1}`;
    result[`${className}.java`] = part;
  });

  return result;
};

/**
 * Объединяет несколько файлов в одну строку для отправки на сервер
 * @param files - Объект с файлами
 * @returns Строка, содержащая все файлы, разделенные двумя переносами строк
 */
const combineFilesForServer = (files: Record<string, string>): string => {
  return Object.keys(files).map(key => files[key]).join('\n\n');
};

/**
 * Страница симулятора POM
 * @param props - Пропсы компонента
 * @param props.initialExerciseOrder - Начальный номер упражнения
 * @returns JSX элемент страницы
 */
const POMSimulatorPage: React.FC<{ initialExerciseOrder?: number }> = ({ initialExerciseOrder }) => {
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [currentExerciseIndex, setCurrentExerciseIndex] = useState(0);
  const [selectedDifficulty, setSelectedDifficulty] = useState<'easy' | 'medium' | 'hard' | null>(null);

  const [files, setFiles] = useState<Record<string, string>>({ 'UserScript.java': '' });
  const [activeFile, setActiveFile] = useState('UserScript.java');
  const [terminalOutput, setTerminalOutput] = useState('// Загрузка упражнений...');
  const [isRunning, setIsRunning] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [showReference, setShowReference] = useState(false);
  const [resultText, setResultText] = useState('Выполните задание!');
  const [showHint, setShowHint] = useState(false);
  const [infoMode, setInfoMode] = useState<InfoMode>('none');
  const [currentBlockIndex, setCurrentBlockIndex] = useState(0);

  const { markComplete, progress, refresh } = useProgress();

  const module3 = progress?.modules.find(m => m.moduleId === 'module-3');
  const completedCount = module3?.completedExercises || 0;

  /**
   * Фильтрация блоков по выбранной сложности
   * @returns Отфильтрованный массив блоков
   */
  const filteredBlocks = useMemo(() => {
    if (!selectedDifficulty) return BLOCKS;
    
    return BLOCKS.filter(block => {
      return exercises.some(ex => ex.blockId === block.blockId && ex.difficulty === selectedDifficulty);
    });
  }, [selectedDifficulty, exercises]);

  /**
   * Фильтрация упражнений по выбранной сложности
   * @returns Отфильтрованный массив упражнений
   */
  const filteredExercises = useMemo(() => {
    if (!selectedDifficulty) return exercises;
    return exercises.filter(ex => ex.difficulty === selectedDifficulty);
  }, [exercises, selectedDifficulty]);

  /**
   * Получение текущего упражнения
   * @returns Текущее упражнение или null
   */
  const getCurrentExercise = (): Exercise | null =>
    filteredExercises[currentExerciseIndex] ?? null;

  const currentExerciseObj = getCurrentExercise();
  const isCurrentExerciseCompleted = currentExerciseObj
    ? progress?.modules
        .find(m => m.moduleId === 'module-3')?.exercises
        .some(ex => ex.exerciseId === currentExerciseObj.id && ex.isCompleted) || false
    : false;

  const totalExercises = selectedDifficulty
    ? filteredExercises.length
    : 57;

  const globalExerciseIndex = selectedDifficulty
    ? currentExerciseIndex + 1
    : currentExerciseObj ? currentExerciseObj.order - 100 : 1;

  /**
   * Загрузка упражнений
   */
  useEffect(() => {
    loadExercises(BLOCKS[0].blockId, initialExerciseOrder);
  }, [initialExerciseOrder]);

  /**
   * Загрузка упражнений для указанного блока
   * @param blockId - ID блока
   * @param targetExerciseOrder - Целевой номер упражнения
   * @param autoSelectIndex - Индекс для автоматического выбора упражнения
   */
  const loadExercises = async (blockId: string, targetExerciseOrder?: number, autoSelectIndex?: number) => {
    setIsLoading(true);
    try {
      const url = `http://localhost:8080/api/exercises/module-3/blocks/${blockId}/exercises`;
      const response = await fetchWithAuth(url);

      if (!response.ok) {
        const errorText = await response.text();

        if (response.status === 401) {
          setTerminalOutput(
            '🔐 Требуется авторизация\n\n' +
            'Вы не вошли в систему. Пожалуйста, войдите под своим аккаунтом.\n\n' +
            '→ Перейти на страницу входа: /login'
          );
          setExercises([]);
          return;
        }

        setTerminalOutput(`❌ Ошибка загрузки упражнений: ${response.status} ${response.statusText}\n${errorText}`);
        setExercises([]);
        return;
      }

      const data = await response.json();

      const mappedExercises: Exercise[] = data.map((ex: any) => ({
        id: ex.id,
        order: ex.order,
        title: ex.title,
        description: ex.description,
        hint: ex.hint,
        initialCode: ex.initialCode || '',
        harnessPageType: getHarnessPageType(ex.order, blockId),
        blockId,
        compileOnly: ex.validationRules?.harnessRules?.compileOnly === true,
        difficulty: ex.difficulty,
      }));
      setExercises(mappedExercises);

      let targetIndex = 0;
      if (autoSelectIndex !== undefined) {
        targetIndex = autoSelectIndex < 0 ? mappedExercises.length - 1 : autoSelectIndex;
      } else if (targetExerciseOrder !== undefined) {
        const foundIndex = mappedExercises.findIndex(ex => ex.order === targetExerciseOrder);
        if (foundIndex !== -1) {
          targetIndex = foundIndex;
        }
      }

      if (mappedExercises.length > 0) {
        applyExercise(mappedExercises[targetIndex]);
        setCurrentExerciseIndex(targetIndex);
      }
    } catch (error) {
      if (error instanceof TypeError && error.message.includes('fetch')) {
        setTerminalOutput(
          '❌ Ошибка соединения с сервером\n\n' +
          'Не удалось подключиться к серверу. Убедитесь, что:\n' +
          '1. Backend запущен на порту 8080\n' +
          '2. Нет проблем с CORS\n\n' +
          'Попробуйте обновить страницу.'
        );
      } else {
        setTerminalOutput(`Ошибка соединения: ${String(error)}\n\nУбедитесь, что сервер запущен на порту 8080.`);
      }

      setExercises([]);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Применяет выбранное упражнение: устанавливает файлы и сбрасывает состояние
   * @param exercise - Выбранное упражнение
   */
  const applyExercise = (exercise: Exercise) => {
    const initialFiles = parseInitialCode(exercise?.initialCode);
    const filesToSet = initialFiles && Object.keys(initialFiles).length > 0
      ? initialFiles
      : { 'UserScript.java': '' };
    setFiles(filesToSet);
    setActiveFile(Object.keys(filesToSet)[0] ?? 'UserScript.java');
    setResultText('Выполните задание!');
    setShowHint(false);
    setInfoMode('none');
    setTerminalOutput('// Код будет отправлен на сервер для проверки');
  };

  /**
   * Обработчик смены блока
   * @param filteredIndex - Индекс блока в отфильтрованном списке
   */
  const handleBlockChange = (filteredIndex: number) => {
    if (!filteredBlocks[filteredIndex]) return;
    
    const targetBlock = filteredBlocks[filteredIndex];
    const realBlockIndex = BLOCKS.findIndex(block => block.blockId === targetBlock.blockId);
    
    if (realBlockIndex === -1) return;
    
    setCurrentBlockIndex(realBlockIndex);
    
    if (selectedDifficulty) {
      const firstExerciseInBlock = filteredExercises.find(ex => ex.blockId === targetBlock.blockId);
      if (firstExerciseInBlock) {
        const exerciseIndex = filteredExercises.findIndex(ex => ex.id === firstExerciseInBlock.id);
        setCurrentExerciseIndex(exerciseIndex);
        applyExercise(firstExerciseInBlock);
      }
    } else {
      setCurrentExerciseIndex(0);
      loadExercises(targetBlock.blockId, undefined, 0);
    }
  };

  /**
   * Обработчик изменения фильтра по сложности 
   * @param difficulty - Выбранная сложность или null для сброса
   */
  const handleDifficultyChange = async (difficulty: 'easy' | 'medium' | 'hard' | null) => {
    setSelectedDifficulty(difficulty);
    setCurrentExerciseIndex(0);
    
    if (difficulty) {
      setIsLoading(true);
      try {
        const allExercises: Exercise[] = [];
        
        for (const block of BLOCKS) {
          const url = `http://localhost:8080/api/exercises/module-3/blocks/${block.blockId}/exercises`;
          const response = await fetchWithAuth(url);
          
          if (response.ok) {
            const data = await response.json();
            const mappedExercises: Exercise[] = data.map((ex: any) => ({
              id: ex.id,
              order: ex.order,
              title: ex.title,
              description: ex.description,
              hint: ex.hint,
              initialCode: ex.initialCode || '',
              harnessPageType: getHarnessPageType(ex.order, block.blockId),
              blockId: block.blockId,
              compileOnly: ex.validationRules?.harnessRules?.compileOnly === true,
              difficulty: ex.difficulty,
            }));
            allExercises.push(...mappedExercises);
          }
        }
        
        setExercises(allExercises);
        
        const firstExerciseWithDifficulty = allExercises.find(ex => ex.difficulty === difficulty);
        
        if (firstExerciseWithDifficulty) {
          const blockIndex = BLOCKS.findIndex(block => block.blockId === firstExerciseWithDifficulty.blockId);
          if (blockIndex !== -1) {
            setCurrentBlockIndex(blockIndex);
          }
          
          applyExercise(firstExerciseWithDifficulty);
        }
      } catch (error) {
        console.error('Ошибка при загрузке упражнений для фильтрации:', error);
        setTerminalOutput('Ошибка при загрузке упражнений для фильтрации');
      } finally {
        setIsLoading(false);
      }
    } else {
      const currentBlock = BLOCKS[currentBlockIndex];
      if (currentBlock) {
        loadExercises(currentBlock.blockId, undefined, 0);
      }
    }
  };

  /**
   * Определяет тип страницы для отображения в iframe на основе номера упражнения и блока
   * @param order - Номер упражнения
   * @param blockId - ID блока
   * @returns Тип страницы для отображения
   */

  const getHarnessPageType = (
    order: number,
    blockId: string
  ): Exercise['harnessPageType'] => {
    if (blockId === 'block-1') {
      if (order >= 107 && order <= 108) return 'items';
      return 'elements';
    }
    if (blockId === 'block-2') {
      if (order <= 117) return 'login';
      if (order <= 122) return 'products';
      if (order === 123) return 'cat-characters';
      return 'home';
    }
    if (blockId === 'block-3') return 'home';
    if (blockId === 'block-4') return 'components';
    if (blockId === 'block-5') {
      if (order >= 153 && order <= 156) return 'products';
      if (order >= 150 && order <= 152) return 'profile';
      return 'login';
    }
    return 'elements';
  };

  /**
   * Обработчик перехода к предыдущему упражнению
   */
  const handlePrevious = () => {
    if (currentExerciseIndex > 0) {
      const newIndex = currentExerciseIndex - 1;
      setCurrentExerciseIndex(newIndex);
      applyExercise(filteredExercises[newIndex]);
    } else {
      const currentFilteredIndex = filteredBlocks.findIndex(b => b.blockId === BLOCKS[currentBlockIndex]?.blockId);
      if (currentFilteredIndex > 0) {
        const prevBlock = filteredBlocks[currentFilteredIndex - 1];
        const prevBlockRealIndex = BLOCKS.findIndex(b => b.blockId === prevBlock.blockId);
        
        if (prevBlockRealIndex !== -1) {
          setCurrentBlockIndex(prevBlockRealIndex);
          loadExercises(prevBlock.blockId, undefined, -1);
        }
      }
    }
  };

  /**
   * Обработчик перехода к следующему упражнению
   */
  const handleNext = () => {
    if (currentExerciseIndex < filteredExercises.length - 1) {
      const newIndex = currentExerciseIndex + 1;
      setCurrentExerciseIndex(newIndex);
      applyExercise(filteredExercises[newIndex]);
    } else {
      const currentFilteredIndex = filteredBlocks.findIndex(b => b.blockId === BLOCKS[currentBlockIndex]?.blockId);
      if (currentFilteredIndex < filteredBlocks.length - 1) {
        const nextBlock = filteredBlocks[currentFilteredIndex + 1];
        const nextBlockRealIndex = BLOCKS.findIndex(b => b.blockId === nextBlock.blockId);
        
        if (nextBlockRealIndex !== -1) {
          setCurrentBlockIndex(nextBlockRealIndex);
          loadExercises(nextBlock.blockId, undefined, 0);
        }
      }
    }
  };

  /**
   * Обработчик открытия справочного окна
   */
  const handleShowReference = () => {
    setShowReference(true);
    setInfoMode('reference');
    setResultText('Справочная информация открыта.');
  };

  /**
   * Обработчик показа подсказки
   */
  const handleShowHint = () => {
    if (!showHint && getCurrentExercise()) {
      setShowHint(true);
      setTimeout(() => setShowHint(false), 8000);
    } else {
      setShowHint(false);
    }
  };

  /**
   * Обработчик выполнения кода
   * Отправляет код на сервер для валидации и обновляет прогресс
   */
  const handleRunCode = async () => {
    setIsRunning(true);
    setInfoMode('none');

    try {
      const exercise = getCurrentExercise();
      if (!exercise) {
        setTerminalOutput('❌ Упражнение не выбрано');
        setIsRunning(false);
        return;
      }

      const code = combineFilesForServer(files);
      const isEmpty = Object.values(files).every(f => !f.trim());
      if (isEmpty) {
        setTerminalOutput('⚠️ Код пустой. Напишите решение перед отправкой на сервер.');
        setResultText('Добавьте код в редактор и попробуйте снова.');
        setIsRunning(false);
        
        try {
          await markComplete('module-3', currentExerciseObj!.id, {
            codeSnapshot: '',
            isSuccess: false,
            errorMessage: 'Код пустой'
          });
          await refresh();
        } catch {
        }
        return;
      }

      setTerminalOutput('📋 Отправка кода на сервер для валидации...\n');

      const globalExerciseNum = exercise.order;
      const isModule3 = globalExerciseNum >= 101 && globalExerciseNum <= 157;

      const requestBody: any = {
        exercise: globalExerciseNum,
        moduleId: 'module-3',
        baseUrl: 'http://localhost:5173',
      };

      if (isModule3) {
        requestBody.files = files;
      } else {
        requestBody.code = combineFilesForServer(files);
      }

      const response = await fetchWithAuth('http://localhost:8080/api/code/run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });

      const data = await response.json();

      if (data.success) {
        setTerminalOutput(
          '✅ Код успешно прошёл полную валидацию!\n\n' +
          '┌─────────────────────────────────────┐\n' +
          '│  Динамическая проверка: ✅ Пройдена │\n' +
          '│  Статическая проверка:  ✅ Пройдена │\n' +
          '└─────────────────────────────────────┘\n\n' +
          (data.message || '')
        );
        setResultText('🎉 Задание выполнено! Код соответствует требованиям POM.');

        try {
          const code = combineFilesForServer(files);
          await markComplete('module-3', currentExerciseObj!.id, {
            codeSnapshot: code,
            isSuccess: true,
            errorMessage: null
          });
          await refresh();
        } catch {
        }
      } else {
        const fullOutput = data.stdout || '';
        const errors = data.stderr || data.error || '';

        let resultMessage = '';

        if (fullOutput.includes('Динамическая проверка: УСПЕХ') && errors.includes('Статическая проверка: НЕ УСПЕХ')) {
          resultMessage = '┌────────────────────────────────────────┐\n' +
                          '│  Динамическая проверка: ✅ Пройдена    │\n' +
                          '│  Статическая проверка:  ❌ НЕ ПРОЙДЕНА │\n' +
                          '└────────────────────────────────────────┘\n\n' +
                          'Код не соответствует требованиям упражнения.\n' +
                          'Проверьте методы и селекторы в коде.\n\n' +
                          errors;
          setResultText('❌ Статическая проверка не пройдена');
        } else if (fullOutput.includes('Динамическая проверка: НЕ УСПЕХ')) {
          resultMessage = '┌────────────────────────────────────────┐\n' +
                          '│  Динамическая проверка: ❌ НЕ ПРОЙДЕНА │\n' +
                          '│  Статическая проверка:  ❌ НЕ ПРОЙДЕНА │\n' +
                          '└────────────────────────────────────────┘\n\n' +
                          errors;
          setResultText('❌ Проверки не пройдены');
        } else {
          resultMessage = '❌ Валидация не пройдена:\n\n' + errors;
          setResultText('Код не прошёл валидацию. Исправьте ошибки.');
        }

        setTerminalOutput(resultMessage);
        
        try {
          const code = combineFilesForServer(files);
          await markComplete('module-3', currentExerciseObj!.id, {
            codeSnapshot: code,
            isSuccess: false,
            errorMessage: resultMessage
          });
          await refresh();
        } catch {
        }
      }
    } catch (error) {
      setTerminalOutput(
        `Ошибка соединения с сервером: ${String(error)}\n\nУбедитесь, что сервер запущен на порту 8080.`
      );
      setResultText('Произошла ошибка при выполнении кода.');
    } finally {
      setIsRunning(false);
    }
  };

  /**
   * Информационный бейдж для ResultCard
   * @returns Текст информационного сообщения
   */
  const infoBadge = useMemo(() => {
    if (infoMode === 'reference') return 'Показана справочная информация.';
    if (showHint) return 'Активна подсказка по заданию.';
    return '';
  }, [infoMode, showHint]);

  /**
   * Получение заголовка текущего блока
   * @returns Заголовок блока
   */
  const getBlockTitle = () => BLOCKS[currentBlockIndex]?.title ?? `Блок ${currentBlockIndex + 1}`;

  const currentPageType = getCurrentExercise()?.harnessPageType ?? 'elements';

  /**
   * Получение URL тестовой страницы для отображения в iframe 
   * @param pageType - Тип страницы
   * @returns URL страницы
   */
  const getTestPageUrl = (pageType: string): string => {
    const base = 'http://localhost:5173/test-harness/module3';
    switch (pageType) {
      case 'elements': return `${base}/block1/elements`;
      case 'items': return `${base}/block1/items`;
      case 'login': return `${base}/login`;
      case 'home': return `${base}/home`;
      case 'products': return `${base}/products`;
      case 'profile': return `${base}/profile`;
      case 'components': return `${base}/components`;
      case 'cat-characters': return `${base}/cat-characters`;
      default: return `${base}/block1/elements`;
    }
  };

  if (isLoading) {
    return (
      <div className="pom-simulator-page">
        <PageHeader activeNav="pom" />
        <main className="pom-main-content">
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <h2>Загрузка упражнений...</h2>
          </div>
        </main>
      </div>
    );
  }

  if (selectedDifficulty && filteredExercises.length === 0 && !isLoading) {
    return (
      <div className="pom-simulator-page">
        <PageHeader activeNav="pom" />
        <main className="pom-main-content">
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <h2>Нет упражнений с выбранной сложностью</h2>
            <p>Попробуйте выбрать другой уровень сложности или сбросить фильтр.</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="pom-simulator-page">
      <PageHeader activeNav="pom" />

      <ReferenceModal
        isOpen={showReference}
        onClose={() => setShowReference(false)}
        module="pom"
      />

      <main className="pom-main-content">
        <ExerciseHeader
          currentExercise={globalExerciseIndex}
          totalExercises={totalExercises}
          completedExercises={completedCount}
          blockTitle={getBlockTitle()}
          taskTitle={getCurrentExercise()?.title ?? ''}
          taskDescription={getCurrentExercise()?.description ?? ''}
          onPrevious={handlePrevious}
          onNext={handleNext}
          onShowReference={handleShowReference}
          onShowHint={handleShowHint}
          blocks={filteredBlocks}
          currentBlockIndex={filteredBlocks.findIndex(b => b.blockId === BLOCKS[currentBlockIndex]?.blockId)}
          onBlockChange={handleBlockChange}
          isCurrentExerciseCompleted={isCurrentExerciseCompleted}
          selectedDifficulty={selectedDifficulty}
          onDifficultyChange={handleDifficultyChange}
          originalBlocks={BLOCKS}
        />

        <div className="pom-middle-section">
          {!getCurrentExercise()?.compileOnly && (
          <div className="pom-card website-card">
            <h3 className="pom-card-title">Тренировочный сайт</h3>
            <iframe
              className="test-page-iframe"
              src={getTestPageUrl(currentPageType)}
              title="Тестовая страница"
            />
          </div>
          )}

          <div className="ide-container">
            <IDE
              files={files}
              terminalOutput={terminalOutput}
              isRunning={isRunning}
              activeFile={activeFile}
              onFilesChange={setFiles}
              onActiveFileChange={setActiveFile}
              onRunCode={handleRunCode}
            />
          </div>
        </div>

        {showHint && getCurrentExercise()?.hint && (
          <div className="hint-toast">
            <div className="hint-toast-content">
              <span className="hint-text">{getCurrentExercise()!.hint}</span>
              <button className="hint-close" onClick={() => setShowHint(false)}>×</button>
            </div>
          </div>
        )}

        <ResultCard resultText={resultText} infoBadge={infoBadge} />
      </main>
    </div>
  );
};

export default POMSimulatorPage;
