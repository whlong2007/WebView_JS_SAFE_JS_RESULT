package com.whl.test.webview.browser;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * WebView文件打开各个版本
 * Created by Allen on 2015/10/27.
 */
public interface LFileChooserListener {

    /**
     * 11+
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType);


    /**
     * 16+
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture);

    /**
     * 21+
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, WebChromeClient.FileChooserParams params);
}
