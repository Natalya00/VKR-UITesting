import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProgress } from '../../hooks/useProgress';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import PageHeader from '../../components/PageHeader/PageHeader';
import ModuleDetails from '../../components/ModuleDetails/ModuleDetails';
import './ProgressPage.css';

const ProgressPage: React.FC = () => {
  const navigate = useNavigate();
  const { progress, isLoading, error } = useProgress();
  const [selectedModuleId, setSelectedModuleId] = useState<string | null>(null);

  const pieData = progress
    ? [
        { name: 'Выполнено', value: progress.totalCompleted, color: '#4CAF50' },
        { name: 'Осталось', value: progress.totalExercises - progress.totalCompleted, color: '#E0E0E0' },
      ]
    : [];

  if (isLoading) {
    return (
      <div className="progress-page">
        <PageHeader activeNav="none" />
        <main className="progress-main">
          <div className="loading">Загрузка прогресса...</div>
        </main>
      </div>
    );
  }

  if (error || !progress) {
    return (
      <div className="progress-page">
        <PageHeader activeNav="none" />
        <main className="progress-main">
          <div className="error">Ошибка загрузки прогресса</div>
        </main>
      </div>
    );
  }

  return (
    <div className="progress-page">
      <PageHeader activeNav="none" />

      <main className="progress-main">
        <h1 className="page-title">Ваш прогресс</h1>

        <div className="stats-cards">
          <div className="stat-card">
            <div className="stat-value">{progress.totalCompleted}</div>
            <div className="stat-label">Выполнено упражнений</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{progress.totalExercises}</div>
            <div className="stat-label">Всего упражнений</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{progress.totalPercentage.toFixed(1)}%</div>
            <div className="stat-label">Общий прогресс</div>
          </div>
        </div>

        <div className="progress-content">
          <div className="chart-container">
            <h2 className="section-title">Общий прогресс</h2>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  paddingAngle={5}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="modules-container">
            <h2 className="section-title">Прогресс по модулям</h2>
            <div className="modules-hint">
              ℹ️ Нажмите на модуль для просмотра детальной информации
            </div>
            {progress.modules.map((module) => (
              <div 
                key={module.moduleId} 
                className="module-card clickable"
                onClick={() => setSelectedModuleId(module.moduleId)}
                title="Нажмите для просмотра детальной информации"
              >
                <div className="module-header">
                  <h3 className="module-title">{module.moduleTitle}</h3>
                  <span className="module-percentage">{module.percentage.toFixed(1)}%</span>
                </div>
                <div className="progress-bar-container">
                  <div
                    className="progress-bar"
                    style={{ width: `${module.percentage}%` }}
                  />
                </div>
                <div className="module-stats">
                  <span>{module.completedExercises} из {module.totalExercises} упражнений</span>
                  <span className="click-hint">→ Подробнее</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>

      {selectedModuleId && (
        <ModuleDetails 
          moduleId={selectedModuleId} 
          onClose={() => setSelectedModuleId(null)} 
        />
      )}
    </div>
  );
};

export default ProgressPage;
