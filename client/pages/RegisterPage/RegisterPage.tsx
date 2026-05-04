import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './RegisterPage.css';
import PageHeader from '../../components/PageHeader/PageHeader';

const RegisterPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const validateForm = () => {
    if (!email || !password || !confirmPassword) {
      setError('Пожалуйста, заполните все поля');
      return false;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError('Введите корректный email адрес');
      return false;
    }
    
    if (password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      return false;
    }
    
    if (password !== confirmPassword) {
      setError('Пароли не совпадают');
      return false;
    }
    
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsLoading(true);
    setError('');
    setSuccess('');

    try {
      await register(email, password);
      setSuccess('Регистрация успешна! Теперь вы можете войти в систему.');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err: any) {
      let errorMessage = 'Ошибка регистрации. Попробуйте еще раз.';
      
      if (err.response?.status === 400) {
        if (err.response?.data?.message?.includes('already exists')) {
          errorMessage = 'Пользователь с таким email уже существует';
        } else {
          errorMessage = err.response.data.message || errorMessage;
        }
      } else if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      }
      
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-page">
      <PageHeader
        showLogout={false}
        headerLinks={[
          { label: 'Регистрация', href: '#', active: true },
          { label: 'Прогресс', href: '#' }
        ]}
      />

      <main className="main-content">
        <h1 className="page-title">Регистрация</h1>

        <div className="register-card">
          <form onSubmit={handleSubmit} className="register-form" noValidate>
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
                placeholder="Минимум 6 символов"
                required
                minLength={6}
                aria-label="Пароль"
              />
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword" className="form-label">
                Подтверждение пароля
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="form-input"
                placeholder="Повторите пароль"
                required
                minLength={6}
                aria-label="Подтверждение пароля"
              />
            </div>

            {error && (
              <div className="error-message" role="alert" aria-live="polite">
                {error}
              </div>
            )}

            {success && (
              <div className="success-message" role="status" aria-live="polite">
                {success}
              </div>
            )}

            <button
              type="submit"
              className={`register-button ${isLoading ? 'loading' : ''}`}
              disabled={isLoading}
              aria-label={isLoading ? 'Регистрация...' : 'Зарегистрироваться'}
            >
              {isLoading ? 'Регистрация...' : 'Зарегистрироваться'}
            </button>

            <div className="form-links">
              <Link to="/login" className="form-link">
                Уже есть аккаунт? Войти
              </Link>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default RegisterPage;