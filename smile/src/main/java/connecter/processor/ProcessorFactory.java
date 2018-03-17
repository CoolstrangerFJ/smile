/**
 * 
 */
package connecter.processor;

import java.nio.channels.SocketChannel;

import connecter.selector.OutputSelector;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 下午12:27:03
 *
 */
public class ProcessorFactory {

	/**
	 * @param socketChannel
	 * @param outputSelector
	 * @return
	 */
	public static IProcessor createProcessor(SocketChannel socketChannel, OutputSelector outputSelector) {
		return new HTTPProcessor(socketChannel, outputSelector);
	}

}
