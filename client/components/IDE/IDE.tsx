import React, { useState } from 'react';
import './IDE.css';

/**
 * Пропсы для многофайловой версии IDE
 */
interface IDEProps {
  /** Объект с файлами (имя файла -> содержимое) */
  files?: Record<string, string>;
  /** Вывод терминала */
  terminalOutput: string;
  /** Флаг выполнения кода */
  isRunning: boolean;
  /** Имя активного файла */
  activeFile?: string;
  /** Обработчик изменения файлов */
  onFilesChange: (files: Record<string, string>) => void;
  /** Обработчик смены активного файла */
  onActiveFileChange: (fileName: string) => void;
  /** Обработчик запуска кода */
  onRunCode: () => void;
}

/**
 * Пропсы для однофайловой версии IDE
 */
interface LegacyIDEProps {
  /** Содержимое кода */
  codeValue?: string;
  /** Вывод терминала */
  terminalOutput: string;
  /** Флаг выполнения кода */
  isRunning: boolean;
  /** Обработчик изменения кода */
  onCodeChange: (code: string) => void;
  /** Обработчик запуска кода */
  onRunCode: () => void;
  /** Имя файла */
  fileName?: string;
}

/** Объединенные пропсы для обратной совместимости */
type IDECombinedProps = IDEProps | LegacyIDEProps;

/**
 * Компонент среды разработки (IDE)
 * 
 * @param props - Пропсы компонента (однофайловые или многофайловые)
 * @returns JSX элемент IDE
 */
const IDE: React.FC<IDECombinedProps> = (props) => {
  const isLegacy = 'codeValue' in props && 'onCodeChange' in props;

  if (isLegacy) {
    return <LegacyIDE {...(props as LegacyIDEProps)} />;
  }

  return <NewIDE {...(props as IDEProps)} />;
};

const INDENT = '    ';

/**
 * Обработчик клавиатурных событий в редакторе кода
 * Обеспечивает автоматические отступы, обработку Tab и Enter
 * @param e - Событие клавиатуры
 * @param currentValue - Текущее значение в редакторе
 * @param onValueChange - Колбэк для изменения значения
 */
const handleEditorKeyDown = (
  e: React.KeyboardEvent<HTMLTextAreaElement>,
  currentValue: string,
  onValueChange: (newValue: string) => void
) => {
  const textarea = e.currentTarget;

  if (e.key === 'Tab') {
    e.preventDefault();
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;

    if (start !== end) {
      const before = currentValue.substring(0, start);
      const selected = currentValue.substring(start, end);
      const after = currentValue.substring(end);

      const lines = selected.split('\n');
      const indented = lines.map(line => INDENT + line).join('\n');
      const newValue = before + indented + after;

      onValueChange(newValue);

      requestAnimationFrame(() => {
        textarea.selectionStart = start;
        textarea.selectionEnd = end + indented.length - selected.length;
      });
    } else {
      const before = currentValue.substring(0, start);
      const after = currentValue.substring(start);
      onValueChange(before + INDENT + after);

      requestAnimationFrame(() => {
        textarea.selectionStart = textarea.selectionEnd = start + INDENT.length;
      });
    }
    return;
  }

  if (e.key === 'Tab' && e.shiftKey) {
    e.preventDefault();
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;

    if (start !== end) {
      const before = currentValue.substring(0, start);
      const selected = currentValue.substring(start, end);
      const after = currentValue.substring(end);

      const lines = selected.split('\n');
      const dedented = lines.map(line =>
        line.startsWith(INDENT) ? line.substring(INDENT.length) :
        line.startsWith('\t') ? line.substring(1) :
        line.replace(/^ {1,4}/, '')
      ).join('\n');
      const newValue = before + dedented + after;

      onValueChange(newValue);
      requestAnimationFrame(() => {
        textarea.selectionStart = start;
        textarea.selectionEnd = start + dedented.length;
      });
    }
    return;
  }

  if (e.key === 'Enter') {
    e.preventDefault();
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const before = currentValue.substring(0, start);

    const lineStart = before.lastIndexOf('\n') + 1;
    const currentLine = before.substring(lineStart);

    const leadingSpaces = currentLine.match(/^(\s*)/)?.[1] || '';

    const trimmedBefore = currentLine.trimEnd();
    const extraIndent = trimmedBefore.endsWith('{') ? INDENT : '';

    let adjustedIndent = leadingSpaces;
    if (trimmedBefore.endsWith('}') && leadingSpaces.length >= INDENT.length) {
      adjustedIndent = leadingSpaces.substring(INDENT.length);
    }

    const indent = adjustedIndent + extraIndent;
    const newValue = currentValue.substring(0, start) + '\n' + indent + currentValue.substring(end);

    onValueChange(newValue);

    requestAnimationFrame(() => {
      const cursorPos = start + 1 + indent.length;
      textarea.selectionStart = textarea.selectionEnd = cursorPos;
    });
    return;
  }
};

