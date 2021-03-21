package com.nfcreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


public class NFCModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    private static final String EVENT_NFC_UNAVAILABLE = "event_nfc_unavailable";
    private static final String EVENT_NFC_DISABLED = "event_nfc_disabled";
    private static final String EVENT_TAG_DISCOVERED = "event_tag_discovered";

    private final NfcAdapter adapter;

    public NFCModule(ReactApplicationContext context) {
        super(context);
        context.addLifecycleEventListener(this);
        adapter = NfcAdapter.getDefaultAdapter(context);
    }

    @NonNull
    @Override
    public String getName() {
        return "NFCModule";
    }

    private Activity getActivity() {
        return getReactApplicationContext().getCurrentActivity();
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(EVENT_NFC_DISABLED, EVENT_NFC_DISABLED);
        constants.put(EVENT_NFC_UNAVAILABLE, EVENT_NFC_UNAVAILABLE);
        constants.put(EVENT_TAG_DISCOVERED, EVENT_TAG_DISCOVERED);
        return constants;
    }

    public void sendEvent(String eventName, @Nullable WritableMap params) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void initialize() {
        if (adapter != null) {
            if (adapter.isEnabled()) {
                setupForegroundDispatch(getCurrentActivity());
            } else {
                Log.w("EVENT", EVENT_NFC_DISABLED);
                sendEvent(EVENT_NFC_DISABLED, null);
            }
        } else {
            Log.w("EVENT", EVENT_NFC_UNAVAILABLE);
            sendEvent(EVENT_NFC_UNAVAILABLE, null);
        }
    }

    public void setupForegroundDispatch(Activity activity) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        try {
            adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        } catch (Exception e) {
            Log.d("NFC_READER_ERROR", "Failed enabling foreground dispatch");
        }
    }

    public void stopForegroundDispatch(final Activity activity) {
        try{
            adapter.disableForegroundDispatch(activity);
        }catch(Exception e){
            Log.d("NFC_READER_ERROR", "Error in stopping forground dispatch");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            handleIntent(intent);
        }
    }

    public void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                byte[] tagId = tag.getId();
                WritableMap params = Arguments.createMap();
                params.putString("hexTag", toHex(tagId));
                sendEvent(EVENT_TAG_DISCOVERED, params);
                stopForegroundDispatch(getActivity());
            }
        }
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
        stopForegroundDispatch(getActivity());
    }

    @Override
    public void onHostDestroy() {
        stopForegroundDispatch(getActivity());
    }
}
