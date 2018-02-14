/**
 * 
 */
package connecter.response;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import connecter.buffer.IBuffer;
import connecter.request.Request;
import factory.BufferFactory;
import launcher.Configuration;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 上午11:37:50
 *
 */
public class Response implements HttpServletResponse, ResponseWriter {

	private static final int HEADER_ING = 0;
	private static final int BODY_ING = 1;
	private static final boolean COMPLETED = true;
	private static final String DATE_PATTERN = Configuration.DATE_PATTERN;
	private static final String NEW_LINE = "\r\n";
	private SocketChannel socketChannel;
	private Request request;
	private IBuffer headerBuffer;
	private IBuffer bodyBuffer;
	/**
	 * 此变量仅用于非阻塞输出阶段,标识有无响应体,每次触发writable事件时,减少对有无响应体检查的性能消耗
	 */
	private boolean hasBody;
	private String charset = Configuration.charset;
	private ServletOutputStream servletOutputStream;
	private PrintWriter printWriter;
	private boolean finished;
	private int outputStatus;
	private HashMap<String, String> headers = new HashMap<>(8);
	private List<Cookie> cookies;
	private int statusCode = SC_OK;
	private String statusMessage;
	private boolean hasHandled;

	/**
	 * @param socketChannel
	 */
	public Response(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		this.headerBuffer = BufferFactory.createBuffer(socketChannel, 0);
		setContentType("text/html; charset=" + charset);
	}

	private boolean isConnectionKeepALive;

	public boolean isConnectionKeepALive() {
		return isConnectionKeepALive;
	}

	public void connectionKeepALive() {
		setHeader("Connection", "keep-alive");
	}

	public void connectionClose() {
		setHeader("Connection", "close");
	}

	/**
	 * connecter已实现了自适应的的buffer,不需要此方法
	 */
	@Override
	public void flushBuffer() throws IOException {
	}

