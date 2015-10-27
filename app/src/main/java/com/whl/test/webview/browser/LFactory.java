package com.whl.test.webview.browser;

import android.content.Context;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 生成相应的JS字符串
 * Created by 1 on 2015/9/9.
 */
class LFactory {
    private Context mContext;
    private final String mTag;

    LFactory(Context context) {
        this.mContext = context;
        this.mTag = Base64.encodeToString(UUID.randomUUID().toString().getBytes(), Base64.NO_WRAP | Base64.NO_PADDING);
    }

    String generatePrompt(String obj, String method, Map<String, Object> params) {
        return generateMethod("invokeExecute", obj, method, params);
    }

    String generateConfirm(String obj, String method, Map<String, Object> params) {
        return generateMethod("invokeVerify", obj, method, params);
    }

    private String generateMethod(String jsMethod, String obj, String method, Map<String, Object> params) {
        StringBuilder jsb = new StringBuilder();
        jsb.append(jsMethod).append("({");
        jsb.append("obj: \"").append(obj).append("\",");
        jsb.append("action: \"").append(method).append("\",");

        if (params == null) {
            params = new HashMap<String, Object>();
        }
        JSONObject paramsJson = new JSONObject(params);
        jsb.append("args: ").append(paramsJson);
        jsb.append("});");

        return jsb.toString();
    }

    /**
     * function addCallback(eventName, callback) {
     * window.addEventListener(eventName, callback, false);
     * }
     */
    String addCallback() {
        StringBuilder sb = new StringBuilder();
        sb.append("function addCallback(eventName, callback) {");
        sb.append("window.addEventListener(eventName, callback, false);");
        sb.append("}");
        return sb.toString();
    }

    /**
     * function removeCallback(eventName, callback) {
     * window.removeEventListener(eventName, callback);
     * }
     */
    String removeCallback() {
        StringBuilder sb = new StringBuilder();
        sb.append("function removeCallback(eventName, callback) {");
        sb.append("window.removeEventListener(eventName, callback);");
        sb.append("}");
        return sb.toString();
    }

    /**
     * function generateJSEvent(eventName, args) {
     * var event = document.createEvent("CustomEvent");
     * event.initCustomEvent(eventName, false, false, args);
     * window.dispatchEvent(event);
     * }
     */
    String generateJSEvent() {
        StringBuilder sb = new StringBuilder();
        sb.append("function dispatchJSEvent(eventName, args) {");
        sb.append("var event = document.createEvent(\"CustomEvent\");");
        sb.append("event.initCustomEvent(eventName, false, false, args);");
        sb.append("window.dispatchEvent(event);");
        sb.append("}");
        return sb.toString();
    }

    String dispatchJSEvent(String event, Object result) {
        StringBuilder sb = new StringBuilder();
        sb.append("dispatchJSEvent(\"").append(event).append("\"");

        if (result != null) {
            sb.append(",").append(result);
        }

        sb.append(");");
        return sb.toString();
    }

    /**
     * 依赖{@link LFactory#getMessage()}<br/>
     * invokeExecute({
     * obj: "yk.main",
     * action: "hello",
     * args: {
     * name: "KO"
     * }
     * });
     */
    String getPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        sb.append(getAssetsString("browser/l_invoke.js"));
        return sb.toString();
    }

    /**
     * 依赖{@link LFactory#getMessage()}<br/>
     * invokeVerify({
     * obj: "yk",
     * action: "verify",
     * args: {
     * name: "KO"
     * }
     * });
     */
    String getConfirm() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage());
        sb.append(getAssetsString("browser/l_verify.js"));
        return sb.toString();
    }

    /**
     * 依赖{@link LFactory#addCallback()}, {@link LFactory#removeCallback()}<br/>
     */
    String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAssetsString("browser/l_message.js"));
        sb.append("Message.prototype.tag = \"").append(mTag).append("\";");

        sb.append(addCallback());
        sb.append(removeCallback());

        return sb.toString();
    }

    /**
     * @return Message是否有效
     */
    boolean isMessageValid(LMessage msg) {
        return mTag.equals(msg.tag);
    }

    private String getAssetsString(String filename) {
        BufferedReader reader = null;
        final StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(mContext.getResources().getAssets().open(filename)));
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n\r");
            }
        } catch (IOException e) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {

            }
        }
        return sb.toString();
    }
}
