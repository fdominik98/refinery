/*
 * generated by Xtext 2.25.0
 */
package org.eclipse.viatra.solver.language.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class ServerLauncher {
	private static final Slf4jLog LOG = new Slf4jLog(ServerLauncher.class.getName());

	private final Server server;

	public ServerLauncher(InetSocketAddress bindAddress, Resource baseResource) {
		server = new Server(bindAddress);
		var ctx = new WebAppContext();
		ctx.setBaseResource(baseResource);
		ctx.setWelcomeFiles(new String[] { "index.html" });
		ctx.setContextPath("/");
		ctx.setConfigurations(new Configuration[] { new AnnotationConfiguration(), new WebXmlConfiguration(),
				new WebInfConfiguration(), new MetaInfConfiguration() });
		ctx.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*/build/classes/.*,.*\\.jar");
		ctx.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		server.setHandler(ctx);
	}

	public void start() throws Exception {
		server.start();
		LOG.info("Server started " + server.getURI() + "...");
		LOG.info("Press enter to stop the server...");
		int key = System.in.read();
		if (key != -1) {
			server.stop();
		} else {
			LOG.warn(
					"Console input is not available. In order to stop the server, you need to cancel process manually.");
		}
	}

	private static InetSocketAddress getBindAddress(String listenAddress, int port) {
		if (listenAddress == null) {
			return new InetSocketAddress(port);
		}
		return new InetSocketAddress(listenAddress, port);
	}

	private static Resource getBaseResource(String baseResourceOverride) throws IOException, URISyntaxException {
		if (baseResourceOverride != null) {
			return Resource.newResource(baseResourceOverride);
		}
		var indexUrlInJar = ServerLauncher.class.getResource("/webapp/index.html");
		if (indexUrlInJar == null) {
			throw new IOException("Cannot find pacakged web assets");
		}
		var webRootUri = URI.create(indexUrlInJar.toURI().toASCIIString().replaceFirst("/index.html$", "/"));
		return Resource.newResource(webRootUri);
	}

	public static void main(String[] args) {
		var listenAddress = System.getenv("LISTEN_ADDRESS");
		if (listenAddress == null) {
			listenAddress = "localhost";
		}
		var port = 1312;
		var portStr = System.getenv("LISTEN_PORT");
		if (portStr != null) {
			try {
				port = Integer.parseInt(portStr);
			} catch (NumberFormatException e) {
				LOG.warn(e);
				System.exit(1);
			}
		}
		var baseResourceOverride = System.getenv("BASE_RESOURCE");
		try {
			var bindAddress = getBindAddress(listenAddress, port);
			var baseResource = getBaseResource(baseResourceOverride);
			var serverLauncher = new ServerLauncher(bindAddress, baseResource);
			serverLauncher.start();
		} catch (Exception exception) {
			LOG.warn(exception);
			System.exit(1);
		}
	}
}
