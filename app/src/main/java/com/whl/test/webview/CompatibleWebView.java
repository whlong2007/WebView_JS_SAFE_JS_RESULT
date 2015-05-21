package com.whl.test.webview;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

public class CompatibleWebView extends WebView {

	private final static String TAG = "CompatibleWebView";

	private Map<String, Object> mInterfaces;
	private Map<String, Object> mJSExecutResults;

	public CompatibleWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CompatibleWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CompatibleWebView(Context context) {
		super(context);
		init();
	}

	public void init() {
		mInterfaces = new HashMap<String, Object>();
		mJSExecutResults = new HashMap<String, Object>();
		setWebViewClient(new WebViewClient(this));
		setWebChromeClient(new WebChromeClient(this));
	}

	@Override
	public void addJavascriptInterface(Object obj, String name) {
		if (!WebViewConfig.isUsePrompt()) {
			super.addJavascriptInterface(obj, name);
		} else {
			mInterfaces.put(name, obj);
		}
	}

	@Override
	public void removeJavascriptInterface(String name) {
		if (!WebViewConfig.isUsePrompt()) {
			super.removeJavascriptInterface(name);
		} else {
			mInterfaces.remove(name);
		}
	}

	public void addJSExecutResult(Object obj, String name) {
		mJSExecutResults.put(name, obj);
	}

	public void removeJSExecutResult(String name) {
		mJSExecutResults.remove(name);
	}

	@Override
	public void setWebViewClient(android.webkit.WebViewClient client) {
		if (!(client instanceof WebViewClient)) {
			throw new IllegalArgumentException("请使用 com.whl.test.webview.CompatibleWebView.WebViewClient");
		}

		super.setWebViewClient(client);
	}

	@Override
	public void setWebChromeClient(android.webkit.WebChromeClient client) {
		if (!(client instanceof WebChromeClient)) {
			throw new IllegalArgumentException("请使用 com.whl.test.webview.CompatibleWebView.WebChromeClient");
		}

		super.setWebChromeClient(client);
	}

	public static class WebViewClient extends android.webkit.WebViewClient {
		private CompatibleWebView webView;

		public WebViewClient(CompatibleWebView wrapper) {
			this.webView = wrapper;
		}

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

			// 加载BUG_FIX.JS文件
			if (WebViewConfig.URL_JS_BUG_FIX.equalsIgnoreCase(url)) {
				Log.i(TAG, "shouldInterceptRequest URL_JS_BUG_FIX: " + url);

				String mimeType = "application/javascript";
				String encoding = "UTF-8";
				InputStream data = null;

				if (WebViewConfig.isUsePrompt()) {
					String js = JSPromptUtil.generateInterfaceJS("window", WebViewConfig.TYPE_PROMPT_INTERFACE, webView.mInterfaces);
					Log.i(TAG, "js: " + js);
					if (js != null) {
						data = new ByteArrayInputStream(js.getBytes());
					}
				}

				return new WebResourceResponse(mimeType, encoding, data);
			}

			return super.shouldInterceptRequest(view, url);
		}
	}

	public static class WebChromeClient extends android.webkit.WebChromeClient {
		private CompatibleWebView webView;

		public WebChromeClient(CompatibleWebView wrapper) {
			this.webView = wrapper;
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
			if (WebViewConfig.isUsePrompt()) {
				Message msg = Message.obtain(message, defaultValue);

				if (msg.veryfy()) {
					Object resultObj = null;

					if (!(TextUtils.isEmpty(msg.interfaceName) || TextUtils.isEmpty(msg.interfaceMethod))) {
						Object obj = null;

						if (msg.isInterface()) {
							obj = webView.mInterfaces.get(msg.interfaceName);
						} else if (msg.isResult()) {
							obj = webView.mJSExecutResults.get(msg.interfaceName);
						}

						try {
							Method method = obj.getClass().getMethod(msg.interfaceMethod, String.class);

							if (!method.isAccessible()) {
								method.setAccessible(true);
							}

							resultObj = method.invoke(obj, defaultValue);
						} catch (Exception e) {
							Log.i(TAG, "执行Native接口失败", e);
						}
					} else {
						Log.i(TAG, "JS执行错误");
					}

					if (resultObj != null) {
						result.confirm(resultObj.toString());
					} else {
						result.confirm();
					}

					return true;
				}
			}

			return super.onJsPrompt(view, url, message, defaultValue, result);
		}
	}

	private static class Message {

		int type;
		String interfaceName;
		String interfaceMethod;

		static Message obtain(String header, String params) {
			Message msg = new Message();

			try {
				JSONObject headerObj = new JSONObject(header);
				new JSONObject(params);
				msg.type = headerObj.optInt(WebViewConfig.KEY_PROMPT_TYPE);
				msg.interfaceName = headerObj.optString(WebViewConfig.KEY_PROMPT_INTERFACE_NAME);
				msg.interfaceMethod = headerObj.optString(WebViewConfig.KEY_PROMPT_INTERFACE_METHOD);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return msg;
		}

		boolean veryfy() {
			return isInterface() || isResult();
		}

		/**
		 * 是否调用Native接口
		 */
		boolean isInterface() {
			// && !TextUtils.isEmpty(interfaceName)
			// && !TextUtils.isEmpty(interfaceMethod);
			return type == WebViewConfig.TYPE_PROMPT_INTERFACE;
		}

		/**
		 * 是否是JS的Result
		 */
		boolean isResult() {
			return type == WebViewConfig.TYPE_PROMPT_RESULT;
		}

	}

}
