/**
 * 
 */
package container.dynamicResource.servlet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;

import container.dynamicResource.Mapping;

/**
 * @author CoolStranger
 * @date 2018年2月8日
 * @time 下午12:04:26
 *
 */
public class ServletManager {

	private ConcurrentHashMap<String, HttpServlet> name2ServletMap;

	private List<Mapping<HttpServlet>> mappings;

	
	public ConcurrentHashMap<String, HttpServlet> getName2ServletMap() {
		return name2ServletMap;
	}

	public void setName2ServletMap(ConcurrentHashMap<String, HttpServlet> name2ServletMap) {
		this.name2ServletMap = name2ServletMap;
	}

	public void setMappings(List<Mapping<HttpServlet>> mappings) {
		this.mappings = mappings;
	}

	public HttpServlet getServletByName(String name) {
		return name2ServletMap.get(name);
	}

	public HttpServlet getServletByURL(String url) {
		url = getNoProjNameUrl(url);
		HttpServlet servlet = null;

		for (Mapping<HttpServlet> mapping : mappings) {
			if (mapping.match(url)) {
				servlet = name2ServletMap.get(mapping.getName());
				break;
			}
		}
		return servlet;
	}
	
	private String getNoProjNameUrl(String url) {
		int secondSlash = url.indexOf('/', 1);
		if (secondSlash != -1) {
			return url.substring(secondSlash);
		} else {
			throw new IllegalArgumentException("illegal url");
		}
	}
}
