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

## Enabling the console
JavaFX WebView does not have a console defined, so console calls inside JavaScript are not possible by default. In order to enable JavaScript logging inside JavaFX, a bridge class and a separate listener for JavaScript are required.

#### Bridge class

```java
public class JavaBridge {

    public void log(String text) {
        System.out.println(text);
    }

    public void error(String text) {
        System.err.println(text);
    }
}
```
It is recommended to use `System.out.println` for writing to console and avoid any custom loggers, as they can cause the entire method to be undefined.

#### Code inside Main class
```java
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {

                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    JavaBridge javaBridge = new JavaBridge();
                    window.setMember("console", javaBridge); // "console" object is now known to JavaScript
                }
            }
        });

```
Note that the name *console* in `window.setMember` can be arbitrary, but it is recommended to override the console instead because many JavaScript APIs make hidden calls to the console. Since the console is undefined by default, JavaScript will most likely stop working after that point.

#### JavaScript example

```javascript
function initializeMap() {

    var moscowLonLat = [37.618423, 55.751244];

    map = new ol.Map({
        target: 'map',
        view: new ol.View({
            center: ol.proj.fromLonLat(moscowLonLat),
            zoom: 14
        })
    });
    mapLayer = new ol.layer.Tile({
        source: new ol.source.OSM()
    });

    map.addLayer(mapLayer);
    
    console.log("Map initialized!"); // This will appear in the JavaFX console
}
```
