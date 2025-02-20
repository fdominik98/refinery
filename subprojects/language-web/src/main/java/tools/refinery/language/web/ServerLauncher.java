/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/*
 * generated by Xtext 2.25.0
 */
package tools.refinery.language.web;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import tools.refinery.language.web.api.ConcretizeApi;
import tools.refinery.language.web.api.GenerateApi;
import tools.refinery.language.web.api.SemanticsApi;
import tools.refinery.language.web.api.provider.ConstraintViolationExceptionMapperProvider;
import tools.refinery.language.web.api.provider.RefineryResponseFilter;
import tools.refinery.language.web.api.provider.ServerExceptionMapperProvider;
import tools.refinery.language.web.config.BackendConfigServlet;
import tools.refinery.language.web.gson.GsonJerseyProvider;
import tools.refinery.language.web.xtext.servlet.XtextWebSocketServlet;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Map;

public class ServerLauncher {
	public static final String DEFAULT_LISTEN_HOST = "localhost";

	public static final int DEFAULT_LISTEN_PORT = 1312;

	public static final int DEFAULT_PUBLIC_PORT = 443;

	public static final int HTTP_DEFAULT_PORT = 80;

	public static final int HTTPS_DEFAULT_PORT = 443;

	public static final String ALLOWED_ORIGINS_SEPARATOR = ",";

	private static final Logger LOG = LoggerFactory.getLogger(ServerLauncher.class);

	// Register Xtext services as soon as this class is instantiated.
	private final ProblemInjectorHolder injectorHolder = new ProblemInjectorHolder();

	private final Server server;

