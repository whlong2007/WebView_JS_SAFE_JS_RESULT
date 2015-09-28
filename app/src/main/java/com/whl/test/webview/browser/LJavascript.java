package com.whl.test.webview.browser;

import android.webkit.JavascriptInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为了兼容4.2以下版本没有{@link JavascriptInterface}这个类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LJavascript {
    /**
     * 方法名称，默认为定义方法的名称
     */
    String action() default "";

    /**
     * 是否需要验证
     */
    boolean validate() default true;

    /**
     * 是否会有回调
     */
    boolean callback() default true;


}
