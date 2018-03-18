package container.staticResource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import connector.response.ResponseWriter;
import launcher.Configuration;
import util.XmlLoader;

public class StaticResourceManager {
	private CacheManager cacheManager = new CacheManager();
	private static ConcurrentHashMap<String, String> mimeMap;
	private URI webappsUri;
	private String charset = Configuration.charset;
	private FileListener fileListener;
	/**
	 * 静态资源白名单
	 */
	private ConcurrentHashMap<String, File> staticResourceContent = new ConcurrentHashMap<>(512);

	public StaticResourceManager() {
		System.out.println("正在加载MIME列表...");
		mimeMap = XmlLoader.getMIMEMap();
		System.out.println("MIME列表加载完成");
		fileListener = new FileListener(this);
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
			Path path = app.toPath();
			fileListener.register(path);
		}
		fileListener.start();
//		webappsUri = null;
	}

	private void fileScanner(File file) {
		URI uri = file.toURI();
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File subFile : files) {
				fileScanner(subFile);
			}
			Path path = Paths.get(uri);
			fileListener.register(path);
		} else {
			System.out.println("path: "+file.toPath().toString());
			System.out.println("pathToURI: "+webappsUri.relativize(file.toPath().toUri()));
			staticResourceContent.put("/" + webappsUri.relativize(uri), file);
		}
	}
	
	private String toRelativizePath(URI uri){
		return "/"+webappsUri.relativize(uri);
	}
	
	public void removeResource(URI uri){
		String path = toRelativizePath(uri);
		staticResourceContent.remove(path);
		cacheManager.remove(path);
	}
	
	public void createResource(URI uri){
		String path = toRelativizePath(uri);
		staticResourceContent.put(path, new File(uri));
	}
	
	public void modifyResource(URI uri){
		String path = toRelativizePath(uri);
		cacheManager.remove(path);
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
