package com.whl.test.webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.Button;

import com.whl.test.webview.CompatibleWebView.WebChromeClient;
import com.whl.test.webview.CompatibleWebView.WebViewClient;

public class MainActivity extends Activity {
	public final static String TAG = "MainActivity";

	CompatibleWebView webView;
	Button callJS;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (CompatibleWebView) findViewById(R.id.webview);
		callJS = (Button) findViewById(R.id.call_js);
		settingWebView();

		webView.addJavascriptInterface(new JSBridge(), JSBridge.class.getSimpleName());
		webView.loadUrl("file:///android_asset/html/index.html");

		callJS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread() {
					public void run() {
						String script = "javascript:returnBack()";
						String result = new JSExecutor(webView).execute(script).toString();
						Log.i(TAG, "javascript.result: " + result);
					}
				}.start();
			}
		});

	}

	private void settingWebView() {
		WebSettings settings = webView.getSettings();

		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			settings.setDisplayZoomControls(false);
		}

		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		// 客串文件
		settings.setAllowFileAccess(true);
		// 可解析js
		settings.setJavaScriptEnabled(true);
		settings.setPluginState(WebSettings.PluginState.ON);
		settings.setLightTouchEnabled(true);

		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setGeolocationEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setDomStorageEnabled(true);

		webView.setWebViewClient(new WebViewClient(webView));
		webView.setWebChromeClient(new WebChromeClient(webView));
	}

	public static class JSBridge {
		@JavascriptInterface
		public String getName(String params) {
			Log.i(TAG, "params: " + params);

			return "执行成功";
		}
	}
}
