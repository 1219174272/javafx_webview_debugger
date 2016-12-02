# JavaFX WebView debugger
Based on the solution found by Bosko Popovic found under [this question](http://stackoverflow.com/questions/9398879/html-javascript-debugging-in-javafx-webview/34444807#34444807).

To enable debugging on a chosen WebView, you have to add following code using its `webEngine`:
```java
DevToolsDebuggerServer.startDebugServer(webEngine.impl_getDebugger(), 51742);
```

Then you only need to open following URL in a chrome browser:
```
chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/
```

For a proper shutdown you have to call following when exiting to stop in background running server:
```java
DevToolsDebuggerServer.stopDebugServer();
```

## Installing the library
In addition to the possibility of downloading and importing the code, one can use following maven dependency to install this library:

```
repositories {
			maven { url 'https://jitpack.io' }
}

dependencies {
      compile 'com.github.mohamnag:javafx_webview_debugger:-SNAPSHOT'
}
```

Above snippet is the gradle version but you can find relevant snippets for maven, ant and ... here in (jitpack)[https://jitpack.io/#mohamnag/javafx_webview_debugger].
