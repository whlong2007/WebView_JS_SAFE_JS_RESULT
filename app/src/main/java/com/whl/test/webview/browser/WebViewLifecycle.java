package com.whl.test.webview.browser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * 将WebView与Activity关联生命周期抽离出来
 * Created by Allen on 2015/10/15.
 */
public class WebViewLifecycle extends Fragment {
    private final static String TAG = "WebViewLifecycle";

    private Activity mActivity;
    private WebView mWebView;
    private LProvider mProvider;

    private UploadHandler mUploadHandler;

    /**使用{@link WebViewLifecycle#addToActivity(Activity, String, LProvider, WebView)}
     * @deprecated
     */
    public WebViewLifecycle(){
    }

    public static WebViewLifecycle addToActivity(Activity activity, String tag, LProvider provider, WebView view) {
        FragmentManager fm = activity.getFragmentManager();
        WebViewLifecycle web = (WebViewLifecycle) fm.findFragmentByTag(tag);

        if (web == null) {
            web = new WebViewLifecycle();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(web, tag);
            ft.commit();
        }

        web.mWebView = view;
        web.mProvider = provider;

        return web;
    }

    public void setUploadHandler(UploadHandler handler) {
        this.mUploadHandler = handler;
    }

    public LProvider getProvider() {
        return mProvider;
    }

    private boolean isProviderInited() {
        return mWebView != null && mProvider != null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isProviderInited()) {
            mProvider.onAttach(mActivity, mWebView, mProvider);
            mProvider.onCreate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isProviderInited()) {
            mProvider.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isProviderInited()) {
            mProvider.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (isProviderInited()) {
            mProvider.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isProviderInited()) {
            mProvider.onDestroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mUploadHandler != null) {
            mUploadHandler.onResult(requestCode, resultCode, data);
        }

        mUploadHandler = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
        mWebView = null;
        mProvider = null;
        mUploadHandler = null;
    }
}
