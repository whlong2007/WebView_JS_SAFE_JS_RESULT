package com.whl.test.webview.browser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 上传文件总的类
 * Created by Allen on 2015/10/12.
 */
public abstract class UploadHandler<T> {

    private final static String IMAGE_MIME_TYPE = "image/*";
    private final static String VIDEO_MIME_TYPE = "video/*";
    private final static String AUDIO_MIME_TYPE = "audio/*";

    private static final String MEDIA_SOURCE_KEY = "capture";

    private static final String MEDIA_SOURCE_VALUE_CAMERA = "camera";
    private static final String MEDIA_SOURCE_VALUE_FILE_SYSTEM = "filesystem";
    private static final String MEDIA_SOURCE_VALUE_CAMCORDER = "camcorder";
    private static final String MEDIA_SOURCE_VALUE_MICROPHONE = "microphone";

    private Activity mActivity;
    private Fragment mFragment;

    private int mRequestCode;
    private String mTitle;

    private ValueCallback<T> mUploadMessage;
    private FileChooserParams mParams;
    private boolean mHandled;

    private String mCameraFilePath;

    public UploadHandler(Activity activity, int requestCode, String title) {
        mActivity = activity;
        mRequestCode = requestCode;
        mTitle = title;
    }

    public UploadHandler(Fragment fragment, int requestCode, String title) {
        mFragment = fragment;
        mRequestCode = requestCode;
        mTitle = title;
    }

    public Context getContext() {
        Context context = null;
        if (mActivity != null) {
            context = mActivity;
        } else if (mFragment != null) {
            context = mFragment.getActivity();
        }
        if (context == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        return context;
    }

    public void openFileChooser(ValueCallback<T> uploadMsg, String acceptType, String capture) {
        openFileChooser(uploadMsg, generateParams(acceptType, capture));
    }

    public void openFileChooser(ValueCallback<T> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, generateParams(acceptType));
    }

    public void openFileChooser(ValueCallback<T> callback, WebChromeClient.FileChooserParams params) {
        openFileChooser(callback, generateParams(params));
    }

