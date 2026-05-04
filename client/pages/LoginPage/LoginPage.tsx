import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './LoginPage.css';
import PageHeader from '../../components/PageHeader/PageHeader';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/xpath-simulator');
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      setError('Пожалуйста, заполните все поля');
      return;
    }
    
    if (!email.includes('@') || !email.includes('.')) {
      setError('Введите корректный email адрес');
      return;
    }
    
    if (password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      return;
    }
    
    setIsLoading(true);
    setError('');

    try {
      await login(email, password);
      navigate('/xpath-simulator');
    } catch (err: any) {
      let errorMessage = 'Ошибка входа. Попробуйте еще раз.';

      if (err.response?.status === 400) {
        errorMessage = 'Неверный email или пароль';
      } else if (err.response?.status === 401) {
        errorMessage = 'Неверный email или пароль';
      } else if (err.response?.status === 404) {
        errorMessage = 'Пользователь не найден';
      } else if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      }

      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page">
      <PageHeader
        showLogout={false}
        headerLinks={[
          { label: 'Вход', href: '#', active: true },
          { label: 'Прогресс', href: '#' }
        ]}
      />

      <main className="main-content">
        <h1 className="page-title">Вход в систему</h1>

        <div className="login-card">
          <form onSubmit={handleSubmit} className="login-form" noValidate>
            <div className="form-group">
              <label htmlFor="email" className="form-label">
                Электронная почта
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="form-input"
                placeholder="example@mail.com"
                required
                aria-label="Email адрес"
                aria-describedby={error ? "email-error" : undefined}
              />
            </div>

            <div className="form-group">
              <label htmlFor="password" className="form-label">
                Пароль
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="form-input"
                placeholder="Введите пароль"
                required
                minLength={6}
                aria-label="Пароль"
                aria-describedby={error ? "password-error" : undefined}
              />
            </div>

            {error && (
              <div className="error-message" role="alert" aria-live="polite">
                {error}
              </div>
            )}

            <button
              type="submit"
              className={`login-button ${isLoading ? 'loading' : ''}`}
              disabled={isLoading}
              aria-label={isLoading ? 'Вход...' : 'Войти'}
            >
              {isLoading ? 'Вход...' : 'Войти'}
            </button>

            <div className="form-links">
              <button 
                type="button"
                onClick={() => navigate('/forgot-password')}
                className="form-link forgot-link"
                aria-label="Восстановление пароля"
              >
                Забыли пароль?
              </button>
              <Link to="/register" className="form-link" aria-label="Перейти к регистрации">
                Регистрация
              </Link>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default LoginPage;