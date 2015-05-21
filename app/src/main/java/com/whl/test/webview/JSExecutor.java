package com.whl.test.webview;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import android.util.Log;

public class JSExecutor {
	public final static String TAG = "JSExecutor";

	private CompatibleWebView mWebView;
	private JSExecutResult mResult;
	private String mInterfaceName;

	public JSExecutor(CompatibleWebView view) {
		this(view, new JSExecutResult());
	}

	public JSExecutor(CompatibleWebView view, JSExecutResult obj) {
		this(view, obj, obj.getClass().getSimpleName());
	}

	public JSExecutor(CompatibleWebView view, JSExecutResult obj, String name) {
		this.mWebView = view;
		this.mResult = obj;
		this.mInterfaceName = name;
	}

	public String execute(String script) {
		mWebView.addJSExecutResult(mResult, mInterfaceName);

		if (script.startsWith("javascript:")) {
			script = script.substring("javascript:".length());
		}

		if (script.endsWith(";")) {
			script = script.substring(0, script.length() - 1);
		}

		Map<String, Object> interfaces = new HashMap<String, Object>();
		interfaces.put(mInterfaceName, mResult);
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
		sb.append(mInterfaceName).append(".confirm(JSON.stringify(result));");
		sb.append("})()");

		Log.i(TAG, "JSExecutor: " + sb.toString());

		mWebView.post(new Runnable() {
			@Override
			public void run() {
				mWebView.loadUrl(sb.toString());
			}
		});

		String result;

		try {
			result = mResult.waitForResult();
		} catch (TimeoutException e) {
			result = "result: {\"error\":" + WebViewConfig.ERROR_JS_EXECUTOR_TIMEOUT + ",\"result\":\"JavaScript execute timeout!\"}";
		}

		mWebView.removeJSExecutResult(mInterfaceName);
		return result;
	}
}
