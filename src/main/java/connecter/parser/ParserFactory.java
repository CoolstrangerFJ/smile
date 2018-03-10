/**
 * 
 */
package connecter.parser;

import java.nio.channels.SocketChannel;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 下午12:39:58
 *
 */
public class ParserFactory {

	/**
	 *
	 * @param socketChannel
	 * @return
	 */
	public static IRequestParser createParser(SocketChannel socketChannel) {
		HTTPRequestParser httpRequestParser = new HTTPRequestParser(socketChannel);
		return httpRequestParser;
	}

}
