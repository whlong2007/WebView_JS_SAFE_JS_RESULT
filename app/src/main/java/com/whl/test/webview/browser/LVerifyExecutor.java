package com.whl.test.webview.browser;

import android.webkit.JsResult;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Method;

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
    protected boolean isParameterTypesValid(Class<?>[] paramTypes) {
        return paramTypes.length == 3 && paramTypes[0].isAssignableFrom(WebView.class) && paramTypes[1].isAssignableFrom(String.class) && paramTypes[2].isAssignableFrom(JSONObject.class);
    }

    @Override
    protected boolean isReturnTypeValid(Class<?> ReturnType) {
        return ReturnType == boolean.class;
    }

    @Override
    protected Object decodeParams(String params) {
        JSONObject obj;

        try {
            obj = JSON.parseObject(params);
        } catch (Exception e) {
            obj = new JSONObject();
        }

        return obj;
    }

    @Override
    protected Object encodeResult(Object obj) {
        return obj;
    }

    @Override
    protected void onJsResult(Object result, JsResult jsResult) {
        if ((boolean) result) {
            jsResult.confirm();
        } else {
            jsResult.cancel();
        }
    }

    @Override
    protected Object invoke(Method methodImp, Object objImp, WebView view, String url, LMessage msg) throws Exception {
        return methodImp.invoke(objImp, view, url, decodeParams(msg.args));
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



        JSONObject ready = new JSONObject();
        ready.put("error", 1);
        ready.put("timestamp", timestamp);
        ready.put("desc", "sdk is ready!");

        sb.append(mYKJSProvider.getYKJSFactory().generateJSEvent());
        sb.append(mYKJSProvider.getYKJSFactory().dispatchJSEvent(LProvider.EVENT_JS_SDK_READY, ready.toJSONString()));

        WebConsoleLog.DEBUG(TAG,sb.toString());

        view.loadUrl("javascript:(function(){" + sb + "})();");
    }

    private boolean activeMethods(WebView view, String url, JSONObject params) {
        if (params != null) {
            JSONArray api = params.getJSONArray("api");

            if (api != null) {

                LProvider.ActiveTransaction at = mYKJSProvider.beginActiveMethod(view);

                for (int i = 0; i < api.size(); i++) {
                    JSONObject apiInfo = api.getJSONObject(i);

                    if (apiInfo != null) {
                        String obj = apiInfo.getString("obj");
                        JSONArray apiList = apiInfo.getJSONArray("apiList");

                        if (apiList != null) {
                            for (int j = 0; j < apiList.size(); j++) {
                                String method = apiList.getString(j);
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
