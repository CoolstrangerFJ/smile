/**
 * 
 */
package util;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import container.dynamicResource.DynamicResourceManager;
import container.dynamicResource.Mapping;
import container.dynamicResource.ResourceInfo;
import container.dynamicResource.filter.MyFilterChain;
import container.dynamicResource.filter.MyFilterConfig;
import container.dynamicResource.servlet.MyServletConfig;
import container.dynamicResource.servlet.ServletManager;

/**
 * @author CoolStranger
 * @date 2018年1月27日
 * @time 下午7:32:54
 *
 */
@SuppressWarnings("unchecked")
public class DeploymentDescriptorParser {

	private DynamicResourceManager dynamicResourceManager;

	private File config;

	private Element root;

	private URLClassLoader loader;

	private ConcurrentHashMap<String, String> initParams;

	private MyFilterChain filterChain;

	private ServletManager servletManager;

	private List<ServletContextListener> servletContextListeners;
	private List<ServletContextAttributeListener> servletContextAttributeListeners;
	private List<HttpSessionListener> httpSessionListeners;
	private List<HttpSessionActivationListener> httpSessionActivationListeners;
	private List<HttpSessionAttributeListener> httpSessionAttributeListeners;
	private List<ServletRequestListener> servletRequestListeners;
	private List<ServletRequestAttributeListener> servletRequestAttributeListeners;

	public DeploymentDescriptorParser() {
	}

	public DeploymentDescriptorParser(DynamicResourceManager servletManager, File config, URLClassLoader loader) {
		this.dynamicResourceManager = servletManager;
		this.config = config;
		this.loader = loader;
	}

	public void parse() throws Exception {
		Document doc = new SAXReader().read(config);
		this.root = doc.getRootElement();
	}

	// ===============================================context-parameters========================================

	public void loadInitParams() {
		initParams = new ConcurrentHashMap<>();
		List<Element> kvList = root.elements("context-param");
		String name = null, value = null;
		for (Element param : kvList) {
			name = param.element("param-name").getTextTrim();
			value = param.element("param-value").getTextTrim();
			this.initParams.put(name, value);
		}
	}

	public ConcurrentHashMap<String, String> getInitParams() {
		return initParams;
	}

	public ConcurrentHashMap<String, String> loadAndGetInitParams() {
		loadInitParams();
		return initParams;
	}

	// ===================================================listener================================================

	public void loadListeners() throws Exception {

		this.servletContextListeners = new ArrayList<>();
		this.servletContextAttributeListeners = new ArrayList<>();
		this.httpSessionListeners = new ArrayList<>();
		this.httpSessionActivationListeners = new ArrayList<>();
		this.httpSessionAttributeListeners = new ArrayList<>();
		this.servletRequestListeners = new ArrayList<>();
		this.servletRequestAttributeListeners = new ArrayList<>();

		System.out.println("加载listeners");
		List<Element> listenerList = root.elements("listener");
		String listenerName = null;
		for (Element listenerInfo : listenerList) {
			listenerName = listenerInfo.element("listener-class").getTextTrim();
			Class<?> listenerClass = loader.loadClass(listenerName);
			Object ListenerObj = listenerClass.newInstance();

			listenerAllocation(ListenerObj);
		}
	}

	private void listenerAllocation(Object listener) {

		if (listener instanceof ServletContextListener) {
			servletContextListeners.add((ServletContextListener) listener);
		}

		if (listener instanceof ServletContextAttributeListener) {
			servletContextAttributeListeners.add((ServletContextAttributeListener) listener);
		}

		if (listener instanceof HttpSessionListener) {
			httpSessionListeners.add((HttpSessionListener) listener);
		}

		if (listener instanceof HttpSessionActivationListener) {
			httpSessionActivationListeners.add((HttpSessionActivationListener) listener);
		}

		if (listener instanceof HttpSessionAttributeListener) {
			httpSessionAttributeListeners.add((HttpSessionAttributeListener) listener);
		}

		if (listener instanceof ServletRequestListener) {
			servletRequestListeners.add((ServletRequestListener) listener);
		}

		if (listener instanceof ServletRequestAttributeListener) {
			servletRequestAttributeListeners.add((ServletRequestAttributeListener) listener);
		}

	}

	public List<ServletContextListener> getServletContextListeners() {
		return servletContextListeners;
	}

	public List<ServletContextAttributeListener> getServletContextAttributeListeners() {
		return servletContextAttributeListeners;
	}

