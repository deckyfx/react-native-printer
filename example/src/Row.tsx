import React from 'react';

import { View } from 'react-native';

interface Property {
  children: React.ReactNode | undefined;
}

const Row = ({ children }: Property) => {
  return (
    <View
      style={{
        flexDirection: 'row',
        marginBottom: 5,
      }}
    >
      {children}
    </View>
  );
};

export default Row;