    public void onResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == mRequestCode) {
            // As the media capture is always supported, we can't use
            // FileChooserParams.parseResult().
            Object uris = parseResult(resultCode, intent);

            // mUploadMessage.onReceiveValue(uris);
            try {
                Method method = ValueCallback.class.getDeclaredMethod("onReceiveValue", Object.class);
                method.invoke(mUploadMessage, uris);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mHandled = true;
        }
    }

    private void openFileChooser(ValueCallback<T> uploadMsg, FileChooserParams params) {
        if (mUploadMessage != null) {
            // Already a file picker operation in progress.
            return;
        }

        this.mUploadMessage = uploadMsg;
        this.mParams = params;

        //Ensure it is not still set from a previous upload.
        mCameraFilePath = null;

        Intent[] captureIntents = createCaptureIntent();
        Intent intent = null;
        // Go to the media capture directly if capture is specified, this is the
        // preferred way.
        if (params.isCaptureEnabled() && captureIntents.length == 1) {
            intent = captureIntents[0];
        } else {
            intent = new Intent(Intent.ACTION_CHOOSER);
            intent.putExtra(Intent.EXTRA_TITLE, mTitle);
            intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, captureIntents);
            intent.putExtra(Intent.EXTRA_INTENT, params.createIntent());
        }
        startActivity(intent);
    }

    private Object parseResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return null;
        }
        Uri uri = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
        // As we ask the camera to save the result of the user taking
        // a picture, the camera application does not return anything other
        // than RESULT_OK. So we need to check whether the file we expected
        // was written to disk in the in the case that we
        // did not get an intent returned but did get a RESULT_OK. If it was,
        // we assume that this result has came back from the camera.
        if (uri == null && intent == null && resultCode == Activity.RESULT_OK && mCameraFilePath != null) {
            uri = Uri.fromFile(new File(mCameraFilePath));
        }

        Object result = null;

        if (isUriArray()) {
            if (uri != null) {
                Uri[] uris = new Uri[1];
                uris[0] = uri;
                result = uris;
            }
        } else {
            result = uri;
        }

        return result;
    }

    /**
     * @return 是否返回URI数组
     */
    private boolean isUriArray() {
        boolean result = false;

        Type clazz = getClass().getGenericSuperclass();
        if (clazz instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) clazz;
            Type[] argsTypes = parameterized.getActualTypeArguments();

            if (argsTypes != null && argsTypes.length > 0) {
                result = argsTypes[0] instanceof GenericArrayType;
            }
        }

        return result;
    }

    private Intent[] createCaptureIntent() {
        String mimeType = "*/*";
        String[] acceptTypes = mParams.getAcceptTypes();
        if (acceptTypes != null && acceptTypes.length > 0) {
            mimeType = acceptTypes[0];
        }
        Intent[] intents;
        if (IMAGE_MIME_TYPE.equals(mimeType)) {
            intents = new Intent[1];
            intents[0] = createCameraIntent();
        } else if (VIDEO_MIME_TYPE.equals(mimeType)) {
            intents = new Intent[1];
            intents[0] = createCamcorderIntent();
        } else if (AUDIO_MIME_TYPE.equals(mimeType)) {
            intents = new Intent[1];
            intents[0] = createSoundRecorderIntent();
        } else {
            intents = new Intent[3];
            intents[0] = createCameraIntent();
            intents[1] = createCamcorderIntent();
            intents[2] = createSoundRecorderIntent();
        }
        return intents;
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() + File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

    private Intent createCamcorderIntent() {
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }

    private Intent createSoundRecorderIntent() {
        return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }

    private void startActivity(Intent intent) {
        try {
            if (mFragment != null) {
                mFragment.startActivityForResult(intent, mRequestCode);
            } else {
                mActivity.startActivityForResult(intent, mRequestCode);
            }


        } catch (ActivityNotFoundException e) {
            // No installed app was able to handle the intent that
            // we sent, so file upload is effectively disabled.
            Toast.makeText(getContext(), "上传失败", Toast.LENGTH_LONG).show();
        }
    }

    FileChooserParams generateParams(final String acceptType) {
        return generateParams(acceptType, null);
    }

    FileChooserParams generateParams(final String acceptType, final String capture) {
        return new FileChooserParams() {
            String mimeType;
            boolean isCaptureEnabled;

            {
                if (!TextUtils.isEmpty(acceptType)) {
                    String params[] = acceptType.split(";");
                    mimeType = params[0];
                    String mediaSource = null;

                    if (!TextUtils.isEmpty(capture)) {//Build.VERSION_CODES.JELLY_BEAN
                        mediaSource = capture;
                    } else {
                        for (String p : params) {
                            String[] keyValue = p.split("=");
                            if (keyValue.length == 2) {
                                // Process key=value parameters.
                                if (MEDIA_SOURCE_KEY.equals(keyValue[0])) {
                                    mediaSource = keyValue[1];
                                }
                            }
                        }
                    }

                    isCaptureEnabled = "*".equals(mediaSource) || IMAGE_MIME_TYPE.equals(mimeType) && MEDIA_SOURCE_VALUE_CAMERA.equals(mediaSource) || ((VIDEO_MIME_TYPE.equals(mimeType) && MEDIA_SOURCE_VALUE_CAMCORDER.equals(mediaSource))) || ((AUDIO_MIME_TYPE.equals(mimeType) && MEDIA_SOURCE_VALUE_MICROPHONE.equals(mediaSource)));
                }
            }

            @Override
            boolean isCaptureEnabled() {
                return isCaptureEnabled;
            }

            @Override
            String[] getAcceptTypes() {
                return new String[]{mimeType};
            }

            @Override
            Intent createIntent() {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(TextUtils.isEmpty(this.mimeType) ? "*/*" : this.mimeType);
                return i;
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    FileChooserParams generateParams(final WebChromeClient.FileChooserParams params) {
        return new FileChooserParams() {
            @Override
            boolean isCaptureEnabled() {
                return params.isCaptureEnabled();
            }

            @Override
            String[] getAcceptTypes() {
                return params.getAcceptTypes();
            }

            @Override
            Intent createIntent() {
                return params.createIntent();
            }
        };
    }

    static abstract class FileChooserParams {
        abstract boolean isCaptureEnabled();

        abstract String[] getAcceptTypes();

        abstract Intent createIntent();
    }
}
