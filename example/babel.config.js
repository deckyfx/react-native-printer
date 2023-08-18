const path = require('path');
const pak = require('../package.json');

console.log([pak.name + '/*'], path.join(__dirname, '..', 'src', '*'))

module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    [
      'module-resolver',
      {
        extensions: ['.tsx', '.ts', '.js', '.json'],
        alias: {
          [pak.name + '/*']: path.join(__dirname, '..', 'src', '*'),
        },
      },
    ],
  ],
};
