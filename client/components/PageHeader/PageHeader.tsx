import React from 'react';
import './PageHeader.css';
import { Link } from 'react-router-dom';
import UserMenu from '../UserMenu/UserMenu';

/**
 * Пропсы компонента PageHeader
 */
interface PageHeaderProps {
  /** Имя пользователя */
  userName?: string;
  /** Показывать ли прогресс */
  showProgress?: boolean;
  /** Показывать ли кнопку выхода */
  showLogout?: boolean;
  /** Активная вкладка навигации */
  activeNav?: 'xpath' | 'element' | 'pom' | 'tests' | 'none';
  /** Дополнительные ссылки в заголовке */
  headerLinks?: Array<{ label: string; href: string; active?: boolean }>;
}

/**
 * Компонент заголовка страницы с основной навигацией
 * Отображает навигационное меню с ссылками на основные модули и меню пользователя
 * 
 * @param props - Пропсы компонента
 * @returns JSX элемент заголовка страницы
 */
const PageHeader: React.FC<PageHeaderProps> = ({
  userName = '',
  showProgress = true,
  showLogout = true,
  activeNav = 'none',
  headerLinks
}) => {
  return (
    <header className="page-header">
      <nav className="main-navigation">
        {/* Левая часть навигации */}
        <div className="nav-left"></div>
        
        {/* Центральная часть с основными ссылками */}
        <div className="nav-center">
          <Link
            to="/xpath-simulator"
            className={`nav-link ${activeNav === 'xpath' ? 'active' : ''}`}
          >
            Симулятор XPath-запросов
          </Link>
          <Link
            to="/element-simulator"
            className={`nav-link ${activeNav === 'element' ? 'active' : ''}`}
          >
            Симулятор работы с элементами страницы
          </Link>
          <Link to="/pom-simulator" className={`nav-link ${activeNav === 'pom' ? 'active' : ''}`}>
            Работа с Page Object Model
          </Link>
          <Link to="/progress" className="nav-link progress-nav">
            Прогресс
          </Link>
        </div>
        
        {/* Правая часть с меню пользователя */}
        <div className="nav-right">
          <UserMenu />
        </div>
      </nav>
    </header>
  );
};

export default PageHeader;