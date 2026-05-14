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

/**
 * Пропсы компонента ReferenceModal
 */
interface ReferenceModalProps {
  /** Флаг открытости модального окна */
  isOpen: boolean;
  /** Обработчик закрытия модального окна */
  onClose: () => void;
  /** Тип модуля для отображения соответствующей справки */
  module?: 'xpath' | 'elements' | 'pom';
}

/** Типы вкладок справочной информации */
type TabType = 'basics' | 'examples' | 'errors' | 'tips';

/**
 * Компонент модального окна со справочной информацией
 * Отображает справочные материалы для различных модулей с вкладками:
 * - Основы - базовая информация
 * - Примеры - практические примеры
 * - Частые ошибки - распространенные ошибки
 * - Советы - полезные рекомендации
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент модального окна или null
 */
const ReferenceModal: React.FC<ReferenceModalProps> = ({
  isOpen,
  onClose,
  module = 'xpath'
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('basics');

  if (!isOpen) return null;

  /**
   * Отображает содержимое активной вкладки в зависимости от модуля
   * @returns JSX элемент с содержимым вкладки
   */
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
          {/* Заголовок модального окна */}
          <div className="reference-header">
            <h2>Справочная информация</h2>
            <button className="close-button" onClick={onClose}>✕</button>
          </div>

          {/* Вкладки справочной информации */}
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

          {/* Основное содержимое справки */}
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
