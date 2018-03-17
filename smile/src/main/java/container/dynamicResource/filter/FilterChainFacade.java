/**
 * 
 */
package container.dynamicResource.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import container.dynamicResource.Mapping;
import container.dynamicResource.ResourceInfo;
import container.dynamicResource.servlet.ServletManager;

/**
 * @author CoolStranger
 * @date 2018年1月29日
 * @time 上午12:18:53
 *
 */
public class FilterChainFacade implements FilterChain {

	private int statusIndex;
	private MyFilterChain chain;
	private List<ResourceInfo<Filter>> filters;
	private int length;
	private ServletManager servletManager;

	public FilterChainFacade(MyFilterChain chain, ServletManager servletManager) {
		this.chain = chain;
		this.filters = chain.getFilters();
		this.length = chain.getLength();
		this.servletManager = servletManager;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		String url = null;
		HttpServletRequest req = (HttpServletRequest) request;
		url = getNoProjNameUrl(req.getRequestURI());
		
		Filter filter = getNextFilter(url);
		if (filter != null) {
			filter.doFilter(request, response, this);
		} else {
			HttpServlet httpServlet = servletManager.getServletByURL(req.getRequestURI());
			if (httpServlet != null) {
				httpServlet.service(request, response);
			}
		}
	}

	private String getNoProjNameUrl(String url) {
		int secondSlash = url.indexOf('/', 1);
		if (secondSlash != -1) {
			return url.substring(secondSlash);
		} else {
			throw new IllegalArgumentException("illegal url");
		}
	}

	private Filter getNextFilter(String url) {
		while (statusIndex < length) {
			ResourceInfo<Filter> filterInfo = filters.get(statusIndex++);
			List<Mapping<Filter>> mappings = filterInfo.getMappings();

			for (Mapping<Filter> mapping : mappings) {
				if (mapping.match(url)) {
					return chain.getFilter(mapping.getName());
				}
			}
		}
		return null;
	}

}
