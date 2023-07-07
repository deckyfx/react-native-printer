import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';

import { multiply, scanNetworkDevices } from '@decky.fx/react-native-printer';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button
        onPress={async () => {
          const result = await scanNetworkDevices();
          console.log(result);
        }}
        title="Scan Network"
        color="#841584"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
