package com.whl.test.webview.browser;

import android.webkit.JsResult;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证接口的执行
 * Created by 1 on 2015/9/10.
 */
class LVerifyExecutor extends LExecutor {
    public final static String TAG = "VerifyExecutor";

    /**
     * 配置接口的对象集合名称
     */
    public final static String TOP_JS_OBJ = "yk";
    /**
     * 用来配置相应的对象方法
     */
    public final static String TOP_JS_OBJ_CONFIG = "config";

    LVerifyExecutor(LProvider provider) {
        super(provider);

        add(TOP_JS_OBJ, this);
    }

    @Override
    protected boolean isReturnTypeValid(Class<?> ReturnType) {
        return ReturnType == Boolean.TYPE || ReturnType == Boolean.class;
    }

    @Override
    protected void onJsResult(Object result, JsResult jsResult) {
        if ((boolean) result) {
            jsResult.confirm();
        } else {
            jsResult.cancel();
        }
    }

    /**
     * TODO 验证API暂时都验证通过
     *
     * @return 验证成功返回true
     */
    @LJavascript
    public boolean config(WebView view, String curUrl, JSONObject params) {
        return activeMethods(view, curUrl, params);
    }

    /**
     * 执行注入
     */
    public void injectConfigJS(WebView view) {
        long timestamp = System.currentTimeMillis();

        String obj = TOP_JS_OBJ;
        String method = TOP_JS_OBJ_CONFIG;

        StringBuilder sb = new StringBuilder();

        sb.append(mYKJSProvider.getYKJSFactory().getConfirm());
        sb.append(mYKJSProvider.getYKJSFactory().generateConfirm(obj, method, null));

        Map<String, Object> ready = new HashMap<String, Object>();
        ready.put("error", 1);
        ready.put("timestamp", timestamp);
        ready.put("desc", "sdk is ready!");

        sb.append(mYKJSProvider.getYKJSFactory().generateJSEvent());
        sb.append(mYKJSProvider.getYKJSFactory().dispatchJSEvent(LProvider.EVENT_JS_SDK_READY, new JSONObject(ready).toString()));

        WebConsoleLog.DEBUG(TAG, sb.toString());

        view.loadUrl("javascript:(function(){" + sb + "})();");
    }

    private boolean activeMethods(WebView view, String url, JSONObject params) {
        if (params != null) {
            JSONArray api = params.optJSONArray("api");

            if (api != null) {

                LProvider.ActiveTransaction at = mYKJSProvider.beginActiveMethod(view);

                for (int i = 0; i < api.length(); i++) {
                    JSONObject apiInfo = api.optJSONObject(i);

                    if (apiInfo != null) {
                        String obj = apiInfo.optString("obj");
                        JSONArray apiList = apiInfo.optJSONArray("apiList");

                        if (apiList != null) {
                            for (int j = 0; j < apiList.length(); j++) {
                                String method = apiList.optString(j);
                                at.activeMethod(obj, method);
                            }
                        }
                    }
                }

                at.commit();
                return true;
            }
        }

        return false;
    }
}
