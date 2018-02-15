/**
 * 
 */
package connecter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import connecter.selector.InputSelector;
import connecter.selector.OutputSelector;
import container.Container;
import launcher.Configuration;

/**
 * @author CoolStranger
 * @date 2017年12月26日
 * @time 下午11:30:23
 *
 */
public class HttpConnecterNIO {
	private Selector readSelector;
	private Selector writeSelector;
	private static long startLoading;

	public void launch() throws IOException {
		startLoading = System.currentTimeMillis();
		
		ServerSocketChannel serverChannel1 = ServerSocketChannel.open();
		serverChannel1.configureBlocking(false);
		serverChannel1.bind(new InetSocketAddress("127.0.0.1", Configuration.serverPort));
		
		ServerSocketChannel serverChannel2 = ServerSocketChannel.open();
		serverChannel2.configureBlocking(false);
		serverChannel2.bind(new InetSocketAddress(InetAddress.getLocalHost(),Configuration.serverPort));
		
		readSelector = Selector.open();
		writeSelector = Selector.open();
		// try {
		// Class.forName("container.dispatcher.ResquestDispatcherImpl");
		// } catch (ClassNotFoundException e) {
		// throw new RuntimeException(e);
		// }
		Container.init();

		serverChannel1.register(readSelector, SelectionKey.OP_ACCEPT);
		serverChannel2.register(readSelector, SelectionKey.OP_ACCEPT);
		
		OutputSelector outputSelector = new OutputSelector(writeSelector);
		// outputSelector.run();
		new Thread(outputSelector).start();
		new Thread(new InputSelector(readSelector, outputSelector)).start();
	}

	/**
	 * @return
	 */
	public static long getStartLoading() {
		return startLoading;
	}
}
