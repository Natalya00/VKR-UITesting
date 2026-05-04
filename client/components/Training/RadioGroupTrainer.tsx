import React, { useState } from 'react';
import './Training.css';
import { generateId, generateName } from '../../utils/attributeGenerator';

interface RadioOption {
  value: string;
  label: string;
  id?: string;
}

interface RadioGroupTrainerConfig {
  groupName: string;
  options: RadioOption[];
  targetSelector: string;
  pageStyle?: {
    backgroundColor?: string;
  };
  formTitle?: string;
  formDescription?: string;
  submitButton?: boolean;
  exerciseId?: string;
}

interface RadioGroupTrainerProps {
  config: RadioGroupTrainerConfig;
}

const RadioGroupTrainer: React.FC<RadioGroupTrainerProps> = ({ config }) => {
  const {
    groupName,
    options,
    pageStyle,
    formTitle = 'Опрос',
    formDescription = '',
    submitButton = false
  } = config;

  const [generatedAttrs] = useState(() => {
    return {
      groupName: generateName(),
      options: config.options.map((option) => ({
        id: generateId(),
        value: generateName(),
        originalValue: option.value
      }))
    };
  });

  return (
    <div
      key={config.exerciseId}
      className="training-container radio-group-trainer"
      style={pageStyle}
    >
      <div className="form-wrapper survey-form">
        <h3 className="form-title">{formTitle}</h3>
        {formDescription && <p className="form-description">{formDescription}</p>}
        <form className="real-form" onSubmit={(e) => e.preventDefault()}>
          <div className="radio-group">
            {options.map((option, index) => {
              const isTarget = config.targetSelector.includes(`value='${option.value}'`);

              return (
                <div key={index} className="radio-option">
                  <input
                    type="radio"
                    id={generatedAttrs.options[index].id}
                    name={generatedAttrs.groupName}
                    value={generatedAttrs.options[index].value}
                    data-target={isTarget ? 'true' : undefined}
                  />
                  <label htmlFor={generatedAttrs.options[index].id}>
                    {option.label}
                  </label>
                </div>
              );
            })}
          </div>
          {submitButton && (
            <div className="form-actions">
              <button type="submit" className="submit-button">Отправить</button>
            </div>
          )}
        </form>
      </div>
    </div>
  );
};

export default RadioGroupTrainer;
