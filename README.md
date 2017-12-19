# JavaFX WebView debugger
Based on the solution found by Bosko Popovic found under [this question](http://stackoverflow.com/questions/9398879/html-javascript-debugging-in-javafx-webview/34444807#34444807).

Using debugger is done in three main steps:
 1. Starting debug server
 1. Connecting chrome debugger 
 1. Clean up

### Starting debug server
If you are using Java up to version 8, to enable debugging on a chosen WebView, you have to add following code using its `webEngine`:
```java
DevToolsDebuggerServer.startDebugServer(webEngine.impl_getDebugger(), 51742);
```

`WebEngine.impl_getDebugger()` is an internal API and is subject to change which is happened in Java 9. So if you are using Java 9, you need to use following code instead to start the debug server:

```java
Class webEngineClazz = WebEngine.class;

Field debuggerField = webEngineClazz.getDeclaredField("debugger");
debuggerField.setAccessible(true);

Debugger debugger = (Debugger) debuggerField.get(webView.getEngine());
DevToolsDebuggerServer.startDebugServer(debugger, 51742);
```

For this to work, you have to pass this parameter to Java compiler: `--add-exports javafx.web/com.sun.javafx.scene.web=ALL-UNNAMED`. 

As examples, this can be done for Maven as follows:
```xml
       <plugin>
           <groupId>org.apache.maven.plugins</groupId>
           <artifactId>maven-compiler-plugin</artifactId>
           <version>3.7.0</version>
           <configuration>
               <source>9</source>
               <target>9</target>
               <compilerArgs>
                   <arg>--add-exports</arg>
                   <arg>javafx.web/com.sun.javafx.scene.web=ALL-UNNAMED</arg>
               </compilerArgs>
           </configuration>
        </plugin>
```

or for IntelliJ under **Additional command line parameters** in **Preferences > Build, Execution, Deployment > Compiler > Java Compiler**.

### Connecting chrome debugger
Then you only need to open following URL in a chrome browser:
```
chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/
```

### Clean up
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
