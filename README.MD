优点:
    1.摈弃JavascriptInterfaces方式,避免高危漏洞执行shell代码.
    2.隐藏Native实现方式,随机生成tag来校验JS调用,增加安全性.
    3.可配置Interface,JS需要配置并且验证才能调用相关Native接口.

缺点:
    1.和JavascriptInterfaces不一样,该JS方法是加载页面过程中注入的,所以需要等JS加载完才能调用.


```Java
//使用前初始化一下LProvider
LProvider mYKJSProvider = LProvider.init(this, mWebView, "MainActivity#WebView", map);

//使用自己的YKWebChromeClient和YKWebViewClient
mWebView.setWebChromeClient(new LWebChromeClient(mYKJSProvider));
mWebView.setWebViewClient(new LWebViewClient(mYKJSProvider));
```
