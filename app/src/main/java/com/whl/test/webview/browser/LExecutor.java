package com.whl.test.webview.browser;

import android.text.TextUtils;
import android.webkit.JsResult;
import android.webkit.WebView;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 接口对象的添加和执行
 * Created by 1 on 2015/9/10.
 */
abstract class LExecutor {
    private final static String TAG = "JSExecutor";

    protected LProvider mYKJSProvider;
    private final Map<String, Map<String, Object>> mObjectMap;
    private final Map<String, Map<String, Method>> mMethodMap;

    LExecutor(LProvider provider) {
        this.mYKJSProvider = provider;
        this.mObjectMap = new HashMap<String, Map<String, Object>>();
        this.mMethodMap = new HashMap<String, Map<String, Method>>();
    }

    /**
     * 方法的参数类型是否有效
     */
    protected boolean isParameterTypesValid(Class<?>[] paramTypes) {
        return true;
    }

    /**
     * 方法的返回类型是否有效
     */
    protected boolean isReturnTypeValid(Class<?> ReturnType) {
        return true;
    }

    protected abstract Object decodeParams(String params);

    /**
     * 将方法的执行结果转换为JSON或者bool
     */
    protected abstract Object encodeResult(Object obj);

    /**
     * 调用结束返回给JS
     *
     * @param object 这里用的json和bool,其它的形式需要加解析
     */
    protected abstract void onJsResult(Object object, JsResult jsResult);

    /**
     * invoke method
     */
    protected abstract Object invoke(Method methodImp, Object objImp, final WebView view, String url, final LMessage msg) throws Exception;

