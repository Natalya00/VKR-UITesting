import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader/PageHeader';
import { authService } from '../../services/authService';
import './ResetPasswordPage.css';

/**
 * Компонент страницы сброса пароля
 * Позволяет пользователю установить новый пароль по токену сброса, полученному на этапе восстановления пароля
 * @returns JSX элемент страницы сброса пароля
 */
const ResetPasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [token, setToken] = useState('');

  const tokenParam = searchParams.get('token');

  useEffect(() => {
    if (!tokenParam) {
      setError('Токен сброса пароля не найден');
      return;
    }
    setToken(tokenParam);
  }, [tokenParam]);

  /**
   * Обрабатывает отправку формы сброса пароля
   * Выполняет валидацию паролей, отправляет запрос на сервер
   * и перенаправляет на страницу входа при успехе
   * @param e - Событие отправки формы
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      return;
    }
    
    if (password !== confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }

    setIsLoading(true);
    setError('');
    setMessage('');

    try {
      await authService.resetPassword(token, password);
      setMessage('Пароль успешно изменен! Переход на страницу входа...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      if (err.response?.status === 400) {
        setError(err.response?.data?.message || 'Неверный, просроченный или уже использованный токен');
      } else {
        setError(err.response?.data?.message || 'Ошибка сброса пароля');
      }
    } finally {
      setIsLoading(false);
    }
  };

  if (error && !tokenParam) {
    return (
      <div className="reset-password-page">
        <PageHeader showLogout={false} />
        <main className="main-content">
          <div className="error-card">
            <h1>Ошибка</h1>
            <p>{error}</p>
            <button onClick={() => navigate('/login')}>К входу</button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="reset-password-page">
      <PageHeader
        showLogout={false}
        headerLinks={[
          { label: 'Сброс пароля', href: '#', active: true }
        ]}
      />

      <main className="main-content">
        <h1 className="page-title">Новый пароль</h1>
        
        <div className="reset-card">
          <form onSubmit={handleSubmit} className="reset-form">
            <div className="form-group">
              <label htmlFor="password" className="form-label">
                Новый пароль
              </label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="form-input"
                placeholder="Минимум 6 символов"
                minLength={6}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword" className="form-label">
                Подтверждение пароля
              </label>
              <input
                type="password"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="form-input"
                placeholder="Повторите пароль"
                required
              />
            </div>

            {error && (
              <div className="error-message" role="alert">
                {error}
              </div>
            )}

            {message && (
              <div className="success-message" role="alert">
                {message}
              </div>
            )}

            <button
              type="submit"
              className={`reset-button ${isLoading ? 'loading' : ''}`}
              disabled={isLoading || !token}
            >
              {isLoading ? 'Сброс...' : 'Сбросить пароль'}
            </button>

            <div className="form-links">
              <button 
                type="button"
                onClick={() => navigate('/login')}
                className="back-link"
                disabled={isLoading}
              >
                ← Вернуться на страницу входа
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default ResetPasswordPage;