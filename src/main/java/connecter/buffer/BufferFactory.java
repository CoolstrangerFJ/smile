/**
 * 
 */
package connecter.buffer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 上午11:54:28
 *
 */
public class BufferFactory {


	/**
	 * @param socketChannel
	 * @return
	 */
	public static IBuffer createBuffer(SocketChannel socketChannel) {
		return new IOBuffer(socketChannel);
	}

	/**
	 * @param socketChannel
	 * @param sizeLevel
	 * @return
	 */
	public static IBuffer createBuffer(SocketChannel socketChannel, int sizeLevel) {
		return new IOBuffer(socketChannel, sizeLevel);
	}

	/**
	 * @param socketChannel
	 * @param buffer
	 * @return
	 */
	public static IBuffer createBuffer(SocketChannel socketChannel, ByteBuffer buffer) {
		return new IOBuffer(socketChannel, buffer);
	}

}