	public List<HttpSessionListener> getHttpSessionListeners() {
		return httpSessionListeners;
	}

	public List<HttpSessionActivationListener> getHttpSessionActivationListeners() {
		return httpSessionActivationListeners;
	}

	public List<HttpSessionAttributeListener> getHttpSessionAttributeListeners() {
		return httpSessionAttributeListeners;
	}

	public List<ServletRequestListener> getServletRequestListeners() {
		return servletRequestListeners;
	}

	public List<ServletRequestAttributeListener> getServletRequestAttributeListeners() {
		return servletRequestAttributeListeners;
	}

	// ===================================================Filter=================================================

	public void loadFilters() throws Exception {
		this.filterChain = new MyFilterChain();

		ConcurrentHashMap<String, Filter> name2FilterMap = new ConcurrentHashMap<>();

		List<Element> filterList = root.elements("filter");
		String filterName = null, filterClassName = null, name = null, value = null;

		for (Element element : filterList) {

			filterName = element.element("filter-name").getTextTrim();
			filterClassName = element.element("filter-class").getTextTrim();

			Class<?> filterClass = loader.loadClass(filterClassName);
			Filter filter = (Filter) filterClass.newInstance();

			MyFilterConfig filterConfig = new MyFilterConfig();
			filterConfig.setFilterName(filterName);
			filterConfig.setServletContext(dynamicResourceManager);

			List<Element> initParams = element.elements("init-param");
			for (Element initParam : initParams) {
				name = initParam.element("param-name").getTextTrim();
				value = initParam.element("param-value").getTextTrim();
				filterConfig.setInitParam(name, value);
			}

			filter.init(filterConfig);
			name2FilterMap.put(filterName, filter);
		}

		filterChain.setName2FilterMap(name2FilterMap);

		List<ResourceInfo<Filter>> filters = new ArrayList<>();

		String urlPattern = null;
		List<Element> mappingList = root.elements("filter-mapping");
		for (Element mapping : mappingList) {

			ResourceInfo<Filter> filterInfo = new ResourceInfo<>();

			name = mapping.element("filter-name").getTextTrim();
			filterInfo.setName(name);

			List<Element> urls = mapping.elements("url-pattern");
			for (Element url : urls) {
				urlPattern = url.getTextTrim();
				Mapping<Filter> filterMapping = new Mapping<>(name, urlPattern);
				filterInfo.addMapping(filterMapping);
			}

			filters.add(filterInfo);
		}

		filterChain.setFilters(filters);

	}

	public MyFilterChain getFilters() {
		return filterChain;
	}

	// ===================================================Servlet=================================================

	/**
	 * 暂时实现为容器启动直接加载全部servlet
	 * 
	 * @throws Exception
	 */
	public void loadServlets() throws Exception {
		this.servletManager = new ServletManager();

		// 建立name-servlet映射
		ConcurrentHashMap<String, HttpServlet> name2ServletMap = new ConcurrentHashMap<>();

		List<Element> servletList = root.elements("servlet");
		String servletName = null, servletClassName = null, name = null, value = null;

		for (Element element : servletList) {

			servletName = element.element("servlet-name").getTextTrim();
			servletClassName = element.element("servlet-class").getTextTrim();

			Class<?> servletClass = loader.loadClass(servletClassName);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();

			MyServletConfig servletConfig = new MyServletConfig();
			servletConfig.setServletName(servletName);
			servletConfig.setServletContext(dynamicResourceManager);

			List<Element> initParams = element.elements("init-param");
			for (Element initParam : initParams) {
				name = initParam.element("param-name").getTextTrim();
				value = initParam.element("param-value").getTextTrim();
				servletConfig.setInitParam(name, value);
			}

			servlet.init(servletConfig);
			name2ServletMap.put(servletName, servlet);
		}

		servletManager.setName2ServletMap(name2ServletMap);

		// 建立url-name映射
		List<Mapping<HttpServlet>> mappings = new ArrayList<>();

		String urlPattern = null;
		List<Element> mappingList = root.elements("servlet-mapping");
		for (Element mapping : mappingList) {

			name = mapping.element("servlet-name").getTextTrim();

			List<Element> urls = mapping.elements("url-pattern");
			for (Element url : urls) {
				urlPattern = url.getTextTrim();
				Mapping<HttpServlet> servletMapping = new Mapping<>(name, urlPattern);
				mappings.add(servletMapping);
			}

		}

		servletManager.setMappings(mappings);
	}

	public ServletManager getServletManager() {
		return servletManager;
	}
}