    /**
     * 执行方法
     */
    public void execute(final WebView view, String url, final LMessage msg, JsResult result) {
        if (isMethodActive(msg.obj, msg.action)) {

            Method methodImp = getMethod(msg.obj, msg.action);
            Object objImp = getObject(msg.obj, msg.action);

            try {
                Object execObject = invoke(methodImp, objImp, view, url, msg);
                onJsResult(encodeResult(execObject), result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return 方法是否可用
     */
    protected boolean isMethodActive(String obj, String method) {
        Map<String, Method> methodMap = mMethodMap.get(obj);
        if (methodMap != null) {
            Method methodImp = methodMap.get(method);

            if (methodImp != null) {
                Map<String, Object> objectMap = mObjectMap.get(obj);
                return objectMap != null && objectMap.get(methodImp.getDeclaringClass().getName()) != null;
            }
        }

        return false;
    }

    /**
     * 添加接口
     *
     * @param name 添加到哪个接口集合里,请使用 yk.main 形式来表示name对象,yk是主干,请以 yk 为top object
     * @param obj  接口对象,方法必须使用JavascriptInterface注解,并且参数是JSON类型的String,返回值也是JSON类型的String
     */
    void add(String name, Object obj) {
        WebConsoleLog.DEBUG(TAG, "add: " + name + "@" + obj);

        addObject(name, obj);

        Method[] methods = obj.getClass().getDeclaredMethods();
        if (methods != null) {
            for (Method method : methods) {
                LJavascript anno = method.getAnnotation(LJavascript.class);
                if (anno != null) {

                    Class<?>[] paramTypes = method.getParameterTypes();
                    Class<?> returnType = method.getReturnType();

                    if (!isParameterTypesValid(paramTypes)) {
                        throw new IllegalArgumentException("The ParameterTypes of " + method.getName() + " #" + name + "@" + obj.getClass().getName() + " is invalid.  ");
                    }

                    if (!isReturnTypeValid(returnType)) {
                        throw new IllegalArgumentException("The ReturnType of " + method.getName() + " #" + name + "@" + obj.getClass().getName() + " is invalid.  ");
                    }

                    String methodName = anno.action();
                    methodName = TextUtils.isEmpty(methodName) ? method.getName() : methodName;

                    addMethod(name, methodName, method);
                }
            }
        }
    }

    /**
     * 删除所有接口
     */
    void clear() {
        mObjectMap.clear();
        mMethodMap.clear();
    }

    /**
     * 删除接口集合
     *
     * @param name 接口集合的名称
     */
    void remove(String name) {
        WebConsoleLog.DEBUG(TAG, "remove: " + name);

        mObjectMap.remove(name);
        mMethodMap.remove(name);
    }

    /**
     * 删除name接口集合下的tag分支
     *
     * @param name 接口集合的名称
     * @param tag  接口在集合的Key,目前以类名作为Key
     */
    void remove(String name, String tag) {
        WebConsoleLog.DEBUG(TAG, "remove: " + name + "@" + tag);

        Map<String, Object> objs = mObjectMap.get(name);
        if (objs != null) {
            objs.remove(tag);
        }

        Map<String, Method> methods = mMethodMap.get(name);
        if (methods != null) {
            for (Map.Entry<String, Method> entry : methods.entrySet()) {
                if (entry.getValue().getDeclaringClass().getName().equals(tag)) {
                    methods.remove(entry.getKey());
                }
            }
        }
    }

    /**
     * 将类放到接口的映射表里,映射表根据类名来区分集合里的各个接口
     *
     * @param name 接口集合的名称
     * @param obj  接口
     */
    private void addObject(String name, Object obj) {
        getNonNullCollection(mObjectMap, name).put(obj.getClass().getName(), obj);
    }

    /**
     * Nullable
     * 先根据方法名拿到Active的方法,再根据定义方法的类名来获取对象
     */
    private Object getObject(String name, String method) {
        Method methodImp = getMethod(name, method);

        if (methodImp != null) {
            Map<String, Object> objectMap = mObjectMap.get(name);

            if (objectMap != null) {
                return objectMap.get(methodImp.getDeclaringClass().getName());
            }
        }

        return null;
    }

    /**
     * 添加到总的Method里
     */
    private void addMethod(String obj, String name, Method method) {
        WebConsoleLog.DEBUG(TAG, "addMethod: " + obj + "." + name + " >> " + method.getDeclaringClass().getName() + "." + method.getName());

        Method previous = getNonNullCollection(mMethodMap, obj).put(name, method);

        if (previous != null) {
            throw new IllegalArgumentException("The method " + method.getDeclaringClass().getName() + "#" + method.getName() + " of " + obj + " is repeating. previous is " + previous.getDeclaringClass().getName() + "#" + previous.getName());
        }
    }

    /**
     * @return Nullable 有效的Method方法
     */
    private Method getMethod(String name, String method) {
        Map<String, Method> methodMap = mMethodMap.get(name);

        if (methodMap != null) {
            return methodMap.get(method);
        }
        return null;
    }

    /**
     * @return NonNull 子Map
     */
    protected static <T> Map<String, T> getNonNullCollection(Map<String, Map<String, T>> map, String obj) {
        Map<String, T> result = map.get(obj);

        if (result == null) {
            result = new HashMap<String, T>();
            map.put(obj, result);
        }

        return result;
    }

    /**
     * @return 注册的接口实现类
     */
    protected Iterable<Object> getInterfaces() {
        return new Iterable<Object>() {
            @Override
            public Iterator<Object> iterator() {

                return new Iterator<Object>() {
                    Iterator<Map<String, Object>> all = mObjectMap.values().iterator();
                    Iterator<Object> sub;

                    private Iterator<Object> nextSub() {
                        if ((sub == null || !sub.hasNext()) && all.hasNext()) {
                            sub = all.next().values().iterator();
                            if (!sub.hasNext()) {
                                nextSub();
                            }
                        }
                        return sub;
                    }

                    @Override
                    public boolean hasNext() {
                        return nextSub() != null && sub.hasNext();
                    }

                    @Override
                    public Object next() {
                        return nextSub() != null ? sub.next() : null;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
    }

    /**
     * @return NonNull 注册的接口实现的方法
     */
    protected Map<String, Map<String, Method>> getMethods() {
        return mMethodMap;
    }
}
