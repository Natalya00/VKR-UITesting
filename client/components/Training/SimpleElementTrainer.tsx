import React, { useState, useEffect, useMemo } from 'react';
import './Training.css';
import { generateId, generateName, generateClass } from '../../utils/attributeGenerator';

interface SimpleElement {
  tag: 'input' | 'button';
  id?: string;
  name?: string;
  className?: string;
  type?: string;
  placeholder?: string;
  value?: string;
  text?: string;
}

interface SimpleElementTrainerConfig {
  element: SimpleElement;
  targetSelector: string;
  pageStyle?: {
    backgroundColor?: string;
  };
  formTitle?: string;
  additionalFields?: SimpleElement[];
  exerciseId?: string;
}

interface SimpleElementTrainerProps {
  config: SimpleElementTrainerConfig;
}

const SimpleElementTrainer: React.FC<SimpleElementTrainerProps> = ({ config }) => {
  const { element, pageStyle, formTitle = 'Форма', additionalFields = [] } = config;

  const [generatedAttrs, setGeneratedAttrs] = useState<Record<string, any>>({});

  useEffect(() => {
    const attrs: Record<string, any> = {};

    if (config.element.id) attrs.elementId = generateId();
    if (config.element.name) attrs.elementName = generateName();
    if (config.element.className) attrs.elementClass = generateClass();

    (config.additionalFields || []).forEach((field, idx) => {
      if (field.id) attrs[`field${idx}Id`] = generateId();
      if (field.name) attrs[`field${idx}Name`] = generateName();
      if (field.className) attrs[`field${idx}Class`] = generateClass();
    });

    setGeneratedAttrs(attrs);
  }, [config.element.id, config.element.name, config.element.className]);

  const renderElement = (el: SimpleElement, index?: number, isTarget = false) => {
    const { tag, text, id, name, className, ...props } = el;

    const elementProps: Record<string, unknown> = { ...props };

    if (isTarget) {
      if (id) elementProps.id = generatedAttrs.elementId;
      if (name) elementProps.name = generatedAttrs.elementName;
      if (className) (elementProps as Record<string, unknown>).className = generatedAttrs.elementClass;
      (elementProps as Record<string, unknown>)['data-target'] = 'true';
    } else if (index !== undefined) {
      if (id) elementProps.id = generatedAttrs[`field${index}Id`];
      if (name) elementProps.name = generatedAttrs[`field${index}Name`];
      if (className) (elementProps as Record<string, unknown>).className = generatedAttrs[`field${index}Class`];
    }

    if (tag === 'input') {
      return <input key={index} {...elementProps} />;
    }

    if (tag === 'button') {
      return (
        <button key={index} {...elementProps}>
          {text || 'Кнопка'}
        </button>
      );
    }

    return null;
  };

  const allFields = useMemo(() => {
    const fields: Array<{ element: SimpleElement; index?: number; isTarget: boolean }> = [];

    additionalFields.forEach((field, idx) => {
      if (field.tag === 'button') {
        fields.push({ element, index: undefined, isTarget: true });
      }
      fields.push({ element: field, index: idx, isTarget: false });
    });

    if (!additionalFields.some(f => f.tag === 'button')) {
      fields.push({ element, index: undefined, isTarget: true });
    }

    return fields;
  }, [element, additionalFields]);

  return (
    <div
      key={config.exerciseId}
      className="training-container simple-element-trainer"
      style={pageStyle}
    >
      <div className="form-wrapper">
        <h3 className="form-title">{formTitle}</h3>
        <form className="real-form" onSubmit={(e) => e.preventDefault()}>
          {allFields.map((field, idx) => (
            <div key={idx} className="form-group">
              {renderElement(field.element, field.index, field.isTarget)}
            </div>
          ))}
        </form>
      </div>
    </div>
  );
};

export default SimpleElementTrainer;
