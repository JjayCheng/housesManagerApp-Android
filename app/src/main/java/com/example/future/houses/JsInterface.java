package com.example.future.houses;

import android.webkit.JavascriptInterface;

/**
 * Created by future on 19-4-5.
 */

public class JsInterface {
    private JsBridge jsBridge;
    public JsInterface(JsBridge jsBridge) {
        this.jsBridge = jsBridge;
    }

    @JavascriptInterface
    public void callPhone(String phoneNumber) {
        jsBridge.callPhone(phoneNumber);
    }

    @JavascriptInterface
    public String getClipboardText() {

        // ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        return jsBridge.getClipboardText();
    }
    @JavascriptInterface
    public String importData() {

        // ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        return jsBridge.importData();
    }
    @JavascriptInterface
    public void exportData(String data) {

        // ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        jsBridge.exportData(data);
    }

}
