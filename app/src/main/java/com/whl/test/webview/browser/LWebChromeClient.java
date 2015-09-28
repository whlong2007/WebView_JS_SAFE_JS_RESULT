package com.whl.test.webview.browser;

import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * 使用YKJSProvider来注入和配置JS接口
 * Created by 1 on 2015/9/10.
 */
public class LWebChromeClient extends WebChromeClient {
    private LProvider mProvider;

    public LWebChromeClient(LProvider provider) {
        this.mProvider = provider;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return mProvider.verify(view, url, message, result) || super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return mProvider.execute(view, url, message, defaultValue, result) || super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        mProvider.freeMethods();
        result.confirm();
        return true;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        WebConsoleLog.DEBUG("WebConsoleLog", consoleMessage.message());
        return true;
    }
}