	public ServerLauncher(InetSocketAddress bindAddress, String[] allowedOrigins, String webSocketUrl) {
		server = new Server(bindAddress);
		((QueuedThreadPool) server.getThreadPool()).setName("jetty");
		var handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		handler.setContextPath("/");
		addProblemServlet(handler, allowedOrigins);
		addApiServlet(handler);
		addBackendConfigServlet(handler, webSocketUrl);
		addHealthCheckServlet(handler);
		var baseResource = getBaseResource();
		if (baseResource != null) {
			handler.setBaseResource(baseResource);
			handler.setWelcomeFiles(new String[]{"index.html"});
			addDefaultServlet(handler);
		}
		handler.addFilter(CacheControlFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		handler.addFilter(SecurityHeadersFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		server.setHandler(handler);
	}

	private void addProblemServlet(ServletContextHandler handler, String[] allowedOrigins) {
		var problemServletHolder = new ServletHolder(XtextWebSocketServlet.class);
		if (allowedOrigins == null) {
			LOG.warn("All WebSocket origins are allowed! This setting should not be used in production!");
		} else {
			var allowedOriginsString = String.join(XtextWebSocketServlet.ALLOWED_ORIGINS_SEPARATOR,
					allowedOrigins);
			problemServletHolder.setInitParameter(XtextWebSocketServlet.ALLOWED_ORIGINS_INIT_PARAM,
					allowedOriginsString);
		}
		handler.addServlet(problemServletHolder, "/xtext-service");
		JettyWebSocketServletContainerInitializer.configure(handler, null);
	}

	private void addApiServlet(ServletContextHandler handler) {
		var resourceConfig = new ResourceConfig();
		resourceConfig.setProperties(Map.of(
				// See https://stackoverflow.com/a/69986144
				ServerProperties.WADL_FEATURE_DISABLE, "true",
				// See https://stackoverflow.com/a/34358215
				ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, "0"
		));
		// See https://stackoverflow.com/a/46840247
		// We'll have to use {@code jakarta.inject.Inject} instead of {@code com.google.inject.Inject} to inject
		// Guice dependencies into beans instantiated by HK2.
		resourceConfig.register(new AbstractContainerLifecycleListener() {
			@Override
			public void onStartup(Container container) {
				var servletContainer = (ServletContainer) container;
				var injectionManager = servletContainer.getApplicationHandler().getInjectionManager();
				var serviceLocator = injectionManager.getInstance(ServiceLocator.class);
				GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
				var guiceIntoHK2Bridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
				guiceIntoHK2Bridge.bridgeGuiceInjector(injectorHolder.getInjector());
			}
		});
		resourceConfig.register(GsonJerseyProvider.class);
		resourceConfig.register(RefineryResponseFilter.class);
		resourceConfig.register(ServerExceptionMapperProvider.class);
		resourceConfig.register(ConstraintViolationExceptionMapperProvider.class);
		resourceConfig.register(ConcretizeApi.class);
		resourceConfig.register(GenerateApi.class);
		resourceConfig.register(SemanticsApi.class);
		var apiServletHolder = new ServletHolder(new ServletContainer(resourceConfig));
		handler.addServlet(apiServletHolder, "/api/*");
	}

	private void addBackendConfigServlet(ServletContextHandler handler, String webSocketUrl) {
		var backendConfigServletHolder = new ServletHolder(BackendConfigServlet.class);
		backendConfigServletHolder.setInitParameter(BackendConfigServlet.WEBSOCKET_URL_INIT_PARAM, webSocketUrl);
		handler.addServlet(backendConfigServletHolder, "/config.json");
	}

	private void addHealthCheckServlet(ServletContextHandler handler) {
		var healthCheckServletHolder = new ServletHolder(HealthCheckServlet.class);
		handler.addServlet(healthCheckServletHolder, "/health");
	}

	private void addDefaultServlet(ServletContextHandler handler) {
		var defaultServletHolder = new ServletHolder(DefaultServlet.class);
		var isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		// Avoid file locking on Windows: https://stackoverflow.com/a/4985717
		// See also the related Jetty ticket:
		// https://github.com/eclipse/jetty.project/issues/2925
		defaultServletHolder.setInitParameter("useFileMappedBuffer", isWindows ? "false" : "true");
		defaultServletHolder.setInitParameter("precompressed", "br=.br,gzip=.gz");
		handler.addServlet(defaultServletHolder, "/");
	}

	private Resource getBaseResource() {
		var factory = ResourceFactory.of(server);
		var baseResourceOverride = System.getenv("REFINERY_BASE_RESOURCE");
		if (baseResourceOverride != null) {
			// If a user override is provided, use it.
			return factory.newResource(baseResourceOverride);
		}
		var indexUrlInJar = ServerLauncher.class.getResource("/webapp/index.html");
		if (indexUrlInJar != null) {
			// If the app is packaged in the jar, serve it.
			URI webRootUri;
			try {
				webRootUri = URI.create(indexUrlInJar.toURI().toASCIIString()
						.replaceFirst("/index.html$", "/")
						// Enable running without warnings from a jar.
						.replaceFirst("^jar:file:", "jar:file://"));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Jar has invalid base resource URI", e);
			}
			return factory.newResource(webRootUri);
		}
		// Look for unpacked production artifacts (convenience for running from IDE).
		var unpackedResourcePathComponents = new String[]{System.getProperty("user.dir"), "build", "webpack",
				"production"};
		var unpackedResourceDir = new File(String.join(File.separator, unpackedResourcePathComponents));
		if (unpackedResourceDir.isDirectory()) {
			return factory.newResource(unpackedResourceDir.toPath());
		}
		// Fall back to just serving a 404.
		return null;
	}

	public void start() throws Exception {
		try {
			server.start();
			LOG.info("Server started on {}", server.getURI());
			server.join();
		} finally {
			injectorHolder.dispose();
		}
	}

	public static void main(String[] args) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		try {
			var bindAddress = getBindAddress();
			var allowedOrigins = getAllowedOrigins();
			var webSocketUrl = getWebSocketUrl();
			var serverLauncher = new ServerLauncher(bindAddress, allowedOrigins, webSocketUrl);
			serverLauncher.start();
		} catch (Exception exception) {
			LOG.error("Fatal server error", exception);
			System.exit(1);
		}
	}

	private static String getListenAddress() {
		var listenAddress = System.getenv("REFINERY_LISTEN_HOST");
		if (listenAddress == null) {
			return DEFAULT_LISTEN_HOST;
		}
		return listenAddress;
	}

	private static int getListenPort() {
		var portStr = System.getenv("REFINERY_LISTEN_PORT");
		if (portStr != null) {
			return Integer.parseUnsignedInt(portStr);
		}
		return DEFAULT_LISTEN_PORT;
	}

	private static InetSocketAddress getBindAddress() {
		var listenAddress = getListenAddress();
		var listenPort = getListenPort();
		return new InetSocketAddress(listenAddress, listenPort);
	}

	private static String getPublicHost() {
		var publicHost = System.getenv("REFINERY_PUBLIC_HOST");
		if (publicHost != null) {
			return publicHost.toLowerCase();
		}
		return null;
	}

	private static int getPublicPort() {
		var portStr = System.getenv("REFINERY_PUBLIC_PORT");
		if (portStr != null) {
			return Integer.parseUnsignedInt(portStr);
		}
		return DEFAULT_PUBLIC_PORT;
	}

	private static String[] getAllowedOrigins() {
		var allowedOrigins = System.getenv("REFINERY_ALLOWED_ORIGINS");
		if (allowedOrigins != null) {
			return allowedOrigins.split(ALLOWED_ORIGINS_SEPARATOR);
		}
		return getAllowedOriginsFromPublicHostAndPort();
	}

	// This method returns <code>null</code> to indicate that all origins are allowed.
	@SuppressWarnings("squid:S1168")
	private static String[] getAllowedOriginsFromPublicHostAndPort() {
		var publicHost = getPublicHost();
		if (publicHost == null) {
			return null;
		}
		int publicPort = getPublicPort();
		var scheme = publicPort == HTTPS_DEFAULT_PORT ? "https" : "http";
		var urlWithPort = String.format("%s://%s:%d", scheme, publicHost, publicPort);
		if (publicPort == HTTPS_DEFAULT_PORT || publicPort == HTTP_DEFAULT_PORT) {
			var urlWithoutPort = String.format("%s://%s", scheme, publicHost);
			return new String[]{urlWithPort, urlWithoutPort};
		}
		return new String[]{urlWithPort};
	}

	private static String getWebSocketUrl() {
		String host;
		int port;
		var publicHost = getPublicHost();
		if (publicHost == null) {
			return null;
		}
		host = publicHost;
		port = getPublicPort();
		var scheme = port == HTTPS_DEFAULT_PORT ? "wss" : "ws";
		return String.format("%s://%s:%d/xtext-service", scheme, host, port);
	}
}
