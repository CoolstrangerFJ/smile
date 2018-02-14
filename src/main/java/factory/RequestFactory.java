/**
 * 
 */
package factory;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

import connecter.buffer.IBuffer;
import connecter.request.Request;

/**
 * @author CoolStranger
 * @date 2017年12月28日
 * @time 上午11:51:10
 *
 */
public class RequestFactory {


	/**
	 * 
	 * @param buffer
	 * @param headers
	 * @param queryStr
	 * @param charset
	 * @param charset2 
	 * @param hasBody
	 * @return
	 */
	public static Request createRequest(IBuffer buffer, HashMap<String, String> headers, String queryStr,
			String sessionIdFromURL, String charset, SocketChannel socketChannel, boolean hasBody) {
		Request request = new Request();
		request.setSocketChannel(socketChannel);
		request.setBuffer(buffer);
		request.setHeaders(headers);
		request.setJSessionId(sessionIdFromURL);
		request.setCharset(charset);
		request.setQueryStr(queryStr);
		request.setHasBody(hasBody);
		return request;
	}

}
