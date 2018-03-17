package container.dynamicResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;

import connecter.request.Request;
import connecter.response.Response;
import container.Container;
import container.LoaderFactory;
import container.dynamicResource.dispatcher.DispatcherManager;
import container.dynamicResource.filter.FilterChainFacade;
import container.dynamicResource.filter.MyFilterChain;
import container.dynamicResource.servlet.ServletManager;
import container.session.SessionManager;
import container.staticResource.StaticResourceManager;
import launcher.Configuration;
import util.DeploymentDescriptorParser;

public class DynamicResourceManager implements ServletContext {

	// private ConcurrentHashMap<String, ClassInfo> urlToClass = new
	// ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> initParams;
	private ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<>();

	private MyFilterChain filterChain;

	private ServletManager servletManager;

	private SessionManager sessionManager = new SessionManager(this);

	private DispatcherManager dispatcherManager;

	private PrintStream printStream = System.out;
	private String projectName;
	private URLClassLoader loader;
	private DeploymentDescriptorParser deploymentDescriptorParser;
	
	//以后需要把listener全部放到一个对象中,方便管理
	private List<ServletContextListener> servletContextListeners;
	private List<ServletContextAttributeListener> servletContextAttributeListeners;
	@SuppressWarnings("unused")
	private List<HttpSessionListener> httpSessionListeners;
	@SuppressWarnings("unused")
	private List<HttpSessionActivationListener> httpSessionActivationListeners;
	@SuppressWarnings("unused")
	private List<HttpSessionAttributeListener> httpSessionAttributeListeners;
	@SuppressWarnings("unused")
	private List<ServletRequestListener> servletRequestListeners;
	@SuppressWarnings("unused")
	private List<ServletRequestAttributeListener> servletRequestAttributeListeners;

