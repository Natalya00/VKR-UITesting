import React from 'react';
import './TaskDescription.css';

interface TaskDescriptionProps {
  description: string;
}

const TaskDescription: React.FC<TaskDescriptionProps> = ({ description }) => {
  return (
    <div className="task-description">
      <h4>Задание:</h4>
      <p>{description}</p>
    </div>
  );
};

export default TaskDescription;
