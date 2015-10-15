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
public class LWebChromeClient extends WebChromeClient {
    public final static int REQUEST_CODE_UPLOADFILE = 0x2015;
    public final static String TITLE_UPLOADFILE = "上传文件";

    private WebViewLifecycle mLifecycle;
    private int mUploadReqCode;
    private String mUploadTitle;

    public LWebChromeClient(WebViewLifecycle lifecycle) {
        this(lifecycle, REQUEST_CODE_UPLOADFILE, TITLE_UPLOADFILE);
    }

    public LWebChromeClient(WebViewLifecycle lifecycle, int uploadReqCode, String uploadTitle) {
        this.mLifecycle = lifecycle;
        this.mUploadReqCode = uploadReqCode;
        this.mUploadTitle = uploadTitle;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result) || mLifecycle.getProvider().verify(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result) || mLifecycle.getProvider().execute(view, url, message, defaultValue, result);
    }

    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
        UploadHandler<Uri> uploadHandler = new UploadHandler<Uri>(mLifecycle, mUploadReqCode, mUploadTitle) {
        };
        mLifecycle.setUploadHandler(uploadHandler);
        uploadHandler.openFileChooser(uploadFile, acceptType);
    }

    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        UploadHandler<Uri> uploadHandler = new UploadHandler<Uri>(mLifecycle, mUploadReqCode, mUploadTitle) {
        };
        mLifecycle.setUploadHandler(uploadHandler);
        uploadHandler.openFileChooser(uploadFile, acceptType, capture);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        UploadHandler<Uri[]> uploadHandler = new UploadHandler<Uri[]>(mLifecycle, mUploadReqCode, mUploadTitle) {
        };
        mLifecycle.setUploadHandler(uploadHandler);
        uploadHandler.openFileChooser(filePathCallback, fileChooserParams);
        return true;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        WebConsoleLog.DEBUG("WebConsoleLog", consoleMessage.message());
        return true;
    }
}
