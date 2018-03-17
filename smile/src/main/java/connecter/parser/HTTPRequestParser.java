/**
 * 
 */
package connecter.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import connecter.buffer.BufferFactory;
import connecter.buffer.IBuffer;
import connecter.request.Request;
import connecter.request.RequestFactory;
import launcher.Configuration;

/**
 * 该类用于解析HTTP请求
 * 
 * @author CoolStranger
 * @date 2017年12月25日
 * @time 下午12:24:35
 *
 */
public class HTTPRequestParser implements IRequestParser {

	// 请求解析状态码：0为请求行、1为请求头、2为请求体
	private static final int PARSING_LINE = 0;
	private static final int PARSING_HEADER = 1;
	private static final int PARSING_BODY = 2;
	private static final StringBuilder RELEASE = null;

	private String charset = Configuration.charset;
	private HashMap<String, String> headers = new HashMap<>(8);
	private SocketChannel socketChannel;
	private StringBuilder sb;
	private IBuffer buffer;
	private IBuffer tempBuffer;
	private int lastChar;
	private int parseStatus;
	private int contentLen;
	private boolean hasBody;
	private boolean completed;
	private String queryStr;
	private String sessionIdFromURL;

	public HTTPRequestParser(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		buffer = BufferFactory.createBuffer(socketChannel);
		// System.out.println("constructor: " + buffer);
		sb = new StringBuilder(20);
	}

	private void reset() {
		headers = new HashMap<>(8);
		buffer = tempBuffer;
		sb = new StringBuilder(20);
		lastChar = 0;
		parseStatus = 0;
		contentLen = 0;
		completed = false;
		hasBody = false;
		queryStr = null;
	}
	
	public boolean read(){
		return buffer.readFromChannel();
	}

	public Request getRequest() {
		if (parse()) {
			Request request = RequestFactory.createRequest(buffer, headers, queryStr, sessionIdFromURL, charset, socketChannel, hasBody);
			reset();
			return request;
		}
		return null;
	}

	/**
	 * 初步解析缓存中的请求行与请求头的信息,并缓存请求体,此方法已经兼顾了性能和可读性
	 *
	 * @return 解析是否已完成
	 */
	public boolean parse() {
		if (completed) {
			return true;
		}

		String s = null;

		switch (parseStatus) {

		case PARSING_LINE:
			if (tokenReady()) {
				s = getToken();
				try {
					readRequestLine(s);
					parseStatus++;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			} else {
				return false;
			}

		case PARSING_HEADER:
			here: while (true) {
				if (tokenReady()) {
					s = getToken();
					contentLen = getContentLength(s);
					if (contentLen < 0) {
						readHeader(s);
					} else if (contentLen == 0) {
						sb = RELEASE;
						completed = true;
						// System.out.println("done!");
						tempBuffer = buffer.getAdhering(contentLen);
						return true;
					} else {// 有响应体
						sb = RELEASE;
						hasBody = true;
						buffer.prepare(contentLen);
						parseStatus++;
						break here;
					}
				} else {
					return false;
				}
			}

		case PARSING_BODY:
			if (buffer.remaining() < contentLen) {
				return false;
			}
			completed = true;
			tempBuffer = buffer.getAdhering(contentLen);
			return true;

		default:
			throw new RuntimeException("UnknownStatus");
		}
	}

	/**
	 * 是否已读入了一行
	 *
	 * @param c
	 *            当前字符
	 * @return 是否已读入一行
	 */
	private boolean tokenReady() {
		int c = -1;
		while ((c = buffer.get()) != -1) {
			if (c == '\r' || c == '\n') {
				if (c == '\n' && lastChar == '\r') {
					return true;
				}
				lastChar = c;
			} else {
				sb.append((char) c);
			}
		}
		return false;
	}

	/**
	 * 读取一行
	 * 
	 * @return
	 */
	private String getToken() {
		String s = sb.toString();
		// System.out.println(s);
		sb = new StringBuilder(20);
		return s;
	}

	/**
	 * 解析请求头信息
	 * 
	 * @param s
	 */
	private void readHeader(String s) {
		int index = 0;
		String key = null, value = null;
		index = s.indexOf(":");
		key = s.substring(0, index).trim();
		value = s.substring(index + 1).trim();
		try {
			value = URLDecoder.decode(value, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		headers.put(key, value);

	}

	/**
	 * 初步解析请求行
	 * 
	 * @param s
	 *            请求行
	 * @throws UnsupportedEncodingException
	 */
	private void readRequestLine(String s) throws UnsupportedEncodingException {
		String[] reqLine = s.split(" ");
		headers.put("method", reqLine[0]);
		String srcUrl = reqLine[1];
		int semicolonIndex = srcUrl.indexOf(';');
		if (semicolonIndex != -1) {
			sessionIdFromURL = srcUrl.substring(semicolonIndex + 1);
			srcUrl = srcUrl.substring(0, semicolonIndex);
		}
		int index = srcUrl.indexOf("?");
		String url = null;
		if (index == -1) {
			url = URLDecoder.decode(srcUrl, charset);
		} else {
			url = URLDecoder.decode(srcUrl.substring(0, index), charset);
			queryStr = srcUrl.substring(index + 1);
			// queryStr = URLDecoder.decode(srcUrl.substring(index + 1),
			// charSet);
		}
		url = url.replaceAll("/+", "/");
		headers.put("url", url);
		headers.put("Protocol", reqLine[2]);
	}

	/**
	 * 判断当前行是否为空串，若为空串则获取请求体长度
	 * 
	 * @param s
	 *            当前行
	 * @return 返回请求体长度, 若无请求体返回0, 若当前行不为空串则返回-1
	 */
	private int getContentLength(String s) {
		if (s.equals("")) {
			String lenStr = headers.get("Content-Length");
			if (lenStr != null) {
				return Integer.parseInt(lenStr);
			} else {
				return 0;
			}
		}
		return -1;
	}

}
