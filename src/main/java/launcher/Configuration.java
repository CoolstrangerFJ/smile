/**
 * 
 */
package launcher;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author CoolStranger
 * @date 2018年1月2日
 * @time 下午10:51:10
 *
 */
public class Configuration {
	public static final String SERVER_NAME = "Smile/1.0";
	private static final int KB = 1024;
	private static final int MB = KB * KB;
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60 * SECOND;
	public static final String WEBAPPS = "webapps/";
	public static final String DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
	public static final String DEFAULT_PAGE = "index.html";
	public static int serverPort = 80;
	public static String version = "1.0";
	public static String charset = "UTF-8";
	public static int processorThreadPoolSize = 3;
	public static long processorTimeOut = 300;// second
	public static int tinySize = 1 * KB;
	public static int smallSize = 4 * KB;
	public static int mediumSize = 64 * KB;
	public static int bigSize = 256 * KB;
	public static int largeSize = 1 * MB;
	public static int hugeSize = 4 * MB;
	public static int minCacheSize = 50 * MB;
	public static int maxCacheSize = 200 * MB;
	public static final long CACHE_MAX_INACTIVE_INTERVAL;
	//second
	public static final int DEFAULT_SESSION_MAX_INACTIVE_INTERVAL = 1800;

	public static final String UNIMPLEMENTED_INVOKE = "An unimplemented method was invoked !!!";

	static {
		SAXReader saxReader = new SAXReader();
		File config = new File("conf/server.xml");
		Document doc = null;
		try {
			doc = saxReader.read(config);
		} catch (DocumentException e) {
			System.out.println("找不到server.xml");
			System.exit(0);
		}
		Element root = doc.getRootElement();
		Element context = root.element("context");
		serverPort = Integer.parseInt(context.attribute("port").getText());
		version = context.attribute("version").getText();
		charset = context.attribute("charset").getText();
		processorThreadPoolSize = Integer.parseInt(context.attribute("processor-thread-pool-size").getText());

		Element bufferSize = root.element("buffer-size");
		tinySize = Integer.parseInt(bufferSize.attribute("tiny").getText()) * KB;
		smallSize = Integer.parseInt(bufferSize.attribute("small").getText()) * KB;
		mediumSize = Integer.parseInt(bufferSize.attribute("medium").getText()) * KB;
		bigSize = Integer.parseInt(bufferSize.attribute("big").getText()) * KB;
		largeSize = Integer.parseInt(bufferSize.attribute("large").getText()) * KB;
		hugeSize = Integer.parseInt(bufferSize.attribute("huge").getText()) * KB;

		Element cachePool = root.element("cache-pool");
		Element cacheSize = cachePool.element("size");
		minCacheSize = Integer.parseInt(cacheSize.attribute("min").getText()) * MB;
		maxCacheSize = Integer.parseInt(cacheSize.attribute("max").getText()) * MB;

		Element cacheTimeOut = cachePool.element("time-out");
		if (cacheTimeOut != null) {
			CACHE_MAX_INACTIVE_INTERVAL = Long.parseLong(cacheTimeOut.getTextTrim()) * MINUTE;
		} else {
			CACHE_MAX_INACTIVE_INTERVAL = 30 * MINUTE;
		}

		System.out.println("配置信息已加载...");
	}
}
