package com.whl.test.webview.browser;

import android.app.Activity;
import android.webkit.WebView;

/**
 * JSInterface,配有需要和Activity生命周期绑定的Activity
 */
public interface LInterface {

    void onCreate(Activity activity, WebView view, LProvider provider);

    void onPause();

    void onStop();

    void onDestroy();

    void onResume();
}
