/**
 * 
 */
package container.dynamicResource.filter;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * @author CoolStranger
 * @date 2018年1月28日
 * @time 上午12:45:34
 *
 */
public class MyFilterConfig implements FilterConfig {
	private String filterName;
	private ConcurrentHashMap<String, String> initParams = new ConcurrentHashMap<>();
	private ServletContext servletContext;

	public MyFilterConfig() {
	}

	@Override
	public String getFilterName() {
		return this.filterName;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return initParams.keys();
	}

	public void setInitParam(String name, String value) {
		this.initParams.put(name, value);
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
