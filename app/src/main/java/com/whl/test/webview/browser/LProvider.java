package com.whl.test.webview.browser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JS生产和执行提供
 */
public class LProvider {
    private final static String TAG = "YKJSProvider";

    public final static String EVENT_JS_SDK_READY = "com.whl.l_sdk.init#ready";

    private LFactory mYKJSFactory;
    private LVerifyExecutor mVerifyExecutor;
    private LInterfaceExecutor mInterfaceExecutor;

    private LProvider(Context context) {
        this.mYKJSFactory = new LFactory(context);
        this.mVerifyExecutor = new LVerifyExecutor(this);
        this.mInterfaceExecutor = new LInterfaceExecutor(this);
    }

    /**
     * 在load url之前init
     *
     * @param tag 用来放Fragment
     */
    public static LProvider init(Activity activity, WebView view, String tag, Map<String, List<LInterface>> interfaces) {
        LProvider provider = new LProvider(activity);

        for (Map.Entry<String, List<LInterface>> entry : interfaces.entrySet()) {
            String name = entry.getKey();

            for (LInterface obj : entry.getValue()) {
                provider.addInterface(name, obj);
            }
        }

        provider.init(activity, view, tag);

        return provider;
    }

    /**
     * 在load url之前init
     *
     * @param tag 用来放Fragment
     */
    private void init(Activity activity, WebView view, String tag) {
        Lifecycle fragment = (Lifecycle) activity.getFragmentManager().findFragmentByTag(tag);

        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();

        if (fragment != null) {
            ft.remove(fragment);
        } else {
            fragment = new Lifecycle();
        }

        fragment.init(this, view);

        ft.add(fragment, tag).commit();
    }

    public LFactory getYKJSFactory() {
        return mYKJSFactory;
    }

    /**
     * {@link LVerifyExecutor#injectConfigJS(WebView)}
     */
    public void injectConfigJS(WebView view) {
        mVerifyExecutor.injectConfigJS(view);
    }

    /**
     * {@link LInterfaceExecutor#freeMethods()}
     */
    public void freeMethods() {
        mInterfaceExecutor.freeMethods();
    }

    /**
     * {@link ActiveTransaction}
     */
    public ActiveTransaction beginActiveMethod(WebView view) {
        return new ActiveTransaction(view);
    }

    /**
     * {@link LInterfaceExecutor#add(String, Object)}
     */
    public void addInterface(String name, LInterface obj) {
        mInterfaceExecutor.add(name, obj);
    }

    /**
     * {@link LInterfaceExecutor#remove(String)}
     */
    public void removeInterface(String name) {
        mInterfaceExecutor.remove(name);
    }

    /**
     * {@link LInterfaceExecutor#remove(String, String)}
     */
    public void removeInterface(String name, String classname) {
        mInterfaceExecutor.remove(name, classname);
    }

    /**
     * {@link LInterfaceExecutor#clear()}
     */
    public void clearInterfaces() {
        mInterfaceExecutor.clear();
    }

    /**
     * {@link LVerifyExecutor#execute(WebView, String, LMessage, JsResult)}
     *
     * @return 拦截返回true
     */
    public boolean verify(WebView view, String url, String message, JsResult result) {
        return executeImp(mVerifyExecutor, view, url, message, result);
    }

    /**
     * {@link LInterfaceExecutor#execute(WebView, String, LMessage, JsResult)}
     */
    public boolean execute(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return executeImp(mInterfaceExecutor, view, url, message, result);
    }

    private boolean executeImp(LExecutor executor, WebView view, String url, String message, JsResult result) {
        LMessage msg = LMessage.parseMessage(message);
        if (msg.isVaild()) {
            executor.execute(view, url, msg, result);
            return true;
        }

        return false;
    }

    /**
     * 回调JS的callback
     *
     * @param result 回调给JS的参数
     */
    public void dispatchJSEvent(WebView view, String event, Object result) {
        StringBuilder sb = new StringBuilder();
        sb.append(mYKJSFactory.generateJSEvent());
        sb.append(mYKJSFactory.dispatchJSEvent(event, result));

        WebConsoleLog.DEBUG(TAG, "callback: javascript:(function(){" + sb + "})();");

        view.loadUrl("javascript:(function(){" + sb + "})();");
    }

    /**
     * Commit之后才会生成相应的JS对象并且执行
     */
    public class ActiveTransaction {
        private boolean isCommit;
        private StringBuilder sb;
        private Set<String> mObjSet;
        private WebView mWebView;

        ActiveTransaction(WebView view) {
            this.mWebView = view;

            sb = new StringBuilder();
            sb.append(mYKJSFactory.getPrompt());

            mObjSet = new HashSet<String>();
        }

        /**
         * {@link LInterfaceExecutor#activateMethod(String, String)}
         */
        public ActiveTransaction activeMethod(String obj, String method) {
            checkCommit();
            sb.append(mInterfaceExecutor.activateMethod(obj, method));
            return this;
        }

        /**
         * 提交并且调用{@link WebView#loadUrl(String)}来执行JS
         */
        public void commit() {
            WebConsoleLog.DEBUG(TAG, "ActiveTransaction#commit: " + sb);

            isCommit = true;

            for (String obj : mObjSet) {
                sb.append("Object.freeze(").append(obj).append(");");
            }

            mWebView.loadUrl("javascript:(function(){" + sb + "})();");
        }

        void checkCommit() {
            if (isCommit) {
                throw new IllegalStateException("Transaction already commit!");
            }
        }
    }

    public static class Lifecycle extends Fragment {
        private Activity mActivity;
        private WebView mWebView;
        private LProvider mYKJSProvider;
        //遍历执行YKJSInterface的onCreate等方法
        private LInterface mPoxy;

        public void init(LProvider provider, WebView view) {
            this.mWebView = view;
            this.mYKJSProvider = provider;
            initPoxy();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            this.mActivity = activity;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (isInited()) {
                mPoxy.onCreate(mActivity, mWebView, mYKJSProvider);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (isInited()) {
                mPoxy.onResume();
                mWebView.onResume();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (isInited()) {
                mPoxy.onPause();
                mWebView.onPause();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (isInited()) {
                mPoxy.onStop();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            if (isInited()) {
                mPoxy.onDestroy();
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mActivity = null;
            mWebView = null;
            mYKJSProvider = null;
        }

        private boolean isInited() {
            return mWebView != null && mYKJSProvider != null;
        }

        private void initPoxy() {
            if (mYKJSProvider != null) {

                InvocationHandler handler = new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        WebConsoleLog.DEBUG(TAG, method.getName());

                        for (Object obj : mYKJSProvider.mInterfaceExecutor.getInterfaces()) {
                            method.invoke(obj, args);
                        }
                        return null;
                    }
                };

                mPoxy = (LInterface) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{LInterface.class}, handler);
            }
        }
    }
}
