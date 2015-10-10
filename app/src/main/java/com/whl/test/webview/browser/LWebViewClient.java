package com.whl.test.webview.browser;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * JS的注入和释放
 * Created by 1 on 2015/8/31.
 */
public class LWebViewClient extends WebViewClient {
    private LProvider mProvider;

    public LWebViewClient(LProvider provider) {
        this.mProvider = provider;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mProvider.freeMethods();
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
        mProvider.injectConfigJS(view);
    }
}
