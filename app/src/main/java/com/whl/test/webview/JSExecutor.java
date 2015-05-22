package com.whl.test.webview;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import android.os.*;
import android.util.Log;
import android.view.View;

public class JSExecutor {
    public final static String TAG = "JSExecutor";

    private CompatibleWebView mWebView;

    public JSExecutor(CompatibleWebView view) {
        this.mWebView = view;
    }

    public String execute(String script) {

        if (Looper.myLooper() == Looper.getMainLooper()) {//会阻塞loadURL执行JS
            throw new IllegalStateException("Cannot be called from main thread");
        }

        JSExecutResult interfaceObj = new JSExecutResult();
        String interfaceName = JSExecutResult.class.getSimpleName() + interfaceObj.hashCode();

        mWebView.addJSExecutResult(interfaceObj, interfaceName);

        if (script.startsWith("javascript:")) {
            script = script.substring("javascript:".length());
        }

        if (script.endsWith(";")) {
            script = script.substring(0, script.length() - 1);
        }

        Map<String, Object> interfaces = new HashMap<String, Object>();
        interfaces.put(interfaceName, interfaceObj);
        String JSExecutResultJS = JSPromptUtil.generateInterfaceJS(null, WebViewConfig.TYPE_PROMPT_RESULT, interfaces);

        final StringBuilder sb = new StringBuilder();
        sb.append("javascript:(");
        sb.append("function(){");
        sb.append(JSExecutResultJS).append(";");

        sb.append("var result=new Object();");
        sb.append("try{");
        sb.append("result.error=").append(WebViewConfig.ERROR_JS_EXECUTOR_OK).append(";");
        sb.append("result.result=").append(script).append(";");
        sb.append("}catch(e){");
        sb.append("result.error=").append(WebViewConfig.ERROR_JS_EXECUTOR_JS).append(";");
        sb.append("result.result=e.name+':'+e.message;");
        sb.append("}");
        sb.append(interfaceName).append(".confirm(JSON.stringify(result));");
        sb.append("})()");

        Log.i(TAG, "JSExecutor: " + sb.toString());

        mWebView.post(new Runnable() {
            public void run() {
                mWebView.loadUrl(sb.toString());
            }
        });

        String result;

        try {
            result = interfaceObj.waitForResult();
        } catch (TimeoutException e) {
            result = "result: {\"error\":" + WebViewConfig.ERROR_JS_EXECUTOR_TIMEOUT + ",\"result\":\"JavaScript execute timeout!\"}";
        }

        mWebView.removeJSExecutResult(interfaceName);
        return result;
    }
}
