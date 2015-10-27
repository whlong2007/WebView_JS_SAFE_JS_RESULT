package com.whl.test.webview.browser;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * 实现上传文件的ChromeClient
 * Created by Allen on 2015/10/27.
 */
public class UploadChromeClient extends WebChromeClient implements LFileChooserListener {
    private UploadLifecycle mLifecycle;
    private int mUploadReqCode;
    private String mUploadTitle;

    public UploadChromeClient(UploadLifecycle lifecycle, int uploadReqCode, String uploadTitle) {
        this.mLifecycle = lifecycle;
        this.mUploadReqCode = uploadReqCode;
        this.mUploadTitle = uploadTitle;
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
        mLifecycle.<Uri>newUploadHandler(mUploadReqCode, mUploadTitle).openFileChooser(uploadFile, acceptType);
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        mLifecycle.<Uri>newUploadHandler(mUploadReqCode, mUploadTitle).openFileChooser(uploadFile, acceptType, capture);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mLifecycle.<Uri[]>newUploadHandler(mUploadReqCode, mUploadTitle).openFileChooser(filePathCallback, fileChooserParams);
        return true;
    }
}
