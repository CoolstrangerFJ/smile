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
public interface IProcessor extends Runnable{

	void tryRead();

	void process() throws IOException;

	void tryWrite() throws IOException;

	SocketChannel getSocketChannel();

	void updateLastUsed();
	
	void invalidIncrease();
	
	void reset();
}
