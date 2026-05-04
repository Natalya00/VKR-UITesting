import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './UserMenu.css';

const UserMenu: React.FC = () => {
  const { user, logout } = useAuth();
  const [isOpen, setIsOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
    } catch {
    } finally {
      setIsOpen(false);
      window.location.href = '/login';
    }
  };

  if (!user) {
    return null;
  }

  return (
    <div className="user-menu">
      <button
        className="user-menu-trigger"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Меню пользователя"
      >
        <div className="user-avatar">
          {user.email.charAt(0).toUpperCase()}
        </div>
        <span className="user-email">{user.email}</span>
        <span className={`user-menu-arrow ${isOpen ? 'open' : ''}`}>▼</span>
      </button>

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
