import React from 'react';
import { Routes, Route, Navigate, useSearchParams } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage/LoginPage';
import RegisterPage from './pages/RegisterPage/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage/ResetPasswordPage';
import XPathSimulatorPage from './pages/XPathSimulatorPage/XPathSimulatorPage';
import ElementSimulatorPage from './pages/ElementSimulatorPage/ElementSimulatorPage';
import POMSimulatorPage from './pages/POMSimulatorPage/POMSimulatorPage';
import ProgressPage from './pages/ProgressPage/ProgressPage';

import {
  HarnessElementsPage,
  HarnessItemsPage,
  HarnessLoginPage,
  HarnessHomePage,
  HarnessProductsPage,
  HarnessProfilePage,
  HarnessComponentsPage,
  HarnessCatCharacterPage,
} from './pages/HarnessPages/HarnessPages';

/**
 * Компонент сообщения о необходимости авторизации
 * Отображается неавторизованным пользователям при попытке доступа к защищенным страницам
 * Предоставляет ссылку для перехода на страницу входа
 * @returns JSX элемент с сообщением и кнопкой входа
 */
const UnauthorizedMessage: React.FC = () => {
  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh',
      textAlign: 'center',
      padding: '40px',
      backgroundColor: '#ffffff',  
      color: '#333333'           
    }}>
      <h2 style={{ color: '#333333', marginBottom: '16px' }}>Требуется авторизация</h2>
      <p style={{ color: '#666666', marginBottom: '24px' }}>
        Пожалуйста, войдите в систему для доступа к упражнениям.
      </p>
      <a 
        href="/login" 
        style={{ 
          backgroundColor: '#3b82f6', 
          color: 'white',    
          textDecoration: 'none',
          padding: '12px 24px',
          border: 'none',
          borderRadius: '8px',
          fontSize: '16px',
          fontWeight: '500',
          cursor: 'pointer',
          transition: 'all 0.3s ease',
          display: 'inline-block'
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.backgroundColor = '#2563eb'; 
          e.currentTarget.style.transform = 'translateY(-2px)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = '#3b82f6';
          e.currentTarget.style.transform = 'translateY(0)';
        }}
      >
        Перейти на страницу входа
      </a>
    </div>
  );
};

/** Пропсы для компонента ProtectedRoute */
interface ProtectedRouteProps {
  /** Дочерние компоненты для рендеринга при успешной авторизации */
  children: React.ReactNode;
}

/**
 * Компонент защищенного маршрута
 * Проверяет авторизацию пользователя и отображает контент только авторизованным пользователям
 * @param props - Пропсы компонента
 * @returns JSX элемент с защищенным контентом или сообщением об авторизации
 */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  const [searchParams] = useSearchParams();
  const hasExerciseParam = searchParams.has('exercise');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <div>Загрузка...</div>
      </div>
    );
  }

  if (hasExerciseParam && !isAuthenticated) {
    return <>{children}</>;
  }

  if (!isAuthenticated) {
    return <UnauthorizedMessage />;
  }

  return <>{children}</>;
};

/**
 * Главный компонент приложения
 * Настраивает маршрутизацию, провайдер авторизации и защищенные маршруты
 * @returns JSX элемент корневого компонента приложения
 */
function App() {
  return (
    <AuthProvider>
      <div className="App">
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/" element={<Navigate to="/login" replace />} />

            <Route
              path="/progress"
              element={
                <ProtectedRoute>
                  <ProgressPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/xpath-simulator"
              element={
                <ProtectedRoute>
                  <XPathSimulatorPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/element-simulator"
              element={
                <ProtectedRoute>
                  <ElementSimulatorPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/pom-simulator"
              element={
                <ProtectedRoute>
                  <POMSimulatorPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/test-harness/module3/block1/elements"
              element={<HarnessElementsPage />}
            />
            <Route
              path="/test-harness/module3/block1/items"
              element={<HarnessItemsPage />}
            />

            <Route
              path="/test-harness/module3/login"
              element={<HarnessLoginPage />}
            />
            <Route
              path="/test-harness/module3/home"
              element={<HarnessHomePage />}
            />
            <Route
              path="/test-harness/module3/products"
              element={<HarnessProductsPage />}
            />

            <Route
              path="/test-harness/module3/components"
              element={<HarnessComponentsPage />}
            />
            <Route
              path="/test-harness/module3/cat-characters"
              element={<HarnessCatCharacterPage />}
            />

            <Route
              path="/test-harness/module3/profile"
              element={<HarnessProfilePage />}
            />
          </Routes>
        </div>
      </AuthProvider>
  );
}

export default App;