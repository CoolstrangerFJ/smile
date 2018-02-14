package container.staticResource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import connecter.response.ResponseWriter;
import launcher.Configuration;
import util.XmlLoader;

public class StaticResourceManager {
	private CacheManager cacheManager = new CacheManager();
	private static ConcurrentHashMap<String, String> mimeMap;
	private URI webappsUri;
	private String charset = Configuration.charset;
	/**
	 * 静态资源白名单
	 */
	private ConcurrentHashMap<String, File> staticResourceContent = new ConcurrentHashMap<>(512);

	public StaticResourceManager() {
		System.out.println("正在加载MIME列表...");
		mimeMap = XmlLoader.getMIMEMap();
		System.out.println("MIME列表加载完成");
		scanResource();
		cacheManager.setStaticResourceContent(staticResourceContent);
	}

	private void scanResource() {
		File webapps = new File(Configuration.WEBAPPS);
		webappsUri = webapps.toURI();
		System.out.println(webappsUri);
		File[] apps = webapps.listFiles();
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.equals("WEB-INF") || name.equals("META-INF")) {
					return false;
				}
				return true;
			}
		};
		for (File app : apps) {
			File[] files = app.listFiles(filter);
			for (File file : files) {
				fileScanner(file);
			}
		}
		webappsUri = null;
	}

	private void fileScanner(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File subFile : files) {
				fileScanner(subFile);
			}
		} else {
			URI uri = file.toURI();
			staticResourceContent.put("/" + webappsUri.relativize(uri), file);
		}
	}

	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestURI();
		get(path, request, response);
	}

	public void get(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
		StaticCache resource = null;

		//有无该资源
		if (staticResourceContent.containsKey(path)) {
			resource = cacheManager.getResource(path);

			// 判断是否304
			long ifModifiedSince = request.getDateHeader("If-Modified-Since");

			if (ifModifiedSince != -1) {
				long lastModified = resource.getLastModified() / 1000 * 1000;

				if (ifModifiedSince >= lastModified) {
					response.setStatus(304);
					return;
				}
			}
			
			response.setStatus(200);
			assemble(response, resource);
		//无则404
		} else {
			returnNotFound(response);
		}

	}

	public void returnNotFound(HttpServletResponse response) throws IOException {
		StaticCache resource = cacheManager.getNotFound();
		response.setStatus(404);
		assemble(response, resource);
	}

	private void assemble(HttpServletResponse response, StaticCache resource) throws IOException {
		String resName = resource.getName();
		int lastDot = resName.lastIndexOf('.');
		String extension = null;
		String contentType = "text/plain";
		if (lastDot != -1) {
			extension = resName.substring(lastDot + 1);
			contentType = mimeMap.get(extension);
		}

		if (contentType.startsWith("text")) {
			contentType += "; charset=" + charset;
		}

		response.setContentType(contentType);
		response.addDateHeader("Last-Modified", resource.getLastModified());
		// 把静态资源加入响应体
		if (response instanceof ResponseWriter) {
			ResponseWriter responseWriter = (ResponseWriter) response;
			responseWriter.put(resource.getBuffer());
		} else {
			System.out.println("response使用了包装类,无法使用缓存池");
			response.getOutputStream().write(resource.getBuffer().array());
		}
	}

	public static Map<String, String> getmimeMap() {
		return mimeMap;
	}
}
