package com.whl.test.webview;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Prompt
 */
class JSPromptUtil {

	public final static String TAG = "PromptUtil";

	/**
	 * 生成JavascriptInterface的JS字符串
	 * 
	 * @param domain
	 *            对象,传空默认是var
	 * @param type
	 * @param interfaces
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	final static String generateInterfaceJS(String domain, int type, Map<String, Object> interfaces) {

		if (interfaces == null || interfaces.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (Entry<String, Object> entry : interfaces.entrySet()) {

			if (TextUtils.isEmpty(domain)) {
				sb.append("var ");
			} else {
				sb.append(domain).append(".");
			}

			sb.append(entry.getKey()).append("=");
			sb.append("{");

			if (entry.getValue() != null) {
				try {
					Method[] methodArray = entry.getValue().getClass().getDeclaredMethods();

					for (Method method : methodArray) {

						// 这里一些不适用的访问就不加进来了
						if ((method.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC)) != 0) {
							continue;
						}

						JavascriptInterface anno = method.getAnnotation(JavascriptInterface.class);

						if (anno == null) {
							Log.i(TAG, method.getDeclaringClass().getSimpleName() + "." + method.getName() + " is not a JavascriptInterface Method");
							continue;
						}

						sb.append(method.getName()).append(":function(params)");
						sb.append("{");
						sb.append("return prompt(");
						sb.append("\"").append("{");
						sb.append("'").append(WebViewConfig.KEY_PROMPT_TYPE).append("':").append(type);
						sb.append(",");
						sb.append("'").append(WebViewConfig.KEY_PROMPT_INTERFACE_NAME).append("':'").append(entry.getKey()).append("'");
						sb.append(",");
						sb.append("'").append(WebViewConfig.KEY_PROMPT_INTERFACE_METHOD).append("':'").append(method.getName()).append("'");
						sb.append("}").append("\"");
						sb.append(",").append("params");
						sb.append(");");
						sb.append("}");
						sb.append(",");
					}

					if (sb.lastIndexOf(",") == sb.length() - 1) {
						sb.deleteCharAt(sb.length() - 1);
					}

				} catch (Exception e) {
				}
			}

			sb.append("}");
		}

		return sb.toString();
	}
}
