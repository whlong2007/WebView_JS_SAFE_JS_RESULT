package com.whl.test.webview.browser;

import android.net.Uri;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * 使用YKJSProvider来注入和配置JS接口
 * Created by 1 on 2015/9/10.
 */
public class LWebChromeClient extends UploadChromeClient implements LFileChooserListener {
    public final static int REQUEST_CODE_UPLOADFILE = 0x2015;
    public final static String TITLE_UPLOADFILE = "上传文件";

    private WebViewLifecycle mLifecycle;

    public LWebChromeClient(WebViewLifecycle lifecycle) {
        this(lifecycle, REQUEST_CODE_UPLOADFILE, TITLE_UPLOADFILE);
    }

    public LWebChromeClient(WebViewLifecycle lifecycle, int uploadReqCode, String uploadTitle) {
        super(lifecycle, uploadReqCode, uploadTitle);
        this.mLifecycle = lifecycle;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result) || mLifecycle.getProvider().verify(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result) || mLifecycle.getProvider().execute(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        WebConsoleLog.DEBUG("WebConsoleLog", consoleMessage.message());
        return true;
    }
}
