package connecter.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.nio.channels.SocketChannel;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import connecter.ServerMoniter;
import connecter.buffer.IBuffer;
import container.Container;
import container.dynamicResource.DynamicResourceManager;
import container.session.Session;
import container.session.SessionManager;
import launcher.Configuration;
import manage.Managable;
import manage.Moniter;

public class Request implements HttpServletRequest, Managable {

	private static final String DEFAULT_PAGE = Configuration.DEFAULT_PAGE;
	private static final String DATE_PATTERN = Configuration.DATE_PATTERN;
	private HashMap<String, Object> parammeters = new HashMap<>();
	private HashMap<String, String> headers = new HashMap<>(8);
	private HashMap<String, Object> attrs = new HashMap<>(8);
	private SocketChannel socketChannel;
	private String charset;
	private IBuffer buffer;
	private String queryStr;
	private boolean hasBody;
	private boolean bodyUsed;
	private boolean isCorrected;
	private ServletInputStream servletInputStream;
	private BufferedReader bufferedReader;
	private String noProjNameUrl;
	private String ProjName;
	private DynamicResourceManager dynamicResourceManager;

	@SuppressWarnings("unused")
	private int getFirstSpaceIndex(byte[] buf, int off, int len) throws IOException {
		if (len <= 0) {
			return -1;
		}
		int count = 0, c;

		while (count < len) {
			c = buf[off++];
			count++;
			if (c == ' ' || count == len) {
				break;
			}
		}
		return count > 0 ? count + off : -1;
	}

