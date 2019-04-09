package com.example.future.houses;

/**
 * Created by future on 19-4-5.
 */

public interface JsBridge {
    void callPhone(String phoneNumber);
    String getClipboardText();
    void exportData(String data);
    String importData();
}
