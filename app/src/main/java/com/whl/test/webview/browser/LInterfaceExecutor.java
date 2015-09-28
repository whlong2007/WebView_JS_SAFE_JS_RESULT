package com.whl.test.webview.browser;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口对象的添加和执行
 * Created by 1 on 2015/9/10.
 */
class LInterfaceExecutor extends LExecutor {
    private final static String TAG = "InterfaceExecutor";

    private final Map<String, Map<String, Method>> mActiveMethodMap;

    LInterfaceExecutor(LProvider provider) {
        super(provider);
        this.mActiveMethodMap = new HashMap<String, Map<String, Method>>();
    }

    /**
     * 将所有方法从activateMethod中清除
     */
    void freeMethods() {
        WebConsoleLog.DEBUG(TAG, "freezeMethods");

        mActiveMethodMap.clear();
    }

    /**
     * 激活方法
     *
     * @return 转译的JS
     */
    String activateMethod(/*@NonNull*/ String obj, /*@NonNull*/ String method) {
        WebConsoleLog.DEBUG(TAG, "activateMethod " + obj + "." + method);

        Map<String, Method> all = getMethods().get(obj);
        Method methodImp;

        if (all == null || (methodImp = all.get(method)) == null) {
            return "console.error(\"do not have method at " + obj + "." + method + "\");";
        }

        Map<String, Method> temp = getNonNullCollection(mActiveMethodMap, obj);
        temp.put(method, methodImp);

        return mYKJSProvider.getYKJSFactory().generatePrompt(obj, method, null);
    }

    @Override
    protected boolean isMethodActive(String obj, String method) {
        return super.isMethodActive(obj, method) && getActiveMethod(obj, method) != null;
    }

    @Override
    protected boolean isParameterTypesValid(Class<?>[] paramTypes) {
        return paramTypes.length == 4 && paramTypes[0].isAssignableFrom(WebView.class) && paramTypes[1].isAssignableFrom(String.class) && paramTypes[2].isAssignableFrom(JSONObject.class) && paramTypes[3].isAssignableFrom(LCallback.class);
    }

    @Override
    protected boolean isReturnTypeValid(Class<?> ReturnType) {
        return ReturnType.isAssignableFrom(JSONObject.class);
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
    protected Object encodeResult(Object result) {
        Object obj;

        try {
            obj = JSON.toJSONString(result);
        } catch (Exception e) {
            obj = "";
        }

        return obj;
    }

    @Override
    protected void onJsResult(Object jsonObject, JsResult jsResult) {
        JsPromptResult result = (JsPromptResult) jsResult;
        result.confirm(jsonObject == null ? "{}" : jsonObject.toString());
    }

    @Override
    protected Object invoke(Method methodImp, Object objImp, final WebView view, String url, final LMessage msg) throws Exception {
        LCallback callback = new LCallback() {
            @Override
            public void dispatch(Object result) {
                mYKJSProvider.dispatchJSEvent(view, msg.obj + "." + msg.action + "#" + msg.timestamp, encodeResult(result));
            }
        };

        return methodImp.invoke(objImp, view, url, decodeParams(msg.args), callback);
    }

    @Override
    void clear() {
        super.clear();
        mActiveMethodMap.clear();
    }

    @Override
    void remove(String name) {
        super.remove(name);
        mActiveMethodMap.remove(name);
    }

    @Override
    void remove(String name, String tag) {
        super.remove(name, tag);

        Map<String, Method> activeMethods = mActiveMethodMap.get(name);
        if (activeMethods != null) {
            for (Map.Entry<String, Method> entry : activeMethods.entrySet()) {
                if (entry.getValue().getDeclaringClass().getName().equals(tag)) {
                    activeMethods.remove(entry.getKey());
                }
            }
        }
    }

    /**
     * @return Nullable 有效的Method方法
     */
    private Method getActiveMethod(String name, String method) {
        Map<String, Method> methodMap = mActiveMethodMap.get(name);

        if (methodMap != null) {
            return methodMap.get(method);
        }
        return null;
    }
}
