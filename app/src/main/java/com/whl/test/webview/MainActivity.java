package com.whl.test.webview;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.whl.test.webview.browser.LCallback;
import com.whl.test.webview.browser.LInterface;
import com.whl.test.webview.browser.LJavascript;
import com.whl.test.webview.browser.LProvider;
import com.whl.test.webview.browser.LWebChromeClient;
import com.whl.test.webview.browser.LWebViewClient;
import com.whl.test.webview.browser.SimpleLInterface;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    public final static String TAG = "MainActivity";

    private WebView mWebview;
//    private LUploadHandler mUploadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebview = (WebView) findViewById(R.id.webview);
//        mUploadHandler = new LUploadHandler(this, 0x2015, "上传文件");

        WebSettings settings = mWebview.getSettings();

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 客串文件
        settings.setAllowFileAccess(true);
        // 可解析js
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLightTouchEnabled(true);

        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        Map<String, List<LInterface>> map = new HashMap<String, List<LInterface>>();
        List<LInterface> ykList = new ArrayList<LInterface>();
        ykList.add(new Login());
        ykList.add(new Login2());

        map.put("yk", ykList);

        LProvider mYKJSProvider = LProvider.init(this, mWebview, "MainActivity#WebView", map);

        mWebview.setWebChromeClient(new LWebChromeClient(mYKJSProvider) {

//            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
//                mUploadHandler.openFileChooser(uploadFile, acceptType, capture);
//            }
        });
        mWebview.setWebViewClient(new LWebViewClient(mYKJSProvider) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebview.loadUrl("file:///android_asset/browser/test.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        mUploadHandler.onResult(resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    class Login extends SimpleLInterface {
        @LJavascript
        public JSONObject login(WebView view, String curUrl, JSONObject params, LCallback callback) {

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("hello", "world");

            return new JSONObject(result);
        }
    }

    class Login2 extends SimpleLInterface {

        @LJavascript(action = "login3")
        public JSONObject login2(LCallback callback) {

            Map<String, Object> map = new HashMap<String, Object>();

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("error", "Login2");
            result.put("callback", 1);

            Map<String, String> cookie = new HashMap<String, String>();
            cookie.put("password", "sdsd25524");

            callback.confirm(new JSONObject(result));

            result.remove("callback");
            return new JSONObject(result);
        }
    }

}
