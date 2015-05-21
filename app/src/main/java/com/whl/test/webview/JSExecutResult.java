package com.whl.test.webview;

import java.util.concurrent.TimeoutException;

import android.webkit.JavascriptInterface;

public class JSExecutResult {
	private boolean mIsResultReady;
	private String mResult;

	private int mWaitTimes;

	@JavascriptInterface
	public synchronized void confirm(String result) {
		mResult = result;
		notifyResultIsReady();
	}

	protected synchronized void notifyResultIsReady() {
		mIsResultReady = true;
		notify();
	}

	protected synchronized String waitForResult() throws TimeoutException {
		mWaitTimes = 0;
		while (!mIsResultReady) {
			try {
				wait(WebViewConfig.EXECUTOR_JS_PERIOD);
				mWaitTimes++;
			} catch (Exception e) {
				continue;
			}

			if (!mIsResultReady) {
				if (mWaitTimes * WebViewConfig.EXECUTOR_JS_PERIOD >= WebViewConfig.EXECUTOR_JS_TIMEOUT) {
					throw new TimeoutException("Wait timed out");
				}
			}
		}
		mIsResultReady = false;

		return mResult;
	}
}