	@SuppressWarnings("unused")
	private String decode(byte[] bytes) {
		try {
			String s = new String(bytes, charset);
			return URLDecoder.decode(s, charset).trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 确定是动态资源以后再解析查询字符串和请求体
	 *
	 */
	public void parseAll() {
		try {
			if (hasBody) {
				readRequestBody();
			}
			if (queryStr != null) {
				readQueryStr();
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析查询字符串,覆盖请求体中相同的字段
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private void readQueryStr() throws UnsupportedEncodingException {
		String s = URLDecoder.decode(queryStr, charset);
		String[] params = s.split("&");
		String key = null, val = null;
		String[] kvPair;
		HashMap<String, Object> queryParam = new HashMap<>();
		for (String param : params) {
			kvPair = param.split("=");
			if (kvPair.length == 2) {
				key = kvPair[0];
				val = kvPair[1];
				// System.out.println(key + "=" + val);
				putInto(queryParam, key, val);
			}
		}
		for (Entry<String, Object> entry : queryParam.entrySet()) {
			parammeters.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 解析请求体参数
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private void readRequestBody() throws UnsupportedEncodingException {
		String contentType = headers.get("Content-Type");
		// System.out.println(contentType);
		if (contentType != null) {
			int firstSemicolon = contentType.indexOf(';');
			String dataFormat = contentType.substring(0, firstSemicolon);
			switch (dataFormat) {
			case "application/x-www-form-urlencoded":
				parseXWwwFormUrlencoded();
				break;

			case "multipart/form-data":

				break;

			default:
				// do nothing
				break;
			}
			return;
		}

	}

	private void parseXWwwFormUrlencoded() throws UnsupportedEncodingException {
		int len = buffer.remaining();
		byte[] buf = new byte[len];
		buffer.get(buf);

		String body = new String(buf, charset);
		try {
			String tempBody = body.trim();
			String key = null, val = null;
			String[] kvPairs = tempBody.split("&");
			for (String kvPair : kvPairs) {
				String[] pair = kvPair.split("=");
				key = URLDecoder.decode(pair[0], charset);
				if (pair.length == 2) {
					val = URLDecoder.decode(pair[1], charset);
					putInto(parammeters, key, val);
				}
			}
		} catch (Exception e) {
			body = URLDecoder.decode(body, charset);
			throw new RuntimeException("解析请求体异常,可能不是键值对", e);
		}
	}

	/**
	 * 把参数存入哈希表,如有相同字段,则改为字符数组
	 * 
	 * @param map
	 *            要存入的哈希表
	 * @param key
	 *            要存入的键
	 * @param val
	 *            要存入的值
	 */
	private void putInto(HashMap<String, Object> map, String key, String val) {
		if (map.containsKey(key)) {
			Object tempVal = map.get(key);
			if (tempVal instanceof String) {
				map.put(key, new String[] { tempVal.toString(), val });
			} else if (tempVal instanceof String[]) {
				String[] tempValArr = (String[]) tempVal;
				int tempLen = tempValArr.length;
				String[] valArr = new String[tempLen + 1];
				System.arraycopy(tempValArr, 0, valArr, 0, tempLen);
				valArr[tempLen] = val;
				map.put(key, valArr);
			}
		} else {
			map.put(key, val);
		}
	}

	public String getNoProjNameUrl() {
		if (noProjNameUrl == null) {
			String url = getRequestURI();
			int secondSlash = url.indexOf('/', 1);
			if (secondSlash != -1) {
				noProjNameUrl = url.substring(secondSlash);
			}
		}
		return this.noProjNameUrl;
	}

	public String getProjName() {
		if (ProjName == null) {
			String uri = getRequestURI();
//			System.out.println("uri: "+uri);
			
			//判断是否为根目录
			if (uri.length() == 1) {//url.equal("/")
				return null;
			}
			
			int secondSlash = uri.indexOf('/', 1);
//			System.out.println("secondSlash: "+ secondSlash);
			if (secondSlash != -1) {
				ProjName = uri.substring(1, secondSlash);
			}else {
				//这里不能返回null,因为有可能遇到以下情况:
				//1、URI:/project
				//2、刚刚访问过该项目,且存有cookie
				//这种情况下,若返回null
//				ProjName = uri.substring(1);
			}
		}
		return ProjName;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public void setBuffer(IBuffer buffer) {
		this.buffer = buffer;
	}

	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}

	public void setHasBody(boolean hasBody) {
		this.hasBody = hasBody;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	@Override
	public Object getAttribute(String name) {
		// System.out.println("getAttr: "+name);
		return attrs.get(name);
	}

	@Override
	public Enumeration<?> getAttributeNames() {
		Enumeration<String> attrNames = new Enumeration<String>() {

			private Iterator<String> iterator = attrs.keySet().iterator();

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
		return attrNames;
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		charset = env;
	}

	@Override
	public int getContentLength() {
		return Integer.parseInt(headers.get("Content-Length"));
	}

	@Override
	public String getContentType() {
		return headers.get("Content-Type");
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (bufferedReader != null) {
			throw new IllegalStateException();
		}
		if (servletInputStream == null) {
			bodyUsed = true;
			servletInputStream = new ServletInputStreamImpl(buffer);
		}
		return servletInputStream;
	}

	@Override
	public String getParameter(String name) {
		if (!bodyUsed) {
			parseAll();
			bodyUsed = true;
		}
		Object parammeter = parammeters.get(name);
		// System.out.println("getParam: "+name+";param: "+parammeter);
		if (parammeter instanceof String) {
			return (String) parammeter;
		} else if (parammeter instanceof String[]) {
			return ((String[]) parammeter)[0];
		} else {
			return null;
		}
	}

	@Override
	public Enumeration<?> getParameterNames() {
		if (!bodyUsed) {
			parseAll();
			bodyUsed = true;
		}
		Enumeration<String> parameterNames = new Enumeration<String>() {

			private Iterator<String> iterator = parammeters.keySet().iterator();

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
		return parameterNames;
	}

	@Override
	public String[] getParameterValues(String name) {
		if (!bodyUsed) {
			parseAll();
			bodyUsed = true;
		}
		Object parammeter = parammeters.get(name);
		if (parammeter instanceof String) {
			return new String[] { (String) parammeter };
		} else if (parammeter instanceof String[]) {
			return ((String[]) parammeter);
		} else {
			return null;
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		if (!bodyUsed) {
			parseAll();
			bodyUsed = true;
		}
		return parammeters;
	}

	@Override
	public String getProtocol() {
		return headers.get("Protocol");
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return Configuration.SERVER_NAME;
	}

	@Override
	public int getServerPort() {
		return Configuration.serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (servletInputStream != null) {
			throw new IllegalStateException();
		}
		if (bufferedReader == null) {
			bodyUsed = true;
			bufferedReader = new BufferedReader(new InputStreamReader(new ServletInputStreamImpl(buffer), charset));
		}
		return bufferedReader;
	}

	private InetSocketAddress getRemoteAddress() throws IOException {
		SocketAddress remoteAddress = socketChannel.getRemoteAddress();
		if (remoteAddress instanceof InetSocketAddress) {
			return (InetSocketAddress) remoteAddress;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public String getRemoteAddr() {
		try {
			return getRemoteAddress().getAddress().getHostAddress();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public String getRemoteHost() {
		try {
			return getRemoteAddress().getAddress().getHostName();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void setAttribute(String name, Object o) {
		attrs.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attrs.remove(name);
	}

	private static ConcurrentHashMap<String, Locale> availableLocales = new ConcurrentHashMap<>();
	static {
		Locale[] availableLocales = Locale.getAvailableLocales();
		for (Locale locale : availableLocales) {
			Request.availableLocales.put(locale.toString(), locale);
		}
	}
	private List<Locale> locales;

	private List<Locale> getLocaleList() {
		if (this.locales == null) {
			this.locales = new ArrayList<>();
			String acceptLanguage = headers.get("Accept-Language");
			if (acceptLanguage == null) {
				System.err.println("Accept-Language is null!!!");
				acceptLanguage = headers.get("accept-language");
				if (acceptLanguage == null) {
					System.err.println("accept-language is null!!!");
					
				}
			}
			// eg： zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
			if (acceptLanguage != null) {
				String[] locales = acceptLanguage.split(",");

				Locale locale = null;
				for (String localeInfo : locales) {

					String localeStr = localeInfo.split(";")[0];

					if (localeStr != null && !localeStr.equals("")) {
						localeStr = localeStr.replace('-', '_');
						locale = availableLocales.get(localeStr);
						if (locale != null) {
							this.locales.add(locale);
						}
					}
				}
			}
		}
		if (this.locales.isEmpty()) {
			this.locales.add(Locale.getDefault());
		}
		return locales;
	}

	@Override
	public Locale getLocale() {
		List<Locale> localeList = getLocaleList();
		return localeList.get(0);
	}

	@Override
	public Enumeration<Locale> getLocales() {
		List<Locale> localeList = getLocaleList();
		final Iterator<Locale> iterator = localeList.iterator();

		return new Enumeration<Locale>() {

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public Locale nextElement() {
				return iterator.next();
			}
		};
	}

	/**
	 * 暂时不支持HTTPS
	 */
	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return getDynamicResourceManager().getRequestDispatcher(path);
	}

	@Override
	public String getRealPath(String path) {
		DynamicResourceManager dynamicResourceManager = getDynamicResourceManager();
		if (dynamicResourceManager != null) {
			return dynamicResourceManager.getRealPath(path);
		}
		return null;
	}

	@Override
	public int getRemotePort() {
		try {
			return getRemoteAddress().getPort();
		} catch (IOException e) {
			return -1;
		}
	}

	private Socket getSocket(){
		return socketChannel.socket();
	}
	
	@Override
	public String getLocalName() {
		return getSocket().getLocalAddress().getHostName();
	}

	@Override
	public String getLocalAddr() {
		return getSocket().getLocalAddress().getHostAddress();
	}

	@Override
	public int getLocalPort() {
		return getSocket().getLocalPort();
	}

	/**
	 * 暂时不支持
	 */
	@Override
	public String getAuthType() {
		return null;
	}

	private Cookie[] cookies;

	@Override
	public Cookie[] getCookies() {
		if (this.cookies != null) {
			return this.cookies;
		}
		String cookieStr = headers.get("Cookie");

		if (cookieStr == null) {
			return null;
		}
		String[] cookies = cookieStr.split("; ");
		int index = 0;
		String key = null, value = null;
		List<Cookie> cookieList = new ArrayList<>();

		for (String cookiePair : cookies) {
			index = cookiePair.indexOf("=");
			key = cookiePair.substring(0, index).trim();
			value = cookiePair.substring(index + 1).trim();

			cookieList.add(new Cookie(key, value));
		}
		Cookie[] cookiesArr = new Cookie[cookieList.size()];
		this.cookies = cookieList.toArray(cookiesArr);
		return this.cookies;
	}

	@Override
	public long getDateHeader(String name) {
		String date = headers.get(name);
		if (date == null) {
			return -1;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String getHeader(String name) {
		correct();
		return headers.get(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		String header = this.headers.get(name);
		String[] headers = header.split(",");
		List<String> headerList = Arrays.asList(headers);
		final Iterator<String> iterator = headerList.iterator();
		return new Enumeration<String>() {

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
	}

	@Override
	public Enumeration<?> getHeaderNames() {
		Enumeration<String> headerNames = new Enumeration<String>() {

			private Iterator<String> iterator = headers.keySet().iterator();

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
		return headerNames;
	}

	@Override
	public int getIntHeader(String name) {
		try {
			int num = Integer.parseInt(headers.get(name));
			return num;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public String getMethod() {
		return headers.get("method");
	}

	/**
	 * 暂时不支持
	 */
	@Override
	public String getPathInfo() {
		return null;
	}

	/**
	 * 暂时不支持
	 */
	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getContextPath() {
		String projectName = getProjName();
		if (projectName == null) {
			return getRequestURI();
		}
		if (projectName.equals("root")) {
			return "";
		}
		return "/" + projectName;
	}

	@Override
	public String getQueryString() {
		if (queryStr == null) {
			return null;
		}
		try {
			return URLDecoder.decode(queryStr, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 暂时不支持
	 */
	@Override
	public String getRemoteUser() {
		return null;
	}

	/**
	 * 暂时不支持
	 */
	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	/**
	 * 暂时不支持
	 */
	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	/**
	 * 这里之所以会有两个sessionId,是因为如果客户端的传入的jsessionid是无效的,
	 * 我们需要一个新的sessionId,但是Servlet-API要求我们返回浏览器传入的jsessionid。
	 */
	private String jsessionid;

	private String sessionId;

	public void setJSessionId(String jsessionid) {
		if (jsessionid != null) {
			this.jsessionid = jsessionid;
			isRequestedSessionIdFromURL = true;
		}
	}

	@Override
	public String getRequestedSessionId() {
		if (jsessionid == null) {
			// 目前支持cookieSession和重写URL,若有URL带有jsessionId,会在request在创建时传入,所以这里只尝试从cookie中获取
			isRequestedSessionIdFromCookie();
		}
		return jsessionid;
	}

	@Override
	public String getRequestURI() {
		String url = headers.get("url");
		if (url.endsWith("/") && !url.equals("/")) {
			url += DEFAULT_PAGE;
			headers.put("url", url);
		}
		return url;
	}

	@Override
	public StringBuffer getRequestURL() {
		String host = headers.get("Host");
		StringBuffer sb = new StringBuffer("http://" + host + getRequestURI());
		return sb;
	}

	@Override
	public String getServletPath() {
		return getNoProjNameUrl();
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (sessionId == null) {
			sessionId = getRequestedSessionId();
		}

		if (sessionId == null) {
			if (create) {
				DynamicResourceManager dynamicResourceManager = getDynamicResourceManager();
				SessionManager sessionManager = dynamicResourceManager.getSessionManager();
				Session session = sessionManager.createSession();
				sessionId = session.getId();
				return session;
			} else {
				return null;
			}
		} else {
			DynamicResourceManager dynamicResourceManager = getDynamicResourceManager();
			SessionManager sessionManager = dynamicResourceManager.getSessionManager();
			Session session = sessionManager.getSession(sessionId, true);
			sessionId = session.getId();
			return session;
		}
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	public HttpSession getSessionWithoutNoNew() {
		if (sessionId == null) {
			sessionId = getRequestedSessionId();
		}

		if (sessionId != null) {
			//这里NullPointerException
			DynamicResourceManager dynamicResourceManager = getDynamicResourceManager();
			SessionManager sessionManager = dynamicResourceManager.getSessionManager();
			return sessionManager.getSessionWithoutNoNew(sessionId);
		} else {
			return null;
		}
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		String jssesionid = getRequestedSessionId();

		if (jssesionid == null) {
			return false;
		} else {
			DynamicResourceManager dynamicResourceManager = getDynamicResourceManager();
			SessionManager sessionManager = dynamicResourceManager.getSessionManager();
			return sessionManager.hasSession(jssesionid);
		}
	}

	private boolean noCookieSessionId;

	private boolean isRequestedSessionIdFromCookie;
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		if (isRequestedSessionIdFromCookie) {
			return true;
		}
		if (noCookieSessionId) {
			return false;
		}

		Cookie[] cookies = getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("jsessionid")) {
					jsessionid = cookie.getValue();
					isRequestedSessionIdFromCookie = true;
					return true;
				}
			}
		}
		return false;
	}

	private boolean isRequestedSessionIdFromURL;

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return isRequestedSessionIdFromURL;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	/**
	 * 尝试修正请求中不规范的地方
	 * 
	 */
	public void correct() {
		if (!isCorrected) {
			correctHeader("Referer");
			isCorrected = true;
		}
	}

	/**
	 * 除去多余的斜杠
	 * 
	 */
	private void correctHeader(String headerName) {
		String header = headers.get(headerName);
		if (header != null) {
			header = header.replaceAll("/+", "/");
			header = header.replaceFirst("/", "//");
			headers.put(headerName, header);
		}
	}
	
	private DynamicResourceManager getDynamicResourceManager(){
		if (dynamicResourceManager == null) {
			dynamicResourceManager = Container.getDynamicResourceManager(getProjName());
			System.out.println("getDynamicResourceManager.getProjName: "+getProjName());
		}
		return dynamicResourceManager;
	}
	
	public Moniter getMoniter(){
		return ServerMoniter.getInstance();
	}
}
