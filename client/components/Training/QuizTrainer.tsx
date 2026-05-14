import React, { useState, useEffect } from 'react';
import './QuizTrainer.css';

/** Вариант ответа в викторине */
interface QuizOption {
  /** Метка варианта */
  label: string;
  /** Текст варианта ответа */
  text: string;
}

/** Конфигурация тренажера-викторины */
interface QuizTrainerConfig {
  /** Текст вопроса */
  question: string;
  /** Массив вариантов ответов */
  options: QuizOption[];
  /** Правильный ответ (буква A, B, C, D) */
  correctAnswer: string;
  /** Заголовок страницы */
  pageTitle?: string;
  /** Тип макета */
  layout?: string;
  /** Отключить IDE */
  disableIDE?: boolean;
}

/** Пропсы компонента QuizTrainer */
interface QuizTrainerProps {
  /** Конфигурация викторины */
  config: QuizTrainerConfig;
  /** ID упражнения */
  exerciseId?: string;
  /** Колбэк для обработки ответа */
  onAnswer?: (answer: string) => void;
}

/**
 * Компонент тренажера-викторины
 * Отображает вопрос с множественными вариантами ответов
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент викторины
 */
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

  /**
   * Обрабатывает выбор ответа пользователем
   * Проверяет правильность и обновляет состояние компонента
   * @param answer - Выбранный ответ (буква A, B, C, D)
   */
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
