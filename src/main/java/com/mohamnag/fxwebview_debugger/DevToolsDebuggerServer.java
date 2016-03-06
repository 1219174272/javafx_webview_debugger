package com.mohamnag.fxwebview_debugger;

import com.sun.javafx.scene.web.Debugger;
import javafx.application.Platform;
import javafx.util.Callback;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletContext;
import java.io.IOException;

public class DevToolsDebuggerServer {
	private static ServletContextHandler contextHandler;
	private static Debugger debugger;
	private static Server server;

	public static void startDebugServer(Debugger debugger) throws Exception {

		int debuggerPort = 51742;
		server = new Server(debuggerPort);

		debugger.setEnabled(true);
		debugger.sendMessage("{\"id\" : -1, \"method\" : \"Network.enable\"}"); 

		contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		contextHandler.setContextPath("/"); 

		ServletHolder devToolsHolder = new ServletHolder(new DevToolsWebSocketServlet());
		contextHandler.addServlet(devToolsHolder, "/devtools/page/1"); 

		server.setHandler(contextHandler);
		server.start();

		DevToolsDebuggerServer.debugger = debugger;
		debugger.setMessageCallback(new Callback<String, Void>() {
			@Override
			public Void call(String data) {
				DevToolsWebSocket mainSocket = (DevToolsWebSocket) contextHandler.getServletContext()
						.getAttribute(DevToolsWebSocket.WEB_SOCKET_ATTR_NAME);
				if (mainSocket != null) {
					try {
						mainSocket.sendMessage(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
		
		String remoteUrl = "chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/devtools/page/1"; 
		System.out.println("To debug open chrome and load next url: " + remoteUrl);
	}

	public static void stopDebugServer() throws Exception {
		if (server != null) {
			server.stop();
			server.join();
		}
	}

	public static void sendMessageToBrowser(final String data) {
		Platform.runLater(new Runnable() {// Display.asyncExec won't be
											// successful here
			@Override
			public void run() {
				debugger.sendMessage(data);
			}
		});
	}

	public static String getServerState() {
		return server == null ? null : server.getState();
	}

	public static ServletContext getServletContext() {
		return (contextHandler != null) ? contextHandler.getServletContext() : null;
	}
}