/**
 * 
 */
package connecter.processor;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author CoolStranger
 * @date 2017年12月26日
 * @time 下午11:54:50
 *
 */
public interface IProcessor extends Runnable {

	/**
	 * 输入选择器遇到readable事件时,调用此方法
	 */
	void tryRead();

	/**
	 * 容器线程处理请求时,调用此方法
	 */
	void process() throws IOException;

	/**
	 * 输出选择器遇到writable事件时,调用此方法
	 */
	void tryWrite();

	SocketChannel getSocketChannel();

	void updateLastUsed();

	void invalidIncrease();

	void reset();
	
	
}
