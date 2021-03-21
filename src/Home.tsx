import React, { ReactElement, useEffect } from 'react';
import NfcReaderModule from './NfcReaderModule';
import { View, Text, NativeEventEmitter } from 'react-native';

const Home = (): ReactElement => {
  useEffect(() => {
    NfcReaderModule.initialize();
    const {
      event_nfc_disabled,
      event_nfc_unavailable,
      event_tag_discovered,
    } = NfcReaderModule.getConstants();

    const nfcEventEmitter = new NativeEventEmitter(NfcReaderModule);
    const unavailableListener = nfcEventEmitter.addListener(
      event_nfc_unavailable,
      () => {
        // It means device doesn't have nfc adapter.
      },
    );
    const disabledListener = nfcEventEmitter.addListener(
      event_nfc_disabled,
      () => {
        // It means nfc is present on the device but it's disabled.
      },
    );
    const discoveredListener = nfcEventEmitter.addListener(
      event_tag_discovered,
      (event) => {
        // data is in the 'hexTag' property
        console.log(event.hexTag);
      },
    );
    return () => {
      disabledListener.remove();
      discoveredListener.remove();
      unavailableListener.remove();
    };
  }, []);

  return (
    <View>
      <Text>HOME</Text>
    </View>
  );
};

export default Home;
