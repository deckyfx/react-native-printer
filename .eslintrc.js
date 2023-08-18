module.exports = {
  root: true,
  extends: '@react-native',
  plugins: ['react-hooks'],
  parser: '@babel/eslint-parser',
  presets: ['@babel/preset-env', '@babel/preset-react'],
  parserOptions: {
    requireConfigFile: false,
  },
  rules: {
    'react-hooks/rules-of-hooks': 'error',
    'react-hooks/exhaustive-deps': 'off',
    'react-native/no-inline-styles': 'off',
    'no-useless-escape': 'off',
    'eqeqeq': 'off',
    'no-undef': 'off',
    'prettier/prettier': [
      'error',
      {
        endOfLine: 'auto',
      },
    ],
  },
};
