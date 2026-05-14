import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './UserMenu.css';

/**
 * Компонент меню пользователя
 * Отображает аватар, email пользователя и выпадающее меню с опциями
 * При отсутствии авторизации не отображается
 * 
 * @returns JSX элемент меню пользователя или null
 */
const UserMenu: React.FC = () => {
  const { user, logout } = useAuth();
  const [isOpen, setIsOpen] = useState(false);

  /**
   * Обработчик выхода из системы
   * Выполняет logout, закрывает меню и перенаправляет на страницу входа
   */
  const handleLogout = async () => {
    try {
      await logout();
    } catch {
    } finally {
      setIsOpen(false);
      window.location.href = '/login';
    }
  };

  // Если пользователь не авторизован, не отображаем меню
  if (!user) {
    return null;
  }

  return (
    <div className="user-menu">
      {/* Кнопка открытия меню с аватаром и email */}
      <button
        className="user-menu-trigger"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Меню пользователя"
      >
        {/* Аватар с первой буквой email */}
        <div className="user-avatar">
          {user.email.charAt(0).toUpperCase()}
        </div>
        <span className="user-email">{user.email}</span>
        <span className={`user-menu-arrow ${isOpen ? 'open' : ''}`}>▼</span>
      </button>

      {/* Выпадающее меню */}
      {isOpen && (
        <div className="user-menu-dropdown">
          <div className="user-menu-item">
            <span className="user-display-name">
              {user.email.split('@')[0]}
            </span>
          </div>
          <div className="user-menu-divider" />
          <button className="user-menu-item logout" onClick={handleLogout}>
            Выйти
          </button>
        </div>
      )}
    </div>
  );
};

export default UserMenu;
