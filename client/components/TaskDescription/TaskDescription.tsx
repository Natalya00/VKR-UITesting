import React from 'react';
import './TaskDescription.css';

/**
 * Пропсы компонента TaskDescription
 */
interface TaskDescriptionProps {
  /** Текст описания задания */
  description: string;
}

/**
 * Компонент описания задания
 * Отображает текст задания с заголовком "Задание:"
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент с описанием задания
 */
const TaskDescription: React.FC<TaskDescriptionProps> = ({ description }) => {
  return (
    <div className="task-description">
      <h4>Задание:</h4>
      <p>{description}</p>
    </div>
  );
};

export default TaskDescription;
