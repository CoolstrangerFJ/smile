/**
 * 
 */
package container.dynamicResource;

/**
 * @author CoolStranger
 * @date 2018年1月28日
 * @time 下午11:02:54
 *
 */
public class Mapping<T> {

	private MappingType mappingType;
	private String urlPattern;
	private String name;

	public Mapping() {
	}

	public Mapping(String name, String urlPattern) {
		this.name = name;

		if (urlPattern.startsWith("*")) {
			mappingType = MappingType.END_WITH;
			this.urlPattern = urlPattern.substring(1);

		} else if (urlPattern.endsWith("*")) {
			mappingType = MappingType.START_WITH;
			this.urlPattern = urlPattern.substring(0, urlPattern.length() - 1);

		} else if (urlPattern.equals("/")) {
			mappingType = MappingType.START_WITH;
			this.urlPattern = "/";

		} else {
			mappingType = MappingType.EQUALS;
			this.urlPattern = urlPattern;
		}
	}

	public boolean match(String url) {

		switch (mappingType) {
		case EQUALS:
			return url.equals(urlPattern);

		case START_WITH:
			return url.startsWith(urlPattern);

		case END_WITH:
			return url.endsWith(urlPattern);

		default:
			return false;
		}

	}

	public MappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(MappingType mappingType) {
		this.mappingType = mappingType;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
