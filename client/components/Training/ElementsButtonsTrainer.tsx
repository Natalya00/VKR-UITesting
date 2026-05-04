import React from 'react';
import './Training.css';

export interface TrainerElement {
  tag: 'button' | 'a' | 'div' | 'input';
  id?: string;
  class?: string;
  type?: string;
  href?: string;
  text?: string;
  'data-page'?: string;
  disabled?: boolean;
  hidden?: boolean;
}

export interface ElementsButtonsTrainerConfig {
  targetSelector?: string;
  expectedAction?: string;
  requireCondition?: boolean;
  formTitle?: string;
  elements?: TrainerElement[];
  exerciseId?: string;
}

interface ElementsButtonsTrainerProps {
  config?: ElementsButtonsTrainerConfig;
  exerciseId?: string;
}

const renderElement = (el: TrainerElement, index: number, isTarget: boolean) => {
  const props: Record<string, unknown> = {
    key: index,
  };

  if (el.id) props.id = el.id;
  if (el.class) props.className = el.class;
  if (el.type) (props as Record<string, unknown>).type = el.type;
  if (el.href) (props as Record<string, unknown>).href = el.href;
  if (el.disabled) (props as Record<string, unknown>).disabled = el.disabled;
  if (el.hidden) (props as Record<string, unknown>).hidden = el.hidden;

  if (isTarget) {
    props['data-target'] = 'true';
    props['data-element-index'] = String(index);
  }
  props['data-element-tag'] = el.tag;
  if (el.id) props['data-element-id'] = el.id;
  if (el.class) props['data-element-class'] = el.class;
  if (el['data-page']) props['data-page'] = el['data-page'];

  switch (el.tag) {
    case 'button':
      return (
        <button {...props}>
          {el.text || 'Кнопка'}
        </button>
      );
    case 'a':
      return (
        <a {...props}>
          {el.text || 'Ссылка'}
        </a>
      );
    case 'div':
      return (
        <div {...props}>
          {el.text || 'Элемент'}
        </div>
      );
    case 'input':
      return (
        <input {...props} placeholder={el.text || ''} />
      );
    default:
      return null;
  }
};

const DEFAULT_ELEMENTS: TrainerElement[] = [
  { tag: 'button', class: 'nav-item', 'data-page': 'home', text: 'Главная' },
  { tag: 'a', href: '#about', class: 'nav-link', text: 'О проекте' },
  { tag: 'button', class: 'nav-item', 'data-page': 'contact', text: 'Контакты' },
  { tag: 'button', id: 'submit-btn', type: 'button', class: 'primary-btn', text: 'Отправить' },
  { tag: 'button', id: 'cancel-btn', type: 'button', class: 'secondary-btn', text: 'Отмена' },
  { tag: 'div', id: 'menu-item', class: 'menu-item', text: 'Меню' },
  { tag: 'div', id: 'submenu', class: 'submenu', text: 'Подменю' },
];

const ElementsButtonsTrainer: React.FC<ElementsButtonsTrainerProps> = ({ config, exerciseId }) => {
  const elements = config?.elements || DEFAULT_ELEMENTS;
  const formTitle = config?.formTitle || 'Интерактивный макет страницы';

  const hasNav = elements.some(el => el.class?.includes('nav-'));
  const hasButtons = elements.some(el => el.tag === 'button' && (el.id?.includes('btn') || el.class?.includes('btn') || el.class?.includes('primary') || el.class?.includes('secondary')));
  const hasMenu = elements.some(el => el.id?.includes('menu'));

  return (
    <div
      key={config?.exerciseId || exerciseId}
      className="training-container elements-buttons-layout"
      data-exercise-id={exerciseId}
      data-target-selector={config?.targetSelector}
      data-expected-action={config?.expectedAction}
    >
      <div className="form-wrapper">
        <h3 className="form-title">{formTitle}</h3>

        {hasNav && (
          <nav className="top-nav">
            {elements
              .filter(el => el.class?.includes('nav-'))
              .map((el, idx) => renderElement(el, idx, el.id === config?.targetSelector?.replace('#', '')))}
          </nav>
        )}

        {hasButtons && (
          <div className="buttons-row">
            {elements
              .filter(el => el.tag === 'button' && (el.id?.includes('btn') || el.class?.includes('btn') || el.class?.includes('primary') || el.class?.includes('secondary')))
              .map((el, idx) => renderElement(el, idx, el.id === config?.targetSelector?.replace('#', '')))}
          </div>
        )}

        {hasMenu && (
          <div className="menu-container">
            {elements
              .filter(el => el.id?.includes('menu'))
              .map((el, idx) => renderElement(el, idx, el.id === config?.targetSelector?.replace('#', '')))}
          </div>
        )}

        <div className="additional-elements">
          {elements
            .filter(el => !el.class?.includes('nav-') && !el.id?.includes('menu') && !(el.tag === 'button' && (el.id?.includes('btn') || el.class?.includes('btn') || el.class?.includes('primary') || el.class?.includes('secondary'))))
            .map((el, idx) => renderElement(el, idx, el.id === config?.targetSelector?.replace('#', '')))}
        </div>
      </div>
    </div>
  );
};

export default ElementsButtonsTrainer;
