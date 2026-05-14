import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader/PageHeader';
import { authService } from '../../services/authService';
import './ForgotPasswordPage.css';

/**
 * Компонент страницы восстановления пароля
 * Предоставляет интерфейс для восстановления доступа к аккаунту
 * через проверку email адреса
 * @returns JSX элемент страницы восстановления пароля
 */
const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [token, setToken] = useState<string | null>(null);
  const navigate = useNavigate();

  /**
   * Обрабатывает отправку формы восстановления пароля
   * Проверяет email и отправляет запрос на сервер для получения токена сброса
   * @param e - Событие отправки формы
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !email.includes('@')) {
      setError('Введите корректный email адрес');
      return;
    }

    setIsLoading(true);
    setError('');
    setMessage('');

    try {
      const response = await authService.forgotPassword(email);
      setToken(response.token);
      setMessage('Email подтвержден! Теперь вы можете установить новый пароль.');
    } catch (err: any) {
      if (err.response?.status === 404) {
        setError('Пользователь с таким email не найден. Зарегистрируйтесь.');
      } else {
        setError(err.response?.data?.message || 'Ошибка проверки email');
      }
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Перенаправляет на страницу сброса пароля с полученным токеном
   */
  const handleGoToReset = () => {
    if (token) {
      navigate(`/reset-password?token=${token}`);
    }
  };

  return (
    <div className="forgot-password-page">
      <PageHeader
        showLogout={false}
        headerLinks={[
          { label: 'Восстановление пароля', href: '#', active: true }
        ]}
      />

      <main className="main-content">
        <h1 className="page-title">Восстановление пароля</h1>
        
        <div className="forgot-card">
          {!token ? (
            <form onSubmit={handleSubmit} className="forgot-form">
              <div className="form-group">
                <label htmlFor="email" className="form-label">
                  Электронная почта
                </label>
                <input
                  type="email"
                  id="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="form-input"
                  placeholder="example@mail.com"
                  required
                />
                <p className="form-hint">
                  Введите email, который использовали при регистрации
                </p>
              </div>

              {error && (
                <div className="error-message" role="alert">
                  {error}
                  {error.includes('не найден') && (
                    <button 
                      type="button"
                      onClick={() => navigate('/register')}
                      className="register-link"
                    >
                      Перейти к регистрации
                    </button>
                  )}
                </div>
              )}

              {message && (
                <div className="success-message">{message}</div>
              )}

              <button
                type="submit"
                className={`forgot-button ${isLoading ? 'loading' : ''}`}
                disabled={isLoading}
              >
                {isLoading ? 'Проверка...' : 'Проверить email'}
              </button>

              <div className="form-links">
                <button 
                  type="button"
                  onClick={() => navigate('/login')}
                  className="back-link"
                >
                  Вернуться на страницу входа
                </button>
              </div>
            </form>
          ) : (
            <div className="success-screen">
              <div className="checkmark">✓</div>
              <h2 className="success-title">{message}</h2>
              <p className="success-text">
                Нажмите кнопку ниже, чтобы установить новый пароль.
              </p>
              <button 
                onClick={handleGoToReset}
                className="return-button"
              >
                Установить новый пароль
              </button>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default ForgotPasswordPage;