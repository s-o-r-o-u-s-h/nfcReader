package com.nfcreader;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.util.ArrayList;
import java.util.List;

public class NdefMessageParser {
    private NdefMessageParser() {}

    public static List<NFCParsedRecord> parse(NdefMessage message) {
        return getRecords(message.getRecords());
    }

    public static List<NFCParsedRecord> getRecords(NdefRecord[] records) {
        List<NFCParsedRecord> elements = new ArrayList<>();

        for (final NdefRecord record: records) {
            if (NFCTextParsedRecord.isText(record)) {
                elements.add(NFCTextParsedRecord.parse(record));
            } else {
                elements.add(new NFCParsedRecord() {
                    @Override
                    public String str() {
                        return new String(record.getPayload());
                    }
                });
            }
        }

        return elements;
    }
}
