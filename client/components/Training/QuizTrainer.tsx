import React, { useState, useEffect } from 'react';
import './QuizTrainer.css';

interface QuizOption {
  label: string;
  text: string;
}

interface QuizTrainerConfig {
  question: string;
  options: QuizOption[];
  correctAnswer: string;
  pageTitle?: string;
  layout?: string;
  disableIDE?: boolean;
}

interface QuizTrainerProps {
  config: QuizTrainerConfig;
  exerciseId?: string;
  onAnswer?: (answer: string) => void;
}

const QuizTrainer: React.FC<QuizTrainerProps> = ({ config, exerciseId, onAnswer }) => {
  const {
    question,
    options,
    correctAnswer,
    pageTitle = 'Тест',
  } = config;

  const [selectedAnswer, setSelectedAnswer] = useState<string>('');
  const [isCorrect, setIsCorrect] = useState<boolean | null>(null);
  const [feedback, setFeedback] = useState('');

  const handleAnswerChange = (answer: string) => {
    setSelectedAnswer(answer);
    const correct = answer === correctAnswer;
    setIsCorrect(correct);

    if (correct) {
      setFeedback('Правильно!');
    } else {
      setFeedback('Попробуйте ещё раз');
    }

    onAnswer?.(answer);
  };

  useEffect(() => {
    setSelectedAnswer('');
    setIsCorrect(null);
    setFeedback('');
  }, [exerciseId]);

  return (
    <div
      key={exerciseId}
      className="training-container quiz-trainer"
      data-exercise-id={exerciseId}
    >
      <div className="quiz-wrapper">
        <h3 className="quiz-title">{pageTitle}</h3>

        <div className="quiz-question">
          <p>{question}</p>
        </div>

        <div className="quiz-options">
          {options.map((option, index) => {
            const answerKey = String.fromCharCode(65 + index);
            const isSelected = selectedAnswer === answerKey;
            const showCorrect = isCorrect === true && answerKey === correctAnswer;
            const showWrong = isCorrect === false && answerKey === selectedAnswer;
            const optionClass = `quiz-option ${isSelected ? 'selected' : ''} ${
              showWrong ? 'wrong' : ''
            } ${showCorrect ? 'correct' : ''}`;

            return (
              <label key={answerKey} className={optionClass}>
                <input
                  type="radio"
                  name="quiz-answer"
                  value={answerKey}
                  checked={selectedAnswer === answerKey}
                  onChange={() => handleAnswerChange(answerKey)}
                  disabled={isCorrect === true}
                />
                <span className="quiz-label">
                  <strong>{answerKey}.</strong> {option.text}
                </span>
              </label>
            );
          })}
        </div>

        {feedback && !isCorrect && (
          <div className={`quiz-feedback ${isCorrect ? 'success' : 'error'}`}>
            {feedback}
          </div>
        )}
      </div>
    </div>
  );
};

export default QuizTrainer;
