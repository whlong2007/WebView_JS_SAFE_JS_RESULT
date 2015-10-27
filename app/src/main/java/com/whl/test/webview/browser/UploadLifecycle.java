package com.whl.test.webview.browser;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

/**
 * UploadHandler与Activity生命周期相关
 * Created by Allen on 2015/10/15.
 */
public class UploadLifecycle extends Fragment {
    private UploadHandler mUploadHandler;

    public UploadLifecycle() {
    }

    public <T> UploadHandler<T> newUploadHandler(int requestCode, String title) {
        UploadHandler<T> handler = new UploadHandler<T>(this, requestCode, title) {
        };
        this.mUploadHandler = handler;
        return handler;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mUploadHandler = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mUploadHandler != null) {
            mUploadHandler.onResult(requestCode, resultCode, data);
        }

        mUploadHandler = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mUploadHandler = null;
    }
}
