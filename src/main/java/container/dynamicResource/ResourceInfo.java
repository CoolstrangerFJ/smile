/**
 * 
 */
package container.dynamicResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CoolStranger
 * @date 2018年1月28日
 * @time 下午11:38:15
 *
 */
public class ResourceInfo<T> {

	private String name;
	private List<Mapping<T>> mappings = new ArrayList<>(3);

	public ResourceInfo() {
	}

	public ResourceInfo(String name, List<Mapping<T>> mappings) {
		this.name = name;
		this.mappings = mappings;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Mapping<T>> getMappings() {
		return mappings;
	}

	public void addMapping(Mapping<T> mapping) {
		this.mappings.add(mapping);
	}
}
