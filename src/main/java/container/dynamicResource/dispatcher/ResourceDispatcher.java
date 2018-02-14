/**
 * 
 */
package container.dynamicResource.dispatcher;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import container.Container;
import container.staticResource.StaticResourceManager;

/**
 * @author CoolStranger
 * @date 2018年2月8日
 * @time 下午7:29:30
 *
 */
public class ResourceDispatcher implements RequestDispatcher {

	private String key;
	private HttpServlet servlet;
	private static StaticResourceManager staticResourceManager = Container.getStaticResourceManager();

	public ResourceDispatcher(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public HttpServlet getServlet() {
		return servlet;
	}

	public void setServlet(HttpServlet servlet) {
		this.servlet = servlet;
	}

	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		if (servlet != null) {
			servlet.service(request, response);
		} else {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			staticResourceManager.get(key, req, res);
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		forward(request, response);
	}

}
