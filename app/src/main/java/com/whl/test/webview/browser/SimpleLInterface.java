package com.whl.test.webview.browser;

import android.app.Activity;
import android.webkit.WebView;


/**
 * 抽象的JSInterface,提供了Activity和WebView
 */
public class SimpleLInterface implements LInterface {
    private final static String TAG = "SimpleJSInterface";

    protected Activity mActivity;
    protected WebView mWebView;
    protected LProvider mYKJSProvider;

    @Override
    public void onCreate(Activity activity, WebView view, LProvider provider) {
        this.mActivity = activity;
        this.mWebView = view;
        this.mYKJSProvider = provider;
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        this.mActivity = null;
        this.mWebView = null;
        this.mYKJSProvider = null;
    }
}
