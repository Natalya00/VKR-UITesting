const path = require('path');
const tsJestPath = path.resolve(__dirname, 'node_modules/ts-jest');
const jsdomPath = path.resolve(__dirname, 'node_modules/jest-environment-jsdom');

module.exports = {
  testEnvironment: jsdomPath,
  rootDir: path.resolve(__dirname, '../../..'),
  testMatch: [
    '<rootDir>/tests/unit/frontend/**/*.test.{ts,tsx}'
  ],
  setupFilesAfterEnv: [
    path.resolve(__dirname, 'setup.ts')
  ],
  transform: {
    '^.+\\.(ts|tsx)$': [tsJestPath, {
      tsconfig: path.resolve(__dirname, 'tsconfig.json')
    }]
  },
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    '^react$': path.resolve(__dirname, 'node_modules/react'),
    '^react-dom$': path.resolve(__dirname, 'node_modules/react-dom')
  },
  coverageProvider: 'v8',
  collectCoverageFrom: [
    '<rootDir>/client/**/*.{ts,tsx}',
    '!<rootDir>/client/**/*.d.ts'
  ],
  coverageDirectory: path.resolve(__dirname, 'coverage'),
  coverageReporters: ['text', 'html']
};
