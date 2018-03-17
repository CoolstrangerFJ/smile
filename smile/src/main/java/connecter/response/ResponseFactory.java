/**
 * 
 */
package connecter.response;

import java.nio.channels.SocketChannel;

import connecter.request.Request;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 上午11:36:08
 *
 */
public class ResponseFactory {

	/**
	 * @param socketChannel
	 * @param request 
	 *
	 * @return
	 */
	public static Response createResponse(SocketChannel socketChannel, Request request) {
		Response response = new Response(socketChannel);
		response.setRequest(request);
		return response;
	}

}
