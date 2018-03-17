package util;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


@SuppressWarnings("unchecked")
public class XmlLoader {

	public static ConcurrentHashMap<String, String> getMIMEMap() {
		ConcurrentHashMap<String, String> mimeMap = new ConcurrentHashMap<>(2048);
		File mimefile = new File("conf/mime.xml");
		SAXReader saxReader = new SAXReader();
		Document doc = null;
		try {
			doc = saxReader.read(mimefile);
		} catch (DocumentException e) {
			System.out.println("Loading conf/mime.xml failed");
			System.exit(0);
		}
		Element root = doc.getRootElement();
		List<Element> mimeList = root.elements("mime-mapping");
		String extension = null;
		String type = null;
		for (Element mime : mimeList) {
			extension = mime.element("extension").getTextTrim();
			type = mime.element("mime-type").getTextTrim();
			mimeMap.put(extension, type);
		}
		return mimeMap;
	}
}
