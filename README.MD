感谢[在WebView中如何让JS与Java安全地互相调用](http://www.pedant.cn/2014/07/04/webview-js-java-interface-research/),[WebView中接口隐患与手机挂马利用](http://drops.wooyun.org/papers/548);

在这两篇文章的基础上,修改了一下JS注入的方式(通过resource引用来);

还添加了JS立即返回功能(参照Android SDK Source里的webviewtest);
后续JS立即返回可以按照SDK 19的WebView.evaluateJavascript()来做,暂时还没定好要不要这么做.
