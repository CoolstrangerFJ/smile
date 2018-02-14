package factory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import launcher.Configuration;

public class LoaderFactory {

	public static URLClassLoader getURLClassLoader(String projectName)
			throws MalformedURLException, IOException {
		URLClassLoader loader = null;
		File classPath = new File(Configuration.WEBAPPS + projectName
				+ "/WEB-INF/classes/");
		List<URL> urlList = new ArrayList<>();
		urlList.add(classPath.toURI().toURL());
		File jarRoot = new File(Configuration.WEBAPPS + projectName + "/WEB-INF/lib/");
		File[] jarFiles = jarRoot.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith("jar") || name.endsWith("zip")) {
					return true;
				}
				return false;
			}
		});
		for (File file : jarFiles) {
			System.out.println("  |--"+projectName+"中的"+file.getName()+"已加载...");
			urlList.add(file.toURI().toURL());
		}
		URL[] urls = new URL[urlList.size()];
		urls = urlList.toArray(urls);
		loader = new URLClassLoader(urls);
		return loader;
	}
}
