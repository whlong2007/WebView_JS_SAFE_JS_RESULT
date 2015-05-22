package com.whl.test.webview;

public class WebViewConfig {
	/**
	 * 用来修复Android4.2版本以下添加JavaScriptInterfaces能够调用Class的漏洞
	 */
	public final static String URL_JS_BUG_FIX = "file:///android_js_bug_fix/bug_fix.js";

	/**
	 * prompt类型为Interface
	 */
	public final static int TYPE_PROMPT_INTERFACE = 1;

	/**
	 * prompt类型为Result
	 */
	public final static int TYPE_PROMPT_RESULT = 2;

	/**
	 * prompt类型Key
	 */
	public final static String KEY_PROMPT_TYPE = "type";

	/**
	 * prompt类型接口名称Key
	 */
	public final static String KEY_PROMPT_INTERFACE_NAME = "name";

	/**
	 * prompt类型接口方法Key
	 */
	public final static String KEY_PROMPT_INTERFACE_METHOD = "method";

	/**
	 * JS代码执行成功
	 */
	public final static int ERROR_JS_EXECUTOR_OK = 0;

	/**
	 * JS代码错误
	 */
	public final static int ERROR_JS_EXECUTOR_JS = -1;

	/**
	 * JS执行TIMEOUT
	 */
	public final static int ERROR_JS_EXECUTOR_TIMEOUT = -2;

	/**
	 * JS执行等待周期
	 */
	public final static int EXECUTOR_JS_PERIOD = 500;
	/**
	 * JS执行TIMEOUT时长
	 */
	public final static int EXECUTOR_JS_TIMEOUT = 10000;

	/**
	 * 是否使用Prompt来实现Interface
	 */
	public final static boolean isUsePrompt() {
		return true;
		// return android.os.Build.VERSION.SDK_INT <
		// Build.VERSION_CODES.JELLY_BEAN_MR1;
	}

}
