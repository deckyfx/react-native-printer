import * as React from 'react';

import { Text, TouchableHighlight } from 'react-native';

interface Property {
  text: string;
  onClick: () => void;
}

const Button = ({ text, onClick }: Property) => {
  return (
    <TouchableHighlight
      style={{
        alignItems: 'center',
        backgroundColor: '#DDDDDD',
        padding: 10,
        marginRight: 5,
      }}
      onPress={onClick}
    >
      <Text>{text}</Text>
    </TouchableHighlight>
  );
};
export default Button;
