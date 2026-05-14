import React from 'react';
import './CodeEditor.css';

/**
 * Пропсы компонента CodeEditor
 */
interface CodeEditorProps {
  /** Текущее значение кода в редакторе */
  codeValue: string;
  /** Вывод терминала (результат выполнения кода) */
  terminalOutput: string;
  /** Флаг выполнения кода (для отображения состояния загрузки) */
  isRunning: boolean;
  /** Обработчик изменения кода */
  onCodeChange: (value: string) => void;
  /** Обработчик запуска кода */
  onRunCode: () => void;
  /** Обработчик проверки решения */
  onVerifySolution: () => void;
}

/**
 * Компонент редактора кода для Java/Selenide
 * Предоставляет текстовое поле для ввода кода, область вывода результатов
 * и кнопки для запуска и проверки кода
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент редактора кода
 */
const CodeEditor: React.FC<CodeEditorProps> = ({
  codeValue,
  terminalOutput,
  isRunning,
  onCodeChange,
  onRunCode,
  onVerifySolution
}) => {
  return (
    <div className="card ide-card">
      <div className="ide-body">
        {/* Текстовое поле для ввода кода */}
        <textarea 
          className="code-editor" 
          value={codeValue} 
          onChange={(e) => onCodeChange(e.target.value)} 
        />
        {/* Область вывода результатов выполнения */}
        <pre className="terminal-output">{terminalOutput}</pre>
      </div>
      {/* Подсказка о структуре кода */}
      <p className="ide-hint">
        Оставьте класс <code>UserScript</code> с методом <code>run()</code> — он вызывается на сервере. Доступны Selenide и селекторы.
      </p>
      {/* Кнопки действий */}
      <div className="ide-actions">
        <button 
          className="run-button" 
          type="button" 
          onClick={onRunCode} 
          disabled={isRunning}
        >
          {isRunning ? 'Запуск...' : 'Запустить код'}
        </button>
        <button 
          className="verify-button" 
          type="button" 
          onClick={onVerifySolution}
        >
          Проверить решение
        </button>
      </div>
    </div>
  );
};

export default CodeEditor;
