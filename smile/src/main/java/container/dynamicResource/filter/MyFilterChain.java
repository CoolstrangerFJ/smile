/**
 * 
 */
package container.dynamicResource.filter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;

import container.dynamicResource.ResourceInfo;

/**
 * @author CoolStranger
 * @date 2018年1月29日
 * @time 上午12:01:37
 *
 */
public class MyFilterChain {

	private List<ResourceInfo<Filter>> filters;
	private ConcurrentHashMap<String, Filter> name2FilterMap;
	private int length;
	
	public MyFilterChain() {
	}
	
	public MyFilterChain(List<ResourceInfo<Filter>> filters, ConcurrentHashMap<String, Filter> name2FilterMap) {
		this.filters = filters;
		this.name2FilterMap = name2FilterMap;
	}

	public List<ResourceInfo<Filter>> getFilters() {
		return filters;
	}

	public void setFilters(List<ResourceInfo<Filter>> filters) {
		this.filters = filters;
		this.length = filters.size();
	}

	public ConcurrentHashMap<String, Filter> getName2FilterMap() {
		return name2FilterMap;
	}

	public void setName2FilterMap(ConcurrentHashMap<String, Filter> name2FilterMap) {
		this.name2FilterMap = name2FilterMap;
	}
	
	public Filter getFilter(String name){
		return name2FilterMap.get(name);
	}

	public int getLength() {
		return length;
	}
	
	
}
