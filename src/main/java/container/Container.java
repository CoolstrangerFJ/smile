/**
 * 
 */
package container;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import connecter.HttpConnecterNIO;
import connecter.request.Request;
import connecter.response.Response;
import container.dynamicResource.DynamicResourceManager;
import container.staticResource.StaticResourceManager;
import launcher.Configuration;

/**
 * @author CoolStranger
 * @date 2018年1月29日
 * @time 上午9:27:27
 *
 */
public class Container {

	public ConcurrentHashMap<String, DynamicResourceManager> dynSrcMgrMap = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, Boolean> projectMap = new ConcurrentHashMap<>();
	private StaticResourceManager staticResourceManager;
	private static Container container = new Container();

	private Container() {
	}

	public void test() {
	}

	public static Container getInstance() {
		return container;
	}

	public static void init() {
		container.staticResourceManager = new StaticResourceManager();
		File webapps = new File(Configuration.WEBAPPS);
		String[] list = webapps.list();
		for (String projectName : list) {
			File config = new File(Configuration.WEBAPPS + projectName + "/WEB-INF/web.xml");

			System.out.println("正在加载 " + projectName + "...");
			try {
				if (config.exists()) {
					container.dynSrcMgrMap.put(projectName, new DynamicResourceManager(projectName));
				}
				container.projectMap.put(projectName, true);
				System.out.println(projectName + " 已加载...");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(projectName + " 加载失败...");
			}
		}
		System.out.println();
		System.out.println("----------欢迎使用" + Configuration.SERVER_NAME + "---------");
		System.out.println("版本: " + Configuration.version);
		try {
			System.out.println("服务器IP:  " + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		long finishLoading = System.currentTimeMillis();
		long startupTime = finishLoading - HttpConnecterNIO.getStartLoading();
		System.out.println("本次启动耗时 " + startupTime + "ms  作者：Coolstranger");
	}

	public void handle(Request request, Response response) throws IOException {
		String projName = request.getProjName();
		// URI不正确,直接重定向 eg:/project --> /project/
//		System.out.println("project: " + projName);
		
//		if (projName == null) {
//			response.sendRedirect("/root/");
//			return;
//		}else {
//			
//		}
		if (projName == null) {
			
			String uri = request.getRequestURI();

//			System.out.println("uri: "+uri);
			if (uri.equals("/")) {
				response.sendRedirect("/root/");
				return;
			}

			projName = uri.substring(1);
			// 这里提前判断一下有无该项目
			if (projectMap.containsKey(projName)) {
				response.sendRedirect(uri + "/");
				// System.out.println("sendRedirect: "+location);
			} else {
				// //判断是否访问了根目录
				// if (uri.equals("/")) {
				// response.sendRedirect("/root/");
				// }else {
				// 转发404页面
				staticResourceManager.returnNotFound(response);
				// }
			}
			return;
		}

		// System.out.println("url: "+request.getRequestURI());
		// System.out.println("projName: "+ request.getProjName());
		// System.out.println("noProjNameUrl: " + request.getNoProjNameUrl());
		// System.out.println("response has handle? " + response.hasHandled());

		DynamicResourceManager dynamicResourceManager = dynSrcMgrMap.get(projName);
		if (dynamicResourceManager == null) {
			staticResourceManager.handle(request, response);
		} else {
			try {
				dynamicResourceManager.handle(request, response);
			} catch (Throwable e) {
				// 这里可以把报错信息打印到响应
				errorPrint(e, response);
			}
			// System.out.println("url: "+request.getRequestURI());
			// System.out.println("projName: "+ request.getProjName());
			// System.out.println("noProjNameUrl: " +
			// request.getNoProjNameUrl());
			// System.out.println("response has handle? " +
			// response.hasHandled());
			if (!response.hasHandled()) {
				staticResourceManager.handle(request, response);
			}

		}
	}

	/**
	 * 这里可以进一步把ERROR_PAGE_PREFIX + errMsg +
	 * ERROR_PAGE_SUFFIX写到HTML文件上面，然后用一个字符串代替errMsg,如：[error message]
	 * 然后容器启动时把文件全部读入写到一个StringBuilder里面, 然后使用indexOf把[error
	 * message]找出来,把前后切开变成两个String, 分别给ERROR_PAGE_PREFIX、ERROR_PAGE_SUFFIX赋值
	 * 
	 */
	private static final String ERROR_PAGE_PREFIX = "<html><style type='text/css'>body {background-color: burlywood}</style><body><h1>欢迎使用Smile</h1><hr size='10px' color='black' /><h2>ERROR MESSAGE</h2><hr size='5px' color='black' /><p>";
	private static final String ERROR_PAGE_SUFFIX = "</p></body></html>";

	private void errorPrint(Throwable e, Response response) {
		try {
			response.reset();
			response.setStatus(500);
			PrintWriter writer = response.getWriter();
			writer.write(ERROR_PAGE_PREFIX);
			e.printStackTrace(writer);
			writer.write(ERROR_PAGE_SUFFIX);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

	}

	public static DynamicResourceManager getDynamicResourceManager(String projectName) {
		return container.dynSrcMgrMap.get(projectName);
	}

	public static StaticResourceManager getStaticResourceManager() {
		return container.staticResourceManager;
	}

}