	public DynamicResourceManager(String projectName) {
		this.projectName = projectName;
		try {
			this.loader = LoaderFactory.getURLClassLoader(projectName);

			String configPath = Configuration.WEBAPPS + projectName + "/WEB-INF/web.xml";
			File config = new File(configPath);
			deploymentDescriptorParser = new DeploymentDescriptorParser(this, config, loader);

			deploymentDescriptorParser.parse();

			initParams = deploymentDescriptorParser.loadAndGetInitParams();

			loadListeners();

			Thread.currentThread().setContextClassLoader(loader);

			// 通知ServletContextListener,ServletContext已创建
			ServletContextEvent event = new ServletContextEvent(this);
			for (ServletContextListener listener : servletContextListeners) {
				listener.contextInitialized(event);
			}

			loadFilters();

			loadServlets();

			dispatcherManager = new DispatcherManager(this);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public MyFilterChain getFilterChain() {
		return filterChain;
	}

	private void loadListeners() throws Exception {
		System.out.println("加载listeners");
		deploymentDescriptorParser.loadListeners();

		servletContextListeners = deploymentDescriptorParser.getServletContextListeners();
		servletContextAttributeListeners = deploymentDescriptorParser.getServletContextAttributeListeners();
		httpSessionListeners = deploymentDescriptorParser.getHttpSessionListeners();
		httpSessionActivationListeners = deploymentDescriptorParser.getHttpSessionActivationListeners();
		httpSessionAttributeListeners = deploymentDescriptorParser.getHttpSessionAttributeListeners();
		servletRequestListeners = deploymentDescriptorParser.getServletRequestListeners();
		servletRequestAttributeListeners = deploymentDescriptorParser.getServletRequestAttributeListeners();
	}

	private void loadFilters() throws Exception {
		deploymentDescriptorParser.loadFilters();
		filterChain = deploymentDescriptorParser.getFilters();
	}

	private void loadServlets() throws Exception {
		deploymentDescriptorParser.loadServlets();
		servletManager = deploymentDescriptorParser.getServletManager();
	}

	// public HttpServlet getInstance(String url) {
	// ClassInfo classInfo = urlToClass.get(url);
	// HttpServlet httpServlet = null;
	// Object obj;
	// if (classInfo != null) {
	// obj = classInfo.getInstance();
	// httpServlet = (HttpServlet) obj;
	// if (obj == null) {
	// String qualifiedName = classInfo.getQualifiedName();
	// obj = loadAndNewInstance(qualifiedName);
	// classInfo.setInstance(obj);
	// httpServlet = (HttpServlet) obj;
	// try {
	// httpServlet.init();
	// } catch (ServletException e) {
	// e.printStackTrace();
	// }
	// }
	// return httpServlet;
	// } else {
	// return null;
	// }
	// }

	@SuppressWarnings("unused")
	private Object loadAndNewInstance(String qualifiedName) {
		Class<?> c = null;
		try {
			c = loader.loadClass(qualifiedName);
			Object obj = c.newInstance();
			return obj;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getContextPath() {
		return "/" + this.projectName;
	}

	@Override
	public ServletContext getContext(String uripath) {
		int secondSlash = uripath.indexOf('/', 1);
		String projectName = null;
		if (secondSlash == -1) {
			projectName = uripath.substring(1);
		} else {
			projectName = uripath.substring(1, secondSlash);
		}
		return Container.getDynamicResourceManager(projectName);
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public String getMimeType(String file) {
		return StaticResourceManager.getmimeMap().get(file);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		System.out.println("getResourcePaths: " + path);
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		System.out.println("getResource: " + path);
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return loader.findResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		try {
			URL resource = getResource(path);
			if (resource != null) {
				return resource.openStream();
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		System.out.println("getRequestDispatcher: " + path);
		return dispatcherManager.getRequestDispatcher(path);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		System.out.println("getNamedDispatcher: " + name);
		return dispatcherManager.getNamedDispatcher(name);
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		System.out.println("getServlet: " + name);
		return servletManager.getName2ServletMap().get(name);
	}

	@Override
	public Enumeration<?> getServlets() {
		System.out.println("getServlets");
		return servletManager.getName2ServletMap().elements();
	}

	@Override
	public Enumeration<String> getServletNames() {
		return servletManager.getName2ServletMap().keys();
	}

	@Override
	public void log(String msg) {
		printStream.println("Log: " + msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		log(msg, exception);
	}

	@Override
	public void log(String message, Throwable throwable) {
		printStream.println("Log: " + message);
		throwable.printStackTrace(printStream);
	}

	@Override
	public String getRealPath(String path) {
		File file = new File(Configuration.WEBAPPS + projectName + path);
		if (file.exists()) {
			return file.getAbsolutePath();
		}
		return null;
	}

	@Override
	public String getServerInfo() {
		return "Smile 1.1";
	}

	@Override
	public String getInitParameter(String name) {
		System.out.println("getInitParameter: name= " + name + ", value= " + initParams.get(name));
		return initParams.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return initParams.keys();
	}

	@Override
	public Object getAttribute(String name) {
		return attrs.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return attrs.keys();
	}

	@Override
	public void setAttribute(String name, Object object) {
		Object val = attrs.put(name, object);
		ServletContextAttributeEvent event = new ServletContextAttributeEvent(this, name, object);
		if (val == null) {
			for (ServletContextAttributeListener listener : servletContextAttributeListeners) {
				listener.attributeAdded(event);
			}
		} else {
			for (ServletContextAttributeListener listener : servletContextAttributeListeners) {
				listener.attributeReplaced(event);
			}
		}
	}

	@Override
	public void removeAttribute(String name) {
		Object remove = attrs.remove(name);
		if (remove != null) {
			ServletContextAttributeEvent event = new ServletContextAttributeEvent(this, name, remove);
			for (ServletContextAttributeListener listener : servletContextAttributeListeners) {
				listener.attributeRemoved(event);
			}
		}
	}

	@Override
	public String getServletContextName() {
		return this.projectName;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public ServletManager getServletManager() {
		return servletManager;
	}

	/**
	 * @param request
	 * @param response
	 */
	public void handle(Request request, Response response) {
		Thread.currentThread().setContextClassLoader(loader);

		FilterChainFacade chain = new FilterChainFacade(filterChain, servletManager);
		try {
			chain.doFilter(request, response);
		} catch (IOException | ServletException e) {
			throw new RuntimeException(e);
		}
	}
}
