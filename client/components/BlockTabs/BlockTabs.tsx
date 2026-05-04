import React from 'react';
import './BlockTabs.css';

interface Block {
  blockId: string;
  title: string;
}

interface BlockTabsProps {
  blocks: Block[];
  currentBlockIndex: number;
  onBlockChange: (index: number) => void;
  originalBlocks?: Block[];
}

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
