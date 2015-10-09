package com.whl.test.webview.browser;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * H5传过来的消息
 * Created by 1 on 2015/9/28.
 */
final class LMessage {
    static final String KEY_TAG = "tag";
    static final String KEY_TIMESTAMP = "timestamp";
    static final String KEY_OBJECT = "obj";
    static final String KEY_ACTION = "action";
    static final String KEY_ARGS = "args";

    public final static String TAG_VALUE;

    static {
        UUID uuid = UUID.randomUUID();
        TAG_VALUE = Base64.encodeToString(uuid.toString().getBytes(), Base64.NO_WRAP | Base64.NO_PADDING);
    }

    String tag;
    String timestamp;

    String obj;
    String action;

    JSONObject args;

    private LMessage() {
    }

    static LMessage parseMessage(String message) {
        LMessage msg = new LMessage();

        try {
            JSONObject obj = new JSONObject(message);
            msg.tag = obj.optString(KEY_TAG);
            msg.timestamp = obj.optString(KEY_TIMESTAMP);
            msg.obj = obj.optString(KEY_OBJECT);
            msg.action = obj.optString(KEY_ACTION);
            msg.args = obj.optJSONObject(KEY_ARGS);
        } catch (JSONException e) {
        }

        if (msg.args == null) msg.args = new JSONObject();

        return msg;
    }

    boolean isVaild() {
        return TAG_VALUE.equals(tag);
    }
}
