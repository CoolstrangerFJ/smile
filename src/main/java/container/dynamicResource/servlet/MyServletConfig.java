/**
 * 
 */
package container.dynamicResource.servlet;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author CoolStranger
 * @date 2018年2月8日
 * @time 上午11:29:22
 *
 */
public class MyServletConfig implements ServletConfig {

	
	private String name;
	private ServletContext servletContext;
	private ConcurrentHashMap<String, String> initParams = new ConcurrentHashMap<>();
	
	
	@Override
	public String getServletName() {
		return name;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
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

	public void setServletName(String servletName) {
		this.name = servletName;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
