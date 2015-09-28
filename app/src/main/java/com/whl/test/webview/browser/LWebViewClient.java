package com.whl.test.webview.browser;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by 1 on 2015/8/31.
 */
public class LWebViewClient extends WebViewClient {
    private LProvider mProvider;

    public LWebViewClient(LProvider provider) {
        this.mProvider = provider;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
        mProvider.injectConfigJS(view);
    }
}
