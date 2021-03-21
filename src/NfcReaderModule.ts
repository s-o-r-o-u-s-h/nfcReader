import { NativeModules } from 'react-native';
const { NFCModule } = NativeModules;

interface INFCModule {
  initialize: () => void;
  getConstants: () => {
    event_nfc_disabled: 'event_nfc_disabled';
    event_nfc_unavailable: 'event_nfc_unavailable';
    event_tag_discovered: 'event_tag_discovered';
  };
  addListener: (eventType: string) => void;
  removeListeners: (count: number) => void;
}

export default NFCModule as INFCModule;
