import React, { useState } from 'react';
import './TableTrainer.css';
import { generateClass } from '../../utils/attributeGenerator';

interface TableCell {
  text: string;
  className?: string;
}

interface TableRow {
  cells: TableCell[];
  checkbox?: boolean;
  className?: string;
}

interface TableTrainerConfig {
  pageTitle: string;
  columns: string[];
  rows: TableRow[];
  targetSelector: string;
  exerciseId?: string;
}

interface TableTrainerProps {
  config: TableTrainerConfig;
}

const TableTrainer: React.FC<TableTrainerProps> = ({ config }) => {
  const { pageTitle, columns, rows } = config;

  const [generatedRowClasses] = useState(() => {
    return config.rows.map(() => generateClass(2));
  });

  return (
    <div
      key={config.exerciseId}
      className="training-container table-trainer"
    >
      <h2 className="page-title">{pageTitle}</h2>
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((col, index) => (
              <th key={index}>{col}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => {
            const generatedClass = row.className ? generatedRowClasses[rowIndex] : undefined;

            const rowContainsTargetText = row.cells.some(cell =>
              config.targetSelector.includes(`text()='${cell.text}'`)
            );

            return (
              <tr key={rowIndex} className={generatedClass}>
                {row.checkbox && (
                  <td>
                    <input
                      type="checkbox"
                      className="table-checkbox"
                      data-target={config.targetSelector.includes('checkbox') && rowContainsTargetText ? 'true' : undefined}
                    />
                  </td>
                )}
                {row.cells.map((cell, cellIndex) => {
                  const generatedCellClass = cell.className ? generateClass(1) : undefined;

                  let isTarget = false;

                  if (rowContainsTargetText && config.targetSelector.includes('//td[')) {
                    const indexMatch = config.targetSelector.match(/\/\/td\[(\d+)\]/);
                    if (indexMatch) {
                      const targetIndex = parseInt(indexMatch[1]) - 1;
                      isTarget = cellIndex === targetIndex;
                    }
                  }
                  else if (config.targetSelector.includes('checkbox') && config.targetSelector.includes(cell.text)) {
                    isTarget = false;
                  }
                  else if (config.targetSelector.includes(`text()='${cell.text}'`)) {
                    isTarget = true;
                  }

                  return (
                    <td
                      key={cellIndex}
                      className={generatedCellClass}
                      data-target={isTarget ? 'true' : undefined}
                    >
                      {cell.text}
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default TableTrainer;