	@Override
	public int getBufferSize() {
		if (bodyBuffer == null) {
			return 0;
		} else {
			return bodyBuffer.size();
		}
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	/**
	 *
	 * @see javax.servlet.ServletResponse#getContentType()
	 * 
	 * @return
	 */
	@Override
	public String getContentType() {
		return headers.get("Content-Type");
	}

	/**
	 * @see javax.servlet.ServletResponse#getLocale()
	 * 
	 * @return
	 */
	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	/**
	 *
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (printWriter != null) {
			throw new IllegalStateException();
		}
		if (servletOutputStream == null) {
			bodyBuffer = BufferFactory.createBuffer(socketChannel);
			servletOutputStream = new ServletOutputStreamImpl(bodyBuffer);
		}
		return servletOutputStream;
	}

	/**
	 *
	 * @see javax.servlet.ServletResponse#getWriter()
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if (servletOutputStream != null) {
			throw new IllegalStateException();
		}
		if (printWriter == null) {
			bodyBuffer = BufferFactory.createBuffer(socketChannel);
			printWriter = new PrintWriter(new OutputStreamWriter(new ServletOutputStreamImpl(bodyBuffer), charset));
		}
		return printWriter;
	}

	private boolean isCommitted;

	/**
	 * 由于连接器会缓存整个响应,所以servlet容器中调用此方法时,响应都未被提交。
	 * 但根据servlet-api规范,在某些方法被调用后,响应就应该被视为已提交。 Because of the connecter which
	 * will buffer the whole response, all the invocation of this method will
	 * happen before the response commit. But according to the servlet-api,
	 * response should be considered to be committed after invoking some method,
	 * such as <code>sendError</code>.
	 */
	@Override
	public boolean isCommitted() {
		return isCommitted;
	}

	@Override
	public void reset() {
		headers = new HashMap<>(8);
		setContentType("text/html; charset=UTF-8");
		// addHeader("Connection", "keep-alive");
		resetBuffer();
		finished = false;
		outputStatus = 0;
		statusCode = 200;
		hasHandled = false;
		isCommitted = false;
		cookies = null;
	}

	@Override
	public void resetBuffer() {
		hasBody = false;
		bodyBuffer = null;
		printWriter = null;
		servletOutputStream = null;
	}

	/**
	 * connecter已实现了自适应的的buffer,不需要此方法
	 */
	@Override
	public void setBufferSize(int arg0) {
	}

	/**
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 * 
	 * @param arg0
	 */
	@Override
	public void setCharacterEncoding(String charSetName) {
		this.charset = charSetName;
	}

	/**
	 *
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 * 
	 * @param val
	 */
	@Override
	public void setContentLength(int val) {
		headers.put("Content-Length", String.valueOf(val));
	}

	/**
	 *
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 * 
	 * @param arg0
	 */
	@Override
	public void setContentType(String value) {
		headers.put("Content-Type", value);
	}

	// private Locale locale = Locale.ENGLISH;

	@Override
	public void setLocale(Locale locale) {
		System.err.println(Configuration.UNIMPLEMENTED_INVOKE + " setLocale");
	}

	@Override
	public void addCookie(Cookie cookie) {
		if (cookies == null) {
			cookies = new ArrayList<>(4);
		}

		cookies.add(cookie);
	}

	private Cookie getSessionCookie() {
		HttpSession session = request.getSessionWithoutNoNew();
		if (session != null && session.isNew()) {
			String jsessionid = session.getId();
			Cookie cookie = new Cookie("jsessionid", jsessionid);
			cookie.setPath(request.getContextPath() + "/");
			// servlet2.5 暂不支持HttpOnly字段,所以在响应的时候拿出来单独处理
			// cookie.setHttpOnly(true);
			return cookie;
		}
		return null;
	}

	@Override
	public void addDateHeader(String headerName, long time) {
		String date = dateFormatConvert(time);
		addHeader(headerName, date);
	}

	private String dateFormatConvert(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
		return sdf.format(new Date(time));
	}

	@Override
	public void addHeader(String key, String value) {
		String header = headers.get(key);
		if (header == null) {
			headers.put(key, value);
		} else {
			headers.put(key, header + "; " + value);
		}
	}

	@Override
	public void addIntHeader(String key, int value) {
		addHeader(key, Integer.toString(value));
	}

	@Override
	public boolean containsHeader(String key) {
		return headers.containsKey(key);
	}

	@Override
	public String encodeRedirectURL(String url) {
		try {
			HttpSession session = request.getSessionWithoutNoNew();
			// hasSession?
			if (session != null) {
				String sessionId = session.getId();

				// FromCookie
				if (request.getCookies() != null) {

					url = URLEncoder.encode(url, charset);

					// 没有cookie,就要重写URL
				} else {

					url = URLEncoder.encode(url, charset);
					url += ";jsessionid=" + sessionId;
				}
				// noSessionId
			} else {
				url = URLEncoder.encode(url, charset);
			}
			return url;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return encodeRedirectURL(url);
	}

	@Override
	public String encodeURL(String url) {
		return encodeRedirectURL(url);
	}

	@Override
	public String encodeUrl(String url) {
		return encodeRedirectURL(url);
	}

	@Override
	public void sendError(int arg0) throws IOException {
		System.err.println(Configuration.UNIMPLEMENTED_INVOKE);
		System.err.println("sendError, error status code: " + arg0);
		isCommitted = true;
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		System.err.println(Configuration.UNIMPLEMENTED_INVOKE);
		System.err.println("sendError, error status code: " + arg0 + ", msg: " + arg1);
		isCommitted = true;
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		statusCode = SC_FOUND;
		String host = request.getHeader("Host");

		if (location.startsWith("/")) {// absolute path

			location = "http://" + host + location;
		} else {// relative path or full path

			if (!location.toLowerCase().startsWith("http")) {// relative path

				String requestURI = request.getRequestURI();
				int lastSlash = requestURI.lastIndexOf('/');

				if (lastSlash == 0) {// equal("/")
					location = "http://" + host + "/" + location;

				} else {// "/project/../resource.extension"
					requestURI = requestURI.substring(0, lastSlash + 1);
					location = "http://" + host + requestURI + location;
				}
			} // else full path

		}
		// System.out.println("Location: " + location);
		setHeader("Location", location);
		isCommitted = true;
		hasHandled = true;
	}

	@Override
	public void setDateHeader(String name, long time) {
		String date = dateFormatConvert(time);
		headers.put(name, date);
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		headers.put(arg0, arg1);
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		String val = String.valueOf(arg1);
		headers.put(arg0, val);
	}

	@Override
	public void setStatus(int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public void setStatus(int statusCode, String statusMessage) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}

	/**
	 * 把请求头写入缓存
	 * 
	 *
	 * @see connecter.response.ResponseWriter#ready4Write()
	 */
	public void ready4Write() {
		StringBuilder sb = new StringBuilder(512);
		// 拼接响应行
		String statusMessage = getStatusMessage();
		sb.append("HTTP/1.1 ").append(statusCode).append(' ').append(statusMessage).append(NEW_LINE);
		// 计算响应体长度
		if (bodyBuffer != null) {
			flushToBuffer();
			setContentLength(bodyBuffer.remaining());
			hasBody = true;
			bodyBuffer.ready4WriteToChannel();
		}

		// 判断连接是否复用
		if (isCommitted) {
			connectionClose();
		} else {
			String connection = request.getHeader("Connection");
			if ("keep-alive".equalsIgnoreCase(connection)) {
				connectionKeepALive();
				isConnectionKeepALive = true;
			}
		}

		// 加上响应时间
		setDateHeader("Date", System.currentTimeMillis());

		// 拼接响应头
		Set<Entry<String, String>> entrySet = headers.entrySet();
		for (Entry<String, String> entry : entrySet) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(NEW_LINE);
		}

		cookieHeaderHandler(sb);

		sb.append("Server: ").append(Configuration.SERVER_NAME).append(NEW_LINE);
		sb.append(NEW_LINE);
		String responseHeader = sb.toString();
		// 存入响应头缓存块
		headerBuffer.put(responseHeader.getBytes());
		headerBuffer.ready4WriteToChannel();
	}

	private void cookieHeaderHandler(StringBuilder sb) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				addCookieHeader(cookie, sb, false);
			}
		}

		if (statusCode != 404) {
			// sessionId比较重要,由于servlet2.5没有支持HttpOnly,所以sessionCookie要特殊处理
			// 顺便更新session的lastAccessedTime
			Cookie sessionCookie = getSessionCookie();
			if (sessionCookie != null) {
				addCookieHeader(sessionCookie, sb, true);
			}
		}
	}

	private void addCookieHeader(Cookie cookie, StringBuilder sb, boolean isHttpOnly) {

		sb.append("Set-Cookie: ").append(cookie.getName()).append('=').append(cookie.getValue());

		int version = cookie.getVersion();
		if (version != 0) {
			sb.append("; ").append("Version=").append(version);
		}

		String comment = cookie.getComment();
		if (comment != null) {
			sb.append("; ").append("Comment=").append(comment);
		}

		int maxAge = cookie.getMaxAge();
		if (maxAge >= 0) {
			sb.append("; ").append("Max-Age=").append(maxAge);

			long expires = System.currentTimeMillis() + maxAge;
			sb.append("; ").append("Expires=").append(dateFormatConvert(expires));
		}

		String path = cookie.getPath();
		if (path != null) {
			sb.append("; ").append("Path=").append(path);
		}

		String domain = cookie.getDomain();
		if (domain != null) {
			sb.append("; ").append("Domain=").append(domain);
		}

		if (cookie.getSecure()) {
			sb.append("; ").append("Secure");
		}

		if (isHttpOnly) {
			sb.append("; ").append("HttpOnly");
		}

		sb.append(NEW_LINE);
	}

	/**
	 * 强制写出所有缓冲的输出字节
	 * 
	 */
	private void flushToBuffer() {
		if (servletOutputStream != null) {
			try {
				servletOutputStream.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (printWriter != null) {
			printWriter.flush();
		}
	}

	/**
	 * 根据状态码返回相应单词
	 * 
	 * @return
	 */
	private String getStatusMessage() {
		if (statusMessage != null) {
			return statusMessage;
		}

		switch (statusCode) {
		case SC_OK:
			statusMessage = "OK";
			break;

		case SC_CREATED:
			statusMessage = "CREATED";
			break;

		case SC_ACCEPTED:
			statusMessage = "ACCEPTED";
			break;

		case SC_NON_AUTHORITATIVE_INFORMATION:
			statusMessage = "NON AUTHORITATIVE INFORMATION";
			break;

		case SC_NO_CONTENT:
			statusMessage = "NO CONTENT";
			break;

		case SC_RESET_CONTENT:
			statusMessage = "RESET CONTENT";
			break;

		case SC_PARTIAL_CONTENT:
			statusMessage = "PARTIAL CONTENT";
			break;

		case SC_FOUND:
			statusMessage = "FOUND";
			break;

		case SC_NOT_MODIFIED:
			statusMessage = "NOT MODIFIED";
			break;

		case SC_NOT_FOUND:
			statusMessage = "NOT FOUND";
			break;

		case SC_INTERNAL_SERVER_ERROR:
			statusMessage = "ERROR";
			break;

		default:
			statusMessage = "OK";
			break;
		}

		return statusMessage;
	}

	/**
	 * 此方法主要用于测试
	 * 
	 * @see connecter.response.ResponseWriter#write(java.lang.String)
	 * 
	 * @param s
	 */
	@Override
	public void write(String s) throws UnsupportedEncodingException {
		if (bodyBuffer == null) {

		}
		bodyBuffer.put(s.getBytes(charset));
	}

	/**
	 * 直接使用把一个ByteBuffer放入响应中，一般用于响应静态资源
	 */
	@Override
	public void put(ByteBuffer buffer) {
		bodyBuffer = BufferFactory.createBuffer(socketChannel, buffer);
		hasBody = true;
	}

	/**
	 * 尝试把缓存写入通道
	 * 
	 * @see connecter.response.ResponseWriter#writeToChannel()
	 * 
	 * @return
	 */
	@Override
	public boolean bufferToChannel() {
		if (finished) {
			return true;
		}
		switch (outputStatus) {
		case HEADER_ING:
			if (headerBuffer.writeToChannel() != COMPLETED) {
				return false;
			}
			if (!hasBody) {
				finished = true;
				return true;
			}
			outputStatus++;
		case BODY_ING:
			if (bodyBuffer.writeToChannel() != COMPLETED) {
				return false;
			}
		}
		finished = true;
		return true;
	}

	public boolean hasHandled() {
		if (bodyBuffer != null) {
			return true;
		} else {
			return hasHandled;
		}
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Request getRequest() {
		return request;
	}

}
