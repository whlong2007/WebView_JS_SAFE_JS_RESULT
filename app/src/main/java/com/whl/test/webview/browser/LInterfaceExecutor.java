package com.whl.test.webview.browser;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;

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
    protected void onJsResult(Object jsonObject, JsResult jsResult) {
        JsPromptResult result = (JsPromptResult) jsResult;
        result.confirm(jsonObject == null ? "{}" : jsonObject.toString());
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
