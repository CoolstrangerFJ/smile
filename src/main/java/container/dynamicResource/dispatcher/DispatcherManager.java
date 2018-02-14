/**
 * 
 */
package container.dynamicResource.dispatcher;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;

import container.dynamicResource.DynamicResourceManager;
import container.dynamicResource.servlet.ServletManager;

/**
 * @author CoolStranger
 * @date 2018年2月8日
 * @time 下午4:36:17
 *
 */
public class DispatcherManager {

	// private DynamicResourceManager dynamicResourceManager;
	private ServletManager servletManager;
	private String contextPath;
	private ConcurrentHashMap<String, ResourceDispatcher> dispatchers = new ConcurrentHashMap<>(64);

	public DispatcherManager() {
	}

	public DispatcherManager(DynamicResourceManager dynamicResourceManager) {
		// this.dynamicResourceManager = dynamicResourceManager;
		this.servletManager = dynamicResourceManager.getServletManager();
		this.contextPath = dynamicResourceManager.getContextPath();
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public RequestDispatcher getRequestDispatcher(String key) {
		if (key.startsWith("/")) {
			key = contextPath + key;
		} else {
			key = contextPath + "/" + key;
		}

		ResourceDispatcher dispatcher = dispatchers.get(key);
		if (dispatcher == null) {
			dispatcher = new ResourceDispatcher(key);
			HttpServlet httpServlet = servletManager.getServletByURL(key);
			// 即使httpServlet == null,也可以直接set,反正forward的时候要判断一次
			dispatcher.setServlet(httpServlet);
			dispatchers.put(key, dispatcher);
		}
		return dispatcher;
	}

	public RequestDispatcher getNamedDispatcher(String name) {
		ResourceDispatcher dispatcher = dispatchers.get(name);

		if (dispatcher == null) {
			HttpServlet httpServlet = servletManager.getServletByName(name);

			if (httpServlet != null) {
				dispatcher = new ResourceDispatcher(name);
				dispatcher.setServlet(httpServlet);
				dispatchers.put(name, dispatcher);
			}
		}
		return dispatcher;
	}
}
