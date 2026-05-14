import React from 'react';
import './BlockTabs.css';

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
 * Пропсы компонента BlockTabs
 */
interface BlockTabsProps {
  /** Массив блоков для отображения */
  blocks: Block[];
  /** Индекс текущего активного блока */
  currentBlockIndex: number;
  /** Обработчик смены блока */
  onBlockChange: (index: number) => void;
  /** Оригинальные блоки до фильтрации (для корректной нумерации) */
  originalBlocks?: Block[];
}

/**
 * Компонент вкладок блоков упражнений
 * Отображает горизонтальные вкладки для переключения между блоками
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент с вкладками блоков
 */
const BlockTabs: React.FC<BlockTabsProps> = ({ blocks, currentBlockIndex, onBlockChange, originalBlocks }) => {
  return (
    <div className="block-tabs">
      {blocks.map((block, index) => {
        const blockNumber = originalBlocks 
          ? originalBlocks.findIndex(origBlock => origBlock.blockId === block.blockId) + 1
          : index + 1;
          
        return (
          <button
            key={block.blockId}
            className={`block-tab ${index === currentBlockIndex ? 'active' : ''}`}
            onClick={() => onBlockChange(index)}
          >
            Блок {blockNumber}
          </button>
        );
      })}
    </div>
  );
};

export default BlockTabs;
