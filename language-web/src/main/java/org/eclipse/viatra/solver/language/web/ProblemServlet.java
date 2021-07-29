/*
 * generated by Xtext 2.25.0
 */
package org.eclipse.viatra.solver.language.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.xtext.util.DisposableRegistry;
import org.eclipse.xtext.web.servlet.XtextServlet;

/**
 * Deploy this class into a servlet container to enable DSL-specific services.
 */
@WebServlet(name = "XtextServices", urlPatterns = "/xtext-service/*")
public class ProblemServlet extends XtextServlet {
	
	private static final long serialVersionUID = 1L;
	
	// Xtext requires a mutable servlet instance field.
	@SuppressWarnings("squid:S2226")
	private DisposableRegistry disposableRegistry;
	
	@Override
	public void init() throws ServletException {
		super.init();
		var injector = new ProblemWebSetup().createInjectorAndDoEMFRegistration();
		this.disposableRegistry = injector.getInstance(DisposableRegistry.class);
	}
	
	@Override
	public void destroy() {
		if (disposableRegistry != null) {
			disposableRegistry.dispose();
			disposableRegistry = null;
		}
		super.destroy();
	}
	
}
