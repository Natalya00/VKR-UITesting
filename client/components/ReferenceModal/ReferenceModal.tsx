import React, { useState } from 'react';
import './ReferenceModal.css';
import {
  XPathBasics,
  XPathExamples,
  XPathErrors,
  XPathTips,
  ElementsBasics,
  ElementsExamples,
  ElementsErrors,
  ElementsTips,
  POMBasics,
  POMExamples,
  POMErrors,
  POMTips
} from '../Reference';

interface ReferenceModalProps {
  isOpen: boolean;
  onClose: () => void;
  module?: 'xpath' | 'elements' | 'pom';
}

type TabType = 'basics' | 'examples' | 'errors' | 'tips';

const ReferenceModal: React.FC<ReferenceModalProps> = ({
  isOpen,
  onClose,
  module = 'xpath'
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('basics');

  if (!isOpen) return null;

  const renderContent = () => {
    switch (module) {
      case 'xpath':
        switch (activeTab) {
          case 'basics': return <XPathBasics />;
          case 'examples': return <XPathExamples />;
          case 'errors': return <XPathErrors />;
          case 'tips': return <XPathTips />;
          default: return <XPathBasics />;
        }
      case 'elements':
        switch (activeTab) {
          case 'basics': return <ElementsBasics />;
          case 'examples': return <ElementsExamples />;
          case 'errors': return <ElementsErrors />;
          case 'tips': return <ElementsTips />;
          default: return <ElementsBasics />;
        }
      case 'pom':
        switch (activeTab) {
          case 'basics': return <POMBasics />;
          case 'examples': return <POMExamples />;
          case 'errors': return <POMErrors />;
          case 'tips': return <POMTips />;
          default: return <POMBasics />;
        }
      default:
        return <XPathBasics />;
    }
  };

  return (
    <div className="reference-modal-overlay">
      <div className="reference-modal">
        <div className="reference-content">
          <div className="reference-header">
            <h2>Справочная информация</h2>
            <button className="close-button" onClick={onClose}>✕</button>
          </div>

          <div className="reference-tabs">
            <button
              className={`tab-button ${activeTab === 'basics' ? 'active' : ''}`}
              onClick={() => setActiveTab('basics')}
            >
              Основы
            </button>
            <button
              className={`tab-button ${activeTab === 'examples' ? 'active' : ''}`}
              onClick={() => setActiveTab('examples')}
            >
              Примеры
            </button>
            <button
              className={`tab-button ${activeTab === 'errors' ? 'active' : ''}`}
              onClick={() => setActiveTab('errors')}
            >
              Частые ошибки
            </button>
            <button
              className={`tab-button ${activeTab === 'tips' ? 'active' : ''}`}
              onClick={() => setActiveTab('tips')}
            >
              Советы
            </button>
          </div>

          <div className="reference-body">
            <div className="tab-content">
              {renderContent()}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReferenceModal;
