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

import connecter.selector.AcceptableSelector;
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
	private Selector acceptSelector;
	private Selector writeSelector;
	private long startLoading;
	private static HttpConnecterNIO instance = new HttpConnecterNIO();
	
	private HttpConnecterNIO(){
	}
	
	public static HttpConnecterNIO getInstance(){
		return instance;
	}

	public void launch() throws IOException {
		startLoading = System.currentTimeMillis();
		
		ServerSocketChannel serverChannel1 = ServerSocketChannel.open();
		serverChannel1.configureBlocking(false);
		serverChannel1.bind(new InetSocketAddress("127.0.0.1", Configuration.serverPort));
		
		ServerSocketChannel serverChannel2 = ServerSocketChannel.open();
		serverChannel2.configureBlocking(false);
		serverChannel2.bind(new InetSocketAddress(InetAddress.getLocalHost(),Configuration.serverPort));
		
		acceptSelector = Selector.open();
		writeSelector = Selector.open();
		Container.init();

		serverChannel1.register(acceptSelector, SelectionKey.OP_ACCEPT);
		serverChannel2.register(acceptSelector, SelectionKey.OP_ACCEPT);
		
		OutputSelector outputSelector = new OutputSelector(writeSelector);
		// outputSelector.run();
		new Thread(outputSelector).start();
		new Thread(new AcceptableSelector(acceptSelector, outputSelector)).start();
	}

	public static long getStartLoading() {
		return instance.startLoading;
	}
}
