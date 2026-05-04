import React from 'react';
import './CodeEditor.css';

interface CodeEditorProps {
  codeValue: string;
  terminalOutput: string;
  isRunning: boolean;
  onCodeChange: (value: string) => void;
  onRunCode: () => void;
  onVerifySolution: () => void;
}

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
        <textarea 
          className="code-editor" 
          value={codeValue} 
          onChange={(e) => onCodeChange(e.target.value)} 
        />
        <pre className="terminal-output">{terminalOutput}</pre>
      </div>
      <p className="ide-hint">
        Оставьте класс <code>UserScript</code> с методом <code>run()</code> — он вызывается на сервере. Доступны Selenide и селекторы.
      </p>
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