const LegacyIDE: React.FC<LegacyIDEProps> = ({
  codeValue = '',
  terminalOutput,
  isRunning,
  onCodeChange,
  onRunCode,
  fileName = 'UserScript.java',
}) => {
  const handleCodeChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    onCodeChange(e.target.value);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    handleEditorKeyDown(e, codeValue, onCodeChange);
  };

  return (
    <div className="ide-card">
      <div className="file-tabs">
        <div className="tab-item active">
          <span className="tab-name">{fileName}</span>
        </div>
      </div>

      <div className="ide-body">
        <textarea
          className="code-editor"
          value={codeValue}
          onChange={handleCodeChange}
          onKeyDown={handleKeyDown}
          spellCheck={false}
          placeholder={`// ${fileName}`}
        />
        <pre className="terminal-output">{terminalOutput}</pre>
      </div>

      <div className="ide-actions">
        <button
          className="run-button"
          onClick={onRunCode}
          disabled={isRunning}
        >
          {isRunning ? 'Запуск...' : 'Запустить'}
        </button>
      </div>
    </div>
  );
};

const NewIDE: React.FC<IDEProps> = ({
  files,
  terminalOutput,
  isRunning,
  activeFile,
  onFilesChange,
  onActiveFileChange,
  onRunCode,
}) => {
  const safeFiles = files || { 'UserScript.java': '' };
  const safeActiveFile = activeFile || 'UserScript.java';

  const handleFilesChange = onFilesChange || (() => {});
  const handleActiveFileChange = onActiveFileChange || (() => {});

  const [renamingFile, setRenamingFile] = useState<string | null>(null);
  const [newFileName, setNewFileName] = useState('');

  const handleCodeChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    handleFilesChange({ ...safeFiles, [safeActiveFile]: e.target.value });
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    handleEditorKeyDown(e, safeFiles[safeActiveFile] ?? '', (newValue: string) => {
      handleFilesChange({ ...safeFiles, [safeActiveFile]: newValue });
    });
  };

  const handleAddFile = () => {
    const existingNums = Object.keys(safeFiles)
      .map(name => {
        const m = name.match(/^NewFile(\d+)\.java$/);
        return m ? parseInt(m[1]) : 0;
      })
      .filter(n => n > 0);
    const nextNum = existingNums.length > 0 ? Math.max(...existingNums) + 1 : 1;
    const newFileName = `NewFile${nextNum}.java`;
    handleFilesChange({ ...safeFiles, [newFileName]: `public class NewFile${nextNum} {\n    \n}\n` });
    handleActiveFileChange(newFileName);
  };

  const handleRemoveFile = (fileName: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (Object.keys(safeFiles).length <= 1) return;
    const newFiles = { ...safeFiles };
    delete newFiles[fileName];
    handleFilesChange(newFiles);
    if (activeFile === fileName) {
      handleActiveFileChange(Object.keys(newFiles)[0]);
    }
  };

  const startRename = (fileName: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setRenamingFile(fileName);
    setNewFileName(fileName);
  };

  const confirmRename = () => {
    if (renamingFile && newFileName.trim() && newFileName !== renamingFile) {
      if (safeFiles[newFileName]) {
        alert(`Файл "${newFileName}" уже существует!`);
        setRenamingFile(null);
        return;
      }

      if (!newFileName.endsWith('.java')) {
        alert('Имя файла должно заканчиваться на .java');
        setNewFileName(renamingFile);
        return;
      }

      const newFiles = { ...safeFiles };
      newFiles[newFileName] = newFiles[renamingFile];
      delete newFiles[renamingFile];
      handleFilesChange(newFiles);

      if (activeFile === renamingFile) {
        handleActiveFileChange(newFileName);
      }
    }
    setRenamingFile(null);
  };

  const cancelRename = () => {
    setRenamingFile(null);
    setNewFileName('');
  };

  const handleRenameKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      confirmRename();
    } else if (e.key === 'Escape') {
      cancelRename();
    }
  };

  const fileNames = Object.keys(safeFiles);

  return (
    <div className="ide-card">
      <div className="file-tabs">
        {fileNames.map(fileName => (
          <div
            key={fileName}
            className={`tab-item ${safeActiveFile === fileName ? 'active' : ''}`}
            onClick={() => handleActiveFileChange(fileName)}
          >
            {renamingFile === fileName ? (
              <input
                type="text"
                className="rename-input"
                value={newFileName}
                onChange={(e) => setNewFileName(e.target.value)}
                onBlur={confirmRename}
                onKeyDown={handleRenameKeyDown}
                autoFocus
                onClick={(e) => e.stopPropagation()}
              />
            ) : (
              <span className="tab-name">{fileName}</span>
            )}
            {fileNames.length > 1 && !renamingFile && (
              <>
                <button
                  className="tab-rename"
                  onClick={(e) => startRename(fileName, e)}
                  title="Переименовать файл"
                >
                  ✎
                </button>
                <button
                  className="tab-close"
                  onClick={(e) => handleRemoveFile(fileName, e)}
                  title="Удалить файл"
                >
                  ×
                </button>
              </>
            )}
          </div>
        ))}
        <button
          className="add-file-btn"
          onClick={handleAddFile}
          title="Добавить файл"
        >
          +
        </button>
      </div>

      <div className="ide-body">
        <textarea
          className="code-editor"
          value={safeFiles[safeActiveFile] ?? ''}
          onChange={handleCodeChange}
          onKeyDown={handleKeyDown}
          spellCheck={false}
          placeholder={`// ${safeActiveFile}`}
        />
        <pre className="terminal-output">{terminalOutput}</pre>
      </div>

      <div className="ide-actions">
        <button
          className="run-button"
          onClick={onRunCode}
          disabled={isRunning}
        >
          {isRunning ? 'Запуск...' : 'Запустить'}
        </button>
      </div>
    </div>
  );
};

export default IDE;
