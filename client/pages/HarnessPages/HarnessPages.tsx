import React, { useState } from 'react';
import './HarnessPages.css';

/**
 * Страница с элементами форм
 * Содержит форму регистрации с полями ввода, выпадающими списками и кнопками
 * @returns JSX элемент страницы элементов форм
 */
export const HarnessElementsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(1);

  return (
    <div className="harness-wrapper elements-page">
      <div className="harness-container">
        <section className="harness-section registration-form">
          <h3><span className="section-icon">📝</span>Регистрация на платформе</h3>
          <p style={{ fontSize: '14px', color: '#888', marginTop: '-10px', marginBottom: '20px' }}>
            Заполните форму для создания аккаунта
          </p>

          <div className="elements-forms-grid">
            <div>
              <div className="form-group">
                <label>Имя пользователя:</label>
                <input type="text" id="username" name="username"
                       placeholder="Введите имя" />
              </div>
              <div className="form-group">
                <label>Текст в профиле:</label>
                <input type="text" id="prefilled" name="prefilled"
                       defaultValue="OldValue" />
              </div>
            </div>

            <div>
              <div className="form-group">
                <label>Электронная почта:</label>
                <input type="email" id="email" name="userEmail"
                       placeholder="email@example.com" />
              </div>
              <div className="form-group">
                <label>Логин для входа:</label>
                <input type="text" id="login-field"
                       placeholder="Введите логин" />
              </div>
            </div>
          </div>

          <div className="location-section" style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #f0f0f0' }}>
            <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#555', marginBottom: '12px' }}>
              📍 Регион проживания
            </h4>
            <div className="elements-forms-grid">
              <div className="form-group">
                <label>Страна:</label>
                <select id="country">
                  <option value="">Выберите страну</option>
                  <option value="ru">Россия</option>
                  <option value="us">США</option>
                  <option value="de">Германия</option>
                  <option value="fr">Франция</option>
                </select>
              </div>
              <div className="form-group">
                <label>Город:</label>
                <select id="city" name="userCity">
                  <option value="">Выберите город</option>
                  <option value="msk">Москва</option>
                  <option value="spb">Санкт-Петербург</option>
                  <option value="ekb">Екатеринбург</option>
                </select>
              </div>
            </div>
          </div>

          <div className="form-actions" style={{ marginTop: '24px', paddingTop: '20px', borderTop: '1px solid #f0f0f0' }}>
            <div className="elements-buttons-row" style={{ marginBottom: '12px' }}>
              <button
                id="visible-btn"
                className="btn btn-primary"
                onClick={(e) => e.currentTarget.setAttribute('data-clicked', 'true')}
              >
                Зарегистрироваться
              </button>
              <button
                id="hidden-btn"
                style={{ display: 'none' }}
                onClick={(e) => e.currentTarget.setAttribute('data-clicked', 'true')}
              >
                Скрытая кнопка
              </button>
            </div>

            <div style={{ display: 'flex', gap: '4px', marginTop: '12px', borderBottom: '2px solid #eee' }}>
              <button id="tab-1"
                      className={`btn ${activeTab === 1 ? 'btn-primary' : 'btn-secondary'}`}
                      onClick={() => setActiveTab(1)}
                      style={{ borderBottom: activeTab === 1 ? '2px solid #4a6cf7' : '2px solid transparent', borderRadius: '4px 4px 0 0' }}>
                Личные данные
              </button>
              <button id="tab-2"
                      className={`btn ${activeTab === 2 ? 'btn-primary' : 'btn-secondary'}`}
                      onClick={() => setActiveTab(2)}
                      style={{ borderBottom: activeTab === 2 ? '2px solid #4a6cf7' : '2px solid transparent', borderRadius: '4px 4px 0 0' }}>
                Настройки
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

/**
 * Страница с товарами
 * Отображает карточки товаров с возможностью добавления в корзину
 * @returns JSX элемент страницы товаров
 */
export const HarnessItemsPage: React.FC = () => {
  const [cartCount, setCartCount] = useState(0);

  return (
    <div className="harness-wrapper items-page">
      <div className="harness-container">
        <div className="cart-bar">
          <span>Корзина</span>
          <div className="cart-counter">
            Товаров: <span className="count-value" id="cart-count">{cartCount}</span>
          </div>
        </div>

        <div className="items-grid" id="items-container" data-testid="items-container">
          {[
            { title: 'Ноутбук Pro 15', desc: 'Мощный ноутбук для работы и игр', price: '89 990 ₽' },
            { title: 'Смартфон X200', desc: 'Флагманский смартфон с отличной камерой', price: '59 990 ₽' },
            { title: 'Планшет Air', desc: 'Лёгкий планшет для чтения и видео', price: '34 990 ₽' },
          ].map((item, i) => (
            <div key={i} className="item-card" data-testid={`item-card-${i + 1}`}>
              <h3 className="item-title">{item.title}</h3>
              <p className="item-desc">{item.desc}</p>
              <span className="item-price">{item.price}</span>
              <div className="btn-group">
                <button className="btn btn-primary btn-block btn-action"
                        onClick={() => setCartCount(c => c + 1)}>
                  Купить
                </button>
                <button className="btn btn-secondary btn-block btn-action">
                  Подробнее
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

/**
 * Страница входа
 * Содержит форму авторизации с валидацией пароля
 * @returns JSX элемент страницы входа
 */
export const HarnessLoginPage: React.FC = () => {
  const [errorMsg, setErrorMsg] = useState('');

  return (
    <div className="harness-wrapper harness-login-page">
      <header style={{ textAlign: 'center', padding: '20px 0', opacity: 0.5 }}>
        <img alt="Логотип" id="logo"
             style={{ height: '40px', filter: 'grayscale(1)' }} />
      </header>
      <form id="login-form" data-testid="login-form"
            onSubmit={e => e.preventDefault()}>
        <div id="login-page" data-testid="login-page">
          <h2 className="login-form-title">Вход в систему</h2>
          <p className="login-form-subtitle">Введите свои учётные данные для входа</p>

          <div className="form-group">
            <label>Логин:</label>
            <input type="text" id="login-input" data-testid="login-input"
                   defaultValue="testuser" />
          </div>
          <div className="form-group">
            <label>Пароль:</label>
            <input type="password" id="password-input" data-testid="password-input" />
          </div>
          <button
            id="submit-btn"
            data-testid="submit-btn"
            className="btn btn-primary btn-block"
            onClick={() => {
              const pw = (document.getElementById('password-input') as HTMLInputElement)?.value;
              if (pw === 'testpass') {
                window.location.href = '/test-harness/module3/home';
              } else {
                setErrorMsg('Неверный логин или пароль');
              }
            }}
          >
            Войти
          </button>

          {errorMsg && (
            <div id="error-message" data-testid="error-message">
              ⚠️ {errorMsg}
            </div>
          )}

          <div id="modal-overlay" data-testid="modal-overlay"
               style={{ display: 'none', position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                        background: 'rgba(0,0,0,0.5)', zIndex: 1000 }}>
            <div style={{ background: 'white', padding: '24px', margin: '100px auto',
                          maxWidth: '320px', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
              <p style={{ fontSize: '16px', fontWeight: 600, marginBottom: '16px' }}>Модальное окно</p>
              <button className="btn btn-secondary" onClick={() => {
                document.getElementById('modal-overlay')!.style.display = 'none';
              }}>
                Закрыть
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

/**
 * Главная страница после авторизации
 * Отображает панель администратора и навигационные элементы
 * @returns JSX элемент главной страницы
 */
export const HarnessHomePage: React.FC = () => {
  return (
    <div id="home-page" data-testid="home-page" className="harness-wrapper home-page">
      <header id="home-header" data-testid="home-header">
        <img src="/logo.png" alt="Логотип" id="logo" data-testid="logo" />
        <nav>
          <a href="/test-harness/module3/home" data-testid="nav-home">Главная</a>
          <a href="/test-harness/module3/profile" id="nav-profile" data-testid="nav-profile">Профиль</a>
        </nav>
        <span id="user-name" data-testid="user-name">testuser</span>
        <button id="logout-btn" data-testid="logout-btn"
                onClick={() => window.location.href = '/test-harness/module3/login'}>
          Выйти
        </button>
      </header>

      <main className="home-content">
        <section className="harness-section admin-panel-card">
          <h3><span className="section-icon">⚙️</span>Панель администратора</h3>
          <div className="btn-group">
            <button id="sidebar-menu" data-testid="sidebar-menu"
                    className="btn btn-secondary">
              ☰ Меню
            </button>
            <button id="user-management" data-testid="user-management"
                    className="btn btn-primary">
              Управление пользователями
            </button>
          </div>
        </section>

        <div className="welcome-card">
          <h2>Добро пожаловать!</h2>
          <p>Выберите раздел в меню для продолжения.</p>
          <div id="page-loaded" data-testid="page-loaded">
            ✅ Главная страница загружена
          </div>
          <div className="home-nav-links">
            <a href="/test-harness/module3/products" id="go-products"
               data-testid="go-products">
              Перейти к товарам →
            </a>
            <button id="go-profile-btn" data-testid="go-profile-btn"
                    onClick={() => window.location.href = '/test-harness/module3/profile'}>
              Перейти в профиль
            </button>
          </div>
        </div>
      </main>

      <footer id="footer" data-testid="footer">
        <div className="flex-center" style={{ justifyContent: 'center' }}>
          <span>© 2026 Компания</span>
          <a href="https://vk.com/company" id="footer-vk" data-testid="footer-vk"
             onClick={(e) => { e.preventDefault(); }}>ВКонтакте</a>
          <a href="https://t.me/company" id="footer-tg" data-testid="footer-tg"
             onClick={(e) => { e.preventDefault(); }}>Telegram</a>
        </div>
      </footer>
    </div>
  );
};

/**
 * Страница товаров с фильтрацией и управлением
 * Позволяет добавлять, удалять товары и фильтровать по категориям
 * @returns JSX элемент страницы управления товарами
 */
export const HarnessProductsPage: React.FC = () => {
  const [products, setProducts] = useState([
    { id: 1, name: 'Ноутбук Pro 15',  category: 'electronics', available: true },
    { id: 2, name: 'Смартфон X200',   category: 'electronics', available: true },
    { id: 3, name: 'Планшет Air',     category: 'electronics', available: false },
    { id: 4, name: 'Джинсы Classic',  category: 'clothing',    available: true },
  ]);
  const [filter, setFilter] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newName, setNewName] = useState('');
  const [newCategory, setNewCategory] = useState('electronics');

  const filtered = filter ? products.filter(p => p.category === filter) : products;

  return (
    <div id="products-page" data-testid="products-page" className="harness-wrapper products-page">
      <div className="harness-container">
        <div className="harness-section">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <input id="search-box" data-testid="search-box" type="text" placeholder="Поиск товаров..." />
          </div>
        </div>

        <div className="mb-sm">
          <button id="add-product-btn" data-testid="add-product-btn"
                  className="btn btn-success"
                  onClick={() => setShowAddForm(!showAddForm)}>
            {showAddForm ? '— Скрыть форму' : '+ Добавить товар'}
          </button>
        </div>

        {showAddForm && (
          <div id="add-product-form" data-testid="add-product-form" className="harness-section">
            <h3 style={{ marginTop: 0 }}>Добавление товара</h3>
            <div className="form-group">
              <input id="add-product-name" data-testid="add-product-name"
                     value={newName} onChange={e => setNewName(e.target.value)}
                     placeholder="Название товара" />
            </div>
            <div className="form-group">
              <select id="add-product-category" data-testid="add-product-category"
                      value={newCategory} onChange={e => setNewCategory(e.target.value)}>
                <option value="electronics">electronics</option>
                <option value="clothing">clothing</option>
              </select>
            </div>
            <button id="add-product-submit" data-testid="add-product-submit"
                    className="btn btn-success"
                    onClick={() => {
                      if (newName.trim()) {
                        setProducts(prev => [...prev, {
                          id: prev.length + 1,
                          name: newName.trim(),
                          category: newCategory,
                          available: true
                        }]);
                        setNewName('');
                        setShowAddForm(false);
                      }
                    }}>
              Добавить
            </button>
          </div>
        )}

        <div className="harness-section">
          <div className="products-filter-bar">
            <label style={{ fontSize: '14px', color: '#6b7280', whiteSpace: 'nowrap' }}>Категория:</label>
            <select id="category-filter" data-testid="category-filter"
                    value={filter} onChange={e => setFilter(e.target.value)}>
              <option value="">Все</option>
              <option value="electronics">Электроника</option>
              <option value="clothing">Одежда</option>
            </select>
          </div>
        </div>

        <div className="mb-sm">
          <button id="open-modal-btn" data-testid="open-modal-btn"
                  className="btn btn-secondary"
                  onClick={() => setIsModalOpen(true)}>
            Открыть модальное окно
          </button>
        </div>

        <div id="products-grid" data-testid="products-grid" className="products-grid">
          {filtered.map(p => (
            <div key={p.id}
                 className="product-card"
                 data-testid={`product-card-${p.id}`}
                 data-product-name={p.name}
                 data-product-category={p.category}
                 data-available={p.available ? 'true' : 'false'}>
              <h3 className="product-title">{p.name}</h3>
              <span className="product-category">{p.category}</span>
              <div className="btn-group">
                {p.available
                  ? <button className="btn btn-primary btn-sm btn-select" data-testid={`select-${p.id}`}>Выбрать</button>
                  : <span className="badge badge-gray">Недоступно</span>
                }
                <button className="btn-danger btn-delete" data-testid={`delete-${p.id}`}
                        onClick={() => setProducts(prev => prev.filter(item => item.id !== p.id))}>
                  Удалить
                </button>
              </div>
            </div>
          ))}
          {filtered.length === 0 && (
            <p className="text-muted" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px' }}>Товары не найдены</p>
          )}
        </div>

        <div id="modal-overlay" data-testid="modal-overlay"
             style={{ display: isModalOpen ? 'flex' : 'none' }}>
          <div id="modal-window" data-testid="modal-window">
            <h3>Модальное окно</h3>
            <div className="form-group">
              <input type="text" id="modal-input" data-testid="modal-input"
                     placeholder="Введите значение" />
            </div>
            <div className="btn-group">
              <button id="modal-submit" data-testid="modal-submit"
                      className="btn btn-primary"
                      onClick={() => setIsModalOpen(false)}>OK</button>
              <button id="modal-close" data-testid="modal-close"
                      className="btn btn-secondary"
                      onClick={() => setIsModalOpen(false)}>Закрыть</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

/**
 * Страница профиля пользователя для тестирования
 * Позволяет редактировать имя и загружать фото профиля
 * @returns JSX элемент страницы профиля
 */
export const HarnessProfilePage: React.FC = () => {
  const [draftName, setDraftName] = useState('');
  const [lastSaved, setLastSaved] = useState('Иван Петров');
  const [photoUploaded, setPhotoUploaded] = useState(false);

  return (
    <div id="profile-page" data-testid="profile-page" className="harness-wrapper profile-page">
      <div className="profile-content">
        <div className="profile-card">
          <h2 className="profile-name">
            <span id="profile-name-display" data-testid="profile-name-display">{lastSaved}</span>
          </h2>

          <div className="form-group">
            <label>Новое имя:</label>
            <input id="name" data-testid="name" value={draftName}
                   placeholder="Введите новое имя"
                   onChange={e => setDraftName(e.target.value)} />
          </div>

          <div className="btn-group">
            <button id="save-btn" data-testid="save-btn"
                    className="btn btn-primary"
                    onClick={() => { if (draftName.trim()) { setLastSaved(draftName.trim()); setDraftName(''); } }}>
              Сохранить
            </button>
            <button id="edit-name-btn" data-testid="edit-name-btn"
                    className="btn btn-secondary"
                    onClick={() => setDraftName(lastSaved)}>
              Редактировать
            </button>
          </div>

          <div className="profile-email">
            <label style={{ fontSize: '14px', color: '#6b7280', marginRight: '8px' }}>Email:</label>
            <span id="user-email" data-testid="user-email">testuser@example.com</span>
          </div>

          <div className="profile-photo-section">
            <label style={{ fontSize: '14px', color: '#6b7280', display: 'block', marginBottom: '8px' }}>Фото профиля:</label>
            <input type="file" id="photo-upload" data-testid="photo-upload"
                   accept="image/*"
                   onChange={(e) => { if (e.target.files && e.target.files.length > 0) setPhotoUploaded(true); }} />
            <button id="upload-photo-btn" data-testid="upload-photo"
                    className="btn btn-secondary mt-sm"
                    onClick={() => setPhotoUploaded(true)}>
              Загрузить фото
            </button>
            {photoUploaded && (
              <div id="photo-upload-success" data-testid="photo-upload-success">
                ✅ Фото успешно загружено
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

/**
 * Страница с компонентами интерфейса
 * Содержит различные UI компоненты: таблицы, пагинацию, аккордеоны, тултипы, формы
 * @returns JSX элемент страницы компонентов
 */
export const HarnessComponentsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [searchValue, setSearchValue] = useState('');
  const [alertVisible, setAlertVisible] = useState(true);
  const [alertMessage, setAlertMessage] = useState('Операция выполнена успешно!');
  const [currentPage, setCurrentPage] = useState(2);
  const [expandedSections, setExpandedSections] = useState<Record<number, boolean>>({ 0: true, 1: false, 2: false });
  const [tooltipVisible, setTooltipVisible] = useState(false);
  const [activeFilter, setActiveFilter] = useState('Все');
  const [activeSort, setActiveSort] = useState('По умолчанию');
  const [toolbarRefreshed, setToolbarRefreshed] = useState(false);
  const [formSubmitted, setFormSubmitted] = useState(false);
  const [formData, setFormData] = useState({ name: '', category: '' });

  const suggestions = searchValue.length > 0
    ? [`Результат для "${searchValue}"`, `Подсказка: ${searchValue}`, `История: ${searchValue}`]
    : [];

  const breadcrumbItems = [
    { label: 'Главная', href: '#' },
    { label: 'Каталог', href: '#' },
    { label: 'Товары', href: '#' },
    { label: 'Электроника', href: '#', active: true },
  ];

  const totalPages = 5;
  const paginationItems = Array.from({ length: totalPages }, (_, i) => i + 1);

  const accordionSections = [
    { title: 'Общая информация', content: 'Заказ №1042 от 15 марта 2025. Статус: в обработке. Ожидаемая дата доставки: 20 марта.' },
    { title: 'Состав заказа', content: 'Ноутбук Pro 15 × 1, Смартфон X200 × 2. Итого позиций: 3. Вес: 2.4 кг.' },
    { title: 'Отзывы покупателя', content: 'Средняя оценка: 4.8 из 5 на основе 124 отзывов. Рейтинг продавца: 4.9.' },
  ];

  return (
    <div className="harness-wrapper components-page">
      <div className="admin-dashboard">

        <section className="harness-section dashboard-top-bar">
          <div id="dashboard-widget" data-testid="dashboard-widget">
            <div id="toolbar" data-testid="toolbar">
              <button id="toolbar-refresh" data-testid="toolbar-refresh"
                      className="toolbar-refresh-btn btn btn-secondary btn-sm"
                      onClick={() => setToolbarRefreshed(true)}>🔄 Обновить</button>
              <button id="toolbar-settings" data-testid="toolbar-settings"
                      className="toolbar-settings-btn btn btn-secondary btn-sm">⚙ Настройки</button>
            </div>
            {toolbarRefreshed && (
              <div id="refresh-status" data-testid="refresh-status">✅ Данные обновлены</div>
            )}
          </div>

          <nav id="breadcrumb" data-testid="breadcrumb" aria-label="Breadcrumb">
            {breadcrumbItems.map((item, i) => (
              <span key={i} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                {i > 0 && <span style={{ color: '#9ca3af' }}>/</span>}
                {item.active ? (
                  <span className="breadcrumb-item" data-testid={`breadcrumb-${i}`}>{item.label}</span>
                ) : (
                  <a className="breadcrumb-item" data-testid={`breadcrumb-${i}`} href={item.href}>{item.label}</a>
                )}
              </span>
            ))}
          </nav>

          <div id="tab-panel" data-testid="tab-panel">
            {['overview', 'details', 'settings'].map(tab => (
              <button key={tab} id={`tab-${tab}`} data-testid={`tab-${tab}`}
                      className={activeTab === tab ? 'active' : ''}
                      onClick={() => setActiveTab(tab)}>
                {tab === 'overview' ? 'Обзор' : tab === 'details' ? 'Детали' : 'Настройки'}
              </button>
            ))}
          </div>
        </section>

        <section className="harness-section dashboard-main">
          {alertVisible ? (
            <div id="alert-banner" data-testid="alert-banner">
              <span id="alert-message" data-testid="alert-message">{alertMessage}</span>
              <button id="alert-dismiss" data-testid="alert-dismiss"
                      onClick={() => setAlertVisible(false)}>Закрыть</button>
            </div>
          ) : (
            <div>
              <p className="text-muted mb-sm">Уведомление закрыто</p>
              <button id="alert-show" data-testid="alert-show"
                      className="btn btn-primary"
                      onClick={() => { setAlertVisible(true); setAlertMessage('Новое уведомление!'); }}>
                Показать уведомление
              </button>
            </div>
          )}

          <div className="search-row" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <div id="search-bar" data-testid="search-bar" style={{ position: 'relative', flex: 1, maxWidth: '400px' }}>
              <input type="text" id="search-input" data-testid="search-input"
                     placeholder="Поиск по заказам..." value={searchValue}
                     onChange={(e) => setSearchValue(e.target.value)}
                     style={{ width: '100%', paddingRight: '32px' }} />
              <button className="clear-btn" data-testid="clear-btn"
                      onClick={() => setSearchValue('')}
                      style={{ position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)' }}>✕</button>
              {suggestions.length > 0 && (
                <div data-testid="suggestions-list"
                     style={{ position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 10,
                              background: '#fff', border: '1px solid #d1d5db', borderTop: 'none',
                              borderRadius: '0 0 8px 8px', maxHeight: '200px', overflowY: 'auto' }}>
                  {suggestions.map((s, i) => (
                    <div key={i} className="suggestion-item" data-testid={`suggestion-${i}`}>{s}</div>
                  ))}
                </div>
              )}
            </div>
            <button className="btn btn-secondary" data-testid="other-clear-btn">Сброс</button>
          </div>

          <div className="table-container">
            <table id="data-table" data-testid="data-table" className="harness-table">
              <thead><tr><th>Клиент</th><th>Статус</th></tr></thead>
              <tbody>
                <tr><td>Иванов</td><td><span className="badge badge-info">В обработке</span></td></tr>
                <tr><td>Петров</td><td><span className="badge badge-gray">Доставлен</span></td></tr>
              </tbody>
            </table>
            <div className="empty-table-section">
              <p style={{ fontSize: '13px', color: '#888', marginBottom: '8px' }}>Отменённые заказы</p>
              <table id="empty-table" data-testid="empty-table" className="harness-table">
                <tbody><tr><td colSpan={2} style={{ textAlign: 'center', color: '#aaa', padding: '20px' }}>Нет данных</td></tr></tbody>
              </table>
            </div>
          </div>

          <div id="pagination" data-testid="pagination">
            <button id="pagination-prev" data-testid="pagination-prev"
                    className="btn btn-secondary btn-sm"
                    disabled={currentPage === 1}
                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}>←</button>
            {paginationItems.map(p => (
              <button key={p} id={`page-${p}`} data-testid={`page-${p}`}
                      className={`btn btn-sm pagination-btn ${currentPage === p ? 'active' : ''}`}
                      onClick={() => setCurrentPage(p)}>{p}</button>
            ))}
            <button id="pagination-next" data-testid="pagination-next"
                    className="btn btn-secondary btn-sm"
                    disabled={currentPage === totalPages}
                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}>→</button>
            <span id="pagination-info" data-testid="pagination-info">Страница {currentPage} из {totalPages}</span>
          </div>
        </section>

        <aside className="harness-section dashboard-sidebar">
          <h3 style={{ marginTop: 0, fontSize: '14px', textTransform: 'uppercase', letterSpacing: '0.5px', color: '#888' }}>Статус заказов</h3>
          <div id="sidebar-widget" data-testid="sidebar-widget">
            <div id="filter-panel" data-testid="filter-panel">
              {['Все', 'Активные', 'Завершённые'].map(f => (
                <button key={f} id={`filter-${f}`} data-testid={`filter-${f}`}
                        className="filter-btn">{f}</button>
              ))}
            </div>
            <div id="sort-panel" data-testid="sort-panel" style={{ marginTop: '16px' }}>
              {['По умолчанию', 'По дате', 'По имени'].map(s => (
                <button key={s} id={`sort-${s}`} data-testid={`sort-${s}`}
                        className="sort-btn">{s}</button>
              ))}
            </div>
          </div>

          <div style={{ marginTop: '24px', paddingTop: '16px', borderTop: '1px solid #f0f0f0' }}>
            <h4 style={{ fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.5px', color: '#888', marginBottom: '10px' }}>Дата заказа</h4>
            <div id="date-picker" data-testid="date-picker" style={{ display: 'flex', gap: '6px' }}>
              <select className="datepicker-day" data-testid="datepicker-day">
                <option value="">День</option>
                {Array.from({ length: 31 }, (_, i) => <option key={i + 1} value={i + 1}>{i + 1}</option>)}
              </select>
              <select className="datepicker-month" data-testid="datepicker-month">
                <option value="">Месяц</option>
                {['Январь','Февраль','Март','Апрель','Май','Июнь','Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь']
                  .map((m, i) => <option key={i} value={m}>{m}</option>)}
              </select>
              <select className="datepicker-year" data-testid="datepicker-year">
                <option value="">Год</option>
                {Array.from({ length: 10 }, (_, i) => 2020 + i).map(y => <option key={y} value={y}>{y}</option>)}
              </select>
            </div>
          </div>

          <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid #f0f0f0' }}>
            <h4 style={{ fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.5px', color: '#888', marginBottom: '10px' }}>Подсказка</h4>
            <div id="tooltip-container" data-testid="tooltip-container" style={{ position: 'relative', display: 'inline-block' }}>
              <button id="tooltip-trigger" data-testid="tooltip-trigger"
                      className="btn btn-primary btn-sm"
                      onMouseEnter={() => setTooltipVisible(true)}
                      onMouseLeave={() => setTooltipVisible(false)}>ℹ️ Информация</button>
              {tooltipVisible && (
                <div id="tooltip-content" data-testid="tooltip-content" className="tooltip-content"
                     style={{ position: 'absolute', bottom: 'calc(100% + 8px)', left: '50%',
                              transform: 'translateX(-50%)', background: '#1f2937', color: '#fff',
                              padding: '8px 14px', borderRadius: '8px', fontSize: '13px',
                              whiteSpace: 'nowrap', zIndex: 10, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
                  Используйте фильтры для поиска
                  <div style={{ position: 'absolute', top: '100%', left: '50%', transform: 'translateX(-50%)',
                    width: 0, height: 0, borderLeft: '6px solid transparent',
                    borderRight: '6px solid transparent', borderTop: '6px solid #1f2937' }} />
                </div>
              )}
            </div>
          </div>
        </aside>

        <section className="harness-section dashboard-details">
          <div>
            <h3 style={{ marginTop: 0 }}>Детали заказа</h3>
            <div id="accordion" data-testid="accordion">
              {accordionSections.map((section, i) => (
                <div key={i}>
                  <button id={`accordion-toggle-${i}`} data-testid={`accordion-toggle-${i}`}
                          className={`accordion-toggle ${expandedSections[i] ? 'expanded' : ''}`}
                          onClick={() => setExpandedSections(prev => ({ ...prev, [i]: !prev[i] }))}>
                    <span>{section.title}</span>
                    <span>{expandedSections[i] ? '▲' : '▼'}</span>
                  </button>
                  {expandedSections[i] && (
                    <div id={`accordion-content-${i}`} data-testid={`accordion-content-${i}`}
                         className="accordion-content">{section.content}</div>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div>
            <h3>Оформить заказ</h3>
            <div id="form-widget" data-testid="form-widget">
              <div className="form-group">
                <label>Имя получателя:</label>
                <input id="form-name-input" data-testid="form-name-input" type="text"
                       value={formData.name}
                       onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                       placeholder="Введите имя" />
              </div>
              <div className="form-group">
                <label>Категория товара:</label>
                <select id="form-category-select" data-testid="form-category-select"
                        value={formData.category}
                        onChange={(e) => setFormData(prev => ({ ...prev, category: e.target.value }))}>
                  <option value="">Выберите категорию</option>
                  <option value="Электроника">Электроника</option>
                  <option value="Одежда">Одежда</option>
                  <option value="Книги">Книги</option>
                </select>
              </div>
              <button id="form-submit-btn" data-testid="form-submit-btn"
                      className="btn btn-primary btn-block form-submit-btn"
                      onClick={() => { if (formData.name && formData.category) setFormSubmitted(true); }}>
                Оформить заказ
              </button>
              {formSubmitted && (
                <div id="form-success" data-testid="form-success" className="form-success">
                  ✅ Заказ оформлен: {formData.name} ({formData.category})
                </div>
              )}
            </div>
          </div>
        </section>

      </div>
    </div>
  );
};

export const HarnessCatCharacterPage: React.FC = () => {
  return (
    <div className="harness-wrapper cat-character-page">
      <div id="cat-character-page" data-testid="cat-character-page">
        <div className="cat-search-bar">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>Поиск:</label>
            <input type="text" id="search-input" data-testid="search-input"
                   placeholder="Введите запрос..." />
          </div>
        </div>

        <div className="cat-table-card">
          <table id="data-table" data-testid="data-table" className="harness-table">
            <thead>
              <tr>
                <th>Имя</th>
                <th>Окрас</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Барсик</td>
                <td><span className="badge badge-info">Рыжий</span></td>
              </tr>
              <tr>
                <td>Мурка</td>
                <td><span className="badge badge-gray">Чёрная</span></td>
              </tr>
              <tr>
                <td>Пушок</td>
                <td><span className="badge badge-success">Белый</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
