import React from 'react';
import './PageHeader.css';
import { Link } from 'react-router-dom';
import UserMenu from '../UserMenu/UserMenu';

interface PageHeaderProps {
  userName?: string;
  showProgress?: boolean;
  showLogout?: boolean;
  activeNav?: 'xpath' | 'element' | 'pom' | 'tests' | 'none';
  headerLinks?: Array<{ label: string; href: string; active?: boolean }>;
}

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
        <div className="nav-left"></div>
        
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
        
        <div className="nav-right">
          <UserMenu />
        </div>
      </nav>
    </header>
  );
};

export default PageHeader;